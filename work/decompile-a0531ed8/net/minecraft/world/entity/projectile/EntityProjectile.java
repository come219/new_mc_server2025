package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public abstract class EntityProjectile extends IProjectile {

    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;

    protected EntityProjectile(EntityTypes<? extends EntityProjectile> entitytypes, World world) {
        super(entitytypes, world);
    }

    protected EntityProjectile(EntityTypes<? extends EntityProjectile> entitytypes, double d0, double d1, double d2, World world) {
        this(entitytypes, world);
        this.setPos(d0, d1, d2);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        if (this.tickCount < 2 && d0 < 12.25D) {
            return false;
        } else {
            double d1 = this.getBoundingBox().getSize() * 4.0D;

            if (Double.isNaN(d1)) {
                d1 = 4.0D;
            }

            d1 *= 64.0D;
            return d0 < d1 * d1;
        }
    }

    @Override
    public boolean canUsePortal(boolean flag) {
        return true;
    }

    @Override
    public void tick() {
        this.handleFirstTickBubbleColumn();
        this.applyGravity();
        this.applyInertia();
        MovingObjectPosition movingobjectposition = ProjectileHelper.getHitResultOnMoveVector(this, this::canHitEntity);
        Vec3D vec3d;

        if (movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.MISS) {
            vec3d = movingobjectposition.getLocation();
        } else {
            vec3d = this.position().add(this.getDeltaMovement());
        }

        this.setPos(vec3d);
        this.updateRotation();
        this.applyEffectsFromBlocks();
        super.tick();
        if (movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.MISS && this.isAlive()) {
            this.hitTargetOrDeflectSelf(movingobjectposition);
        }

    }

    private void applyInertia() {
        Vec3D vec3d = this.getDeltaMovement();
        Vec3D vec3d1 = this.position();
        float f;

        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f1 = 0.25F;

                this.level().addParticle(Particles.BUBBLE, vec3d1.x - vec3d.x * 0.25D, vec3d1.y - vec3d.y * 0.25D, vec3d1.z - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            }

            f = 0.8F;
        } else {
            f = 0.99F;
        }

        this.setDeltaMovement(vec3d.scale((double) f));
    }

    private void handleFirstTickBubbleColumn() {
        if (this.firstTick) {
            Iterator iterator = BlockPosition.betweenClosed(this.getBoundingBox()).iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition = (BlockPosition) iterator.next();
                IBlockData iblockdata = this.level().getBlockState(blockposition);

                if (iblockdata.is(Blocks.BUBBLE_COLUMN)) {
                    iblockdata.entityInside(this.level(), blockposition, this);
                }
            }
        }

    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }
}
