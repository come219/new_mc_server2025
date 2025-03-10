package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeStonecutting;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;

public class ContainerStonecutter extends Container {

    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;
    private static final int INV_SLOT_START = 2;
    private static final int INV_SLOT_END = 29;
    private static final int USE_ROW_SLOT_START = 29;
    private static final int USE_ROW_SLOT_END = 38;
    private final ContainerAccess access;
    final ContainerProperty selectedRecipeIndex;
    private final World level;
    private SelectableRecipe.b<RecipeStonecutting> recipesForInput;
    private ItemStack input;
    long lastSoundTime;
    final Slot inputSlot;
    final Slot resultSlot;
    Runnable slotUpdateListener;
    public final IInventory container;
    final InventoryCraftResult resultContainer;

    public ContainerStonecutter(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerStonecutter(int i, PlayerInventory playerinventory, final ContainerAccess containeraccess) {
        super(Containers.STONECUTTER, i);
        this.selectedRecipeIndex = ContainerProperty.standalone();
        this.recipesForInput = SelectableRecipe.b.empty();
        this.input = ItemStack.EMPTY;
        this.slotUpdateListener = () -> {
        };
        this.container = new InventorySubcontainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                ContainerStonecutter.this.slotsChanged(this);
                ContainerStonecutter.this.slotUpdateListener.run();
            }
        };
        this.resultContainer = new InventoryCraftResult();
        this.access = containeraccess;
        this.level = playerinventory.player.level();
        this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return false;
            }

            @Override
            public void onTake(EntityHuman entityhuman, ItemStack itemstack) {
                itemstack.onCraftedBy(entityhuman.level(), entityhuman, itemstack.getCount());
                ContainerStonecutter.this.resultContainer.awardUsedRecipes(entityhuman, this.getRelevantItems());
                ItemStack itemstack1 = ContainerStonecutter.this.inputSlot.remove(1);

                if (!itemstack1.isEmpty()) {
                    ContainerStonecutter.this.setupResultSlot(ContainerStonecutter.this.selectedRecipeIndex.get());
                }

                containeraccess.execute((world, blockposition) -> {
                    long j = world.getGameTime();

                    if (ContainerStonecutter.this.lastSoundTime != j) {
                        world.playSound((EntityHuman) null, blockposition, SoundEffects.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        ContainerStonecutter.this.lastSoundTime = j;
                    }

                });
                super.onTake(entityhuman, itemstack);
            }

            private List<ItemStack> getRelevantItems() {
                return List.of(ContainerStonecutter.this.inputSlot.getItem());
            }
        });
        this.addStandardInventorySlots(playerinventory, 8, 84);
        this.addDataSlot(this.selectedRecipeIndex);
    }

    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex.get();
    }

    public SelectableRecipe.b<RecipeStonecutting> getVisibleRecipes() {
        return this.recipesForInput;
    }

    public int getNumberOfVisibleRecipes() {
        return this.recipesForInput.size();
    }

    public boolean hasInputItem() {
        return this.inputSlot.hasItem() && !this.recipesForInput.isEmpty();
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return stillValid(this.access, entityhuman, Blocks.STONECUTTER);
    }

    @Override
    public boolean clickMenuButton(EntityHuman entityhuman, int i) {
        if (this.selectedRecipeIndex.get() == i) {
            return false;
        } else {
            if (this.isValidRecipeIndex(i)) {
                this.selectedRecipeIndex.set(i);
                this.setupResultSlot(i);
            }

            return true;
        }
    }

    private boolean isValidRecipeIndex(int i) {
        return i >= 0 && i < this.recipesForInput.size();
    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        ItemStack itemstack = this.inputSlot.getItem();

        if (!itemstack.is(this.input.getItem())) {
            this.input = itemstack.copy();
            this.setupRecipeList(itemstack);
        }

    }

    private void setupRecipeList(ItemStack itemstack) {
        this.selectedRecipeIndex.set(-1);
        this.resultSlot.set(ItemStack.EMPTY);
        if (!itemstack.isEmpty()) {
            this.recipesForInput = this.level.recipeAccess().stonecutterRecipes().selectByInput(itemstack);
        } else {
            this.recipesForInput = SelectableRecipe.b.empty();
        }

    }

    void setupResultSlot(int i) {
        Optional optional;

        if (!this.recipesForInput.isEmpty() && this.isValidRecipeIndex(i)) {
            SelectableRecipe.a<RecipeStonecutting> selectablerecipe_a = (SelectableRecipe.a) this.recipesForInput.entries().get(i);

            optional = selectablerecipe_a.recipe().recipe();
        } else {
            optional = Optional.empty();
        }

        optional.ifPresentOrElse((recipeholder) -> {
            this.resultContainer.setRecipeUsed(recipeholder);
            this.resultSlot.set(((RecipeStonecutting) recipeholder.value()).assemble(new SingleRecipeInput(this.container.getItem(0)), this.level.registryAccess()));
        }, () -> {
            this.resultSlot.set(ItemStack.EMPTY);
            this.resultContainer.setRecipeUsed((RecipeHolder) null);
        });
        this.broadcastChanges();
    }

    @Override
    public Containers<?> getType() {
        return Containers.STONECUTTER;
    }

    public void registerUpdateListener(Runnable runnable) {
        this.slotUpdateListener = runnable;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
        return slot.container != this.resultContainer && super.canTakeItemForPickAll(itemstack, slot);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            Item item = itemstack1.getItem();

            itemstack = itemstack1.copy();
            if (i == 1) {
                item.onCraftedBy(itemstack1, entityhuman.level(), entityhuman);
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (i == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.level.recipeAccess().stonecutterRecipes().acceptsInput(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 2 && i < 29) {
                if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 29 && i < 38 && !this.moveItemStackTo(itemstack1, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(entityhuman, itemstack1);
            if (i == 1) {
                entityhuman.drop(itemstack1, false);
            }

            this.broadcastChanges();
        }

        return itemstack;
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.resultContainer.removeItemNoUpdate(1);
        this.access.execute((world, blockposition) -> {
            this.clearContainer(entityhuman, this.container);
        });
    }
}
