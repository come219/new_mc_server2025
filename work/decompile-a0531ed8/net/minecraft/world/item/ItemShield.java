package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;

public class ItemShield extends Item {

    public static final int EFFECTIVE_BLOCK_DELAY = 5;
    public static final float MINIMUM_DURABILITY_DAMAGE = 3.0F;

    public ItemShield(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        EnumColor enumcolor = (EnumColor) itemstack.get(DataComponents.BASE_COLOR);

        return (IChatBaseComponent) (enumcolor != null ? IChatBaseComponent.translatable(this.descriptionId + "." + enumcolor.getName()) : super.getName(itemstack));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        ItemBanner.appendHoverTextFromBannerBlockEntityTag(itemstack, list);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        return 72000;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        entityhuman.startUsingItem(enumhand);
        return EnumInteractionResult.CONSUME;
    }
}
