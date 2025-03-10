package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.AutoRecipe;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.RecipeCrafting;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class AbstractCraftingMenu extends ContainerRecipeBook {

    private final int width;
    private final int height;
    public final InventoryCrafting craftSlots;
    public final InventoryCraftResult resultSlots = new InventoryCraftResult();

    public AbstractCraftingMenu(Containers<?> containers, int i, int j, int k) {
        super(containers, i);
        this.width = j;
        this.height = k;
        this.craftSlots = new TransientCraftingContainer(this, j, k);
    }

    protected Slot addResultSlot(EntityHuman entityhuman, int i, int j) {
        return this.addSlot(new SlotResult(entityhuman, this.craftSlots, this.resultSlots, 0, i, j));
    }

    protected void addCraftingGridSlots(int i, int j) {
        for (int k = 0; k < this.width; ++k) {
            for (int l = 0; l < this.height; ++l) {
                this.addSlot(new Slot(this.craftSlots, l + k * this.width, i + l * 18, j + k * 18));
            }
        }

    }

    @Override
    public ContainerRecipeBook.a handlePlacement(boolean flag, boolean flag1, RecipeHolder<?> recipeholder, WorldServer worldserver, PlayerInventory playerinventory) {
        RecipeHolder<RecipeCrafting> recipeholder1 = recipeholder;

        this.beginPlacingRecipe();

        ContainerRecipeBook.a containerrecipebook_a;

        try {
            List<Slot> list = this.getInputGridSlots();

            containerrecipebook_a = AutoRecipe.placeRecipe(new AutoRecipe.a<RecipeCrafting>() {
                @Override
                public void fillCraftSlotsStackedContents(StackedItemContents stackeditemcontents) {
                    AbstractCraftingMenu.this.fillCraftSlotsStackedContents(stackeditemcontents);
                }

                @Override
                public void clearCraftingContent() {
                    AbstractCraftingMenu.this.resultSlots.clearContent();
                    AbstractCraftingMenu.this.craftSlots.clearContent();
                }

                @Override
                public boolean recipeMatches(RecipeHolder<RecipeCrafting> recipeholder2) {
                    return ((RecipeCrafting) recipeholder2.value()).matches(AbstractCraftingMenu.this.craftSlots.asCraftInput(), AbstractCraftingMenu.this.owner().level());
                }
            }, this.width, this.height, list, list, playerinventory, recipeholder1, flag, flag1);
        } finally {
            this.finishPlacingRecipe(worldserver, recipeholder);
        }

        return containerrecipebook_a;
    }

    protected void beginPlacingRecipe() {}

    protected void finishPlacingRecipe(WorldServer worldserver, RecipeHolder<RecipeCrafting> recipeholder) {}

    public abstract Slot getResultSlot();

    public abstract List<Slot> getInputGridSlots();

    public int getGridWidth() {
        return this.width;
    }

    public int getGridHeight() {
        return this.height;
    }

    protected abstract EntityHuman owner();

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents stackeditemcontents) {
        this.craftSlots.fillStackedContents(stackeditemcontents);
    }
}
