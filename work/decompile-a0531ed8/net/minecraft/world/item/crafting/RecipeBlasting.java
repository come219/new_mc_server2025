package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RecipeBlasting extends RecipeCooking {

    public RecipeBlasting(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        super(s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }

    @Override
    protected Item furnaceIcon() {
        return Items.BLAST_FURNACE;
    }

    @Override
    public RecipeSerializer<RecipeBlasting> getSerializer() {
        return RecipeSerializer.BLASTING_RECIPE;
    }

    @Override
    public Recipes<RecipeBlasting> getType() {
        return Recipes.BLASTING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        RecipeBookCategory recipebookcategory;

        switch (this.category()) {
            case BLOCKS:
                recipebookcategory = RecipeBookCategories.BLAST_FURNACE_BLOCKS;
                break;
            case FOOD:
            case MISC:
                recipebookcategory = RecipeBookCategories.BLAST_FURNACE_MISC;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return recipebookcategory;
    }
}
