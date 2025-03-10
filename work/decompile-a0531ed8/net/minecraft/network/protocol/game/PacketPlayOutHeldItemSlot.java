package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayOutHeldItemSlot(int slot) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<ByteBuf, PacketPlayOutHeldItemSlot> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayOutHeldItemSlot::slot, PacketPlayOutHeldItemSlot::new);

    @Override
    public PacketType<PacketPlayOutHeldItemSlot> type() {
        return GamePacketTypes.CLIENTBOUND_SET_HELD_SLOT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetHeldSlot(this);
    }
}
