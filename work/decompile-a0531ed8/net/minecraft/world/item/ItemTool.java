package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.level.block.Block;

public class ItemTool extends Item {

    protected ItemTool(ToolMaterial toolmaterial, TagKey<Block> tagkey, float f, float f1, Item.Info item_info) {
        super(toolmaterial.applyToolProperties(item_info, tagkey, f, f1));
    }

    @Override
    public boolean hurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        itemstack.hurtAndBreak(2, entityliving1, EnumItemSlot.MAINHAND);
    }
}
