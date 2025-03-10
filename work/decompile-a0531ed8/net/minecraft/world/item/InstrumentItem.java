package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagKey;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {

    private final TagKey<Instrument> instruments;

    public InstrumentItem(TagKey<Instrument> tagkey, Item.Info item_info) {
        super(item_info);
        this.instruments = tagkey;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        HolderLookup.a holderlookup_a = item_b.registries();

        if (holderlookup_a != null) {
            Optional<Holder<Instrument>> optional = this.getInstrument(itemstack, holderlookup_a);

            if (optional.isPresent()) {
                IChatMutableComponent ichatmutablecomponent = ((Instrument) ((Holder) optional.get()).value()).description().copy();

                ChatComponentUtils.mergeStyles(ichatmutablecomponent, ChatModifier.EMPTY.withColor(EnumChatFormat.GRAY));
                list.add(ichatmutablecomponent);
            }

        }
    }

    public static ItemStack create(Item item, Holder<Instrument> holder) {
        ItemStack itemstack = new ItemStack(item);

        itemstack.set(DataComponents.INSTRUMENT, holder);
        return itemstack;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemstack, entityhuman.registryAccess());

        if (optional.isPresent()) {
            Instrument instrument = (Instrument) ((Holder) optional.get()).value();

            entityhuman.startUsingItem(enumhand);
            play(world, entityhuman, instrument);
            entityhuman.getCooldowns().addCooldown(itemstack, MathHelper.floor(instrument.useDuration() * 20.0F));
            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            return EnumInteractionResult.CONSUME;
        } else {
            return EnumInteractionResult.FAIL;
        }
    }

    @Override
    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        Optional<Holder<Instrument>> optional = this.getInstrument(itemstack, entityliving.registryAccess());

        return (Integer) optional.map((holder) -> {
            return MathHelper.floor(((Instrument) holder.value()).useDuration() * 20.0F);
        }).orElse(0);
    }

    private Optional<Holder<Instrument>> getInstrument(ItemStack itemstack, HolderLookup.a holderlookup_a) {
        Holder<Instrument> holder = (Holder) itemstack.get(DataComponents.INSTRUMENT);

        if (holder != null) {
            return Optional.of(holder);
        } else {
            Optional<HolderSet.Named<Instrument>> optional = holderlookup_a.lookupOrThrow(Registries.INSTRUMENT).get(this.instruments);

            if (optional.isPresent()) {
                Iterator<Holder<Instrument>> iterator = ((HolderSet.Named) optional.get()).iterator();

                if (iterator.hasNext()) {
                    return Optional.of((Holder) iterator.next());
                }
            }

            return Optional.empty();
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    private static void play(World world, EntityHuman entityhuman, Instrument instrument) {
        SoundEffect soundeffect = (SoundEffect) instrument.soundEvent().value();
        float f = instrument.range() / 16.0F;

        world.playSound(entityhuman, (Entity) entityhuman, soundeffect, SoundCategory.RECORDS, f, 1.0F);
        world.gameEvent((Holder) GameEvent.INSTRUMENT_PLAY, entityhuman.position(), GameEvent.a.of((Entity) entityhuman));
    }
}
