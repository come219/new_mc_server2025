package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockDirectional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyPistonType;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.EnumPistonReaction;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

// CraftBukkit start
import com.google.common.collect.ImmutableList;
import java.util.AbstractList;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
// CraftBukkit end

public class BlockPiston extends BlockDirectional {

    public static final MapCodec<BlockPiston> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.BOOL.fieldOf("sticky").forGetter((blockpiston) -> {
            return blockpiston.isSticky;
        }), propertiesCodec()).apply(instance, BlockPiston::new);
    });
    public static final BlockStateBoolean EXTENDED = BlockProperties.EXTENDED;
    public static final int TRIGGER_EXTEND = 0;
    public static final int TRIGGER_CONTRACT = 1;
    public static final int TRIGGER_DROP = 2;
    public static final float PLATFORM_THICKNESS = 4.0F;
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape UP_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private final boolean isSticky;

    @Override
    public MapCodec<BlockPiston> codec() {
        return BlockPiston.CODEC;
    }

    public BlockPiston(boolean flag, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockPiston.FACING, EnumDirection.NORTH)).setValue(BlockPiston.EXTENDED, false));
        this.isSticky = flag;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        if ((Boolean) iblockdata.getValue(BlockPiston.EXTENDED)) {
            switch ((EnumDirection) iblockdata.getValue(BlockPiston.FACING)) {
                case DOWN:
                    return BlockPiston.DOWN_AABB;
                case UP:
                default:
                    return BlockPiston.UP_AABB;
                case NORTH:
                    return BlockPiston.NORTH_AABB;
                case SOUTH:
                    return BlockPiston.SOUTH_AABB;
                case WEST:
                    return BlockPiston.WEST_AABB;
                case EAST:
                    return BlockPiston.EAST_AABB;
            }
        } else {
            return VoxelShapes.block();
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        if (!world.isClientSide) {
            this.checkIfExtend(world, blockposition, iblockdata);
        }

    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (!world.isClientSide) {
            this.checkIfExtend(world, blockposition, iblockdata);
        }

    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata1.is(iblockdata.getBlock())) {
            if (!world.isClientSide && world.getBlockEntity(blockposition) == null) {
                this.checkIfExtend(world, blockposition, iblockdata);
            }

        }
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockPiston.FACING, blockactioncontext.getNearestLookingDirection().getOpposite())).setValue(BlockPiston.EXTENDED, false);
    }

    private void checkIfExtend(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockPiston.FACING);
        boolean flag = this.getNeighborSignal(world, blockposition, enumdirection);

        if (flag && !(Boolean) iblockdata.getValue(BlockPiston.EXTENDED)) {
            if ((new PistonExtendsChecker(world, blockposition, enumdirection, true)).resolve()) {
                world.blockEvent(blockposition, this, 0, enumdirection.get3DDataValue());
            }
        } else if (!flag && (Boolean) iblockdata.getValue(BlockPiston.EXTENDED)) {
            BlockPosition blockposition1 = blockposition.relative(enumdirection, 2);
            IBlockData iblockdata1 = world.getBlockState(blockposition1);
            byte b0 = 1;

            if (iblockdata1.is(Blocks.MOVING_PISTON) && iblockdata1.getValue(BlockPiston.FACING) == enumdirection) {
                TileEntity tileentity = world.getBlockEntity(blockposition1);

                if (tileentity instanceof TileEntityPiston) {
                    TileEntityPiston tileentitypiston = (TileEntityPiston) tileentity;

                    if (tileentitypiston.isExtending() && (tileentitypiston.getProgress(0.0F) < 0.5F || world.getGameTime() == tileentitypiston.getLastTicked() || ((WorldServer) world).isHandlingTick())) {
                        b0 = 2;
                    }
                }
            }

            // CraftBukkit start
            if (!this.isSticky) {
                org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, ImmutableList.<org.bukkit.block.Block>of(), CraftBlock.notchToBlockFace(enumdirection));
                world.getCraftServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }
            }
            // PAIL: checkME - what happened to setTypeAndData?
            // CraftBukkit end
            world.blockEvent(blockposition, this, b0, enumdirection.get3DDataValue());
        }

    }

    private boolean getNeighborSignal(SignalGetter signalgetter, BlockPosition blockposition, EnumDirection enumdirection) {
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        int j;

        for (j = 0; j < i; ++j) {
            EnumDirection enumdirection1 = aenumdirection[j];

            if (enumdirection1 != enumdirection && signalgetter.hasSignal(blockposition.relative(enumdirection1), enumdirection1)) {
                return true;
            }
        }

        if (signalgetter.hasSignal(blockposition, EnumDirection.DOWN)) {
            return true;
        } else {
            BlockPosition blockposition1 = blockposition.above();
            EnumDirection[] aenumdirection1 = EnumDirection.values();

            j = aenumdirection1.length;

            for (int k = 0; k < j; ++k) {
                EnumDirection enumdirection2 = aenumdirection1[k];

                if (enumdirection2 != EnumDirection.DOWN && signalgetter.hasSignal(blockposition1.relative(enumdirection2), enumdirection2)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    protected boolean triggerEvent(IBlockData iblockdata, World world, BlockPosition blockposition, int i, int j) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockPiston.FACING);
        IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(BlockPiston.EXTENDED, true);

        if (!world.isClientSide) {
            boolean flag = this.getNeighborSignal(world, blockposition, enumdirection);

            if (flag && (i == 1 || i == 2)) {
                world.setBlock(blockposition, iblockdata1, 2);
                return false;
            }

            if (!flag && i == 0) {
                return false;
            }
        }

        if (i == 0) {
            if (!this.moveBlocks(world, blockposition, enumdirection, true)) {
                return false;
            }

            world.setBlock(blockposition, iblockdata1, 67);
            world.playSound((EntityHuman) null, blockposition, SoundEffects.PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
            world.gameEvent((Holder) GameEvent.BLOCK_ACTIVATE, blockposition, GameEvent.a.of(iblockdata1));
        } else if (i == 1 || i == 2) {
            TileEntity tileentity = world.getBlockEntity(blockposition.relative(enumdirection));

            if (tileentity instanceof TileEntityPiston) {
                ((TileEntityPiston) tileentity).finalTick();
            }

            IBlockData iblockdata2 = (IBlockData) ((IBlockData) Blocks.MOVING_PISTON.defaultBlockState().setValue(BlockPistonMoving.FACING, enumdirection)).setValue(BlockPistonMoving.TYPE, this.isSticky ? BlockPropertyPistonType.STICKY : BlockPropertyPistonType.DEFAULT);

            world.setBlock(blockposition, iblockdata2, 20);
            world.setBlockEntity(BlockPistonMoving.newMovingBlockEntity(blockposition, iblockdata2, (IBlockData) this.defaultBlockState().setValue(BlockPiston.FACING, EnumDirection.from3DDataValue(j & 7)), enumdirection, false, true));
            world.blockUpdated(blockposition, iblockdata2.getBlock());
            iblockdata2.updateNeighbourShapes(world, blockposition, 2);
            if (this.isSticky) {
                BlockPosition blockposition1 = blockposition.offset(enumdirection.getStepX() * 2, enumdirection.getStepY() * 2, enumdirection.getStepZ() * 2);
                IBlockData iblockdata3 = world.getBlockState(blockposition1);
                boolean flag1 = false;

                if (iblockdata3.is(Blocks.MOVING_PISTON)) {
                    TileEntity tileentity1 = world.getBlockEntity(blockposition1);

                    if (tileentity1 instanceof TileEntityPiston) {
                        TileEntityPiston tileentitypiston = (TileEntityPiston) tileentity1;

                        if (tileentitypiston.getDirection() == enumdirection && tileentitypiston.isExtending()) {
                            tileentitypiston.finalTick();
                            flag1 = true;
                        }
                    }
                }

                if (!flag1) {
                    if (i == 1 && !iblockdata3.isAir() && isPushable(iblockdata3, world, blockposition1, enumdirection.getOpposite(), false, enumdirection) && (iblockdata3.getPistonPushReaction() == EnumPistonReaction.NORMAL || iblockdata3.is(Blocks.PISTON) || iblockdata3.is(Blocks.STICKY_PISTON))) {
                        this.moveBlocks(world, blockposition, enumdirection, false);
                    } else {
                        world.removeBlock(blockposition.relative(enumdirection), false);
                    }
                }
            } else {
                world.removeBlock(blockposition.relative(enumdirection), false);
            }

            world.playSound((EntityHuman) null, blockposition, SoundEffects.PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
            world.gameEvent((Holder) GameEvent.BLOCK_DEACTIVATE, blockposition, GameEvent.a.of(iblockdata2));
        }

        return true;
    }

    public static boolean isPushable(IBlockData iblockdata, World world, BlockPosition blockposition, EnumDirection enumdirection, boolean flag, EnumDirection enumdirection1) {
        if (blockposition.getY() >= world.getMinY() && blockposition.getY() <= world.getMaxY() && world.getWorldBorder().isWithinBounds(blockposition)) {
            if (iblockdata.isAir()) {
                return true;
            } else if (!iblockdata.is(Blocks.OBSIDIAN) && !iblockdata.is(Blocks.CRYING_OBSIDIAN) && !iblockdata.is(Blocks.RESPAWN_ANCHOR) && !iblockdata.is(Blocks.REINFORCED_DEEPSLATE)) {
                if (enumdirection == EnumDirection.DOWN && blockposition.getY() == world.getMinY()) {
                    return false;
                } else if (enumdirection == EnumDirection.UP && blockposition.getY() == world.getMaxY()) {
                    return false;
                } else {
                    if (!iblockdata.is(Blocks.PISTON) && !iblockdata.is(Blocks.STICKY_PISTON)) {
                        if (iblockdata.getDestroySpeed(world, blockposition) == -1.0F) {
                            return false;
                        }

                        switch (iblockdata.getPistonPushReaction()) {
                            case BLOCK:
                                return false;
                            case DESTROY:
                                return flag;
                            case PUSH_ONLY:
                                return enumdirection == enumdirection1;
                        }
                    } else if ((Boolean) iblockdata.getValue(BlockPiston.EXTENDED)) {
                        return false;
                    }

                    return !iblockdata.hasBlockEntity();
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean moveBlocks(World world, BlockPosition blockposition, EnumDirection enumdirection, boolean flag) {
        BlockPosition blockposition1 = blockposition.relative(enumdirection);

        if (!flag && world.getBlockState(blockposition1).is(Blocks.PISTON_HEAD)) {
            world.setBlock(blockposition1, Blocks.AIR.defaultBlockState(), 20);
        }

        PistonExtendsChecker pistonextendschecker = new PistonExtendsChecker(world, blockposition, enumdirection, flag);

        if (!pistonextendschecker.resolve()) {
            return false;
        } else {
            Map<BlockPosition, IBlockData> map = Maps.newHashMap();
            List<BlockPosition> list = pistonextendschecker.getToPush();
            List<IBlockData> list1 = Lists.newArrayList();
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition2 = (BlockPosition) iterator.next();
                IBlockData iblockdata = world.getBlockState(blockposition2);

                list1.add(iblockdata);
                map.put(blockposition2, iblockdata);
            }

            List<BlockPosition> list2 = pistonextendschecker.getToDestroy();
            IBlockData[] aiblockdata = new IBlockData[list.size() + list2.size()];
            EnumDirection enumdirection1 = flag ? enumdirection : enumdirection.getOpposite();
            int i = 0;
            // CraftBukkit start
            final org.bukkit.block.Block bblock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());

            final List<BlockPosition> moved = pistonextendschecker.getToPush();
            final List<BlockPosition> broken = pistonextendschecker.getToDestroy();

            List<org.bukkit.block.Block> blocks = new AbstractList<org.bukkit.block.Block>() {

                @Override
                public int size() {
                    return moved.size() + broken.size();
                }

                @Override
                public org.bukkit.block.Block get(int index) {
                    if (index >= size() || index < 0) {
                        throw new ArrayIndexOutOfBoundsException(index);
                    }
                    BlockPosition pos = (BlockPosition) (index < moved.size() ? moved.get(index) : broken.get(index - moved.size()));
                    return bblock.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
                }
            };
            org.bukkit.event.block.BlockPistonEvent event;
            if (flag) {
                event = new BlockPistonExtendEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
            } else {
                event = new BlockPistonRetractEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
            }
            world.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                for (BlockPosition b : broken) {
                    world.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), world.getBlockState(b), 3);
                }
                for (BlockPosition b : moved) {
                    world.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), world.getBlockState(b), 3);
                    b = b.relative(enumdirection1);
                    world.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), world.getBlockState(b), 3);
                }
                return false;
            }
            // CraftBukkit end

            BlockPosition blockposition3;
            int j;
            IBlockData iblockdata1;

            for (j = list2.size() - 1; j >= 0; --j) {
                blockposition3 = (BlockPosition) list2.get(j);
                iblockdata1 = world.getBlockState(blockposition3);
                TileEntity tileentity = iblockdata1.hasBlockEntity() ? world.getBlockEntity(blockposition3) : null;

                dropResources(iblockdata1, world, blockposition3, tileentity);
                world.setBlock(blockposition3, Blocks.AIR.defaultBlockState(), 18);
                world.gameEvent((Holder) GameEvent.BLOCK_DESTROY, blockposition3, GameEvent.a.of(iblockdata1));
                if (!iblockdata1.is(TagsBlock.FIRE)) {
                    world.addDestroyBlockEffect(blockposition3, iblockdata1);
                }

                aiblockdata[i++] = iblockdata1;
            }

            IBlockData iblockdata2;

            for (j = list.size() - 1; j >= 0; --j) {
                blockposition3 = (BlockPosition) list.get(j);
                iblockdata1 = world.getBlockState(blockposition3);
                blockposition3 = blockposition3.relative(enumdirection1);
                map.remove(blockposition3);
                iblockdata2 = (IBlockData) Blocks.MOVING_PISTON.defaultBlockState().setValue(BlockPiston.FACING, enumdirection);
                world.setBlock(blockposition3, iblockdata2, 68);
                world.setBlockEntity(BlockPistonMoving.newMovingBlockEntity(blockposition3, iblockdata2, (IBlockData) list1.get(j), enumdirection, flag, false));
                aiblockdata[i++] = iblockdata1;
            }

            if (flag) {
                BlockPropertyPistonType blockpropertypistontype = this.isSticky ? BlockPropertyPistonType.STICKY : BlockPropertyPistonType.DEFAULT;
                IBlockData iblockdata3 = (IBlockData) ((IBlockData) Blocks.PISTON_HEAD.defaultBlockState().setValue(BlockPistonExtension.FACING, enumdirection)).setValue(BlockPistonExtension.TYPE, blockpropertypistontype);

                iblockdata1 = (IBlockData) ((IBlockData) Blocks.MOVING_PISTON.defaultBlockState().setValue(BlockPistonMoving.FACING, enumdirection)).setValue(BlockPistonMoving.TYPE, this.isSticky ? BlockPropertyPistonType.STICKY : BlockPropertyPistonType.DEFAULT);
                map.remove(blockposition1);
                world.setBlock(blockposition1, iblockdata1, 68);
                world.setBlockEntity(BlockPistonMoving.newMovingBlockEntity(blockposition1, iblockdata1, iblockdata3, enumdirection, true, true));
            }

            IBlockData iblockdata4 = Blocks.AIR.defaultBlockState();
            Iterator iterator1 = map.keySet().iterator();

            while (iterator1.hasNext()) {
                BlockPosition blockposition4 = (BlockPosition) iterator1.next();

                world.setBlock(blockposition4, iblockdata4, 82);
            }

            iterator1 = map.entrySet().iterator();

            while (iterator1.hasNext()) {
                Entry<BlockPosition, IBlockData> entry = (Entry) iterator1.next();
                BlockPosition blockposition5 = (BlockPosition) entry.getKey();
                IBlockData iblockdata5 = (IBlockData) entry.getValue();

                iblockdata5.updateIndirectNeighbourShapes(world, blockposition5, 2);
                iblockdata4.updateNeighbourShapes(world, blockposition5, 2);
                iblockdata4.updateIndirectNeighbourShapes(world, blockposition5, 2);
            }

            Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(world, pistonextendschecker.getPushDirection(), (EnumDirection) null);

            i = 0;

            int k;

            for (k = list2.size() - 1; k >= 0; --k) {
                iblockdata2 = aiblockdata[i++];
                BlockPosition blockposition6 = (BlockPosition) list2.get(k);

                iblockdata2.updateIndirectNeighbourShapes(world, blockposition6, 2);
                world.updateNeighborsAt(blockposition6, iblockdata2.getBlock(), orientation);
            }

            for (k = list.size() - 1; k >= 0; --k) {
                world.updateNeighborsAt((BlockPosition) list.get(k), aiblockdata[i++].getBlock(), orientation);
            }

            if (flag) {
                world.updateNeighborsAt(blockposition1, Blocks.PISTON_HEAD, orientation);
            }

            return true;
        }
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockPiston.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockPiston.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockPiston.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPiston.FACING, BlockPiston.EXTENDED);
    }

    @Override
    protected boolean useShapeForLightOcclusion(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockPiston.EXTENDED);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
