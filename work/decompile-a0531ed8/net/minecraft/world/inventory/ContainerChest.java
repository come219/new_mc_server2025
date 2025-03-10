package net.minecraft.world.inventory;

import net.minecraft.world.IInventory;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;

public class ContainerChest extends Container {

    private final IInventory container;
    private final int containerRows;

    private ContainerChest(Containers<?> containers, int i, PlayerInventory playerinventory, int j) {
        this(containers, i, playerinventory, new InventorySubcontainer(9 * j), j);
    }

    public static ContainerChest oneRow(int i, PlayerInventory playerinventory) {
        return new ContainerChest(Containers.GENERIC_9x1, i, playerinventory, 1);
    }

    public static ContainerChest twoRows(int i, PlayerInventory playerinventory) {
        return new ContainerChest(Containers.GENERIC_9x2, i, playerinventory, 2);
    }

    public static ContainerChest threeRows(int i, PlayerInventory playerinventory) {
        return new ContainerChest(Containers.GENERIC_9x3, i, playerinventory, 3);
    }

    public static ContainerChest fourRows(int i, PlayerInventory playerinventory) {
        return new ContainerChest(Containers.GENERIC_9x4, i, playerinventory, 4);
    }

    public static ContainerChest fiveRows(int i, PlayerInventory playerinventory) {
        return new ContainerChest(Containers.GENERIC_9x5, i, playerinventory, 5);
    }

    public static ContainerChest sixRows(int i, PlayerInventory playerinventory) {
        return new ContainerChest(Containers.GENERIC_9x6, i, playerinventory, 6);
    }

    public static ContainerChest threeRows(int i, PlayerInventory playerinventory, IInventory iinventory) {
        return new ContainerChest(Containers.GENERIC_9x3, i, playerinventory, iinventory, 3);
    }

    public static ContainerChest sixRows(int i, PlayerInventory playerinventory, IInventory iinventory) {
        return new ContainerChest(Containers.GENERIC_9x6, i, playerinventory, iinventory, 6);
    }

    public ContainerChest(Containers<?> containers, int i, PlayerInventory playerinventory, IInventory iinventory, int j) {
        super(containers, i);
        checkContainerSize(iinventory, j * 9);
        this.container = iinventory;
        this.containerRows = j;
        iinventory.startOpen(playerinventory.player);
        boolean flag = true;

        this.addChestGrid(iinventory, 8, 18);
        int k = 18 + this.containerRows * 18 + 13;

        this.addStandardInventorySlots(playerinventory, 8, k);
    }

    private void addChestGrid(IInventory iinventory, int i, int j) {
        for (int k = 0; k < this.containerRows; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(iinventory, l + k * 9, i + l * 18, j + k * 18));
            }
        }

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
            if (i < this.containerRows * 9) {
                if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.container.stopOpen(entityhuman);
    }

    public IInventory getContainer() {
        return this.container;
    }

    public int getRowCount() {
        return this.containerRows;
    }
}
