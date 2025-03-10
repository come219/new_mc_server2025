package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record UseRemainder(ItemStack convertInto) {

    public static final Codec<UseRemainder> CODEC = ItemStack.CODEC.xmap(UseRemainder::new, UseRemainder::convertInto);
    public static final StreamCodec<RegistryFriendlyByteBuf, UseRemainder> STREAM_CODEC = StreamCodec.composite(ItemStack.STREAM_CODEC, UseRemainder::convertInto, UseRemainder::new);

    public ItemStack convertIntoRemainder(ItemStack itemstack, int i, boolean flag, UseRemainder.a useremainder_a) {
        if (flag) {
            return itemstack;
        } else if (itemstack.getCount() >= i) {
            return itemstack;
        } else {
            ItemStack itemstack1 = this.convertInto.copy();

            if (itemstack.isEmpty()) {
                return itemstack1;
            } else {
                useremainder_a.apply(itemstack1);
                return itemstack;
            }
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            UseRemainder useremainder = (UseRemainder) object;

            return ItemStack.matches(this.convertInto, useremainder.convertInto);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return ItemStack.hashItemAndComponents(this.convertInto);
    }

    @FunctionalInterface
    public interface a {

        void apply(ItemStack itemstack);
    }
}
