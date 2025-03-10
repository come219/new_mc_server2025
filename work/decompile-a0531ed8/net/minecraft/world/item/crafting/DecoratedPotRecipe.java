package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

public class DecoratedPotRecipe extends IRecipeComplex {

    public DecoratedPotRecipe(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    private static ItemStack back(CraftingInput craftinginput) {
        return craftinginput.getItem(1, 0);
    }

    private static ItemStack left(CraftingInput craftinginput) {
        return craftinginput.getItem(0, 1);
    }

    private static ItemStack right(CraftingInput craftinginput) {
        return craftinginput.getItem(2, 1);
    }

    private static ItemStack front(CraftingInput craftinginput) {
        return craftinginput.getItem(1, 2);
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        return craftinginput.width() == 3 && craftinginput.height() == 3 && craftinginput.ingredientCount() == 4 ? back(craftinginput).is(TagsItem.DECORATED_POT_INGREDIENTS) && left(craftinginput).is(TagsItem.DECORATED_POT_INGREDIENTS) && right(craftinginput).is(TagsItem.DECORATED_POT_INGREDIENTS) && front(craftinginput).is(TagsItem.DECORATED_POT_INGREDIENTS) : false;
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        PotDecorations potdecorations = new PotDecorations(back(craftinginput).getItem(), left(craftinginput).getItem(), right(craftinginput).getItem(), front(craftinginput).getItem());

        return DecoratedPotBlockEntity.createDecoratedPotItem(potdecorations);
    }

    @Override
    public RecipeSerializer<DecoratedPotRecipe> getSerializer() {
        return RecipeSerializer.DECORATED_POT_RECIPE;
    }
}
