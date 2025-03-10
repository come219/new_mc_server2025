package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {

    private final List<ItemCombinerMenuSlotDefinition.b> slots;
    private final ItemCombinerMenuSlotDefinition.b resultSlot;

    ItemCombinerMenuSlotDefinition(List<ItemCombinerMenuSlotDefinition.b> list, ItemCombinerMenuSlotDefinition.b itemcombinermenuslotdefinition_b) {
        if (!list.isEmpty() && !itemcombinermenuslotdefinition_b.equals(ItemCombinerMenuSlotDefinition.b.EMPTY)) {
            this.slots = list;
            this.resultSlot = itemcombinermenuslotdefinition_b;
        } else {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
    }

    public static ItemCombinerMenuSlotDefinition.a create() {
        return new ItemCombinerMenuSlotDefinition.a();
    }

    public ItemCombinerMenuSlotDefinition.b getSlot(int i) {
        return (ItemCombinerMenuSlotDefinition.b) this.slots.get(i);
    }

    public ItemCombinerMenuSlotDefinition.b getResultSlot() {
        return this.resultSlot;
    }

    public List<ItemCombinerMenuSlotDefinition.b> getSlots() {
        return this.slots;
    }

    public int getNumOfInputSlots() {
        return this.slots.size();
    }

    public int getResultSlotIndex() {
        return this.getNumOfInputSlots();
    }

    public static record b(int slotIndex, int x, int y, Predicate<ItemStack> mayPlace) {

        static final ItemCombinerMenuSlotDefinition.b EMPTY = new ItemCombinerMenuSlotDefinition.b(0, 0, 0, (itemstack) -> {
            return true;
        });
    }

    public static class a {

        private final List<ItemCombinerMenuSlotDefinition.b> inputSlots = new ArrayList();
        private ItemCombinerMenuSlotDefinition.b resultSlot;

        public a() {
            this.resultSlot = ItemCombinerMenuSlotDefinition.b.EMPTY;
        }

        public ItemCombinerMenuSlotDefinition.a withSlot(int i, int j, int k, Predicate<ItemStack> predicate) {
            this.inputSlots.add(new ItemCombinerMenuSlotDefinition.b(i, j, k, predicate));
            return this;
        }

        public ItemCombinerMenuSlotDefinition.a withResultSlot(int i, int j, int k) {
            this.resultSlot = new ItemCombinerMenuSlotDefinition.b(i, j, k, (itemstack) -> {
                return false;
            });
            return this;
        }

        public ItemCombinerMenuSlotDefinition build() {
            int i = this.inputSlots.size();

            for (int j = 0; j < i; ++j) {
                ItemCombinerMenuSlotDefinition.b itemcombinermenuslotdefinition_b = (ItemCombinerMenuSlotDefinition.b) this.inputSlots.get(j);

                if (itemcombinermenuslotdefinition_b.slotIndex != j) {
                    throw new IllegalArgumentException("Expected input slots to have continous indexes");
                }
            }

            if (this.resultSlot.slotIndex != i) {
                throw new IllegalArgumentException("Expected result slot index to follow last input slot");
            } else {
                return new ItemCombinerMenuSlotDefinition(this.inputSlots, this.resultSlot);
            }
        }
    }
}
