package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;

public record UseCooldown(float seconds, Optional<MinecraftKey> cooldownGroup) {

    public static final Codec<UseCooldown> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.POSITIVE_FLOAT.fieldOf("seconds").forGetter(UseCooldown::seconds), MinecraftKey.CODEC.optionalFieldOf("cooldown_group").forGetter(UseCooldown::cooldownGroup)).apply(instance, UseCooldown::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, UseCooldown> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, UseCooldown::seconds, MinecraftKey.STREAM_CODEC.apply(ByteBufCodecs::optional), UseCooldown::cooldownGroup, UseCooldown::new);

    public UseCooldown(float f) {
        this(f, Optional.empty());
    }

    public int ticks() {
        return (int) (this.seconds * 20.0F);
    }

    public void apply(ItemStack itemstack, EntityLiving entityliving) {
        if (entityliving instanceof EntityHuman entityhuman) {
            entityhuman.getCooldowns().addCooldown(itemstack, this.ticks());
        }

    }
}
