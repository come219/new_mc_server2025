package net.minecraft.world.item;

import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ISteerable;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;

public class ItemCarrotStick<T extends Entity & ISteerable> extends Item {

    private final EntityTypes<T> canInteractWith;
    private final int consumeItemDamage;

    public ItemCarrotStick(EntityTypes<T> entitytypes, int i, Item.Info item_info) {
        super(item_info);
        this.canInteractWith = entitytypes;
        this.consumeItemDamage = i;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (world.isClientSide) {
            return EnumInteractionResult.PASS;
        } else {
            Entity entity = entityhuman.getControlledVehicle();

            if (entityhuman.isPassenger() && entity instanceof ISteerable) {
                ISteerable isteerable = (ISteerable) entity;

                if (entity.getType() == this.canInteractWith && isteerable.boost()) {
                    EnumItemSlot enumitemslot = EntityLiving.getSlotForHand(enumhand);
                    ItemStack itemstack1 = itemstack.hurtAndConvertOnBreak(this.consumeItemDamage, Items.FISHING_ROD, entityhuman, enumitemslot);

                    return EnumInteractionResult.SUCCESS_SERVER.heldItemTransformedTo(itemstack1);
                }
            }

            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            return EnumInteractionResult.PASS;
        }
    }
}
