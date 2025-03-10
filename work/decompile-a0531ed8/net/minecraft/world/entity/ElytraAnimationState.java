package net.minecraft.world.entity;

import net.minecraft.util.MathHelper;
import net.minecraft.world.phys.Vec3D;

public class ElytraAnimationState {

    private static final float DEFAULT_X_ROT = 0.2617994F;
    private static final float DEFAULT_Z_ROT = -0.2617994F;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float rotXOld;
    private float rotYOld;
    private float rotZOld;
    private final EntityLiving entity;

    public ElytraAnimationState(EntityLiving entityliving) {
        this.entity = entityliving;
    }

    public void tick() {
        this.rotXOld = this.rotX;
        this.rotYOld = this.rotY;
        this.rotZOld = this.rotZ;
        float f;
        float f1;
        float f2;

        if (this.entity.isFallFlying()) {
            float f3 = 1.0F;
            Vec3D vec3d = this.entity.getDeltaMovement();

            if (vec3d.y < 0.0D) {
                Vec3D vec3d1 = vec3d.normalize();

                f3 = 1.0F - (float) Math.pow(-vec3d1.y, 1.5D);
            }

            f = MathHelper.lerp(f3, 0.2617994F, 0.34906584F);
            f1 = MathHelper.lerp(f3, -0.2617994F, -1.5707964F);
            f2 = 0.0F;
        } else if (this.entity.isCrouching()) {
            f = 0.6981317F;
            f1 = -0.7853982F;
            f2 = 0.08726646F;
        } else {
            f = 0.2617994F;
            f1 = -0.2617994F;
            f2 = 0.0F;
        }

        this.rotX += (f - this.rotX) * 0.3F;
        this.rotY += (f2 - this.rotY) * 0.3F;
        this.rotZ += (f1 - this.rotZ) * 0.3F;
    }

    public float getRotX(float f) {
        return MathHelper.lerp(f, this.rotXOld, this.rotX);
    }

    public float getRotY(float f) {
        return MathHelper.lerp(f, this.rotYOld, this.rotY);
    }

    public float getRotZ(float f) {
        return MathHelper.lerp(f, this.rotZOld, this.rotZ);
    }
}
