package net.minecraft.world.item.equipment;

import com.mojang.serialization.Codec;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.EnumItemSlot;

public enum ArmorType implements INamable {

    HELMET(EnumItemSlot.HEAD, 11, "helmet"), CHESTPLATE(EnumItemSlot.CHEST, 16, "chestplate"), LEGGINGS(EnumItemSlot.LEGS, 15, "leggings"), BOOTS(EnumItemSlot.FEET, 13, "boots"), BODY(EnumItemSlot.BODY, 16, "body");

    public static final Codec<ArmorType> CODEC = INamable.fromValues(ArmorType::values);
    private final EnumItemSlot slot;
    private final String name;
    private final int unitDurability;

    private ArmorType(final EnumItemSlot enumitemslot, final int i, final String s) {
        this.slot = enumitemslot;
        this.name = s;
        this.unitDurability = i;
    }

    public int getDurability(int i) {
        return this.unitDurability * i;
    }

    public EnumItemSlot getSlot() {
        return this.slot;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
