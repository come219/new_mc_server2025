package net.minecraft.world.item.equipment;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ArmorMaterial(int durability, Map<ArmorType, Integer> defense, int enchantmentValue, Holder<SoundEffect> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> assetId) {

    public Item.Info humanoidProperties(Item.Info item_info, ArmorType armortype) {
        return item_info.durability(armortype.getDurability(this.durability)).attributes(this.createAttributes(armortype)).enchantable(this.enchantmentValue).component(DataComponents.EQUIPPABLE, Equippable.builder(armortype.getSlot()).setEquipSound(this.equipSound).setAsset(this.assetId).build()).repairable(this.repairIngredient);
    }

    public Item.Info animalProperties(Item.Info item_info, HolderSet<EntityTypes<?>> holderset) {
        return item_info.durability(ArmorType.BODY.getDurability(this.durability)).attributes(this.createAttributes(ArmorType.BODY)).repairable(this.repairIngredient).component(DataComponents.EQUIPPABLE, Equippable.builder(EnumItemSlot.BODY).setEquipSound(this.equipSound).setAsset(this.assetId).setAllowedEntities(holderset).build());
    }

    public Item.Info animalProperties(Item.Info item_info, Holder<SoundEffect> holder, boolean flag, HolderSet<EntityTypes<?>> holderset) {
        if (flag) {
            item_info = item_info.durability(ArmorType.BODY.getDurability(this.durability)).repairable(this.repairIngredient);
        }

        return item_info.attributes(this.createAttributes(ArmorType.BODY)).component(DataComponents.EQUIPPABLE, Equippable.builder(EnumItemSlot.BODY).setEquipSound(holder).setAsset(this.assetId).setAllowedEntities(holderset).setDamageOnHurt(flag).build());
    }

    private ItemAttributeModifiers createAttributes(ArmorType armortype) {
        int i = (Integer) this.defense.getOrDefault(armortype, 0);
        ItemAttributeModifiers.a itemattributemodifiers_a = ItemAttributeModifiers.builder();
        EquipmentSlotGroup equipmentslotgroup = EquipmentSlotGroup.bySlot(armortype.getSlot());
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace("armor." + armortype.getName());

        itemattributemodifiers_a.add(GenericAttributes.ARMOR, new AttributeModifier(minecraftkey, (double) i, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
        itemattributemodifiers_a.add(GenericAttributes.ARMOR_TOUGHNESS, new AttributeModifier(minecraftkey, (double) this.toughness, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
        if (this.knockbackResistance > 0.0F) {
            itemattributemodifiers_a.add(GenericAttributes.KNOCKBACK_RESISTANCE, new AttributeModifier(minecraftkey, (double) this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
        }

        return itemattributemodifiers_a.build();
    }
}
