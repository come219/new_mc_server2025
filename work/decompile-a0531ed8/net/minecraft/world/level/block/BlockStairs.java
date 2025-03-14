package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyHalf;
import net.minecraft.world.level.block.state.properties.BlockPropertyStairsShape;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockStairs extends Block implements IBlockWaterlogged {

    public static final MapCodec<BlockStairs> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(IBlockData.CODEC.fieldOf("base_state").forGetter((blockstairs) -> {
            return blockstairs.baseState;
        }), propertiesCodec()).apply(instance, BlockStairs::new);
    });
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateEnum<BlockPropertyHalf> HALF = BlockProperties.HALF;
    public static final BlockStateEnum<BlockPropertyStairsShape> SHAPE = BlockProperties.STAIRS_SHAPE;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    protected static final VoxelShape TOP_AABB = BlockStepAbstract.TOP_AABB;
    protected static final VoxelShape BOTTOM_AABB = BlockStepAbstract.BOTTOM_AABB;
    protected static final VoxelShape OCTET_NNN = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 8.0D);
    protected static final VoxelShape OCTET_NNP = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 8.0D, 16.0D);
    protected static final VoxelShape OCTET_NPN = Block.box(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 8.0D);
    protected static final VoxelShape OCTET_NPP = Block.box(0.0D, 8.0D, 8.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape OCTET_PNN = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);
    protected static final VoxelShape OCTET_PNP = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape OCTET_PPN = Block.box(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape OCTET_PPP = Block.box(8.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape[] TOP_SHAPES = makeShapes(BlockStairs.TOP_AABB, BlockStairs.OCTET_NNN, BlockStairs.OCTET_PNN, BlockStairs.OCTET_NNP, BlockStairs.OCTET_PNP);
    protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes(BlockStairs.BOTTOM_AABB, BlockStairs.OCTET_NPN, BlockStairs.OCTET_PPN, BlockStairs.OCTET_NPP, BlockStairs.OCTET_PPP);
    private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
    private final Block base;
    protected final IBlockData baseState;

    @Override
    public MapCodec<? extends BlockStairs> codec() {
        return BlockStairs.CODEC;
    }

    private static VoxelShape[] makeShapes(VoxelShape voxelshape, VoxelShape voxelshape1, VoxelShape voxelshape2, VoxelShape voxelshape3, VoxelShape voxelshape4) {
        return (VoxelShape[]) IntStream.range(0, 16).mapToObj((i) -> {
            return makeStairShape(i, voxelshape, voxelshape1, voxelshape2, voxelshape3, voxelshape4);
        }).toArray((i) -> {
            return new VoxelShape[i];
        });
    }

    private static VoxelShape makeStairShape(int i, VoxelShape voxelshape, VoxelShape voxelshape1, VoxelShape voxelshape2, VoxelShape voxelshape3, VoxelShape voxelshape4) {
        VoxelShape voxelshape5 = voxelshape;

        if ((i & 1) != 0) {
            voxelshape5 = VoxelShapes.or(voxelshape, voxelshape1);
        }

        if ((i & 2) != 0) {
            voxelshape5 = VoxelShapes.or(voxelshape5, voxelshape2);
        }

        if ((i & 4) != 0) {
            voxelshape5 = VoxelShapes.or(voxelshape5, voxelshape3);
        }

        if ((i & 8) != 0) {
            voxelshape5 = VoxelShapes.or(voxelshape5, voxelshape4);
        }

        return voxelshape5;
    }

    protected BlockStairs(IBlockData iblockdata, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockStairs.FACING, EnumDirection.NORTH)).setValue(BlockStairs.HALF, BlockPropertyHalf.BOTTOM)).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.STRAIGHT)).setValue(BlockStairs.WATERLOGGED, false));
        this.base = iblockdata.getBlock();
        this.baseState = iblockdata;
    }

    @Override
    protected boolean useShapeForLightOcclusion(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (iblockdata.getValue(BlockStairs.HALF) == BlockPropertyHalf.TOP ? BlockStairs.TOP_SHAPES : BlockStairs.BOTTOM_SHAPES)[BlockStairs.SHAPE_BY_STATE[this.getShapeIndex(iblockdata)]];
    }

    private int getShapeIndex(IBlockData iblockdata) {
        return ((BlockPropertyStairsShape) iblockdata.getValue(BlockStairs.SHAPE)).ordinal() * 4 + ((EnumDirection) iblockdata.getValue(BlockStairs.FACING)).get2DDataValue();
    }

    @Override
    public float getExplosionResistance() {
        return this.base.getExplosionResistance();
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        EnumDirection enumdirection = blockactioncontext.getClickedFace();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockposition);
        IBlockData iblockdata = (IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockStairs.FACING, blockactioncontext.getHorizontalDirection())).setValue(BlockStairs.HALF, enumdirection != EnumDirection.DOWN && (enumdirection == EnumDirection.UP || blockactioncontext.getClickLocation().y - (double) blockposition.getY() <= 0.5D) ? BlockPropertyHalf.BOTTOM : BlockPropertyHalf.TOP)).setValue(BlockStairs.WATERLOGGED, fluid.getType() == FluidTypes.WATER);

        return (IBlockData) iblockdata.setValue(BlockStairs.SHAPE, getStairsShape(iblockdata, blockactioncontext.getLevel(), blockposition));
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockStairs.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return enumdirection.getAxis().isHorizontal() ? (IBlockData) iblockdata.setValue(BlockStairs.SHAPE, getStairsShape(iblockdata, iworldreader, blockposition)) : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    private static BlockPropertyStairsShape getStairsShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockStairs.FACING);
        IBlockData iblockdata1 = iblockaccess.getBlockState(blockposition.relative(enumdirection));

        if (isStairs(iblockdata1) && iblockdata.getValue(BlockStairs.HALF) == iblockdata1.getValue(BlockStairs.HALF)) {
            EnumDirection enumdirection1 = (EnumDirection) iblockdata1.getValue(BlockStairs.FACING);

            if (enumdirection1.getAxis() != ((EnumDirection) iblockdata.getValue(BlockStairs.FACING)).getAxis() && canTakeShape(iblockdata, iblockaccess, blockposition, enumdirection1.getOpposite())) {
                if (enumdirection1 == enumdirection.getCounterClockWise()) {
                    return BlockPropertyStairsShape.OUTER_LEFT;
                }

                return BlockPropertyStairsShape.OUTER_RIGHT;
            }
        }

        IBlockData iblockdata2 = iblockaccess.getBlockState(blockposition.relative(enumdirection.getOpposite()));

        if (isStairs(iblockdata2) && iblockdata.getValue(BlockStairs.HALF) == iblockdata2.getValue(BlockStairs.HALF)) {
            EnumDirection enumdirection2 = (EnumDirection) iblockdata2.getValue(BlockStairs.FACING);

            if (enumdirection2.getAxis() != ((EnumDirection) iblockdata.getValue(BlockStairs.FACING)).getAxis() && canTakeShape(iblockdata, iblockaccess, blockposition, enumdirection2)) {
                if (enumdirection2 == enumdirection.getCounterClockWise()) {
                    return BlockPropertyStairsShape.INNER_LEFT;
                }

                return BlockPropertyStairsShape.INNER_RIGHT;
            }
        }

        return BlockPropertyStairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata1 = iblockaccess.getBlockState(blockposition.relative(enumdirection));

        return !isStairs(iblockdata1) || iblockdata1.getValue(BlockStairs.FACING) != iblockdata.getValue(BlockStairs.FACING) || iblockdata1.getValue(BlockStairs.HALF) != iblockdata.getValue(BlockStairs.HALF);
    }

    public static boolean isStairs(IBlockData iblockdata) {
        return iblockdata.getBlock() instanceof BlockStairs;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockStairs.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockStairs.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockStairs.FACING);
        BlockPropertyStairsShape blockpropertystairsshape = (BlockPropertyStairsShape) iblockdata.getValue(BlockStairs.SHAPE);

        switch (enumblockmirror) {
            case LEFT_RIGHT:
                if (enumdirection.getAxis() == EnumDirection.EnumAxis.Z) {
                    switch (blockpropertystairsshape) {
                        case INNER_LEFT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.INNER_RIGHT);
                        case INNER_RIGHT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.INNER_LEFT);
                        case OUTER_LEFT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.OUTER_LEFT);
                        default:
                            return iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180);
                    }
                }
                break;
            case FRONT_BACK:
                if (enumdirection.getAxis() == EnumDirection.EnumAxis.X) {
                    switch (blockpropertystairsshape) {
                        case INNER_LEFT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.INNER_LEFT);
                        case INNER_RIGHT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.INNER_RIGHT);
                        case OUTER_LEFT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return (IBlockData) iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180).setValue(BlockStairs.SHAPE, BlockPropertyStairsShape.OUTER_LEFT);
                        case STRAIGHT:
                            return iblockdata.rotate(EnumBlockRotation.CLOCKWISE_180);
                    }
                }
        }

        return super.mirror(iblockdata, enumblockmirror);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockStairs.FACING, BlockStairs.HALF, BlockStairs.SHAPE, BlockStairs.WATERLOGGED);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockStairs.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
