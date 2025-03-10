package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public record ClearAllStatusEffectsConsumeEffect() implements ConsumeEffect {

    public static final ClearAllStatusEffectsConsumeEffect INSTANCE = new ClearAllStatusEffectsConsumeEffect();
    public static final MapCodec<ClearAllStatusEffectsConsumeEffect> CODEC = MapCodec.unit(ClearAllStatusEffectsConsumeEffect.INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearAllStatusEffectsConsumeEffect> STREAM_CODEC = StreamCodec.unit(ClearAllStatusEffectsConsumeEffect.INSTANCE);

    @Override
    public ConsumeEffect.a<ClearAllStatusEffectsConsumeEffect> getType() {
        return ConsumeEffect.a.CLEAR_ALL_EFFECTS;
    }

    @Override
    public boolean apply(World world, ItemStack itemstack, EntityLiving entityliving) {
        return entityliving.removeAllEffects();
    }
}
