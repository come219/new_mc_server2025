package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

public record PacketPlayInVehicleMove(Vec3D position, float yRot, float xRot, boolean onGround) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInVehicleMove> STREAM_CODEC = StreamCodec.composite(Vec3D.STREAM_CODEC, PacketPlayInVehicleMove::position, ByteBufCodecs.FLOAT, PacketPlayInVehicleMove::yRot, ByteBufCodecs.FLOAT, PacketPlayInVehicleMove::xRot, ByteBufCodecs.BOOL, PacketPlayInVehicleMove::onGround, PacketPlayInVehicleMove::new);

    public static PacketPlayInVehicleMove fromEntity(Entity entity) {
        return new PacketPlayInVehicleMove(new Vec3D(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ()), entity.getYRot(), entity.getXRot(), entity.onGround());
    }

    @Override
    public PacketType<PacketPlayInVehicleMove> type() {
        return GamePacketTypes.SERVERBOUND_MOVE_VEHICLE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleMoveVehicle(this);
    }
}
