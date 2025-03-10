package net.minecraft.world.item.crafting;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;

public class RecipeStonecutting extends RecipeSingleItem {

    public RecipeStonecutting(String s, RecipeItemStack recipeitemstack, ItemStack itemstack) {
        super(s, recipeitemstack, itemstack);
    }

    @Override
    public Recipes<RecipeStonecutting> getType() {
        return Recipes.STONECUTTING;
    }

    @Override
    public RecipeSerializer<RecipeStonecutting> getSerializer() {
        return RecipeSerializer.STONECUTTER;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new StonecutterRecipeDisplay(this.input().display(), this.resultDisplay(), new SlotDisplay.d(Items.STONECUTTER)));
    }

    public SlotDisplay resultDisplay() {
        return new SlotDisplay.f(this.result());
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.STONECUTTER;
    }
}
