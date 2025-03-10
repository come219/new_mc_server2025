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

// CraftBukkit start
import net.minecraft.world.level.World;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseArmorEvent;
// CraftBukkit end

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

            // CraftBukkit start
            World world = sourceblock.level();
            org.bukkit.block.Block block = CraftBlock.at(world, sourceblock.pos());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

            BlockDispenseArmorEvent event = new BlockDispenseArmorEvent(block, craftItem.clone(), (org.bukkit.craftbukkit.entity.CraftLivingEntity) entityliving.getBukkitEntity());
            if (!BlockDispenser.eventFired) {
                world.getCraftServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                itemstack.grow(1);
                return false;
            }

            if (!event.getItem().equals(craftItem)) {
                itemstack.grow(1);
                // Chain to handler for new item
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.DISPENSER_REGISTRY.get(eventStack.getItem());
                if (idispensebehavior != IDispenseBehavior.NOOP && idispensebehavior != EquipmentDispenseItemBehavior.INSTANCE) {
                    idispensebehavior.dispense(sourceblock, eventStack);
                    return true;
                }
            }

            entityliving.setItemSlot(enumitemslot, CraftItemStack.asNMSCopy(event.getItem()));
            // CraftBukkit end
            if (entityliving instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient) entityliving;

                entityinsentient.setDropChance(enumitemslot, 2.0F);
                entityinsentient.setPersistenceRequired();
            }

            return true;
        }
    }
}
