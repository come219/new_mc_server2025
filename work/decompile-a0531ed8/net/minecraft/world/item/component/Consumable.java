package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.food.FoodInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

public record Consumable(float consumeSeconds, ItemUseAnimation animation, Holder<SoundEffect> sound, boolean hasConsumeParticles, List<ConsumeEffect> onConsumeEffects) {

    public static final float DEFAULT_CONSUME_SECONDS = 1.6F;
    private static final int CONSUME_EFFECTS_INTERVAL = 4;
    private static final float CONSUME_EFFECTS_START_FRACTION = 0.21875F;
    public static final Codec<Consumable> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("consume_seconds", 1.6F).forGetter(Consumable::consumeSeconds), ItemUseAnimation.CODEC.optionalFieldOf("animation", ItemUseAnimation.EAT).forGetter(Consumable::animation), SoundEffect.CODEC.optionalFieldOf("sound", SoundEffects.GENERIC_EAT).forGetter(Consumable::sound), Codec.BOOL.optionalFieldOf("has_consume_particles", true).forGetter(Consumable::hasConsumeParticles), ConsumeEffect.CODEC.listOf().optionalFieldOf("on_consume_effects", List.of()).forGetter(Consumable::onConsumeEffects)).apply(instance, Consumable::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, Consumable> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, Consumable::consumeSeconds, ItemUseAnimation.STREAM_CODEC, Consumable::animation, SoundEffect.STREAM_CODEC, Consumable::sound, ByteBufCodecs.BOOL, Consumable::hasConsumeParticles, ConsumeEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), Consumable::onConsumeEffects, Consumable::new);

    public EnumInteractionResult startConsuming(EntityLiving entityliving, ItemStack itemstack, EnumHand enumhand) {
        if (!this.canConsume(entityliving, itemstack)) {
            return EnumInteractionResult.FAIL;
        } else {
            boolean flag = this.consumeTicks() > 0;

            if (flag) {
                entityliving.startUsingItem(enumhand);
                return EnumInteractionResult.CONSUME;
            } else {
                ItemStack itemstack1 = this.onConsume(entityliving.level(), entityliving, itemstack);

                return EnumInteractionResult.CONSUME.heldItemTransformedTo(itemstack1);
            }
        }
    }

    public ItemStack onConsume(World world, EntityLiving entityliving, ItemStack itemstack) {
        RandomSource randomsource = entityliving.getRandom();

        this.emitParticlesAndSounds(randomsource, entityliving, itemstack, 16);
        if (entityliving instanceof EntityPlayer entityplayer) {
            entityplayer.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
            CriterionTriggers.CONSUME_ITEM.trigger(entityplayer, itemstack);
        }

        itemstack.getAllOfType(ConsumableListener.class).forEach((consumablelistener) -> {
            consumablelistener.onConsume(world, entityliving, itemstack, this);
        });
        if (!world.isClientSide) {
            this.onConsumeEffects.forEach((consumeeffect) -> {
                consumeeffect.apply(world, itemstack, entityliving);
            });
        }

        entityliving.gameEvent(this.animation == ItemUseAnimation.DRINK ? GameEvent.DRINK : GameEvent.EAT);
        itemstack.consume(1, entityliving);
        return itemstack;
    }

    public boolean canConsume(EntityLiving entityliving, ItemStack itemstack) {
        FoodInfo foodinfo = (FoodInfo) itemstack.get(DataComponents.FOOD);

        if (foodinfo != null && entityliving instanceof EntityHuman entityhuman) {
            return entityhuman.canEat(foodinfo.canAlwaysEat());
        } else {
            return true;
        }
    }

    public int consumeTicks() {
        return (int) (this.consumeSeconds * 20.0F);
    }

    public void emitParticlesAndSounds(RandomSource randomsource, EntityLiving entityliving, ItemStack itemstack, int i) {
        float f = randomsource.nextBoolean() ? 0.5F : 1.0F;
        float f1 = randomsource.triangle(1.0F, 0.2F);
        float f2 = 0.5F;
        float f3 = MathHelper.randomBetween(randomsource, 0.9F, 1.0F);
        float f4 = this.animation == ItemUseAnimation.DRINK ? 0.5F : f;
        float f5 = this.animation == ItemUseAnimation.DRINK ? f3 : f1;

        if (this.hasConsumeParticles) {
            entityliving.spawnItemParticles(itemstack, i);
        }

        SoundEffect soundeffect;

        if (entityliving instanceof Consumable.b consumable_b) {
            soundeffect = consumable_b.getConsumeSound(itemstack);
        } else {
            soundeffect = (SoundEffect) this.sound.value();
        }

        SoundEffect soundeffect1 = soundeffect;

        entityliving.playSound(soundeffect1, f4, f5);
    }

    public boolean shouldEmitParticlesAndSounds(int i) {
        int j = this.consumeTicks() - i;
        int k = (int) ((float) this.consumeTicks() * 0.21875F);
        boolean flag = j > k;

        return flag && i % 4 == 0;
    }

    public static Consumable.a builder() {
        return new Consumable.a();
    }

    public interface b {

        SoundEffect getConsumeSound(ItemStack itemstack);
    }

    public static class a {

        private float consumeSeconds = 1.6F;
        private ItemUseAnimation animation;
        private Holder<SoundEffect> sound;
        private boolean hasConsumeParticles;
        private final List<ConsumeEffect> onConsumeEffects;

        a() {
            this.animation = ItemUseAnimation.EAT;
            this.sound = SoundEffects.GENERIC_EAT;
            this.hasConsumeParticles = true;
            this.onConsumeEffects = new ArrayList();
        }

        public Consumable.a consumeSeconds(float f) {
            this.consumeSeconds = f;
            return this;
        }

        public Consumable.a animation(ItemUseAnimation itemuseanimation) {
            this.animation = itemuseanimation;
            return this;
        }

        public Consumable.a sound(Holder<SoundEffect> holder) {
            this.sound = holder;
            return this;
        }

        public Consumable.a soundAfterConsume(Holder<SoundEffect> holder) {
            return this.onConsume(new PlaySoundConsumeEffect(holder));
        }

        public Consumable.a hasConsumeParticles(boolean flag) {
            this.hasConsumeParticles = flag;
            return this;
        }

        public Consumable.a onConsume(ConsumeEffect consumeeffect) {
            this.onConsumeEffects.add(consumeeffect);
            return this;
        }

        public Consumable build() {
            return new Consumable(this.consumeSeconds, this.animation, this.sound, this.hasConsumeParticles, this.onConsumeEffects);
        }
    }
}
