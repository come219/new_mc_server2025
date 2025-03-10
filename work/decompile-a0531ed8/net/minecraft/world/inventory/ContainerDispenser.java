package net.minecraft.world.inventory;

import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;

public class ContainerDispenser extends Container {

    private static final int SLOT_COUNT = 9;
    private static final int INV_SLOT_START = 9;
    private static final int INV_SLOT_END = 36;
    private static final int USE_ROW_SLOT_START = 36;
    private static final int USE_ROW_SLOT_END = 45;
    public final IInventory dispenser;

    public ContainerDispenser(int i, PlayerInventory playerinventory) {
        this(i, playerinventory, new InventorySubcontainer(9));
    }

    public ContainerDispenser(int i, PlayerInventory playerinventory, IInventory iinventory) {
        super(Containers.GENERIC_3x3, i);
        checkContainerSize(iinventory, 9);
        this.dispenser = iinventory;
        iinventory.startOpen(playerinventory.player);
        this.add3x3GridSlots(iinventory, 62, 17);
        this.addStandardInventorySlots(playerinventory, 8, 84);
    }

    protected void add3x3GridSlots(IInventory iinventory, int i, int j) {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 3; ++l) {
                int i1 = l + k * 3;

                this.addSlot(new Slot(iinventory, i1, i + l * 18, j + k * 18));
            }
        }

    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return this.dispenser.stillValid(entityhuman);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
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

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.dispenser.stopOpen(entityhuman);
    }
}
