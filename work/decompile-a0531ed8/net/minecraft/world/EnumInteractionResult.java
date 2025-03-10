package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

public sealed interface EnumInteractionResult {

    EnumInteractionResult.d SUCCESS = new EnumInteractionResult.d(EnumInteractionResult.e.CLIENT, EnumInteractionResult.b.DEFAULT);
    EnumInteractionResult.d SUCCESS_SERVER = new EnumInteractionResult.d(EnumInteractionResult.e.SERVER, EnumInteractionResult.b.DEFAULT);
    EnumInteractionResult.d CONSUME = new EnumInteractionResult.d(EnumInteractionResult.e.NONE, EnumInteractionResult.b.DEFAULT);
    EnumInteractionResult.a FAIL = new EnumInteractionResult.a();
    EnumInteractionResult.c PASS = new EnumInteractionResult.c();
    EnumInteractionResult.f TRY_WITH_EMPTY_HAND = new EnumInteractionResult.f();

    default boolean consumesAction() {
        return false;
    }

    public static record d(EnumInteractionResult.e swingSource, EnumInteractionResult.b itemContext) implements EnumInteractionResult {

        @Override
        public boolean consumesAction() {
            return true;
        }

        public EnumInteractionResult.d heldItemTransformedTo(ItemStack itemstack) {
            return new EnumInteractionResult.d(this.swingSource, new EnumInteractionResult.b(true, itemstack));
        }

        public EnumInteractionResult.d withoutItem() {
            return new EnumInteractionResult.d(this.swingSource, EnumInteractionResult.b.NONE);
        }

        public boolean wasItemInteraction() {
            return this.itemContext.wasItemInteraction;
        }

        @Nullable
        public ItemStack heldItemTransformedTo() {
            return this.itemContext.heldItemTransformedTo;
        }
    }

    public static enum e {

        NONE, CLIENT, SERVER;

        private e() {}
    }

    public static record b(boolean wasItemInteraction, @Nullable ItemStack heldItemTransformedTo) {

        static EnumInteractionResult.b NONE = new EnumInteractionResult.b(false, (ItemStack) null);
        static EnumInteractionResult.b DEFAULT = new EnumInteractionResult.b(true, (ItemStack) null);
    }

    public static record a() implements EnumInteractionResult {

    }

    public static record c() implements EnumInteractionResult {

    }

    public static record f() implements EnumInteractionResult {

    }
}
