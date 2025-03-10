package net.minecraft.core.dispenser;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.phys.AxisAlignedBB;

public class EquipmentDispenseItemBehavior extends DispenseBehaviorItem {

    public static final EquipmentDispenseItemBehavior INSTANCE = new EquipmentDispenseItemBehavior();

    public EquipmentDispenseItemBehavior() {}

    @Override
    protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        return dispenseEquipment(sourceblock, itemstack) ? itemstack : super.execute(sourceblock, itemstack);
    }

    public static boolean dispenseEquipment(SourceBlock sourceblock, ItemStack itemstack) {
        BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
        List<EntityLiving> list = sourceblock.level().getEntitiesOfClass(EntityLiving.class, new AxisAlignedBB(blockposition), (entityliving) -> {
            return entityliving.canEquipWithDispenser(itemstack);
        });

        if (list.isEmpty()) {
            return false;
        } else {
            EntityLiving entityliving = (EntityLiving) list.getFirst();
            EnumItemSlot enumitemslot = entityliving.getEquipmentSlotForItem(itemstack);
            ItemStack itemstack1 = itemstack.split(1);

            entityliving.setItemSlot(enumitemslot, itemstack1);
            if (entityliving instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient) entityliving;

                entityinsentient.setDropChance(enumitemslot, 2.0F);
                entityinsentient.setPersistenceRequired();
            }

            return true;
        }
    }
}
