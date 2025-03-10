package net.minecraft.world.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.World;

public record FoodInfo(int nutrition, float saturation, boolean canAlwaysEat) implements ConsumableListener {

    public static final Codec<FoodInfo> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodInfo::nutrition), Codec.FLOAT.fieldOf("saturation").forGetter(FoodInfo::saturation), Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(FoodInfo::canAlwaysEat)).apply(instance, FoodInfo::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodInfo> DIRECT_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, FoodInfo::nutrition, ByteBufCodecs.FLOAT, FoodInfo::saturation, ByteBufCodecs.BOOL, FoodInfo::canAlwaysEat, FoodInfo::new);

    @Override
    public void onConsume(World world, EntityLiving entityliving, ItemStack itemstack, Consumable consumable) {
        RandomSource randomsource = entityliving.getRandom();

        world.playSound((EntityHuman) null, entityliving.getX(), entityliving.getY(), entityliving.getZ(), (SoundEffect) consumable.sound().value(), SoundCategory.NEUTRAL, 1.0F, randomsource.triangle(1.0F, 0.4F));
        if (entityliving instanceof EntityHuman entityhuman) {
            entityhuman.getFoodData().eat(this);
            world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, MathHelper.randomBetween(randomsource, 0.9F, 1.0F));
        }

    }

    public static class a {

        private int nutrition;
        private float saturationModifier;
        private boolean canAlwaysEat;

        public a() {}

        public FoodInfo.a nutrition(int i) {
            this.nutrition = i;
            return this;
        }

        public FoodInfo.a saturationModifier(float f) {
            this.saturationModifier = f;
            return this;
        }

        public FoodInfo.a alwaysEdible() {
            this.canAlwaysEat = true;
            return this;
        }

        public FoodInfo build() {
            float f = FoodConstants.saturationByModifier(this.nutrition, this.saturationModifier);

            return new FoodInfo(this.nutrition, f, this.canAlwaysEat);
        }
    }
}
