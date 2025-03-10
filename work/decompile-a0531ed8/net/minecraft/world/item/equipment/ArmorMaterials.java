package net.minecraft.world.item.equipment;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.SystemUtils;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;

public interface ArmorMaterials {

    ArmorMaterial LEATHER = new ArmorMaterial(5, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 1);
        enummap.put(ArmorType.LEGGINGS, 2);
        enummap.put(ArmorType.CHESTPLATE, 3);
        enummap.put(ArmorType.HELMET, 1);
        enummap.put(ArmorType.BODY, 3);
    }), 15, SoundEffects.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, TagsItem.REPAIRS_LEATHER_ARMOR, EquipmentAssets.LEATHER);
    ArmorMaterial CHAINMAIL = new ArmorMaterial(15, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 1);
        enummap.put(ArmorType.LEGGINGS, 4);
        enummap.put(ArmorType.CHESTPLATE, 5);
        enummap.put(ArmorType.HELMET, 2);
        enummap.put(ArmorType.BODY, 4);
    }), 12, SoundEffects.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, TagsItem.REPAIRS_CHAIN_ARMOR, EquipmentAssets.CHAINMAIL);
    ArmorMaterial IRON = new ArmorMaterial(15, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 2);
        enummap.put(ArmorType.LEGGINGS, 5);
        enummap.put(ArmorType.CHESTPLATE, 6);
        enummap.put(ArmorType.HELMET, 2);
        enummap.put(ArmorType.BODY, 5);
    }), 9, SoundEffects.ARMOR_EQUIP_IRON, 0.0F, 0.0F, TagsItem.REPAIRS_IRON_ARMOR, EquipmentAssets.IRON);
    ArmorMaterial GOLD = new ArmorMaterial(7, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 1);
        enummap.put(ArmorType.LEGGINGS, 3);
        enummap.put(ArmorType.CHESTPLATE, 5);
        enummap.put(ArmorType.HELMET, 2);
        enummap.put(ArmorType.BODY, 7);
    }), 25, SoundEffects.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, TagsItem.REPAIRS_GOLD_ARMOR, EquipmentAssets.GOLD);
    ArmorMaterial DIAMOND = new ArmorMaterial(33, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 3);
        enummap.put(ArmorType.LEGGINGS, 6);
        enummap.put(ArmorType.CHESTPLATE, 8);
        enummap.put(ArmorType.HELMET, 3);
        enummap.put(ArmorType.BODY, 11);
    }), 10, SoundEffects.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, TagsItem.REPAIRS_DIAMOND_ARMOR, EquipmentAssets.DIAMOND);
    ArmorMaterial TURTLE_SCUTE = new ArmorMaterial(25, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 2);
        enummap.put(ArmorType.LEGGINGS, 5);
        enummap.put(ArmorType.CHESTPLATE, 6);
        enummap.put(ArmorType.HELMET, 2);
        enummap.put(ArmorType.BODY, 5);
    }), 9, SoundEffects.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, TagsItem.REPAIRS_TURTLE_HELMET, EquipmentAssets.TURTLE_SCUTE);
    ArmorMaterial NETHERITE = new ArmorMaterial(37, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 3);
        enummap.put(ArmorType.LEGGINGS, 6);
        enummap.put(ArmorType.CHESTPLATE, 8);
        enummap.put(ArmorType.HELMET, 3);
        enummap.put(ArmorType.BODY, 11);
    }), 15, SoundEffects.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, TagsItem.REPAIRS_NETHERITE_ARMOR, EquipmentAssets.NETHERITE);
    ArmorMaterial ARMADILLO_SCUTE = new ArmorMaterial(4, (Map) SystemUtils.make(new EnumMap(ArmorType.class), (enummap) -> {
        enummap.put(ArmorType.BOOTS, 3);
        enummap.put(ArmorType.LEGGINGS, 6);
        enummap.put(ArmorType.CHESTPLATE, 8);
        enummap.put(ArmorType.HELMET, 3);
        enummap.put(ArmorType.BODY, 11);
    }), 10, SoundEffects.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, TagsItem.REPAIRS_WOLF_ARMOR, EquipmentAssets.ARMADILLO_SCUTE);
}
