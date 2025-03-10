package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.player.Input;

public record PacketPlayInSteerVehicle(Input input) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInSteerVehicle> STREAM_CODEC = StreamCodec.composite(Input.STREAM_CODEC, PacketPlayInSteerVehicle::input, PacketPlayInSteerVehicle::new);

    @Override
    public PacketType<PacketPlayInSteerVehicle> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_INPUT;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePlayerInput(this);
    }
}
