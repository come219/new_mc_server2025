package net.minecraft.world.entity.vehicle;

import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class EntityMinecartRideable extends EntityMinecartAbstract {

    private float rotationOffset;
    private float playerRotationOffset;

    public EntityMinecartRideable(EntityTypes<?> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    public EnumInteractionResult interact(EntityHuman entityhuman, EnumHand enumhand) {
        if (!entityhuman.isSecondaryUseActive() && !this.isVehicle() && (this.level().isClientSide || entityhuman.startRiding(this))) {
            this.playerRotationOffset = this.rotationOffset;
            return (EnumInteractionResult) (!this.level().isClientSide ? (entityhuman.startRiding(this) ? EnumInteractionResult.CONSUME : EnumInteractionResult.PASS) : EnumInteractionResult.SUCCESS);
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
    }

    @Override
    public void activateMinecart(int i, int j, int k, boolean flag) {
        if (flag) {
            if (this.isVehicle()) {
                this.ejectPassengers();
            }

            if (this.getHurtTime() == 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.setDamage(50.0F);
                this.markHurt();
            }
        }

    }

    @Override
    public boolean isRideable() {
        return true;
    }

    @Override
    public void tick() {
        double d0 = (double) this.getYRot();
        Vec3D vec3d = this.position();

        super.tick();
        double d1 = ((double) this.getYRot() - d0) % 360.0D;

        if (this.level().isClientSide && vec3d.distanceTo(this.position()) > 0.01D) {
            this.rotationOffset += (float) d1;
            this.rotationOffset %= 360.0F;
        }

    }

    @Override
    protected void positionRider(Entity entity, Entity.MoveFunction entity_movefunction) {
        super.positionRider(entity, entity_movefunction);
        if (this.level().isClientSide && entity instanceof EntityHuman entityhuman) {
            if (entityhuman.shouldRotateWithMinecart() && useExperimentalMovement(this.level())) {
                float f = (float) MathHelper.rotLerp(0.5D, (double) this.playerRotationOffset, (double) this.rotationOffset);

                entityhuman.setYRot(entityhuman.getYRot() - (f - this.playerRotationOffset));
                this.playerRotationOffset = f;
            }
        }

    }
}
