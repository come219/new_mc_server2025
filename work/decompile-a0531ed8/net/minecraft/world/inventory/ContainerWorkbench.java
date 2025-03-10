package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeCrafting;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.level.block.Blocks;

public class ContainerWorkbench extends AbstractCraftingMenu {

    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    public static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_COUNT = 9;
    private static final int CRAFT_SLOT_END = 10;
    private static final int INV_SLOT_START = 10;
    private static final int INV_SLOT_END = 37;
    private static final int USE_ROW_SLOT_START = 37;
    private static final int USE_ROW_SLOT_END = 46;
    public final ContainerAccess access;
    private final EntityHuman player;
    private boolean placingRecipe;

    public ContainerWorkbench(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, ContainerAccess.NULL);
    }

    public ContainerWorkbench(int i, PlayerInventory playerinventory, ContainerAccess containeraccess) {
        super(Containers.CRAFTING, i, 3, 3);
        this.access = containeraccess;
        this.player = playerinventory.player;
        this.addResultSlot(this.player, 124, 35);
        this.addCraftingGridSlots(30, 17);
        this.addStandardInventorySlots(playerinventory, 8, 84);
    }

    protected static void slotChangedCraftingGrid(Container container, WorldServer worldserver, EntityHuman entityhuman, InventoryCrafting inventorycrafting, InventoryCraftResult inventorycraftresult, @Nullable RecipeHolder<RecipeCrafting> recipeholder) {
        CraftingInput craftinginput = inventorycrafting.asCraftInput();
        EntityPlayer entityplayer = (EntityPlayer) entityhuman;
        ItemStack itemstack = ItemStack.EMPTY;
        Optional<RecipeHolder<RecipeCrafting>> optional = worldserver.getServer().getRecipeManager().getRecipeFor(Recipes.CRAFTING, craftinginput, worldserver, recipeholder);

        if (optional.isPresent()) {
            RecipeHolder<RecipeCrafting> recipeholder1 = (RecipeHolder) optional.get();
            RecipeCrafting recipecrafting = (RecipeCrafting) recipeholder1.value();

            if (inventorycraftresult.setRecipeUsed(entityplayer, recipeholder1)) {
                ItemStack itemstack1 = recipecrafting.assemble(craftinginput, worldserver.registryAccess());

                if (itemstack1.isItemEnabled(worldserver.enabledFeatures())) {
                    itemstack = itemstack1;
                }
            }
        }

        inventorycraftresult.setItem(0, itemstack);
        container.setRemoteSlot(0, itemstack);
        entityplayer.connection.send(new PacketPlayOutSetSlot(container.containerId, container.incrementStateId(), 0, itemstack));
    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        if (!this.placingRecipe) {
            this.access.execute((world, blockposition) -> {
                if (world instanceof WorldServer worldserver) {
                    slotChangedCraftingGrid(this, worldserver, this.player, this.craftSlots, this.resultSlots, (RecipeHolder) null);
                }

            });
        }

    }

    @Override
    public void beginPlacingRecipe() {
        this.placingRecipe = true;
    }

    @Override
    public void finishPlacingRecipe(WorldServer worldserver, RecipeHolder<RecipeCrafting> recipeholder) {
        this.placingRecipe = false;
        slotChangedCraftingGrid(this, worldserver, this.player, this.craftSlots, this.resultSlots, recipeholder);
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.access.execute((world, blockposition) -> {
            this.clearContainer(entityhuman, this.craftSlots);
        });
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return stillValid(this.access, entityhuman, Blocks.CRAFTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i == 0) {
                this.access.execute((world, blockposition) -> {
                    itemstack1.getItem().onCraftedBy(itemstack1, world, entityhuman);
                });
                if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (i >= 10 && i < 46) {
                if (!this.moveItemStackTo(itemstack1, 1, 10, false)) {
                    if (i < 37) {
                        if (!this.moveItemStackTo(itemstack1, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
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
            if (i == 0) {
                entityhuman.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemstack, slot);
    }

    @Override
    public Slot getResultSlot() {
        return (Slot) this.slots.get(0);
    }

    @Override
    public List<Slot> getInputGridSlots() {
        return this.slots.subList(1, 10);
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected EntityHuman owner() {
        return this.player;
    }
}
