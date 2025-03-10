package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.World;

public class RecipeArmorDye extends IRecipeComplex {

    public RecipeArmorDye(CraftingBookCategory craftingbookcategory) {
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
                    if (itemstack.is(TagsItem.DYEABLE)) {
                        if (flag) {
                            return false;
                        }

                        flag = true;
                    } else {
                        if (!(itemstack.getItem() instanceof ItemDye)) {
                            return false;
                        }

                        flag1 = true;
                    }
                }
            }

            return flag1 && flag;
        }
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        List<ItemDye> list = new ArrayList();
        ItemStack itemstack = ItemStack.EMPTY;

        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack1 = craftinginput.getItem(i);

            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(TagsItem.DYEABLE)) {
                    if (!itemstack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    itemstack = itemstack1.copy();
                } else {
                    Item item = itemstack1.getItem();

                    if (!(item instanceof ItemDye)) {
                        return ItemStack.EMPTY;
                    }

                    ItemDye itemdye = (ItemDye) item;

                    list.add(itemdye);
                }
            }
        }

        if (!itemstack.isEmpty() && !list.isEmpty()) {
            return DyedItemColor.applyDyes(itemstack, list);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public RecipeSerializer<RecipeArmorDye> getSerializer() {
        return RecipeSerializer.ARMOR_DYE;
    }
}
