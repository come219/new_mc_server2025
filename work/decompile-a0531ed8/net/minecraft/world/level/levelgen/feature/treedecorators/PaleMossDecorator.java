package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class PaleMossDecorator extends WorldGenFeatureTree {

    public static final MapCodec<PaleMossDecorator> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("leaves_probability").forGetter((palemossdecorator) -> {
            return palemossdecorator.leavesProbability;
        }), Codec.floatRange(0.0F, 1.0F).fieldOf("trunk_probability").forGetter((palemossdecorator) -> {
            return palemossdecorator.trunkProbability;
        }), Codec.floatRange(0.0F, 1.0F).fieldOf("ground_probability").forGetter((palemossdecorator) -> {
            return palemossdecorator.groundProbability;
        })).apply(instance, PaleMossDecorator::new);
    });
    private final float leavesProbability;
    private final float trunkProbability;
    private final float groundProbability;

    @Override
    protected WorldGenFeatureTrees<?> type() {
        return WorldGenFeatureTrees.PALE_MOSS;
    }

    public PaleMossDecorator(float f, float f1, float f2) {
        this.leavesProbability = f;
        this.trunkProbability = f1;
        this.groundProbability = f2;
    }

    @Override
    public void place(WorldGenFeatureTree.a worldgenfeaturetree_a) {
        RandomSource randomsource = worldgenfeaturetree_a.random();
        GeneratorAccessSeed generatoraccessseed = (GeneratorAccessSeed) worldgenfeaturetree_a.level();
        List<BlockPosition> list = SystemUtils.shuffledCopy(worldgenfeaturetree_a.logs(), randomsource);

        if (!list.isEmpty()) {
            Mutable<BlockPosition> mutable = new MutableObject((BlockPosition) list.getFirst());

            list.forEach((blockposition) -> {
                if (blockposition.getY() < ((BlockPosition) mutable.getValue()).getY()) {
                    mutable.setValue(blockposition);
                }

            });
            BlockPosition blockposition = (BlockPosition) mutable.getValue();

            if (randomsource.nextFloat() < this.groundProbability) {
                generatoraccessseed.registryAccess().lookup(Registries.CONFIGURED_FEATURE).flatMap((iregistry) -> {
                    return iregistry.get(VegetationFeatures.PALE_MOSS_PATCH);
                }).ifPresent((holder_c) -> {
                    ((WorldGenFeatureConfigured) holder_c.value()).place(generatoraccessseed, generatoraccessseed.getLevel().getChunkSource().getGenerator(), randomsource, blockposition.above());
                });
            }

            worldgenfeaturetree_a.logs().forEach((blockposition1) -> {
                if (randomsource.nextFloat() < this.trunkProbability) {
                    BlockPosition blockposition2 = blockposition1.below();

                    if (worldgenfeaturetree_a.isAir(blockposition2)) {
                        addMossHanger(blockposition2, worldgenfeaturetree_a);
                    }
                }

            });
            worldgenfeaturetree_a.leaves().forEach((blockposition1) -> {
                if (randomsource.nextFloat() < this.leavesProbability) {
                    BlockPosition blockposition2 = blockposition1.below();

                    if (worldgenfeaturetree_a.isAir(blockposition2)) {
                        addMossHanger(blockposition2, worldgenfeaturetree_a);
                    }
                }

            });
        }
    }

    private static void addMossHanger(BlockPosition blockposition, WorldGenFeatureTree.a worldgenfeaturetree_a) {
        while (worldgenfeaturetree_a.isAir(blockposition.below()) && (double) worldgenfeaturetree_a.random().nextFloat() >= 0.5D) {
            worldgenfeaturetree_a.setBlock(blockposition, (IBlockData) Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, false));
            blockposition = blockposition.below();
        }

        worldgenfeaturetree_a.setBlock(blockposition, (IBlockData) Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, true));
    }
}
