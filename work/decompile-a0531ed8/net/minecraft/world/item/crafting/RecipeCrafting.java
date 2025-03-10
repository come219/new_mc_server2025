package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface RecipeCrafting extends IRecipe<CraftingInput> {

    @Override
    default Recipes<RecipeCrafting> getType() {
        return Recipes.CRAFTING;
    }

    @Override
    RecipeSerializer<? extends RecipeCrafting> getSerializer();

    CraftingBookCategory category();

    default NonNullList<ItemStack> getRemainingItems(CraftingInput craftinginput) {
        return defaultCraftingReminder(craftinginput);
    }

    static NonNullList<ItemStack> defaultCraftingReminder(CraftingInput craftinginput) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(craftinginput.size(), ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            Item item = craftinginput.getItem(i).getItem();

            nonnulllist.set(i, item.getCraftingRemainder());
        }

        return nonnulllist;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        RecipeBookCategory recipebookcategory;

        switch (this.category()) {
            case BUILDING:
                recipebookcategory = RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
                break;
            case EQUIPMENT:
                recipebookcategory = RecipeBookCategories.CRAFTING_EQUIPMENT;
                break;
            case REDSTONE:
                recipebookcategory = RecipeBookCategories.CRAFTING_REDSTONE;
                break;
            case MISC:
                recipebookcategory = RecipeBookCategories.CRAFTING_MISC;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return recipebookcategory;
    }
}
