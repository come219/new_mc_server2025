package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public record ApplyStatusEffectsConsumeEffect(List<MobEffect> effects, float probability) implements ConsumeEffect {

    public static final MapCodec<ApplyStatusEffectsConsumeEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MobEffect.CODEC.listOf().fieldOf("effects").forGetter(ApplyStatusEffectsConsumeEffect::effects), Codec.floatRange(0.0F, 1.0F).optionalFieldOf("probability", 1.0F).forGetter(ApplyStatusEffectsConsumeEffect::probability)).apply(instance, ApplyStatusEffectsConsumeEffect::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ApplyStatusEffectsConsumeEffect> STREAM_CODEC = StreamCodec.composite(MobEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), ApplyStatusEffectsConsumeEffect::effects, ByteBufCodecs.FLOAT, ApplyStatusEffectsConsumeEffect::probability, ApplyStatusEffectsConsumeEffect::new);

    public ApplyStatusEffectsConsumeEffect(MobEffect mobeffect, float f) {
        this(List.of(mobeffect), f);
    }

    public ApplyStatusEffectsConsumeEffect(List<MobEffect> list) {
        this(list, 1.0F);
    }

    public ApplyStatusEffectsConsumeEffect(MobEffect mobeffect) {
        this(mobeffect, 1.0F);
    }

    @Override
    public ConsumeEffect.a<ApplyStatusEffectsConsumeEffect> getType() {
        return ConsumeEffect.a.APPLY_EFFECTS;
    }

    @Override
    public boolean apply(World world, ItemStack itemstack, EntityLiving entityliving) {
        if (entityliving.getRandom().nextFloat() >= this.probability) {
            return false;
        } else {
            boolean flag = false;
            Iterator iterator = this.effects.iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                if (entityliving.addEffect(new MobEffect(mobeffect))) {
                    flag = true;
                }
            }

            return flag;
        }
    }
}
