package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPlayerRotationPacket(float yRot, float xRot) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundPlayerRotationPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, ClientboundPlayerRotationPacket::yRot, ByteBufCodecs.FLOAT, ClientboundPlayerRotationPacket::xRot, ClientboundPlayerRotationPacket::new);

    @Override
    public PacketType<ClientboundPlayerRotationPacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_ROTATION;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleRotatePlayer(this);
    }
}
