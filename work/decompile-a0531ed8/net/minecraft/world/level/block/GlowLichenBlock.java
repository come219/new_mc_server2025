package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class GlowLichenBlock extends MultifaceSpreadeableBlock implements IBlockFragilePlantElement {

    public static final MapCodec<GlowLichenBlock> CODEC = simpleCodec(GlowLichenBlock::new);
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    @Override
    public MapCodec<GlowLichenBlock> codec() {
        return GlowLichenBlock.CODEC;
    }

    public GlowLichenBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    public static ToIntFunction<IBlockData> emission(int i) {
        return (iblockdata) -> {
            return MultifaceBlock.hasAnyFace(iblockdata) ? i : 0;
        };
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return EnumDirection.stream().anyMatch((enumdirection) -> {
            return this.spreader.canSpreadInAnyDirection(iblockdata, iworldreader, blockposition, enumdirection.getOpposite());
        });
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        this.spreader.spreadFromRandomFaceTowardRandomDirection(iblockdata, worldserver, blockposition, randomsource);
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata) {
        return iblockdata.getFluidState().isEmpty();
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }
}
