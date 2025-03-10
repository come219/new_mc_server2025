package net.minecraft.world.item.component;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public interface ConsumableListener {

    void onConsume(World world, EntityLiving entityliving, ItemStack itemstack, Consumable consumable);
}
