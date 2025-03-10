package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockSoulFire extends BlockFireAbstract {

    public static final MapCodec<BlockSoulFire> CODEC = simpleCodec(BlockSoulFire::new);

    @Override
    public MapCodec<BlockSoulFire> codec() {
        return BlockSoulFire.CODEC;
    }

    public BlockSoulFire(BlockBase.Info blockbase_info) {
        super(blockbase_info, 2.0F);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return this.canSurvive(iblockdata, iworldreader, blockposition) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return canSurviveOnBlock(iworldreader.getBlockState(blockposition.below()));
    }

    public static boolean canSurviveOnBlock(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.SOUL_FIRE_BASE_BLOCKS);
    }

    @Override
    protected boolean canBurn(IBlockData iblockdata) {
        return true;
    }
}
