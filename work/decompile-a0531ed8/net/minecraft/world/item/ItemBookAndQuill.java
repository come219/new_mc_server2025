package net.minecraft.world.item;

import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;

public class ItemBookAndQuill extends Item {

    public ItemBookAndQuill(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        entityhuman.openItemGui(itemstack, enumhand);
        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        return EnumInteractionResult.SUCCESS;
    }
}
