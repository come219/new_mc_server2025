package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3D;

public record TrailParticleOption(Vec3D target, int color, int duration) implements ParticleParam {

    public static final MapCodec<TrailParticleOption> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Vec3D.CODEC.fieldOf("target").forGetter(TrailParticleOption::target), ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(TrailParticleOption::color), ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(TrailParticleOption::duration)).apply(instance, TrailParticleOption::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, TrailParticleOption> STREAM_CODEC = StreamCodec.composite(Vec3D.STREAM_CODEC, TrailParticleOption::target, ByteBufCodecs.INT, TrailParticleOption::color, ByteBufCodecs.VAR_INT, TrailParticleOption::duration, TrailParticleOption::new);

    @Override
    public Particle<TrailParticleOption> getType() {
        return Particles.TRAIL;
    }
}
