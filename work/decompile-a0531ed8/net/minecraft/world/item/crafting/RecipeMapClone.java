package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;

public class RecipeMapClone extends IRecipeComplex {

    public RecipeMapClone(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        if (craftinginput.ingredientCount() < 2) {
            return false;
        } else {
            boolean flag = false;
            boolean flag1 = false;

            for (int i = 0; i < craftinginput.size(); ++i) {
                ItemStack itemstack = craftinginput.getItem(i);

                if (!itemstack.isEmpty()) {
                    if (itemstack.has(DataComponents.MAP_ID)) {
                        if (flag1) {
                            return false;
                        }

                        flag1 = true;
                    } else {
                        if (!itemstack.is(Items.MAP)) {
                            return false;
                        }

                        flag = true;
                    }
                }
            }

            return flag1 && flag;
        }
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        int i = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for (int j = 0; j < craftinginput.size(); ++j) {
            ItemStack itemstack1 = craftinginput.getItem(j);

            if (!itemstack1.isEmpty()) {
                if (itemstack1.has(DataComponents.MAP_ID)) {
                    if (!itemstack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    itemstack = itemstack1;
                } else {
                    if (!itemstack1.is(Items.MAP)) {
                        return ItemStack.EMPTY;
                    }

                    ++i;
                }
            }
        }

        if (!itemstack.isEmpty() && i >= 1) {
            return itemstack.copyWithCount(i + 1);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public RecipeSerializer<RecipeMapClone> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}
