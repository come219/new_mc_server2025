package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;

public class BonemealableFeaturePlacerBlock extends Block implements IBlockFragilePlantElement {

    public static final MapCodec<BonemealableFeaturePlacerBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter((bonemealablefeatureplacerblock) -> {
            return bonemealablefeatureplacerblock.feature;
        }), propertiesCodec()).apply(instance, BonemealableFeaturePlacerBlock::new);
    });
    private final ResourceKey<WorldGenFeatureConfigured<?, ?>> feature;

    @Override
    public MapCodec<BonemealableFeaturePlacerBlock> codec() {
        return BonemealableFeaturePlacerBlock.CODEC;
    }

    public BonemealableFeaturePlacerBlock(ResourceKey<WorldGenFeatureConfigured<?, ?>> resourcekey, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.feature = resourcekey;
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return iworldreader.getBlockState(blockposition.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        worldserver.registryAccess().lookup(Registries.CONFIGURED_FEATURE).flatMap((iregistry) -> {
            return iregistry.get(this.feature);
        }).ifPresent((holder_c) -> {
            ((WorldGenFeatureConfigured) holder_c.value()).place(worldserver, worldserver.getChunkSource().getGenerator(), randomsource, blockposition.above());
        });
    }

    @Override
    public IBlockFragilePlantElement.a getType() {
        return IBlockFragilePlantElement.a.NEIGHBOR_SPREADER;
    }
}
