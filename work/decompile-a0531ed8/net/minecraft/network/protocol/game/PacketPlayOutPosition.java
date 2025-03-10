package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;

public record PacketPlayOutPosition(int id, PositionMoveRotation change, Set<Relative> relatives) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutPosition> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayOutPosition::id, PositionMoveRotation.STREAM_CODEC, PacketPlayOutPosition::change, Relative.SET_STREAM_CODEC, PacketPlayOutPosition::relatives, PacketPlayOutPosition::new);

    public static PacketPlayOutPosition of(int i, PositionMoveRotation positionmoverotation, Set<Relative> set) {
        return new PacketPlayOutPosition(i, positionmoverotation, set);
    }

    @Override
    public PacketType<PacketPlayOutPosition> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_POSITION;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleMovePlayer(this);
    }
}
