package net.minecraft.world.inventory;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.World;

public class ContainerPlayer extends AbstractCraftingMenu {

    public static final int CONTAINER_ID = 0;
    public static final int RESULT_SLOT = 0;
    private static final int CRAFTING_GRID_WIDTH = 2;
    private static final int CRAFTING_GRID_HEIGHT = 2;
    public static final int CRAFT_SLOT_START = 1;
    public static final int CRAFT_SLOT_COUNT = 4;
    public static final int CRAFT_SLOT_END = 5;
    public static final int ARMOR_SLOT_START = 5;
    public static final int ARMOR_SLOT_COUNT = 4;
    public static final int ARMOR_SLOT_END = 9;
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 36;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 45;
    public static final int SHIELD_SLOT = 45;
    public static final MinecraftKey EMPTY_ARMOR_SLOT_HELMET = MinecraftKey.withDefaultNamespace("container/slot/helmet");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_CHESTPLATE = MinecraftKey.withDefaultNamespace("container/slot/chestplate");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_LEGGINGS = MinecraftKey.withDefaultNamespace("container/slot/leggings");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_BOOTS = MinecraftKey.withDefaultNamespace("container/slot/boots");
    public static final MinecraftKey EMPTY_ARMOR_SLOT_SHIELD = MinecraftKey.withDefaultNamespace("container/slot/shield");
    private static final Map<EnumItemSlot, MinecraftKey> TEXTURE_EMPTY_SLOTS = Map.of(EnumItemSlot.FEET, ContainerPlayer.EMPTY_ARMOR_SLOT_BOOTS, EnumItemSlot.LEGS, ContainerPlayer.EMPTY_ARMOR_SLOT_LEGGINGS, EnumItemSlot.CHEST, ContainerPlayer.EMPTY_ARMOR_SLOT_CHESTPLATE, EnumItemSlot.HEAD, ContainerPlayer.EMPTY_ARMOR_SLOT_HELMET);
    private static final EnumItemSlot[] SLOT_IDS = new EnumItemSlot[]{EnumItemSlot.HEAD, EnumItemSlot.CHEST, EnumItemSlot.LEGS, EnumItemSlot.FEET};
    public final boolean active;
    private final EntityHuman owner;

    public ContainerPlayer(PlayerInventory playerinventory, boolean flag, final EntityHuman entityhuman) {
        super((Containers) null, 0, 2, 2);
        this.active = flag;
        this.owner = entityhuman;
        this.addResultSlot(entityhuman, 154, 28);
        this.addCraftingGridSlots(98, 18);

        for (int i = 0; i < 4; ++i) {
            EnumItemSlot enumitemslot = ContainerPlayer.SLOT_IDS[i];
            MinecraftKey minecraftkey = (MinecraftKey) ContainerPlayer.TEXTURE_EMPTY_SLOTS.get(enumitemslot);

            this.addSlot(new ArmorSlot(playerinventory, entityhuman, enumitemslot, 39 - i, 8, 8 + i * 18, minecraftkey));
        }

        this.addStandardInventorySlots(playerinventory, 8, 84);
        this.addSlot(new Slot(this, playerinventory, 40, 77, 62) {
            @Override
            public void setByPlayer(ItemStack itemstack, ItemStack itemstack1) {
                entityhuman.onEquipItem(EnumItemSlot.OFFHAND, itemstack1, itemstack);
                super.setByPlayer(itemstack, itemstack1);
            }

            @Override
            public MinecraftKey getNoItemIcon() {
                return ContainerPlayer.EMPTY_ARMOR_SLOT_SHIELD;
            }
        });
    }

    public static boolean isHotbarSlot(int i) {
        return i >= 36 && i < 45 || i == 45;
    }

    @Override
    public void slotsChanged(IInventory iinventory) {
        World world = this.owner.level();

        if (world instanceof WorldServer worldserver) {
            ContainerWorkbench.slotChangedCraftingGrid(this, worldserver, this.owner, this.craftSlots, this.resultSlots, (RecipeHolder) null);
        }

    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.resultSlots.clearContent();
        if (!entityhuman.level().isClientSide) {
            this.clearContainer(entityhuman, this.craftSlots);
        }
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            EnumItemSlot enumitemslot = entityhuman.getEquipmentSlotForItem(itemstack);

            if (i == 0) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (i >= 1 && i < 5) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 5 && i < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (enumitemslot.getType() == EnumItemSlot.Function.HUMANOID_ARMOR && !((Slot) this.slots.get(8 - enumitemslot.getIndex())).hasItem()) {
                int j = 8 - enumitemslot.getIndex();

                if (!this.moveItemStackTo(itemstack1, j, j + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (enumitemslot == EnumItemSlot.OFFHAND && !((Slot) this.slots.get(45)).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 9 && i < 36) {
                if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 36 && i < 45) {
                if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY, itemstack);
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
        return this.slots.subList(1, 5);
    }

    public InventoryCrafting getCraftSlots() {
        return this.craftSlots;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected EntityHuman owner() {
        return this.owner;
    }
}
