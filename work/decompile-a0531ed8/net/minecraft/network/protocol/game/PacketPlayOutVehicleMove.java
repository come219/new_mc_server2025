package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

public record PacketPlayOutVehicleMove(Vec3D position, float yRot, float xRot) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutVehicleMove> STREAM_CODEC = StreamCodec.composite(Vec3D.STREAM_CODEC, PacketPlayOutVehicleMove::position, ByteBufCodecs.FLOAT, PacketPlayOutVehicleMove::yRot, ByteBufCodecs.FLOAT, PacketPlayOutVehicleMove::xRot, PacketPlayOutVehicleMove::new);

    public static PacketPlayOutVehicleMove fromEntity(Entity entity) {
        return new PacketPlayOutVehicleMove(entity.position(), entity.getYRot(), entity.getXRot());
    }

    @Override
    public PacketType<PacketPlayOutVehicleMove> type() {
        return GamePacketTypes.CLIENTBOUND_MOVE_VEHICLE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleMoveVehicle(this);
    }
}
