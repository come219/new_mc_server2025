package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public enum CookingBookCategory implements INamable {

    FOOD(0, "food"), BLOCKS(1, "blocks"), MISC(2, "misc");

    private static final IntFunction<CookingBookCategory> BY_ID = ByIdMap.continuous((cookingbookcategory) -> {
        return cookingbookcategory.id;
    }, values(), ByIdMap.a.ZERO);
    public static final Codec<CookingBookCategory> CODEC = INamable.fromEnum(CookingBookCategory::values);
    public static final StreamCodec<ByteBuf, CookingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(CookingBookCategory.BY_ID, (cookingbookcategory) -> {
        return cookingbookcategory.id;
    });
    private final int id;
    private final String name;

    private CookingBookCategory(final int i, final String s) {
        this.id = i;
        this.name = s;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
