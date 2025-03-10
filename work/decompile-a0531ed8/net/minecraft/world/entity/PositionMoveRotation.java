package net.minecraft.world.entity;

import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3D;

public record PositionMoveRotation(Vec3D position, Vec3D deltaMovement, float yRot, float xRot) {

    public static final StreamCodec<PacketDataSerializer, PositionMoveRotation> STREAM_CODEC = StreamCodec.composite(Vec3D.STREAM_CODEC, PositionMoveRotation::position, Vec3D.STREAM_CODEC, PositionMoveRotation::deltaMovement, ByteBufCodecs.FLOAT, PositionMoveRotation::yRot, ByteBufCodecs.FLOAT, PositionMoveRotation::xRot, PositionMoveRotation::new);

    public static PositionMoveRotation of(Entity entity) {
        return new PositionMoveRotation(entity.position(), entity.getKnownMovement(), entity.getYRot(), entity.getXRot());
    }

    public static PositionMoveRotation ofEntityUsingLerpTarget(Entity entity) {
        return new PositionMoveRotation(new Vec3D(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ()), entity.getKnownMovement(), entity.getYRot(), entity.getXRot());
    }

    public static PositionMoveRotation of(TeleportTransition teleporttransition) {
        return new PositionMoveRotation(teleporttransition.position(), teleporttransition.deltaMovement(), teleporttransition.yRot(), teleporttransition.xRot());
    }

    public static PositionMoveRotation calculateAbsolute(PositionMoveRotation positionmoverotation, PositionMoveRotation positionmoverotation1, Set<Relative> set) {
        double d0 = set.contains(Relative.X) ? positionmoverotation.position.x : 0.0D;
        double d1 = set.contains(Relative.Y) ? positionmoverotation.position.y : 0.0D;
        double d2 = set.contains(Relative.Z) ? positionmoverotation.position.z : 0.0D;
        float f = set.contains(Relative.Y_ROT) ? positionmoverotation.yRot : 0.0F;
        float f1 = set.contains(Relative.X_ROT) ? positionmoverotation.xRot : 0.0F;
        Vec3D vec3d = new Vec3D(d0 + positionmoverotation1.position.x, d1 + positionmoverotation1.position.y, d2 + positionmoverotation1.position.z);
        float f2 = f + positionmoverotation1.yRot;
        float f3 = f1 + positionmoverotation1.xRot;
        Vec3D vec3d1 = positionmoverotation.deltaMovement;

        if (set.contains(Relative.ROTATE_DELTA)) {
            float f4 = positionmoverotation.yRot - f2;
            float f5 = positionmoverotation.xRot - f3;

            vec3d1 = vec3d1.xRot((float) Math.toRadians((double) f5));
            vec3d1 = vec3d1.yRot((float) Math.toRadians((double) f4));
        }

        Vec3D vec3d2 = new Vec3D(calculateDelta(vec3d1.x, positionmoverotation1.deltaMovement.x, set, Relative.DELTA_X), calculateDelta(vec3d1.y, positionmoverotation1.deltaMovement.y, set, Relative.DELTA_Y), calculateDelta(vec3d1.z, positionmoverotation1.deltaMovement.z, set, Relative.DELTA_Z));

        return new PositionMoveRotation(vec3d, vec3d2, f2, f3);
    }

    private static double calculateDelta(double d0, double d1, Set<Relative> set, Relative relative) {
        return set.contains(relative) ? d0 + d1 : d1;
    }
}
