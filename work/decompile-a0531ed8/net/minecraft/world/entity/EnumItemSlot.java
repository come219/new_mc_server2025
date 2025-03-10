package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.world.item.ItemStack;

public enum EnumItemSlot implements INamable {

    MAINHAND(EnumItemSlot.Function.HAND, 0, 0, "mainhand"), OFFHAND(EnumItemSlot.Function.HAND, 1, 5, "offhand"), FEET(EnumItemSlot.Function.HUMANOID_ARMOR, 0, 1, 1, "feet"), LEGS(EnumItemSlot.Function.HUMANOID_ARMOR, 1, 1, 2, "legs"), CHEST(EnumItemSlot.Function.HUMANOID_ARMOR, 2, 1, 3, "chest"), HEAD(EnumItemSlot.Function.HUMANOID_ARMOR, 3, 1, 4, "head"), BODY(EnumItemSlot.Function.ANIMAL_ARMOR, 0, 1, 6, "body");

    public static final int NO_COUNT_LIMIT = 0;
    public static final List<EnumItemSlot> VALUES = List.of(values());
    public static final IntFunction<EnumItemSlot> BY_ID = ByIdMap.continuous((enumitemslot) -> {
        return enumitemslot.id;
    }, values(), ByIdMap.a.ZERO);
    public static final INamable.a<EnumItemSlot> CODEC = INamable.fromEnum(EnumItemSlot::values);
    public static final StreamCodec<ByteBuf, EnumItemSlot> STREAM_CODEC = ByteBufCodecs.idMapper(EnumItemSlot.BY_ID, (enumitemslot) -> {
        return enumitemslot.id;
    });
    private final EnumItemSlot.Function type;
    private final int index;
    private final int countLimit;
    private final int id;
    private final String name;

    private EnumItemSlot(final EnumItemSlot.Function enumitemslot_function, final int i, final int j, final int k, final String s) {
        this.type = enumitemslot_function;
        this.index = i;
        this.countLimit = j;
        this.id = k;
        this.name = s;
    }

    private EnumItemSlot(final EnumItemSlot.Function enumitemslot_function, final int i, final int j, final String s) {
        this(enumitemslot_function, i, 0, j, s);
    }

    public EnumItemSlot.Function getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getIndex(int i) {
        return i + this.index;
    }

    public ItemStack limit(ItemStack itemstack) {
        return this.countLimit > 0 ? itemstack.split(this.countLimit) : itemstack;
    }

    public int getId() {
        return this.id;
    }

    public int getFilterBit(int i) {
        return this.id + i;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == EnumItemSlot.Function.HUMANOID_ARMOR || this.type == EnumItemSlot.Function.ANIMAL_ARMOR;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static EnumItemSlot byName(String s) {
        EnumItemSlot enumitemslot = (EnumItemSlot) EnumItemSlot.CODEC.byName(s);

        if (enumitemslot != null) {
            return enumitemslot;
        } else {
            throw new IllegalArgumentException("Invalid slot '" + s + "'");
        }
    }

    public static enum Function {

        HAND, HUMANOID_ARMOR, ANIMAL_ARMOR;

        private Function() {}
    }
}
