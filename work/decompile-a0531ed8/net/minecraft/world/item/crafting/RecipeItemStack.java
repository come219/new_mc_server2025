package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.IMaterial;

public final class RecipeItemStack implements AutoRecipeStackManager.a<Holder<Item>>, Predicate<ItemStack> {

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeItemStack> CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.ITEM).map(RecipeItemStack::new, (recipeitemstack) -> {
        return recipeitemstack.values;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<RecipeItemStack>> OPTIONAL_CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.ITEM).map((holderset) -> {
        return holderset.size() == 0 ? Optional.empty() : Optional.of(new RecipeItemStack(holderset));
    }, (optional) -> {
        return (HolderSet) optional.map((recipeitemstack) -> {
            return recipeitemstack.values;
        }).orElse(HolderSet.direct());
    });
    public static final Codec<HolderSet<Item>> NON_AIR_HOLDER_SET_CODEC = HolderSetCodec.create(Registries.ITEM, Item.CODEC, false);
    public static final Codec<RecipeItemStack> CODEC = ExtraCodecs.nonEmptyHolderSet(RecipeItemStack.NON_AIR_HOLDER_SET_CODEC).xmap(RecipeItemStack::new, (recipeitemstack) -> {
        return recipeitemstack.values;
    });
    private final HolderSet<Item> values;

    private RecipeItemStack(HolderSet<Item> holderset) {
        holderset.unwrap().ifRight((list) -> {
            if (list.isEmpty()) {
                throw new UnsupportedOperationException("Ingredients can't be empty");
            } else if (list.contains(Items.AIR.builtInRegistryHolder())) {
                throw new UnsupportedOperationException("Ingredient can't contain air");
            }
        });
        this.values = holderset;
    }

    public static boolean testOptionalIngredient(Optional<RecipeItemStack> optional, ItemStack itemstack) {
        Optional optional1 = optional.map((recipeitemstack) -> {
            return recipeitemstack.test(itemstack);
        });

        Objects.requireNonNull(itemstack);
        return (Boolean) optional1.orElseGet(itemstack::isEmpty);
    }

    /** @deprecated */
    @Deprecated
    public Stream<Holder<Item>> items() {
        return this.values.stream();
    }

    public boolean isEmpty() {
        return this.values.size() == 0;
    }

    public boolean test(ItemStack itemstack) {
        return itemstack.is(this.values);
    }

    public boolean acceptsItem(Holder<Item> holder) {
        return this.values.contains(holder);
    }

    public boolean equals(Object object) {
        if (object instanceof RecipeItemStack recipeitemstack) {
            return Objects.equals(this.values, recipeitemstack.values);
        } else {
            return false;
        }
    }

    public static RecipeItemStack of(IMaterial imaterial) {
        return new RecipeItemStack(HolderSet.direct(imaterial.asItem().builtInRegistryHolder()));
    }

    public static RecipeItemStack of(IMaterial... aimaterial) {
        return of(Arrays.stream(aimaterial));
    }

    public static RecipeItemStack of(Stream<? extends IMaterial> stream) {
        return new RecipeItemStack(HolderSet.direct(stream.map((imaterial) -> {
            return imaterial.asItem().builtInRegistryHolder();
        }).toList()));
    }

    public static RecipeItemStack of(HolderSet<Item> holderset) {
        return new RecipeItemStack(holderset);
    }

    public SlotDisplay display() {
        return (SlotDisplay) this.values.unwrap().map(SlotDisplay.h::new, (list) -> {
            return new SlotDisplay.b(list.stream().map(RecipeItemStack::displayForSingleItem).toList());
        });
    }

    public static SlotDisplay optionalIngredientToDisplay(Optional<RecipeItemStack> optional) {
        return (SlotDisplay) optional.map(RecipeItemStack::display).orElse(SlotDisplay.c.INSTANCE);
    }

    private static SlotDisplay displayForSingleItem(Holder<Item> holder) {
        SlotDisplay.d slotdisplay_d = new SlotDisplay.d(holder);
        ItemStack itemstack = ((Item) holder.value()).getCraftingRemainder();

        if (!itemstack.isEmpty()) {
            SlotDisplay.f slotdisplay_f = new SlotDisplay.f(itemstack);

            return new SlotDisplay.j(slotdisplay_d, slotdisplay_f);
        } else {
            return slotdisplay_d;
        }
    }
}
