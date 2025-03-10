package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public class ParticleParamRedstone extends DustParticleOptionsBase {

    public static final int REDSTONE_PARTICLE_COLOR = 16711680;
    public static final ParticleParamRedstone REDSTONE = new ParticleParamRedstone(16711680, 1.0F);
    public static final MapCodec<ParticleParamRedstone> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter((particleparamredstone) -> {
            return particleparamredstone.color;
        }), ParticleParamRedstone.SCALE.fieldOf("scale").forGetter(DustParticleOptionsBase::getScale)).apply(instance, ParticleParamRedstone::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleParamRedstone> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, (particleparamredstone) -> {
        return particleparamredstone.color;
    }, ByteBufCodecs.FLOAT, DustParticleOptionsBase::getScale, ParticleParamRedstone::new);
    private final int color;

    public ParticleParamRedstone(int i, float f) {
        super(f);
        this.color = i;
    }

    @Override
    public Particle<ParticleParamRedstone> getType() {
        return Particles.DUST;
    }

    public Vector3f getColor() {
        return ARGB.vector3fFromRGB24(this.color);
    }
}
