package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBanner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class RecipeBannerDuplicate extends IRecipeComplex {

    public RecipeBannerDuplicate(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        if (craftinginput.ingredientCount() != 2) {
            return false;
        } else {
            EnumColor enumcolor = null;
            boolean flag = false;
            boolean flag1 = false;

            for (int i = 0; i < craftinginput.size(); ++i) {
                ItemStack itemstack = craftinginput.getItem(i);

                if (!itemstack.isEmpty()) {
                    Item item = itemstack.getItem();

                    if (!(item instanceof ItemBanner)) {
                        return false;
                    }

                    ItemBanner itembanner = (ItemBanner) item;

                    if (enumcolor == null) {
                        enumcolor = itembanner.getColor();
                    } else if (enumcolor != itembanner.getColor()) {
                        return false;
                    }

                    int j = ((BannerPatternLayers) itemstack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)).layers().size();

                    if (j > 6) {
                        return false;
                    }

                    if (j > 0) {
                        if (flag1) {
                            return false;
                        }

                        flag1 = true;
                    } else {
                        if (flag) {
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
        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack = craftinginput.getItem(i);

            if (!itemstack.isEmpty()) {
                int j = ((BannerPatternLayers) itemstack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)).layers().size();

                if (j > 0 && j <= 6) {
                    return itemstack.copyWithCount(1);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftinginput) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(craftinginput.size(), ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = craftinginput.getItem(i);

            if (!itemstack.isEmpty()) {
                ItemStack itemstack1 = itemstack.getItem().getCraftingRemainder();

                if (!itemstack1.isEmpty()) {
                    nonnulllist.set(i, itemstack1);
                } else if (!((BannerPatternLayers) itemstack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)).layers().isEmpty()) {
                    nonnulllist.set(i, itemstack.copyWithCount(1));
                }
            }
        }

        return nonnulllist;
    }

    @Override
    public RecipeSerializer<RecipeBannerDuplicate> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }
}
