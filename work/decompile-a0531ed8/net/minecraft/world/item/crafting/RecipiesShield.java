package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemBanner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class RecipiesShield extends IRecipeComplex {

    public RecipiesShield(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        if (craftinginput.ingredientCount() != 2) {
            return false;
        } else {
            boolean flag = false;
            boolean flag1 = false;

            for (int i = 0; i < craftinginput.size(); ++i) {
                ItemStack itemstack = craftinginput.getItem(i);

                if (!itemstack.isEmpty()) {
                    if (itemstack.getItem() instanceof ItemBanner) {
                        if (flag1) {
                            return false;
                        }

                        flag1 = true;
                    } else {
                        if (!itemstack.is(Items.SHIELD)) {
                            return false;
                        }

                        if (flag) {
                            return false;
                        }

                        BannerPatternLayers bannerpatternlayers = (BannerPatternLayers) itemstack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);

                        if (!bannerpatternlayers.layers().isEmpty()) {
                            return false;
                        }

                        flag = true;
                    }
                }
            }

            return flag && flag1;
        }
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack itemstack1 = ItemStack.EMPTY;

        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack2 = craftinginput.getItem(i);

            if (!itemstack2.isEmpty()) {
                if (itemstack2.getItem() instanceof ItemBanner) {
                    itemstack = itemstack2;
                } else if (itemstack2.is(Items.SHIELD)) {
                    itemstack1 = itemstack2.copy();
                }
            }
        }

        if (itemstack1.isEmpty()) {
            return itemstack1;
        } else {
            itemstack1.set(DataComponents.BANNER_PATTERNS, (BannerPatternLayers) itemstack.get(DataComponents.BANNER_PATTERNS));
            itemstack1.set(DataComponents.BASE_COLOR, ((ItemBanner) itemstack.getItem()).getColor());
            return itemstack1;
        }
    }

    @Override
    public RecipeSerializer<RecipiesShield> getSerializer() {
        return RecipeSerializer.SHIELD_DECORATION;
    }
}
