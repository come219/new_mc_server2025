package net.minecraft.world.item;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class ItemArmor extends Item {

    public ItemArmor(ArmorMaterial armormaterial, ArmorType armortype, Item.Info item_info) {
        super(armormaterial.humanoidProperties(item_info, armortype));
    }
}
