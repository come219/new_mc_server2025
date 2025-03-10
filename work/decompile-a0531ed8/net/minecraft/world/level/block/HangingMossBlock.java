package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class HangingMossBlock extends Block implements IBlockFragilePlantElement {

    public static final MapCodec<HangingMossBlock> CODEC = simpleCodec(HangingMossBlock::new);
    private static final int SIDE_PADDING = 1;
    private static final VoxelShape TIP_SHAPE = Block.box(1.0D, 2.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final VoxelShape BASE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    public static final BlockStateBoolean TIP = BlockProperties.TIP;

    @Override
    public MapCodec<HangingMossBlock> codec() {
        return HangingMossBlock.CODEC;
    }

    public HangingMossBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(HangingMossBlock.TIP, true));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (Boolean) iblockdata.getValue(HangingMossBlock.TIP) ? HangingMossBlock.TIP_SHAPE : HangingMossBlock.BASE_SHAPE;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(500) == 0) {
            IBlockData iblockdata1 = world.getBlockState(blockposition.above());

            if (iblockdata1.is(TagsBlock.PALE_OAK_LOGS) || iblockdata1.is(Blocks.PALE_OAK_LEAVES)) {
                world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.PALE_HANGING_MOSS_IDLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }

    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return this.canStayAtPosition(iworldreader, blockposition);
    }

    private boolean canStayAtPosition(IBlockAccess iblockaccess, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.relative(EnumDirection.UP);
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition1);

        return MultifaceBlock.canAttachTo(iblockaccess, EnumDirection.UP, blockposition1, iblockdata) || iblockdata.is(Blocks.PALE_HANGING_MOSS);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (!this.canStayAtPosition(iworldreader, blockposition)) {
            scheduledtickaccess.scheduleTick(blockposition, (Block) this, 1);
        }

        return (IBlockData) iblockdata.setValue(HangingMossBlock.TIP, !iworldreader.getBlockState(blockposition.below()).is((Block) this));
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!this.canStayAtPosition(worldserver, blockposition)) {
            worldserver.destroyBlock(blockposition, true);
        }

    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(HangingMossBlock.TIP);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return this.canGrowInto(iworldreader.getBlockState(this.getTip(iworldreader, blockposition).below()));
    }

    private boolean canGrowInto(IBlockData iblockdata) {
        return iblockdata.isAir();
    }

    public BlockPosition getTip(IBlockAccess iblockaccess, BlockPosition blockposition) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();

        IBlockData iblockdata;

        do {
            blockposition_mutableblockposition.move(EnumDirection.DOWN);
            iblockdata = iblockaccess.getBlockState(blockposition_mutableblockposition);
        } while (iblockdata.is((Block) this));

        return blockposition_mutableblockposition.relative(EnumDirection.UP).immutable();
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        BlockPosition blockposition1 = this.getTip(worldserver, blockposition).below();

        if (this.canGrowInto(worldserver.getBlockState(blockposition1))) {
            worldserver.setBlockAndUpdate(blockposition1, (IBlockData) iblockdata.setValue(HangingMossBlock.TIP, true));
        }
    }
}
