package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class ParticleLeavesBlock extends BlockLeaves {

    public static final MapCodec<ParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.POSITIVE_INT.fieldOf("chance").forGetter((particleleavesblock) -> {
            return particleleavesblock.chance;
        }), Particles.CODEC.fieldOf("particle").forGetter((particleleavesblock) -> {
            return particleleavesblock.particle;
        }), propertiesCodec()).apply(instance, ParticleLeavesBlock::new);
    });
    private final ParticleParam particle;
    private final int chance;

    @Override
    public MapCodec<ParticleLeavesBlock> codec() {
        return ParticleLeavesBlock.CODEC;
    }

    public ParticleLeavesBlock(int i, ParticleParam particleparam, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.chance = i;
        this.particle = particleparam;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        super.animateTick(iblockdata, world, blockposition, randomsource);
        if (randomsource.nextInt(this.chance) == 0) {
            BlockPosition blockposition1 = blockposition.below();
            IBlockData iblockdata1 = world.getBlockState(blockposition1);

            if (!isFaceFull(iblockdata1.getCollisionShape(world, blockposition1), EnumDirection.UP)) {
                ParticleUtils.spawnParticleBelow(world, blockposition, randomsource, this.particle);
            }
        }
    }
}
