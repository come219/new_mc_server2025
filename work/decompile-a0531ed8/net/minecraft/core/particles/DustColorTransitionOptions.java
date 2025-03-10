package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public class DustColorTransitionOptions extends DustParticleOptionsBase {

    public static final int SCULK_PARTICLE_COLOR = 3790560;
    public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(3790560, 16711680, 1.0F);
    public static final MapCodec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("from_color").forGetter((dustcolortransitionoptions) -> {
            return dustcolortransitionoptions.fromColor;
        }), ExtraCodecs.RGB_COLOR_CODEC.fieldOf("to_color").forGetter((dustcolortransitionoptions) -> {
            return dustcolortransitionoptions.toColor;
        }), DustColorTransitionOptions.SCALE.fieldOf("scale").forGetter(DustParticleOptionsBase::getScale)).apply(instance, DustColorTransitionOptions::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DustColorTransitionOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, (dustcolortransitionoptions) -> {
        return dustcolortransitionoptions.fromColor;
    }, ByteBufCodecs.INT, (dustcolortransitionoptions) -> {
        return dustcolortransitionoptions.toColor;
    }, ByteBufCodecs.FLOAT, DustParticleOptionsBase::getScale, DustColorTransitionOptions::new);
    private final int fromColor;
    private final int toColor;

    public DustColorTransitionOptions(int i, int j, float f) {
        super(f);
        this.fromColor = i;
        this.toColor = j;
    }

    public Vector3f getFromColor() {
        return ARGB.vector3fFromRGB24(this.fromColor);
    }

    public Vector3f getToColor() {
        return ARGB.vector3fFromRGB24(this.toColor);
    }

    @Override
    public Particle<DustColorTransitionOptions> getType() {
        return Particles.DUST_COLOR_TRANSITION;
    }
}
