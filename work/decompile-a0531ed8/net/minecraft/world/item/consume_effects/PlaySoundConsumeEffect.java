package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public record PlaySoundConsumeEffect(Holder<SoundEffect> sound) implements ConsumeEffect {

    public static final MapCodec<PlaySoundConsumeEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(SoundEffect.CODEC.fieldOf("sound").forGetter(PlaySoundConsumeEffect::sound)).apply(instance, PlaySoundConsumeEffect::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaySoundConsumeEffect> STREAM_CODEC = StreamCodec.composite(SoundEffect.STREAM_CODEC, PlaySoundConsumeEffect::sound, PlaySoundConsumeEffect::new);

    @Override
    public ConsumeEffect.a<PlaySoundConsumeEffect> getType() {
        return ConsumeEffect.a.PLAY_SOUND;
    }

    @Override
    public boolean apply(World world, ItemStack itemstack, EntityLiving entityliving) {
        world.playSound((EntityHuman) null, entityliving.blockPosition(), (SoundEffect) this.sound.value(), entityliving.getSoundSource(), 1.0F, 1.0F);
        return true;
    }
}
