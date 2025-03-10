package net.minecraft.world.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockMinecartTrackAbstract;
import net.minecraft.world.level.block.BlockPoweredRail;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class OldMinecartBehavior extends MinecartBehavior {

    private static final double MINECART_RIDABLE_THRESHOLD = 0.01D;
    private static final double MAX_SPEED_IN_WATER = 0.2D;
    private static final double MAX_SPEED_ON_LAND = 0.4D;
    private static final double ABSOLUTE_MAX_SPEED = 0.4D;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private Vec3D targetDeltaMovement;

    public OldMinecartBehavior(EntityMinecartAbstract entityminecartabstract) {
        super(entityminecartabstract);
        this.targetDeltaMovement = Vec3D.ZERO;
    }

    @Override
    public void cancelLerp() {
        this.lerpSteps = 0;
    }

    @Override
    public void lerpTo(double d0, double d1, double d2, float f, float f1, int i) {
        this.lerpX = d0;
        this.lerpY = d1;
        this.lerpZ = d2;
        this.lerpYRot = (double) f;
        this.lerpXRot = (double) f1;
        this.lerpSteps = i + 2;
        this.setDeltaMovement(this.targetDeltaMovement);
    }

    @Override
    public double lerpTargetX() {
        return this.lerpSteps > 0 ? this.lerpX : this.minecart.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.lerpSteps > 0 ? this.lerpY : this.minecart.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.lerpSteps > 0 ? this.lerpZ : this.minecart.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.lerpSteps > 0 ? (float) this.lerpXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.lerpSteps > 0 ? (float) this.lerpYRot : this.getYRot();
    }

    @Override
    public void lerpMotion(double d0, double d1, double d2) {
        this.targetDeltaMovement = new Vec3D(d0, d1, d2);
        this.setDeltaMovement(this.targetDeltaMovement);
    }

    @Override
    public void tick() {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            this.minecart.applyGravity();
            BlockPosition blockposition = this.minecart.getCurrentBlockPosOrRailBelow();
            IBlockData iblockdata = this.level().getBlockState(blockposition);
            boolean flag = BlockMinecartTrackAbstract.isRail(iblockdata);

            this.minecart.setOnRails(flag);
            if (flag) {
                this.moveAlongTrack(worldserver);
                if (iblockdata.is(Blocks.ACTIVATOR_RAIL)) {
                    this.minecart.activateMinecart(blockposition.getX(), blockposition.getY(), blockposition.getZ(), (Boolean) iblockdata.getValue(BlockPoweredRail.POWERED));
                }
            } else {
                this.minecart.comeOffTrack(worldserver);
            }

            this.minecart.applyEffectsFromBlocks();
            this.setXRot(0.0F);
            double d0 = this.minecart.xo - this.getX();
            double d1 = this.minecart.zo - this.getZ();

            if (d0 * d0 + d1 * d1 > 0.001D) {
                this.setYRot((float) (MathHelper.atan2(d1, d0) * 180.0D / Math.PI));
                if (this.minecart.isFlipped()) {
                    this.setYRot(this.getYRot() + 180.0F);
                }
            }

            double d2 = (double) MathHelper.wrapDegrees(this.getYRot() - this.minecart.yRotO);

            if (d2 < -170.0D || d2 >= 170.0D) {
                this.setYRot(this.getYRot() + 180.0F);
                this.minecart.setFlipped(!this.minecart.isFlipped());
            }

            this.setXRot(this.getXRot() % 360.0F);
            this.setYRot(this.getYRot() % 360.0F);
            this.pushAndPickupEntities();
        } else {
            if (this.lerpSteps > 0) {
                this.minecart.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
                --this.lerpSteps;
            } else {
                this.minecart.reapplyPosition();
                this.setXRot(this.getXRot() % 360.0F);
                this.setYRot(this.getYRot() % 360.0F);
            }

        }
    }

    @Override
    public void moveAlongTrack(WorldServer worldserver) {
        BlockPosition blockposition = this.minecart.getCurrentBlockPosOrRailBelow();
        IBlockData iblockdata = this.level().getBlockState(blockposition);

        this.minecart.resetFallDistance();
        double d0 = this.minecart.getX();
        double d1 = this.minecart.getY();
        double d2 = this.minecart.getZ();
        Vec3D vec3d = this.getPos(d0, d1, d2);

        d1 = (double) blockposition.getY();
        boolean flag = false;
        boolean flag1 = false;

        if (iblockdata.is(Blocks.POWERED_RAIL)) {
            flag = (Boolean) iblockdata.getValue(BlockPoweredRail.POWERED);
            flag1 = !flag;
        }

        double d3 = 0.0078125D;

        if (this.minecart.isInWater()) {
            d3 *= 0.2D;
        }

        Vec3D vec3d1 = this.getDeltaMovement();
        BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty());

        switch (blockpropertytrackposition) {
            case ASCENDING_EAST:
                this.setDeltaMovement(vec3d1.add(-d3, 0.0D, 0.0D));
                ++d1;
                break;
            case ASCENDING_WEST:
                this.setDeltaMovement(vec3d1.add(d3, 0.0D, 0.0D));
                ++d1;
                break;
            case ASCENDING_NORTH:
                this.setDeltaMovement(vec3d1.add(0.0D, 0.0D, d3));
                ++d1;
                break;
            case ASCENDING_SOUTH:
                this.setDeltaMovement(vec3d1.add(0.0D, 0.0D, -d3));
                ++d1;
        }

        vec3d1 = this.getDeltaMovement();
        Pair<BaseBlockPosition, BaseBlockPosition> pair = EntityMinecartAbstract.exits(blockpropertytrackposition);
        BaseBlockPosition baseblockposition = (BaseBlockPosition) pair.getFirst();
        BaseBlockPosition baseblockposition1 = (BaseBlockPosition) pair.getSecond();
        double d4 = (double) (baseblockposition1.getX() - baseblockposition.getX());
        double d5 = (double) (baseblockposition1.getZ() - baseblockposition.getZ());
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        double d7 = vec3d1.x * d4 + vec3d1.z * d5;

        if (d7 < 0.0D) {
            d4 = -d4;
            d5 = -d5;
        }

        double d8 = Math.min(2.0D, vec3d1.horizontalDistance());

        vec3d1 = new Vec3D(d8 * d4 / d6, vec3d1.y, d8 * d5 / d6);
        this.setDeltaMovement(vec3d1);
        Entity entity = this.minecart.getFirstPassenger();
        Entity entity1 = this.minecart.getFirstPassenger();
        Vec3D vec3d2;

        if (entity1 instanceof EntityPlayer entityplayer) {
            vec3d2 = entityplayer.getLastClientMoveIntent();
        } else {
            vec3d2 = Vec3D.ZERO;
        }

        if (entity instanceof EntityHuman && vec3d2.lengthSqr() > 0.0D) {
            Vec3D vec3d3 = vec3d2.normalize();
            double d9 = this.getDeltaMovement().horizontalDistanceSqr();

            if (vec3d3.lengthSqr() > 0.0D && d9 < 0.01D) {
                this.setDeltaMovement(this.getDeltaMovement().add(vec3d2.x * 0.001D, 0.0D, vec3d2.z * 0.001D));
                flag1 = false;
            }
        }

        double d10;

        if (flag1) {
            d10 = this.getDeltaMovement().horizontalDistance();
            if (d10 < 0.03D) {
                this.setDeltaMovement(Vec3D.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
            }
        }

        d10 = (double) blockposition.getX() + 0.5D + (double) baseblockposition.getX() * 0.5D;
        double d11 = (double) blockposition.getZ() + 0.5D + (double) baseblockposition.getZ() * 0.5D;
        double d12 = (double) blockposition.getX() + 0.5D + (double) baseblockposition1.getX() * 0.5D;
        double d13 = (double) blockposition.getZ() + 0.5D + (double) baseblockposition1.getZ() * 0.5D;

        d4 = d12 - d10;
        d5 = d13 - d11;
        double d14;
        double d15;
        double d16;

        if (d4 == 0.0D) {
            d14 = d2 - (double) blockposition.getZ();
        } else if (d5 == 0.0D) {
            d14 = d0 - (double) blockposition.getX();
        } else {
            d15 = d0 - d10;
            d16 = d2 - d11;
            d14 = (d15 * d4 + d16 * d5) * 2.0D;
        }

        d0 = d10 + d4 * d14;
        d2 = d11 + d5 * d14;
        this.setPos(d0, d1, d2);
        d15 = this.minecart.isVehicle() ? 0.75D : 1.0D;
        d16 = this.minecart.getMaxSpeed(worldserver);
        vec3d1 = this.getDeltaMovement();
        this.minecart.move(EnumMoveType.SELF, new Vec3D(MathHelper.clamp(d15 * vec3d1.x, -d16, d16), 0.0D, MathHelper.clamp(d15 * vec3d1.z, -d16, d16)));
        if (baseblockposition.getY() != 0 && MathHelper.floor(this.minecart.getX()) - blockposition.getX() == baseblockposition.getX() && MathHelper.floor(this.minecart.getZ()) - blockposition.getZ() == baseblockposition.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double) baseblockposition.getY(), this.minecart.getZ());
        } else if (baseblockposition1.getY() != 0 && MathHelper.floor(this.minecart.getX()) - blockposition.getX() == baseblockposition1.getX() && MathHelper.floor(this.minecart.getZ()) - blockposition.getZ() == baseblockposition1.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double) baseblockposition1.getY(), this.minecart.getZ());
        }

        this.setDeltaMovement(this.minecart.applyNaturalSlowdown(this.getDeltaMovement()));
        Vec3D vec3d4 = this.getPos(this.minecart.getX(), this.minecart.getY(), this.minecart.getZ());
        Vec3D vec3d5;
        double d17;

        if (vec3d4 != null && vec3d != null) {
            double d18 = (vec3d.y - vec3d4.y) * 0.05D;

            vec3d5 = this.getDeltaMovement();
            d17 = vec3d5.horizontalDistance();
            if (d17 > 0.0D) {
                this.setDeltaMovement(vec3d5.multiply((d17 + d18) / d17, 1.0D, (d17 + d18) / d17));
            }

            this.setPos(this.minecart.getX(), vec3d4.y, this.minecart.getZ());
        }

        int i = MathHelper.floor(this.minecart.getX());
        int j = MathHelper.floor(this.minecart.getZ());

        if (i != blockposition.getX() || j != blockposition.getZ()) {
            vec3d5 = this.getDeltaMovement();
            d17 = vec3d5.horizontalDistance();
            this.setDeltaMovement(d17 * (double) (i - blockposition.getX()), vec3d5.y, d17 * (double) (j - blockposition.getZ()));
        }

        if (flag) {
            vec3d5 = this.getDeltaMovement();
            d17 = vec3d5.horizontalDistance();
            if (d17 > 0.01D) {
                double d19 = 0.06D;

                this.setDeltaMovement(vec3d5.add(vec3d5.x / d17 * 0.06D, 0.0D, vec3d5.z / d17 * 0.06D));
            } else {
                Vec3D vec3d6 = this.getDeltaMovement();
                double d20 = vec3d6.x;
                double d21 = vec3d6.z;

                if (blockpropertytrackposition == BlockPropertyTrackPosition.EAST_WEST) {
                    if (this.minecart.isRedstoneConductor(blockposition.west())) {
                        d20 = 0.02D;
                    } else if (this.minecart.isRedstoneConductor(blockposition.east())) {
                        d20 = -0.02D;
                    }
                } else {
                    if (blockpropertytrackposition != BlockPropertyTrackPosition.NORTH_SOUTH) {
                        return;
                    }

                    if (this.minecart.isRedstoneConductor(blockposition.north())) {
                        d21 = 0.02D;
                    } else if (this.minecart.isRedstoneConductor(blockposition.south())) {
                        d21 = -0.02D;
                    }
                }

                this.setDeltaMovement(d20, vec3d6.y, d21);
            }
        }

    }

    @Nullable
    public Vec3D getPosOffs(double d0, double d1, double d2, double d3) {
        int i = MathHelper.floor(d0);
        int j = MathHelper.floor(d1);
        int k = MathHelper.floor(d2);

        if (this.level().getBlockState(new BlockPosition(i, j - 1, k)).is(TagsBlock.RAILS)) {
            --j;
        }

        IBlockData iblockdata = this.level().getBlockState(new BlockPosition(i, j, k));

        if (BlockMinecartTrackAbstract.isRail(iblockdata)) {
            BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty());

            d1 = (double) j;
            if (blockpropertytrackposition.isSlope()) {
                d1 = (double) (j + 1);
            }

            Pair<BaseBlockPosition, BaseBlockPosition> pair = EntityMinecartAbstract.exits(blockpropertytrackposition);
            BaseBlockPosition baseblockposition = (BaseBlockPosition) pair.getFirst();
            BaseBlockPosition baseblockposition1 = (BaseBlockPosition) pair.getSecond();
            double d4 = (double) (baseblockposition1.getX() - baseblockposition.getX());
            double d5 = (double) (baseblockposition1.getZ() - baseblockposition.getZ());
            double d6 = Math.sqrt(d4 * d4 + d5 * d5);

            d4 /= d6;
            d5 /= d6;
            d0 += d4 * d3;
            d2 += d5 * d3;
            if (baseblockposition.getY() != 0 && MathHelper.floor(d0) - i == baseblockposition.getX() && MathHelper.floor(d2) - k == baseblockposition.getZ()) {
                d1 += (double) baseblockposition.getY();
            } else if (baseblockposition1.getY() != 0 && MathHelper.floor(d0) - i == baseblockposition1.getX() && MathHelper.floor(d2) - k == baseblockposition1.getZ()) {
                d1 += (double) baseblockposition1.getY();
            }

            return this.getPos(d0, d1, d2);
        } else {
            return null;
        }
    }

    @Nullable
    public Vec3D getPos(double d0, double d1, double d2) {
        int i = MathHelper.floor(d0);
        int j = MathHelper.floor(d1);
        int k = MathHelper.floor(d2);

        if (this.level().getBlockState(new BlockPosition(i, j - 1, k)).is(TagsBlock.RAILS)) {
            --j;
        }

        IBlockData iblockdata = this.level().getBlockState(new BlockPosition(i, j, k));

        if (BlockMinecartTrackAbstract.isRail(iblockdata)) {
            BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty());
            Pair<BaseBlockPosition, BaseBlockPosition> pair = EntityMinecartAbstract.exits(blockpropertytrackposition);
            BaseBlockPosition baseblockposition = (BaseBlockPosition) pair.getFirst();
            BaseBlockPosition baseblockposition1 = (BaseBlockPosition) pair.getSecond();
            double d3 = (double) i + 0.5D + (double) baseblockposition.getX() * 0.5D;
            double d4 = (double) j + 0.0625D + (double) baseblockposition.getY() * 0.5D;
            double d5 = (double) k + 0.5D + (double) baseblockposition.getZ() * 0.5D;
            double d6 = (double) i + 0.5D + (double) baseblockposition1.getX() * 0.5D;
            double d7 = (double) j + 0.0625D + (double) baseblockposition1.getY() * 0.5D;
            double d8 = (double) k + 0.5D + (double) baseblockposition1.getZ() * 0.5D;
            double d9 = d6 - d3;
            double d10 = (d7 - d4) * 2.0D;
            double d11 = d8 - d5;
            double d12;

            if (d9 == 0.0D) {
                d12 = d2 - (double) k;
            } else if (d11 == 0.0D) {
                d12 = d0 - (double) i;
            } else {
                double d13 = d0 - d3;
                double d14 = d2 - d5;

                d12 = (d13 * d9 + d14 * d11) * 2.0D;
            }

            d0 = d3 + d9 * d12;
            d1 = d4 + d10 * d12;
            d2 = d5 + d11 * d12;
            if (d10 < 0.0D) {
                ++d1;
            } else if (d10 > 0.0D) {
                d1 += 0.5D;
            }

            return new Vec3D(d0, d1, d2);
        } else {
            return null;
        }
    }

    @Override
    public double stepAlongTrack(BlockPosition blockposition, BlockPropertyTrackPosition blockpropertytrackposition, double d0) {
        return 0.0D;
    }

    @Override
    public boolean pushAndPickupEntities() {
        AxisAlignedBB axisalignedbb = this.minecart.getBoundingBox().inflate(0.20000000298023224D, 0.0D, 0.20000000298023224D);

        if (this.minecart.isRideable() && this.getDeltaMovement().horizontalDistanceSqr() >= 0.01D) {
            List<Entity> list = this.level().getEntities((Entity) this.minecart, axisalignedbb, IEntitySelector.pushableBy(this.minecart));

            if (!list.isEmpty()) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();

                    if (!(entity instanceof EntityHuman) && !(entity instanceof EntityIronGolem) && !(entity instanceof EntityMinecartAbstract) && !this.minecart.isVehicle() && !entity.isPassenger()) {
                        entity.startRiding(this.minecart);
                    } else {
                        entity.push((Entity) this.minecart);
                    }
                }
            }
        } else {
            Iterator iterator1 = this.level().getEntities(this.minecart, axisalignedbb).iterator();

            while (iterator1.hasNext()) {
                Entity entity1 = (Entity) iterator1.next();

                if (!this.minecart.hasPassenger(entity1) && entity1.isPushable() && entity1 instanceof EntityMinecartAbstract) {
                    entity1.push((Entity) this.minecart);
                }
            }
        }

        return false;
    }

    @Override
    public EnumDirection getMotionDirection() {
        return this.minecart.isFlipped() ? this.minecart.getDirection().getOpposite().getClockWise() : this.minecart.getDirection().getClockWise();
    }

    @Override
    public Vec3D getKnownMovement(Vec3D vec3d) {
        return new Vec3D(MathHelper.clamp(vec3d.x, -0.4D, 0.4D), vec3d.y, MathHelper.clamp(vec3d.z, -0.4D, 0.4D));
    }

    @Override
    public double getMaxSpeed(WorldServer worldserver) {
        return this.minecart.isInWater() ? 0.2D : 0.4D;
    }

    @Override
    public double getSlowdownFactor() {
        return this.minecart.isVehicle() ? 0.997D : 0.96D;
    }
}
