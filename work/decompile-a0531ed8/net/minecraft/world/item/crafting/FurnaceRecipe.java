package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceRecipe extends RecipeCooking {

    public FurnaceRecipe(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        super(s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }

    @Override
    protected Item furnaceIcon() {
        return Items.FURNACE;
    }

    @Override
    public RecipeSerializer<FurnaceRecipe> getSerializer() {
        return RecipeSerializer.SMELTING_RECIPE;
    }

    @Override
    public Recipes<FurnaceRecipe> getType() {
        return Recipes.SMELTING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        RecipeBookCategory recipebookcategory;

        switch (this.category()) {
            case BLOCKS:
                recipebookcategory = RecipeBookCategories.FURNACE_BLOCKS;
                break;
            case FOOD:
                recipebookcategory = RecipeBookCategories.FURNACE_FOOD;
                break;
            case MISC:
                recipebookcategory = RecipeBookCategories.FURNACE_MISC;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return recipebookcategory;
    }
}
