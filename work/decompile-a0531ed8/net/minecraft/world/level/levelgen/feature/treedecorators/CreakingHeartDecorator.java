package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.state.IBlockData;

public class CreakingHeartDecorator extends WorldGenFeatureTree {

    public static final MapCodec<CreakingHeartDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(CreakingHeartDecorator::new, (creakingheartdecorator) -> {
        return creakingheartdecorator.probability;
    });
    private final float probability;

    public CreakingHeartDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected WorldGenFeatureTrees<?> type() {
        return WorldGenFeatureTrees.CREAKING_HEART;
    }

    @Override
    public void place(WorldGenFeatureTree.a worldgenfeaturetree_a) {
        RandomSource randomsource = worldgenfeaturetree_a.random();
        List<BlockPosition> list = worldgenfeaturetree_a.logs();

        if (!list.isEmpty()) {
            if (randomsource.nextFloat() < this.probability) {
                List<BlockPosition> list1 = new ArrayList(list);

                SystemUtils.shuffle(list1, randomsource);
                Optional<BlockPosition> optional = list1.stream().filter((blockposition) -> {
                    EnumDirection[] aenumdirection = EnumDirection.values();
                    int i = aenumdirection.length;

                    for (int j = 0; j < i; ++j) {
                        EnumDirection enumdirection = aenumdirection[j];

                        if (!worldgenfeaturetree_a.checkBlock(blockposition.relative(enumdirection), (iblockdata) -> {
                            return iblockdata.is(TagsBlock.LOGS);
                        })) {
                            return false;
                        }
                    }

                    return true;
                }).findFirst();

                if (!optional.isEmpty()) {
                    worldgenfeaturetree_a.setBlock((BlockPosition) optional.get(), (IBlockData) ((IBlockData) Blocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.ACTIVE, true)).setValue(CreakingHeartBlock.NATURAL, true));
                }
            }
        }
    }
}
