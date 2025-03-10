package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.state.IBlockData;

public class TileEntityBed extends TileEntity {

    public final EnumColor color;

    public TileEntityBed(BlockPosition blockposition, IBlockData iblockdata) {
        this(blockposition, iblockdata, ((BlockBed) iblockdata.getBlock()).getColor());
    }

    public TileEntityBed(BlockPosition blockposition, IBlockData iblockdata, EnumColor enumcolor) {
        super(TileEntityTypes.BED, blockposition, iblockdata);
        this.color = enumcolor;
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    public EnumColor getColor() {
        return this.color;
    }
}
