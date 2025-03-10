package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.block.state.BlockBase;

public class BlockCarpet extends CarpetBlock {

    public static final MapCodec<BlockCarpet> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnumColor.CODEC.fieldOf("color").forGetter(BlockCarpet::getColor), propertiesCodec()).apply(instance, BlockCarpet::new);
    });
    private final EnumColor color;

    @Override
    public MapCodec<BlockCarpet> codec() {
        return BlockCarpet.CODEC;
    }

    protected BlockCarpet(EnumColor enumcolor, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.color = enumcolor;
    }

    public EnumColor getColor() {
        return this.color;
    }
}
