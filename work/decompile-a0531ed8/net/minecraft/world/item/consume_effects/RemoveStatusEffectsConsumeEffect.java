package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public record RemoveStatusEffectsConsumeEffect(HolderSet<MobEffectList> effects) implements ConsumeEffect {

    public static final MapCodec<RemoveStatusEffectsConsumeEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(RemoveStatusEffectsConsumeEffect::effects)).apply(instance, RemoveStatusEffectsConsumeEffect::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveStatusEffectsConsumeEffect> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.MOB_EFFECT), RemoveStatusEffectsConsumeEffect::effects, RemoveStatusEffectsConsumeEffect::new);

    public RemoveStatusEffectsConsumeEffect(Holder<MobEffectList> holder) {
        this((HolderSet) HolderSet.direct(holder));
    }

    @Override
    public ConsumeEffect.a<RemoveStatusEffectsConsumeEffect> getType() {
        return ConsumeEffect.a.REMOVE_EFFECTS;
    }

    @Override
    public boolean apply(World world, ItemStack itemstack, EntityLiving entityliving) {
        boolean flag = false;
        Iterator iterator = this.effects.iterator();

        while (iterator.hasNext()) {
            Holder<MobEffectList> holder = (Holder) iterator.next();

            if (entityliving.removeEffect(holder)) {
                flag = true;
            }
        }

        return flag;
    }
}
