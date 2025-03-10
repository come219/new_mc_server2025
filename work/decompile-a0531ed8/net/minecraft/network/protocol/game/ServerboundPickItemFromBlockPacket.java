package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundPickItemFromBlockPacket(BlockPosition pos, boolean includeData) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<ByteBuf, ServerboundPickItemFromBlockPacket> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, ServerboundPickItemFromBlockPacket::pos, ByteBufCodecs.BOOL, ServerboundPickItemFromBlockPacket::includeData, ServerboundPickItemFromBlockPacket::new);

    @Override
    public PacketType<ServerboundPickItemFromBlockPacket> type() {
        return GamePacketTypes.SERVERBOUND_PICK_ITEM_FROM_BLOCK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePickItemFromBlock(this);
    }
}
