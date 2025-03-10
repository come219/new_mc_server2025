package net.minecraft.world.entity.player;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;

public class StackedItemContents {

    private final AutoRecipeStackManager<Holder<Item>> raw = new AutoRecipeStackManager<>();

    public StackedItemContents() {}

    public void accountSimpleStack(ItemStack itemstack) {
        if (PlayerInventory.isUsableForCrafting(itemstack)) {
            this.accountStack(itemstack);
        }

    }

    public void accountStack(ItemStack itemstack) {
        this.accountStack(itemstack, itemstack.getMaxStackSize());
    }

    public void accountStack(ItemStack itemstack, int i) {
        if (!itemstack.isEmpty()) {
            int j = Math.min(i, itemstack.getCount());

            this.raw.account(itemstack.getItemHolder(), j);
        }

    }

    public boolean canCraft(IRecipe<?> irecipe, @Nullable AutoRecipeStackManager.b<Holder<Item>> autorecipestackmanager_b) {
        return this.canCraft(irecipe, 1, autorecipestackmanager_b);
    }

    public boolean canCraft(IRecipe<?> irecipe, int i, @Nullable AutoRecipeStackManager.b<Holder<Item>> autorecipestackmanager_b) {
        PlacementInfo placementinfo = irecipe.placementInfo();

        return placementinfo.isImpossibleToPlace() ? false : this.canCraft(placementinfo.ingredients(), i, autorecipestackmanager_b);
    }

    public boolean canCraft(List<? extends AutoRecipeStackManager.a<Holder<Item>>> list, @Nullable AutoRecipeStackManager.b<Holder<Item>> autorecipestackmanager_b) {
        return this.canCraft(list, 1, autorecipestackmanager_b);
    }

    private boolean canCraft(List<? extends AutoRecipeStackManager.a<Holder<Item>>> list, int i, @Nullable AutoRecipeStackManager.b<Holder<Item>> autorecipestackmanager_b) {
        return this.raw.tryPick(list, i, autorecipestackmanager_b);
    }

    public int getBiggestCraftableStack(IRecipe<?> irecipe, @Nullable AutoRecipeStackManager.b<Holder<Item>> autorecipestackmanager_b) {
        return this.getBiggestCraftableStack(irecipe, Integer.MAX_VALUE, autorecipestackmanager_b);
    }

    public int getBiggestCraftableStack(IRecipe<?> irecipe, int i, @Nullable AutoRecipeStackManager.b<Holder<Item>> autorecipestackmanager_b) {
        return this.raw.tryPickAll(irecipe.placementInfo().ingredients(), i, autorecipestackmanager_b);
    }

    public void clear() {
        this.raw.clear();
    }
}
