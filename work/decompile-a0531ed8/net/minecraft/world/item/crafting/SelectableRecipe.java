package net.minecraft.world.item.crafting;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record SelectableRecipe<T extends IRecipe<?>>(SlotDisplay optionDisplay, Optional<RecipeHolder<T>> recipe) {

    public static <T extends IRecipe<?>> StreamCodec<RegistryFriendlyByteBuf, SelectableRecipe<T>> noRecipeCodec() {
        return StreamCodec.composite(SlotDisplay.STREAM_CODEC, SelectableRecipe::optionDisplay, (slotdisplay) -> {
            return new SelectableRecipe<>(slotdisplay, Optional.empty());
        });
    }

    public static record b<T extends IRecipe<?>>(List<SelectableRecipe.a<T>> entries) {

        public static <T extends IRecipe<?>> SelectableRecipe.b<T> empty() {
            return new SelectableRecipe.b<>(List.of());
        }

        public static <T extends IRecipe<?>> StreamCodec<RegistryFriendlyByteBuf, SelectableRecipe.b<T>> noRecipeCodec() {
            return StreamCodec.composite(SelectableRecipe.a.noRecipeCodec().apply(ByteBufCodecs.list()), SelectableRecipe.b::entries, SelectableRecipe.b::new);
        }

        public boolean acceptsInput(ItemStack itemstack) {
            return this.entries.stream().anyMatch((selectablerecipe_a) -> {
                return selectablerecipe_a.input.test(itemstack);
            });
        }

        public SelectableRecipe.b<T> selectByInput(ItemStack itemstack) {
            return new SelectableRecipe.b<>(this.entries.stream().filter((selectablerecipe_a) -> {
                return selectablerecipe_a.input.test(itemstack);
            }).toList());
        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public int size() {
            return this.entries.size();
        }
    }

    public static record a<T extends IRecipe<?>>(RecipeItemStack input, SelectableRecipe<T> recipe) {

        public static <T extends IRecipe<?>> StreamCodec<RegistryFriendlyByteBuf, SelectableRecipe.a<T>> noRecipeCodec() {
            return StreamCodec.composite(RecipeItemStack.CONTENTS_STREAM_CODEC, SelectableRecipe.a::input, SelectableRecipe.noRecipeCodec(), SelectableRecipe.a::recipe, SelectableRecipe.a::new);
        }
    }
}
