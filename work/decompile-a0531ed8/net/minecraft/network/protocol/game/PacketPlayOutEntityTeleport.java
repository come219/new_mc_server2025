package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;

public record PacketPlayOutEntityTeleport(int id, PositionMoveRotation change, Set<Relative> relatives, boolean onGround) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutEntityTeleport> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayOutEntityTeleport::id, PositionMoveRotation.STREAM_CODEC, PacketPlayOutEntityTeleport::change, Relative.SET_STREAM_CODEC, PacketPlayOutEntityTeleport::relatives, ByteBufCodecs.BOOL, PacketPlayOutEntityTeleport::onGround, PacketPlayOutEntityTeleport::new);

    public static PacketPlayOutEntityTeleport teleport(int i, PositionMoveRotation positionmoverotation, Set<Relative> set, boolean flag) {
        return new PacketPlayOutEntityTeleport(i, positionmoverotation, set, flag);
    }

    @Override
    public PacketType<PacketPlayOutEntityTeleport> type() {
        return GamePacketTypes.CLIENTBOUND_TELEPORT_ENTITY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTeleportEntity(this);
    }
}
