package net.minecraft.world.item.equipment;

import java.util.Map;
import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.EnumColor;

public interface EquipmentAssets {

    ResourceKey<? extends IRegistry<EquipmentAsset>> ROOT_ID = ResourceKey.createRegistryKey(MinecraftKey.withDefaultNamespace("equipment_asset"));
    ResourceKey<EquipmentAsset> LEATHER = createId("leather");
    ResourceKey<EquipmentAsset> CHAINMAIL = createId("chainmail");
    ResourceKey<EquipmentAsset> IRON = createId("iron");
    ResourceKey<EquipmentAsset> GOLD = createId("gold");
    ResourceKey<EquipmentAsset> DIAMOND = createId("diamond");
    ResourceKey<EquipmentAsset> TURTLE_SCUTE = createId("turtle_scute");
    ResourceKey<EquipmentAsset> NETHERITE = createId("netherite");
    ResourceKey<EquipmentAsset> ARMADILLO_SCUTE = createId("armadillo_scute");
    ResourceKey<EquipmentAsset> ELYTRA = createId("elytra");
    Map<EnumColor, ResourceKey<EquipmentAsset>> CARPETS = SystemUtils.makeEnumMap(EnumColor.class, (enumcolor) -> {
        return createId(enumcolor.getSerializedName() + "_carpet");
    });
    ResourceKey<EquipmentAsset> TRADER_LLAMA = createId("trader_llama");

    static ResourceKey<EquipmentAsset> createId(String s) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, MinecraftKey.withDefaultNamespace(s));
    }
}
