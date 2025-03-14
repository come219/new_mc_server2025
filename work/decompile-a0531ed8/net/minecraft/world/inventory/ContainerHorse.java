package net.minecraft.world.inventory;

import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.animal.horse.EntityHorseAbstract;
import net.minecraft.world.entity.animal.horse.EntityLlama;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ContainerHorse extends Container {

    static final MinecraftKey SADDLE_SLOT_SPRITE = MinecraftKey.withDefaultNamespace("container/slot/saddle");
    private static final MinecraftKey LLAMA_ARMOR_SLOT_SPRITE = MinecraftKey.withDefaultNamespace("container/slot/llama_armor");
    private static final MinecraftKey ARMOR_SLOT_SPRITE = MinecraftKey.withDefaultNamespace("container/slot/horse_armor");
    private final IInventory horseContainer;
    private final IInventory armorContainer;
    private final EntityHorseAbstract horse;
    private static final int SLOT_BODY_ARMOR = 1;
    private static final int SLOT_HORSE_INVENTORY_START = 2;

    public ContainerHorse(int i, PlayerInventory playerinventory, IInventory iinventory, final EntityHorseAbstract entityhorseabstract, int j) {
        super((Containers) null, i);
        this.horseContainer = iinventory;
        this.armorContainer = entityhorseabstract.getBodyArmorAccess();
        this.horse = entityhorseabstract;
        iinventory.startOpen(playerinventory.player);
        this.addSlot(new Slot(this, iinventory, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.is(Items.SADDLE) && !this.hasItem() && entityhorseabstract.isSaddleable();
            }

            @Override
            public boolean isActive() {
                return entityhorseabstract.isSaddleable();
            }

            @Override
            public MinecraftKey getNoItemIcon() {
                return ContainerHorse.SADDLE_SLOT_SPRITE;
            }
        });
        MinecraftKey minecraftkey = entityhorseabstract instanceof EntityLlama ? ContainerHorse.LLAMA_ARMOR_SLOT_SPRITE : ContainerHorse.ARMOR_SLOT_SPRITE;

        this.addSlot(new ArmorSlot(this, this.armorContainer, entityhorseabstract, EnumItemSlot.BODY, 0, 8, 36, minecraftkey) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return entityhorseabstract.isEquippableInSlot(itemstack, EnumItemSlot.BODY);
            }

            @Override
            public boolean isActive() {
                return entityhorseabstract.canUseSlot(EnumItemSlot.BODY);
            }
        });
        if (j > 0) {
            for (int k = 0; k < 3; ++k) {
                for (int l = 0; l < j; ++l) {
                    this.addSlot(new Slot(iinventory, 1 + l + k * j, 80 + l * 18, 18 + k * 18));
                }
            }
        }

        this.addStandardInventorySlots(playerinventory, 8, 84);
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return !this.horse.hasInventoryChanged(this.horseContainer) && this.horseContainer.stillValid(entityhuman) && this.armorContainer.stillValid(entityhuman) && this.horse.isAlive() && entityhuman.canInteractWithEntity((Entity) this.horse, 4.0D);
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            int j = this.horseContainer.getContainerSize() + 1;

            if (i < j) {
                if (!this.moveItemStackTo(itemstack1, j, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (j <= 1 || !this.moveItemStackTo(itemstack1, 2, j, false)) {
                int k = j + 27;
                int l = k + 9;

                if (i >= k && i < l) {
                    if (!this.moveItemStackTo(itemstack1, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= j && i < k) {
                    if (!this.moveItemStackTo(itemstack1, k, l, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, k, k, false)) {
                    return ItemStack.EMPTY;
                }

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
        this.horseContainer.stopOpen(entityhuman);
    }
}
