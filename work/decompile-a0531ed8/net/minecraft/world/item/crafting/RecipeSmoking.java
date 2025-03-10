package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RecipeSmoking extends RecipeCooking {

    public RecipeSmoking(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        super(s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }

    @Override
    protected Item furnaceIcon() {
        return Items.SMOKER;
    }

    @Override
    public Recipes<RecipeSmoking> getType() {
        return Recipes.SMOKING;
    }

    @Override
    public RecipeSerializer<RecipeSmoking> getSerializer() {
        return RecipeSerializer.SMOKING_RECIPE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.SMOKER_FOOD;
    }
}
