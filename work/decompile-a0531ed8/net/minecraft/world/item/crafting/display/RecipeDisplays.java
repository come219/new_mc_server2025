package net.minecraft.world.item.crafting.display;

import net.minecraft.core.IRegistry;

public class RecipeDisplays {

    public RecipeDisplays() {}

    public static RecipeDisplay.a<?> bootstrap(IRegistry<RecipeDisplay.a<?>> iregistry) {
        IRegistry.register(iregistry, "crafting_shapeless", ShapelessCraftingRecipeDisplay.TYPE);
        IRegistry.register(iregistry, "crafting_shaped", ShapedCraftingRecipeDisplay.TYPE);
        IRegistry.register(iregistry, "furnace", FurnaceRecipeDisplay.TYPE);
        IRegistry.register(iregistry, "stonecutter", StonecutterRecipeDisplay.TYPE);
        return (RecipeDisplay.a) IRegistry.register(iregistry, "smithing", SmithingRecipeDisplay.TYPE);
    }
}
