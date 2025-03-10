package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;
import net.minecraft.world.phys.Vec3D;

public abstract class MinecartBehavior {

    protected final EntityMinecartAbstract minecart;

    protected MinecartBehavior(EntityMinecartAbstract entityminecartabstract) {
        this.minecart = entityminecartabstract;
    }

    public void cancelLerp() {}

    public void lerpTo(double d0, double d1, double d2, float f, float f1, int i) {
        this.setPos(d0, d1, d2);
        this.setYRot(f % 360.0F);
        this.setXRot(f1 % 360.0F);
    }

    public double lerpTargetX() {
        return this.getX();
    }

    public double lerpTargetY() {
        return this.getY();
    }

    public double lerpTargetZ() {
        return this.getZ();
    }

    public float lerpTargetXRot() {
        return this.getXRot();
    }

    public float lerpTargetYRot() {
        return this.getYRot();
    }

    public void lerpMotion(double d0, double d1, double d2) {
        this.setDeltaMovement(d0, d1, d2);
    }

    public abstract void tick();

    public World level() {
        return this.minecart.level();
    }

    public abstract void moveAlongTrack(WorldServer worldserver);

    public abstract double stepAlongTrack(BlockPosition blockposition, BlockPropertyTrackPosition blockpropertytrackposition, double d0);

    public abstract boolean pushAndPickupEntities();

    public Vec3D getDeltaMovement() {
        return this.minecart.getDeltaMovement();
    }

    public void setDeltaMovement(Vec3D vec3d) {
        this.minecart.setDeltaMovement(vec3d);
    }

    public void setDeltaMovement(double d0, double d1, double d2) {
        this.minecart.setDeltaMovement(d0, d1, d2);
    }

    public Vec3D position() {
        return this.minecart.position();
    }

    public double getX() {
        return this.minecart.getX();
    }

    public double getY() {
        return this.minecart.getY();
    }

    public double getZ() {
        return this.minecart.getZ();
    }

    public void setPos(Vec3D vec3d) {
        this.minecart.setPos(vec3d);
    }

    public void setPos(double d0, double d1, double d2) {
        this.minecart.setPos(d0, d1, d2);
    }

    public float getXRot() {
        return this.minecart.getXRot();
    }

    public void setXRot(float f) {
        this.minecart.setXRot(f);
    }

    public float getYRot() {
        return this.minecart.getYRot();
    }

    public void setYRot(float f) {
        this.minecart.setYRot(f);
    }

    public EnumDirection getMotionDirection() {
        return this.minecart.getDirection();
    }

    public Vec3D getKnownMovement(Vec3D vec3d) {
        return vec3d;
    }

    public abstract double getMaxSpeed(WorldServer worldserver);

    public abstract double getSlowdownFactor();
}
