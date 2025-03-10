package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;

public class BlockCoralFanWall extends BlockCoralFanWallAbstract {

    public static final MapCodec<BlockCoralFanWall> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockCoral.DEAD_CORAL_FIELD.forGetter((blockcoralfanwall) -> {
            return blockcoralfanwall.deadBlock;
        }), propertiesCodec()).apply(instance, BlockCoralFanWall::new);
    });
    private final Block deadBlock;

    @Override
    public MapCodec<BlockCoralFanWall> codec() {
        return BlockCoralFanWall.CODEC;
    }

    protected BlockCoralFanWall(Block block, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.deadBlock = block;
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        this.tryScheduleDieTick(iblockdata, world, world, world.random, blockposition);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!scanForWater(iblockdata, worldserver, blockposition)) {
            worldserver.setBlock(blockposition, (IBlockData) ((IBlockData) this.deadBlock.defaultBlockState().setValue(BlockCoralFanWall.WATERLOGGED, false)).setValue(BlockCoralFanWall.FACING, (EnumDirection) iblockdata.getValue(BlockCoralFanWall.FACING)), 2);
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (enumdirection.getOpposite() == iblockdata.getValue(BlockCoralFanWall.FACING) && !iblockdata.canSurvive(iworldreader, blockposition)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if ((Boolean) iblockdata.getValue(BlockCoralFanWall.WATERLOGGED)) {
                scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
            }

            this.tryScheduleDieTick(iblockdata, iworldreader, scheduledtickaccess, randomsource, blockposition);
            return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
        }
    }
}
