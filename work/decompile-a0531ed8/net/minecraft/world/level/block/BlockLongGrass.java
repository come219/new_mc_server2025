package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockLongGrass extends BlockPlant implements IBlockFragilePlantElement {

    public static final MapCodec<BlockLongGrass> CODEC = simpleCodec(BlockLongGrass::new);
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    @Override
    public MapCodec<BlockLongGrass> codec() {
        return BlockLongGrass.CODEC;
    }

    protected BlockLongGrass(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockLongGrass.SHAPE;
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return getGrownBlock(iblockdata).defaultBlockState().canSurvive(iworldreader, blockposition) && iworldreader.isEmptyBlock(blockposition.above());
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        BlockTallPlant.placeAt(worldserver, getGrownBlock(iblockdata).defaultBlockState(), blockposition, 2);
    }

    private static BlockTallPlant getGrownBlock(IBlockData iblockdata) {
        return (BlockTallPlant) (iblockdata.is(Blocks.FERN) ? Blocks.LARGE_FERN : Blocks.TALL_GRASS);
    }
}
