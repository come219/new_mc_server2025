package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.World;

public record SuspiciousStewEffects(List<SuspiciousStewEffects.a> effects) implements ConsumableListener, TooltipProvider {

    public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());
    public static final int DEFAULT_DURATION = 160;
    public static final Codec<SuspiciousStewEffects> CODEC = SuspiciousStewEffects.a.CODEC.listOf().xmap(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);
    public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects> STREAM_CODEC = SuspiciousStewEffects.a.STREAM_CODEC.apply(ByteBufCodecs.list()).map(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);

    public SuspiciousStewEffects withEffectAdded(SuspiciousStewEffects.a suspicioussteweffects_a) {
        return new SuspiciousStewEffects(SystemUtils.copyAndAdd(this.effects, (Object) suspicioussteweffects_a));
    }

    @Override
    public void onConsume(World world, EntityLiving entityliving, ItemStack itemstack, Consumable consumable) {
        Iterator iterator = this.effects.iterator();

        while (iterator.hasNext()) {
            SuspiciousStewEffects.a suspicioussteweffects_a = (SuspiciousStewEffects.a) iterator.next();

            entityliving.addEffect(suspicioussteweffects_a.createEffectInstance());
        }

    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        if (tooltipflag.isCreative()) {
            List<MobEffect> list = new ArrayList();
            Iterator iterator = this.effects.iterator();

            while (iterator.hasNext()) {
                SuspiciousStewEffects.a suspicioussteweffects_a = (SuspiciousStewEffects.a) iterator.next();

                list.add(suspicioussteweffects_a.createEffectInstance());
            }

            PotionContents.addPotionTooltip(list, consumer, 1.0F, item_b.tickRate());
        }

    }

    public static record a(Holder<MobEffectList> effect, int duration) {

        public static final Codec<SuspiciousStewEffects.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(MobEffectList.CODEC.fieldOf("id").forGetter(SuspiciousStewEffects.a::effect), Codec.INT.lenientOptionalFieldOf("duration", 160).forGetter(SuspiciousStewEffects.a::duration)).apply(instance, SuspiciousStewEffects.a::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects.a> STREAM_CODEC = StreamCodec.composite(MobEffectList.STREAM_CODEC, SuspiciousStewEffects.a::effect, ByteBufCodecs.VAR_INT, SuspiciousStewEffects.a::duration, SuspiciousStewEffects.a::new);

        public MobEffect createEffectInstance() {
            return new MobEffect(this.effect, this.duration);
        }
    }
}
