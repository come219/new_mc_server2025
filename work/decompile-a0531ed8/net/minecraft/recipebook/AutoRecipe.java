package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerRecipeBook;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class AutoRecipe<R extends IRecipe<?>> {

    private static final int ITEM_NOT_FOUND = -1;
    private final PlayerInventory inventory;
    private final AutoRecipe.a<R> menu;
    private final boolean useMaxItems;
    private final int gridWidth;
    private final int gridHeight;
    private final List<Slot> inputGridSlots;
    private final List<Slot> slotsToClear;

    public static <I extends RecipeInput, R extends IRecipe<I>> ContainerRecipeBook.a placeRecipe(AutoRecipe.a<R> autorecipe_a, int i, int j, List<Slot> list, List<Slot> list1, PlayerInventory playerinventory, RecipeHolder<R> recipeholder, boolean flag, boolean flag1) {
        AutoRecipe<R> autorecipe = new AutoRecipe<>(autorecipe_a, playerinventory, flag, i, j, list, list1);

        if (!flag1 && !autorecipe.testClearGrid()) {
            return ContainerRecipeBook.a.NOTHING;
        } else {
            StackedItemContents stackeditemcontents = new StackedItemContents();

            playerinventory.fillStackedContents(stackeditemcontents);
            autorecipe_a.fillCraftSlotsStackedContents(stackeditemcontents);
            return autorecipe.tryPlaceRecipe(recipeholder, stackeditemcontents);
        }
    }

    private AutoRecipe(AutoRecipe.a<R> autorecipe_a, PlayerInventory playerinventory, boolean flag, int i, int j, List<Slot> list, List<Slot> list1) {
        this.menu = autorecipe_a;
        this.inventory = playerinventory;
        this.useMaxItems = flag;
        this.gridWidth = i;
        this.gridHeight = j;
        this.inputGridSlots = list;
        this.slotsToClear = list1;
    }

    private ContainerRecipeBook.a tryPlaceRecipe(RecipeHolder<R> recipeholder, StackedItemContents stackeditemcontents) {
        if (stackeditemcontents.canCraft(recipeholder.value(), (AutoRecipeStackManager.b) null)) {
            this.placeRecipe(recipeholder, stackeditemcontents);
            this.inventory.setChanged();
            return ContainerRecipeBook.a.NOTHING;
        } else {
            this.clearGrid();
            this.inventory.setChanged();
            return ContainerRecipeBook.a.PLACE_GHOST_RECIPE;
        }
    }

    private void clearGrid() {
        Iterator iterator = this.slotsToClear.iterator();

        while (iterator.hasNext()) {
            Slot slot = (Slot) iterator.next();
            ItemStack itemstack = slot.getItem().copy();

            this.inventory.placeItemBackInInventory(itemstack, false);
            slot.set(itemstack);
        }

        this.menu.clearCraftingContent();
    }

    private void placeRecipe(RecipeHolder<R> recipeholder, StackedItemContents stackeditemcontents) {
        boolean flag = this.menu.recipeMatches(recipeholder);
        int i = stackeditemcontents.getBiggestCraftableStack(recipeholder.value(), (AutoRecipeStackManager.b) null);

        if (flag) {
            Iterator iterator = this.inputGridSlots.iterator();

            while (iterator.hasNext()) {
                Slot slot = (Slot) iterator.next();
                ItemStack itemstack = slot.getItem();

                if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                    return;
                }
            }
        }

        int j = this.calculateAmountToCraft(i, flag);
        List<Holder<Item>> list = new ArrayList();
        IRecipe irecipe = recipeholder.value();

        Objects.requireNonNull(list);
        if (stackeditemcontents.canCraft(irecipe, j, list::add)) {
            int k = clampToMaxStackSize(j, list);

            if (k != j) {
                list.clear();
                irecipe = recipeholder.value();
                Objects.requireNonNull(list);
                if (!stackeditemcontents.canCraft(irecipe, k, list::add)) {
                    return;
                }
            }

            this.clearGrid();
            PlaceRecipeHelper.placeRecipe(this.gridWidth, this.gridHeight, recipeholder.value(), recipeholder.value().placementInfo().slotsToIngredientIndex(), (integer, l, i1, j1) -> {
                if (integer != -1) {
                    Slot slot1 = (Slot) this.inputGridSlots.get(l);
                    Holder<Item> holder = (Holder) list.get(integer);
                    int k1 = k;

                    do {
                        if (k1 <= 0) {
                            return;
                        }

                        k1 = this.moveItemToGrid(slot1, holder, k1);
                    } while (k1 != -1);

                }
            });
        }
    }

    private static int clampToMaxStackSize(int i, List<Holder<Item>> list) {
        Holder holder;

        for (Iterator iterator = list.iterator(); iterator.hasNext(); i = Math.min(i, ((Item) holder.value()).getDefaultMaxStackSize())) {
            holder = (Holder) iterator.next();
        }

        return i;
    }

    private int calculateAmountToCraft(int i, boolean flag) {
        if (this.useMaxItems) {
            return i;
        } else if (flag) {
            int j = Integer.MAX_VALUE;
            Iterator iterator = this.inputGridSlots.iterator();

            while (iterator.hasNext()) {
                Slot slot = (Slot) iterator.next();
                ItemStack itemstack = slot.getItem();

                if (!itemstack.isEmpty() && j > itemstack.getCount()) {
                    j = itemstack.getCount();
                }
            }

            if (j != Integer.MAX_VALUE) {
                ++j;
            }

            return j;
        } else {
            return 1;
        }
    }

    private int moveItemToGrid(Slot slot, Holder<Item> holder, int i) {
        ItemStack itemstack = slot.getItem();
        int j = this.inventory.findSlotMatchingCraftingIngredient(holder, itemstack);

        if (j == -1) {
            return -1;
        } else {
            ItemStack itemstack1 = this.inventory.getItem(j);
            ItemStack itemstack2;

            if (i < itemstack1.getCount()) {
                itemstack2 = this.inventory.removeItem(j, i);
            } else {
                itemstack2 = this.inventory.removeItemNoUpdate(j);
            }

            int k = itemstack2.getCount();

            if (itemstack.isEmpty()) {
                slot.set(itemstack2);
            } else {
                itemstack.grow(k);
            }

            return i - k;
        }
    }

    private boolean testClearGrid() {
        List<ItemStack> list = Lists.newArrayList();
        int i = this.getAmountOfFreeSlotsInInventory();
        Iterator iterator = this.inputGridSlots.iterator();

        while (iterator.hasNext()) {
            Slot slot = (Slot) iterator.next();
            ItemStack itemstack = slot.getItem().copy();

            if (!itemstack.isEmpty()) {
                int j = this.inventory.getSlotWithRemainingSpace(itemstack);

                if (j == -1 && list.size() <= i) {
                    Iterator iterator1 = list.iterator();

                    while (true) {
                        if (iterator1.hasNext()) {
                            ItemStack itemstack1 = (ItemStack) iterator1.next();

                            if (!ItemStack.isSameItem(itemstack1, itemstack) || itemstack1.getCount() == itemstack1.getMaxStackSize() || itemstack1.getCount() + itemstack.getCount() > itemstack1.getMaxStackSize()) {
                                continue;
                            }

                            itemstack1.grow(itemstack.getCount());
                            itemstack.setCount(0);
                        }

                        if (!itemstack.isEmpty()) {
                            if (list.size() >= i) {
                                return false;
                            }

                            list.add(itemstack);
                        }
                        break;
                    }
                } else if (j == -1) {
                    return false;
                }
            }
        }

        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int i = 0;
        Iterator iterator = this.inventory.items.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (itemstack.isEmpty()) {
                ++i;
            }
        }

        return i;
    }

    public interface a<T extends IRecipe<?>> {

        void fillCraftSlotsStackedContents(StackedItemContents stackeditemcontents);

        void clearCraftingContent();

        boolean recipeMatches(RecipeHolder<T> recipeholder);
    }
}
