package net.minecraft.world.entity;

import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.item.ItemStack;

public interface IShearable {

    void shear(WorldServer worldserver, SoundCategory soundcategory, ItemStack itemstack);

    boolean readyForShearing();
}
