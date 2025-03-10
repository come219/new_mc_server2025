package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

public record DeathProtection(List<ConsumeEffect> deathEffects) {

    public static final Codec<DeathProtection> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ConsumeEffect.CODEC.listOf().optionalFieldOf("death_effects", List.of()).forGetter(DeathProtection::deathEffects)).apply(instance, DeathProtection::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DeathProtection> STREAM_CODEC = StreamCodec.composite(ConsumeEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), DeathProtection::deathEffects, DeathProtection::new);
    public static final DeathProtection TOTEM_OF_UNDYING = new DeathProtection(List.of(new ClearAllStatusEffectsConsumeEffect(), new ApplyStatusEffectsConsumeEffect(List.of(new MobEffect(MobEffects.REGENERATION, 900, 1), new MobEffect(MobEffects.ABSORPTION, 100, 1), new MobEffect(MobEffects.FIRE_RESISTANCE, 800, 0)))));

    public void applyEffects(ItemStack itemstack, EntityLiving entityliving) {
        Iterator iterator = this.deathEffects.iterator();

        while (iterator.hasNext()) {
            ConsumeEffect consumeeffect = (ConsumeEffect) iterator.next();

            consumeeffect.apply(entityliving.level(), itemstack, entityliving);
        }

    }
}
