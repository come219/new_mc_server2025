package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;

public class BlockDirtSnow extends Block {

    public static final MapCodec<BlockDirtSnow> CODEC = simpleCodec(BlockDirtSnow::new);
    public static final BlockStateBoolean SNOWY = BlockProperties.SNOWY;

    @Override
    protected MapCodec<? extends BlockDirtSnow> codec() {
        return BlockDirtSnow.CODEC;
    }

    protected BlockDirtSnow(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockDirtSnow.SNOWY, false));
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return enumdirection == EnumDirection.UP ? (IBlockData) iblockdata.setValue(BlockDirtSnow.SNOWY, isSnowySetting(iblockdata1)) : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = blockactioncontext.getLevel().getBlockState(blockactioncontext.getClickedPos().above());

        return (IBlockData) this.defaultBlockState().setValue(BlockDirtSnow.SNOWY, isSnowySetting(iblockdata));
    }

    protected static boolean isSnowySetting(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.SNOW);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockDirtSnow.SNOWY);
    }
}
