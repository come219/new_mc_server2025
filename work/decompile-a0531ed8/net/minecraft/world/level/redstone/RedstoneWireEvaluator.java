package net.minecraft.world.level.redstone;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockRedstoneWire;
import net.minecraft.world.level.block.state.IBlockData;

public abstract class RedstoneWireEvaluator {

    protected final BlockRedstoneWire wireBlock;

    protected RedstoneWireEvaluator(BlockRedstoneWire blockredstonewire) {
        this.wireBlock = blockredstonewire;
    }

    public abstract void updatePowerStrength(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable Orientation orientation, boolean flag);

    protected int getBlockSignal(World world, BlockPosition blockposition) {
        return this.wireBlock.getBlockSignal(world, blockposition);
    }

    protected int getWireSignal(BlockPosition blockposition, IBlockData iblockdata) {
        return iblockdata.is((Block) this.wireBlock) ? (Integer) iblockdata.getValue(BlockRedstoneWire.POWER) : 0;
    }

    protected int getIncomingWireSignal(World world, BlockPosition blockposition) {
        int i = 0;
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            IBlockData iblockdata = world.getBlockState(blockposition1);

            i = Math.max(i, this.getWireSignal(blockposition1, iblockdata));
            BlockPosition blockposition2 = blockposition.above();
            BlockPosition blockposition3;

            if (iblockdata.isRedstoneConductor(world, blockposition1) && !world.getBlockState(blockposition2).isRedstoneConductor(world, blockposition2)) {
                blockposition3 = blockposition1.above();
                i = Math.max(i, this.getWireSignal(blockposition3, world.getBlockState(blockposition3)));
            } else if (!iblockdata.isRedstoneConductor(world, blockposition1)) {
                blockposition3 = blockposition1.below();
                i = Math.max(i, this.getWireSignal(blockposition3, world.getBlockState(blockposition3)));
            }
        }

        return Math.max(0, i - 1);
    }
}
