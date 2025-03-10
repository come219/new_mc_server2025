package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public interface ConsumeEffect {

    Codec<ConsumeEffect> CODEC = BuiltInRegistries.CONSUME_EFFECT_TYPE.byNameCodec().dispatch(ConsumeEffect::getType, ConsumeEffect.a::codec);
    StreamCodec<RegistryFriendlyByteBuf, ConsumeEffect> STREAM_CODEC = ByteBufCodecs.registry(Registries.CONSUME_EFFECT_TYPE).dispatch(ConsumeEffect::getType, ConsumeEffect.a::streamCodec);

    ConsumeEffect.a<? extends ConsumeEffect> getType();

    boolean apply(World world, ItemStack itemstack, EntityLiving entityliving);

    public static record a<T extends ConsumeEffect>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

        public static final ConsumeEffect.a<ApplyStatusEffectsConsumeEffect> APPLY_EFFECTS = register("apply_effects", ApplyStatusEffectsConsumeEffect.CODEC, ApplyStatusEffectsConsumeEffect.STREAM_CODEC);
        public static final ConsumeEffect.a<RemoveStatusEffectsConsumeEffect> REMOVE_EFFECTS = register("remove_effects", RemoveStatusEffectsConsumeEffect.CODEC, RemoveStatusEffectsConsumeEffect.STREAM_CODEC);
        public static final ConsumeEffect.a<ClearAllStatusEffectsConsumeEffect> CLEAR_ALL_EFFECTS = register("clear_all_effects", ClearAllStatusEffectsConsumeEffect.CODEC, ClearAllStatusEffectsConsumeEffect.STREAM_CODEC);
        public static final ConsumeEffect.a<TeleportRandomlyConsumeEffect> TELEPORT_RANDOMLY = register("teleport_randomly", TeleportRandomlyConsumeEffect.CODEC, TeleportRandomlyConsumeEffect.STREAM_CODEC);
        public static final ConsumeEffect.a<PlaySoundConsumeEffect> PLAY_SOUND = register("play_sound", PlaySoundConsumeEffect.CODEC, PlaySoundConsumeEffect.STREAM_CODEC);

        private static <T extends ConsumeEffect> ConsumeEffect.a<T> register(String s, MapCodec<T> mapcodec, StreamCodec<RegistryFriendlyByteBuf, T> streamcodec) {
            return (ConsumeEffect.a) IRegistry.register(BuiltInRegistries.CONSUME_EFFECT_TYPE, s, new ConsumeEffect.a<>(mapcodec, streamcodec));
        }
    }
}
