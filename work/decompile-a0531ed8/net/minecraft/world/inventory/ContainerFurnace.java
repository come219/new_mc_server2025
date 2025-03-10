package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.AutoRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeCooking;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.World;

public abstract class ContainerFurnace extends ContainerRecipeBook {

    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int DATA_COUNT = 4;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    final IInventory container;
    private final IContainerProperties data;
    protected final World level;
    private final Recipes<? extends RecipeCooking> recipeType;
    private final RecipePropertySet acceptedInputs;
    private final RecipeBookType recipeBookType;

    protected ContainerFurnace(Containers<?> containers, Recipes<? extends RecipeCooking> recipes, ResourceKey<RecipePropertySet> resourcekey, RecipeBookType recipebooktype, int i, PlayerInventory playerinventory) {
        this(containers, recipes, resourcekey, recipebooktype, i, playerinventory, new InventorySubcontainer(3), new ContainerProperties(4));
    }

    protected ContainerFurnace(Containers<?> containers, Recipes<? extends RecipeCooking> recipes, ResourceKey<RecipePropertySet> resourcekey, RecipeBookType recipebooktype, int i, PlayerInventory playerinventory, IInventory iinventory, IContainerProperties icontainerproperties) {
        super(containers, i);
        this.recipeType = recipes;
        this.recipeBookType = recipebooktype;
        checkContainerSize(iinventory, 3);
        checkContainerDataCount(icontainerproperties, 4);
        this.container = iinventory;
        this.data = icontainerproperties;
        this.level = playerinventory.player.level();
        this.acceptedInputs = this.level.recipeAccess().propertySet(resourcekey);
        this.addSlot(new Slot(iinventory, 0, 56, 17));
        this.addSlot(new SlotFurnaceFuel(this, iinventory, 1, 56, 53));
        this.addSlot(new SlotFurnaceResult(playerinventory.player, iinventory, 2, 116, 35));
        this.addStandardInventorySlots(playerinventory, 8, 84);
        this.addDataSlots(icontainerproperties);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents stackeditemcontents) {
        if (this.container instanceof AutoRecipeOutput) {
            ((AutoRecipeOutput) this.container).fillStackedContents(stackeditemcontents);
        }

    }

    public Slot getResultSlot() {
        return (Slot) this.slots.get(2);
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return this.container.stillValid(entityhuman);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (i != 1 && i != 0) {
                if (this.canSmelt(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isFuel(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= 3 && i < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(entityhuman, itemstack1);
        }

        return itemstack;
    }

    protected boolean canSmelt(ItemStack itemstack) {
        return this.acceptedInputs.test(itemstack);
    }

    protected boolean isFuel(ItemStack itemstack) {
        return this.level.fuelValues().isFuel(itemstack);
    }

    public float getBurnProgress() {
        int i = this.data.get(2);
        int j = this.data.get(3);

        return j != 0 && i != 0 ? MathHelper.clamp((float) i / (float) j, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int i = this.data.get(1);

        if (i == 0) {
            i = 200;
        }

        return MathHelper.clamp((float) this.data.get(0) / (float) i, 0.0F, 1.0F);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    @Override
    public ContainerRecipeBook.a handlePlacement(boolean flag, boolean flag1, RecipeHolder<?> recipeholder, final WorldServer worldserver, PlayerInventory playerinventory) {
        final List<Slot> list = List.of(this.getSlot(0), this.getSlot(2));

        return AutoRecipe.placeRecipe(new AutoRecipe.a<RecipeCooking>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents stackeditemcontents) {
                ContainerFurnace.this.fillCraftSlotsStackedContents(stackeditemcontents);
            }

            @Override
            public void clearCraftingContent() {
                list.forEach((slot) -> {
                    slot.set(ItemStack.EMPTY);
                });
            }

            @Override
            public boolean recipeMatches(RecipeHolder<RecipeCooking> recipeholder1) {
                return ((RecipeCooking) recipeholder1.value()).matches(new SingleRecipeInput(ContainerFurnace.this.container.getItem(0)), worldserver);
            }
        }, 1, 1, List.of(this.getSlot(0)), list, playerinventory, recipeholder, flag, flag1);
    }
}
