package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundSelectBundleItemPacket(int slotId, int selectedItemIndex) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, ServerboundSelectBundleItemPacket> STREAM_CODEC = Packet.codec(ServerboundSelectBundleItemPacket::write, ServerboundSelectBundleItemPacket::new);

    private ServerboundSelectBundleItemPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt(), packetdataserializer.readVarInt());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.slotId);
        packetdataserializer.writeVarInt(this.selectedItemIndex);
    }

    @Override
    public PacketType<ServerboundSelectBundleItemPacket> type() {
        return GamePacketTypes.SERVERBOUND_BUNDLE_ITEM_SELECTED;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleBundleItemSelectedPacket(this);
    }
}
