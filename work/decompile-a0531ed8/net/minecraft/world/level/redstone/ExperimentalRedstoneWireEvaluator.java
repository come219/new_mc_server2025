package net.minecraft.world.level.redstone;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockRedstoneWire;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyRedstoneSide;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;

public class ExperimentalRedstoneWireEvaluator extends RedstoneWireEvaluator {

    private final Deque<BlockPosition> wiresToTurnOff = new ArrayDeque();
    private final Deque<BlockPosition> wiresToTurnOn = new ArrayDeque();
    private final Object2IntMap<BlockPosition> updatedWires = new Object2IntLinkedOpenHashMap();

    public ExperimentalRedstoneWireEvaluator(BlockRedstoneWire blockredstonewire) {
        super(blockredstonewire);
    }

    @Override
    public void updatePowerStrength(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable Orientation orientation, boolean flag) {
        Orientation orientation1 = getInitialOrientation(world, orientation);

        this.calculateCurrentChanges(world, blockposition, orientation1);
        ObjectIterator<Entry<BlockPosition>> objectiterator = this.updatedWires.object2IntEntrySet().iterator();

        for (boolean flag1 = true; objectiterator.hasNext(); flag1 = false) {
            Entry<BlockPosition> entry = (Entry) objectiterator.next();
            BlockPosition blockposition1 = (BlockPosition) entry.getKey();
            int i = entry.getIntValue();
            int j = unpackPower(i);
            IBlockData iblockdata1 = world.getBlockState(blockposition1);

            if (iblockdata1.is((Block) this.wireBlock) && !((Integer) iblockdata1.getValue(BlockRedstoneWire.POWER)).equals(j)) {
                int k = 2;

                if (!flag || !flag1) {
                    k |= 128;
                }

                world.setBlock(blockposition1, (IBlockData) iblockdata1.setValue(BlockRedstoneWire.POWER, j), k);
            } else {
                objectiterator.remove();
            }
        }

        this.causeNeighborUpdates(world);
    }

    private void causeNeighborUpdates(World world) {
        this.updatedWires.forEach((blockposition, integer) -> {
            Orientation orientation = unpackOrientation(integer);
            IBlockData iblockdata = world.getBlockState(blockposition);
            Iterator iterator = orientation.getDirections().iterator();

            while (iterator.hasNext()) {
                EnumDirection enumdirection = (EnumDirection) iterator.next();

                if (isConnected(iblockdata, enumdirection)) {
                    BlockPosition blockposition1 = blockposition.relative(enumdirection);
                    IBlockData iblockdata1 = world.getBlockState(blockposition1);
                    Orientation orientation1 = orientation.withFrontPreserveUp(enumdirection);

                    world.neighborChanged(iblockdata1, blockposition1, this.wireBlock, orientation1, false);
                    if (iblockdata1.isRedstoneConductor(world, blockposition1)) {
                        Iterator iterator1 = orientation1.getDirections().iterator();

                        while (iterator1.hasNext()) {
                            EnumDirection enumdirection1 = (EnumDirection) iterator1.next();

                            if (enumdirection1 != enumdirection.getOpposite()) {
                                world.neighborChanged(blockposition1.relative(enumdirection1), this.wireBlock, orientation1.withFrontPreserveUp(enumdirection1));
                            }
                        }
                    }
                }
            }

        });
    }

    private static boolean isConnected(IBlockData iblockdata, EnumDirection enumdirection) {
        BlockStateEnum<BlockPropertyRedstoneSide> blockstateenum = (BlockStateEnum) BlockRedstoneWire.PROPERTY_BY_DIRECTION.get(enumdirection);

        return blockstateenum == null ? enumdirection == EnumDirection.DOWN : ((BlockPropertyRedstoneSide) iblockdata.getValue(blockstateenum)).isConnected();
    }

    private static Orientation getInitialOrientation(World world, @Nullable Orientation orientation) {
        Orientation orientation1;

        if (orientation != null) {
            orientation1 = orientation;
        } else {
            orientation1 = Orientation.random(world.random);
        }

        return orientation1.withUp(EnumDirection.UP).withSideBias(Orientation.a.LEFT);
    }

    private void calculateCurrentChanges(World world, BlockPosition blockposition, Orientation orientation) {
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (iblockdata.is((Block) this.wireBlock)) {
            this.setPower(blockposition, (Integer) iblockdata.getValue(BlockRedstoneWire.POWER), orientation);
            this.wiresToTurnOff.add(blockposition);
        } else {
            this.propagateChangeToNeighbors(world, blockposition, 0, orientation, true);
        }

        BlockPosition blockposition1;
        int i;
        Orientation orientation1;
        int j;
        int k;
        int l;
        int i1;
        int j1;

        for (; !this.wiresToTurnOff.isEmpty(); this.propagateChangeToNeighbors(world, blockposition1, j1, orientation1, j > i1)) {
            blockposition1 = (BlockPosition) this.wiresToTurnOff.removeFirst();
            i = this.updatedWires.getInt(blockposition1);
            orientation1 = unpackOrientation(i);
            j = unpackPower(i);
            k = this.getBlockSignal(world, blockposition1);
            l = this.getIncomingWireSignal(world, blockposition1);
            i1 = Math.max(k, l);
            if (i1 < j) {
                if (k > 0 && !this.wiresToTurnOn.contains(blockposition1)) {
                    this.wiresToTurnOn.add(blockposition1);
                }

                j1 = 0;
            } else {
                j1 = i1;
            }

            if (j1 != j) {
                this.setPower(blockposition1, j1, orientation1);
            }
        }

        Orientation orientation2;

        for (; !this.wiresToTurnOn.isEmpty(); this.propagateChangeToNeighbors(world, blockposition1, l, orientation2, false)) {
            blockposition1 = (BlockPosition) this.wiresToTurnOn.removeFirst();
            i = this.updatedWires.getInt(blockposition1);
            int k1 = unpackPower(i);

            j = this.getBlockSignal(world, blockposition1);
            k = this.getIncomingWireSignal(world, blockposition1);
            l = Math.max(j, k);
            orientation2 = unpackOrientation(i);
            if (l > k1) {
                this.setPower(blockposition1, l, orientation2);
            } else if (l < k1) {
                throw new IllegalStateException("Turning off wire while trying to turn it on. Should not happen.");
            }
        }

    }

    private static int packOrientationAndPower(Orientation orientation, int i) {
        return orientation.getIndex() << 4 | i;
    }

    private static Orientation unpackOrientation(int i) {
        return Orientation.fromIndex(i >> 4);
    }

    private static int unpackPower(int i) {
        return i & 15;
    }

    private void setPower(BlockPosition blockposition, int i, Orientation orientation) {
        this.updatedWires.compute(blockposition, (blockposition1, integer) -> {
            return integer == null ? packOrientationAndPower(orientation, i) : packOrientationAndPower(unpackOrientation(integer), i);
        });
    }

    private void propagateChangeToNeighbors(World world, BlockPosition blockposition, int i, Orientation orientation, boolean flag) {
        Iterator iterator = orientation.getHorizontalDirections().iterator();

        EnumDirection enumdirection;
        BlockPosition blockposition1;

        while (iterator.hasNext()) {
            enumdirection = (EnumDirection) iterator.next();
            blockposition1 = blockposition.relative(enumdirection);
            this.enqueueNeighborWire(world, blockposition1, i, orientation.withFront(enumdirection), flag);
        }

        iterator = orientation.getVerticalDirections().iterator();

        while (iterator.hasNext()) {
            enumdirection = (EnumDirection) iterator.next();
            blockposition1 = blockposition.relative(enumdirection);
            boolean flag1 = world.getBlockState(blockposition1).isRedstoneConductor(world, blockposition1);
            Iterator iterator1 = orientation.getHorizontalDirections().iterator();

            while (iterator1.hasNext()) {
                EnumDirection enumdirection1 = (EnumDirection) iterator1.next();
                BlockPosition blockposition2 = blockposition.relative(enumdirection1);
                BlockPosition blockposition3;

                if (enumdirection == EnumDirection.UP && !flag1) {
                    blockposition3 = blockposition1.relative(enumdirection1);
                    this.enqueueNeighborWire(world, blockposition3, i, orientation.withFront(enumdirection1), flag);
                } else if (enumdirection == EnumDirection.DOWN && !world.getBlockState(blockposition2).isRedstoneConductor(world, blockposition2)) {
                    blockposition3 = blockposition1.relative(enumdirection1);
                    this.enqueueNeighborWire(world, blockposition3, i, orientation.withFront(enumdirection1), flag);
                }
            }
        }

    }

    private void enqueueNeighborWire(World world, BlockPosition blockposition, int i, Orientation orientation, boolean flag) {
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (iblockdata.is((Block) this.wireBlock)) {
            int j = this.getWireSignal(blockposition, iblockdata);

            if (j < i - 1 && !this.wiresToTurnOn.contains(blockposition)) {
                this.wiresToTurnOn.add(blockposition);
                this.setPower(blockposition, j, orientation);
            }

            if (flag && j > i && !this.wiresToTurnOff.contains(blockposition)) {
                this.wiresToTurnOff.add(blockposition);
                this.setPower(blockposition, j, orientation);
            }
        }

    }

    @Override
    protected int getWireSignal(BlockPosition blockposition, IBlockData iblockdata) {
        int i = this.updatedWires.getOrDefault(blockposition, -1);

        return i != -1 ? unpackPower(i) : super.getWireSignal(blockposition, iblockdata);
    }
}
