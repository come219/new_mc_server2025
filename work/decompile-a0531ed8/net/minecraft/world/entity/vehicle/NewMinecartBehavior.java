package net.minecraft.world.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockMinecartTrackAbstract;
import net.minecraft.world.level.block.BlockPoweredRail;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class NewMinecartBehavior extends MinecartBehavior {

    public static final int POS_ROT_LERP_TICKS = 3;
    public static final double ON_RAIL_Y_OFFSET = 0.1D;
    public static final double OPPOSING_SLOPES_REST_AT_SPEED_THRESHOLD = 0.005D;
    @Nullable
    private NewMinecartBehavior.b cacheIndexAlpha;
    private int cachedLerpDelay;
    private float cachedPartialTick;
    private int lerpDelay = 0;
    public final List<NewMinecartBehavior.a> lerpSteps = new LinkedList();
    public final List<NewMinecartBehavior.a> currentLerpSteps = new LinkedList();
    public double currentLerpStepsTotalWeight = 0.0D;
    public NewMinecartBehavior.a oldLerp;

    public NewMinecartBehavior(EntityMinecartAbstract entityminecartabstract) {
        super(entityminecartabstract);
        this.oldLerp = NewMinecartBehavior.a.ZERO;
    }

    @Override
    public void tick() {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            BlockPosition blockposition = this.minecart.getCurrentBlockPosOrRailBelow();
            IBlockData iblockdata = this.level().getBlockState(blockposition);

            if (this.minecart.isFirstTick()) {
                this.minecart.setOnRails(BlockMinecartTrackAbstract.isRail(iblockdata));
                this.adjustToRails(blockposition, iblockdata, true);
            }

            this.minecart.applyGravity();
            this.minecart.moveAlongTrack(worldserver);
        } else {
            this.lerpClientPositionAndRotation();
            boolean flag = BlockMinecartTrackAbstract.isRail(this.level().getBlockState(this.minecart.getCurrentBlockPosOrRailBelow()));

            this.minecart.setOnRails(flag);
        }
    }

    private void lerpClientPositionAndRotation() {
        if (--this.lerpDelay <= 0) {
            this.setOldLerpValues();
            this.currentLerpSteps.clear();
            if (!this.lerpSteps.isEmpty()) {
                this.currentLerpSteps.addAll(this.lerpSteps);
                this.lerpSteps.clear();
                this.currentLerpStepsTotalWeight = 0.0D;

                NewMinecartBehavior.a newminecartbehavior_a;

                for (Iterator iterator = this.currentLerpSteps.iterator(); iterator.hasNext(); this.currentLerpStepsTotalWeight += (double) newminecartbehavior_a.weight) {
                    newminecartbehavior_a = (NewMinecartBehavior.a) iterator.next();
                }

                this.lerpDelay = this.currentLerpStepsTotalWeight == 0.0D ? 0 : 3;
            }
        }

        if (this.cartHasPosRotLerp()) {
            this.setPos(this.getCartLerpPosition(1.0F));
            this.setDeltaMovement(this.getCartLerpMovements(1.0F));
            this.setXRot(this.getCartLerpXRot(1.0F));
            this.setYRot(this.getCartLerpYRot(1.0F));
        }

    }

    public void setOldLerpValues() {
        this.oldLerp = new NewMinecartBehavior.a(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), 0.0F);
    }

    public boolean cartHasPosRotLerp() {
        return !this.currentLerpSteps.isEmpty();
    }

    public float getCartLerpXRot(float f) {
        NewMinecartBehavior.b newminecartbehavior_b = this.getCurrentLerpStep(f);

        return MathHelper.rotLerp(newminecartbehavior_b.partialTicksInStep, newminecartbehavior_b.previousStep.xRot, newminecartbehavior_b.currentStep.xRot);
    }

    public float getCartLerpYRot(float f) {
        NewMinecartBehavior.b newminecartbehavior_b = this.getCurrentLerpStep(f);

        return MathHelper.rotLerp(newminecartbehavior_b.partialTicksInStep, newminecartbehavior_b.previousStep.yRot, newminecartbehavior_b.currentStep.yRot);
    }

    public Vec3D getCartLerpPosition(float f) {
        NewMinecartBehavior.b newminecartbehavior_b = this.getCurrentLerpStep(f);

        return MathHelper.lerp((double) newminecartbehavior_b.partialTicksInStep, newminecartbehavior_b.previousStep.position, newminecartbehavior_b.currentStep.position);
    }

    public Vec3D getCartLerpMovements(float f) {
        NewMinecartBehavior.b newminecartbehavior_b = this.getCurrentLerpStep(f);

        return MathHelper.lerp((double) newminecartbehavior_b.partialTicksInStep, newminecartbehavior_b.previousStep.movement, newminecartbehavior_b.currentStep.movement);
    }

    private NewMinecartBehavior.b getCurrentLerpStep(float f) {
        if (f == this.cachedPartialTick && this.lerpDelay == this.cachedLerpDelay && this.cacheIndexAlpha != null) {
            return this.cacheIndexAlpha;
        } else {
            float f1 = ((float) (3 - this.lerpDelay) + f) / 3.0F;
            float f2 = 0.0F;
            float f3 = 1.0F;
            boolean flag = false;

            int i;

            for (i = 0; i < this.currentLerpSteps.size(); ++i) {
                float f4 = ((NewMinecartBehavior.a) this.currentLerpSteps.get(i)).weight;

                if (f4 > 0.0F) {
                    f2 += f4;
                    if ((double) f2 >= this.currentLerpStepsTotalWeight * (double) f1) {
                        float f5 = f2 - f4;

                        f3 = (float) (((double) f1 * this.currentLerpStepsTotalWeight - (double) f5) / (double) f4);
                        flag = true;
                        break;
                    }
                }
            }

            if (!flag) {
                i = this.currentLerpSteps.size() - 1;
            }

            NewMinecartBehavior.a newminecartbehavior_a = (NewMinecartBehavior.a) this.currentLerpSteps.get(i);
            NewMinecartBehavior.a newminecartbehavior_a1 = i > 0 ? (NewMinecartBehavior.a) this.currentLerpSteps.get(i - 1) : this.oldLerp;

            this.cacheIndexAlpha = new NewMinecartBehavior.b(f3, newminecartbehavior_a, newminecartbehavior_a1);
            this.cachedLerpDelay = this.lerpDelay;
            this.cachedPartialTick = f;
            return this.cacheIndexAlpha;
        }
    }

    public void adjustToRails(BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        if (BlockMinecartTrackAbstract.isRail(iblockdata)) {
            BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty());
            Pair<BaseBlockPosition, BaseBlockPosition> pair = EntityMinecartAbstract.exits(blockpropertytrackposition);
            Vec3D vec3d = (new Vec3D((BaseBlockPosition) pair.getFirst())).scale(0.5D);
            Vec3D vec3d1 = (new Vec3D((BaseBlockPosition) pair.getSecond())).scale(0.5D);
            Vec3D vec3d2 = vec3d.horizontal();
            Vec3D vec3d3 = vec3d1.horizontal();

            if (this.getDeltaMovement().length() > 9.999999747378752E-6D && this.getDeltaMovement().dot(vec3d2) < this.getDeltaMovement().dot(vec3d3) || this.isDecending(vec3d3, blockpropertytrackposition)) {
                Vec3D vec3d4 = vec3d2;

                vec3d2 = vec3d3;
                vec3d3 = vec3d4;
            }

            float f = 180.0F - (float) (Math.atan2(vec3d2.z, vec3d2.x) * 180.0D / Math.PI);

            f += this.minecart.isFlipped() ? 180.0F : 0.0F;
            Vec3D vec3d5 = this.position();
            boolean flag1 = vec3d.x() != vec3d1.x() && vec3d.z() != vec3d1.z();
            Vec3D vec3d6;
            Vec3D vec3d7;

            if (flag1) {
                vec3d6 = vec3d1.subtract(vec3d);
                Vec3D vec3d8 = vec3d5.subtract(blockposition.getBottomCenter()).subtract(vec3d);
                Vec3D vec3d9 = vec3d6.scale(vec3d6.dot(vec3d8) / vec3d6.dot(vec3d6));

                vec3d7 = blockposition.getBottomCenter().add(vec3d).add(vec3d9);
                f = 180.0F - (float) (Math.atan2(vec3d9.z, vec3d9.x) * 180.0D / Math.PI);
                f += this.minecart.isFlipped() ? 180.0F : 0.0F;
            } else {
                boolean flag2 = vec3d.subtract(vec3d1).x != 0.0D;
                boolean flag3 = vec3d.subtract(vec3d1).z != 0.0D;

                vec3d7 = new Vec3D(flag3 ? blockposition.getCenter().x : vec3d5.x, (double) blockposition.getY(), flag2 ? blockposition.getCenter().z : vec3d5.z);
            }

            vec3d6 = vec3d7.subtract(vec3d5);
            this.setPos(vec3d5.add(vec3d6));
            float f1 = 0.0F;
            boolean flag4 = vec3d.y() != vec3d1.y();

            if (flag4) {
                Vec3D vec3d10 = blockposition.getBottomCenter().add(vec3d3);
                double d0 = vec3d10.distanceTo(this.position());

                this.setPos(this.position().add(0.0D, d0 + 0.1D, 0.0D));
                f1 = this.minecart.isFlipped() ? 45.0F : -45.0F;
            } else {
                this.setPos(this.position().add(0.0D, 0.1D, 0.0D));
            }

            this.setRotation(f, f1);
            double d1 = vec3d5.distanceTo(this.position());

            if (d1 > 0.0D) {
                this.lerpSteps.add(new NewMinecartBehavior.a(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), flag ? 0.0F : (float) d1));
            }

        }
    }

    private void setRotation(float f, float f1) {
        double d0 = (double) Math.abs(f - this.getYRot());

        if (d0 >= 175.0D && d0 <= 185.0D) {
            this.minecart.setFlipped(!this.minecart.isFlipped());
            f -= 180.0F;
            f1 *= -1.0F;
        }

        f1 = Math.clamp(f1, -45.0F, 45.0F);
        this.setXRot(f1 % 360.0F);
        this.setYRot(f % 360.0F);
    }

    @Override
    public void moveAlongTrack(WorldServer worldserver) {
        for (NewMinecartBehavior.c newminecartbehavior_c = new NewMinecartBehavior.c(); newminecartbehavior_c.shouldIterate() && this.minecart.isAlive(); newminecartbehavior_c.firstIteration = false) {
            Vec3D vec3d = this.getDeltaMovement();
            BlockPosition blockposition = this.minecart.getCurrentBlockPosOrRailBelow();
            IBlockData iblockdata = this.level().getBlockState(blockposition);
            boolean flag = BlockMinecartTrackAbstract.isRail(iblockdata);

            if (this.minecart.isOnRails() != flag) {
                this.minecart.setOnRails(flag);
                this.adjustToRails(blockposition, iblockdata, false);
            }

            Vec3D vec3d1;

            if (flag) {
                this.minecart.resetFallDistance();
                this.minecart.setOldPosAndRot();
                if (iblockdata.is(Blocks.ACTIVATOR_RAIL)) {
                    this.minecart.activateMinecart(blockposition.getX(), blockposition.getY(), blockposition.getZ(), (Boolean) iblockdata.getValue(BlockPoweredRail.POWERED));
                }

                BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty());

                vec3d1 = this.calculateTrackSpeed(worldserver, vec3d.horizontal(), newminecartbehavior_c, blockposition, iblockdata, blockpropertytrackposition);
                if (newminecartbehavior_c.firstIteration) {
                    newminecartbehavior_c.movementLeft = vec3d1.horizontalDistance();
                } else {
                    newminecartbehavior_c.movementLeft += vec3d1.horizontalDistance() - vec3d.horizontalDistance();
                }

                this.setDeltaMovement(vec3d1);
                newminecartbehavior_c.movementLeft = this.minecart.makeStepAlongTrack(blockposition, blockpropertytrackposition, newminecartbehavior_c.movementLeft);
            } else {
                this.minecart.comeOffTrack(worldserver);
                newminecartbehavior_c.movementLeft = 0.0D;
            }

            Vec3D vec3d2 = this.position();

            vec3d1 = vec3d2.subtract(this.minecart.oldPosition());
            double d0 = vec3d1.length();

            if (d0 > 9.999999747378752E-6D) {
                if (vec3d1.horizontalDistanceSqr() > 9.999999747378752E-6D) {
                    float f = 180.0F - (float) (Math.atan2(vec3d1.z, vec3d1.x) * 180.0D / Math.PI);
                    float f1 = this.minecart.onGround() && !this.minecart.isOnRails() ? 0.0F : 90.0F - (float) (Math.atan2(vec3d1.horizontalDistance(), vec3d1.y) * 180.0D / Math.PI);

                    f += this.minecart.isFlipped() ? 180.0F : 0.0F;
                    f1 *= this.minecart.isFlipped() ? -1.0F : 1.0F;
                    this.setRotation(f, f1);
                } else if (!this.minecart.isOnRails()) {
                    this.setXRot(this.minecart.onGround() ? 0.0F : MathHelper.rotLerp(0.2F, this.getXRot(), 0.0F));
                }

                this.lerpSteps.add(new NewMinecartBehavior.a(vec3d2, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float) Math.min(d0, this.getMaxSpeed(worldserver))));
            } else if (vec3d.horizontalDistanceSqr() > 0.0D) {
                this.lerpSteps.add(new NewMinecartBehavior.a(vec3d2, this.getDeltaMovement(), this.getYRot(), this.getXRot(), 1.0F));
            }

            if (d0 > 9.999999747378752E-6D || newminecartbehavior_c.firstIteration) {
                this.minecart.applyEffectsFromBlocks();
                this.minecart.applyEffectsFromBlocks();
            }
        }

    }

    private Vec3D calculateTrackSpeed(WorldServer worldserver, Vec3D vec3d, NewMinecartBehavior.c newminecartbehavior_c, BlockPosition blockposition, IBlockData iblockdata, BlockPropertyTrackPosition blockpropertytrackposition) {
        Vec3D vec3d1 = vec3d;
        Vec3D vec3d2;

        if (!newminecartbehavior_c.hasGainedSlopeSpeed) {
            vec3d2 = this.calculateSlopeSpeed(vec3d, blockpropertytrackposition);
            if (vec3d2.horizontalDistanceSqr() != vec3d.horizontalDistanceSqr()) {
                newminecartbehavior_c.hasGainedSlopeSpeed = true;
                vec3d1 = vec3d2;
            }
        }

        if (newminecartbehavior_c.firstIteration) {
            vec3d2 = this.calculatePlayerInputSpeed(vec3d1);
            if (vec3d2.horizontalDistanceSqr() != vec3d1.horizontalDistanceSqr()) {
                newminecartbehavior_c.hasHalted = true;
                vec3d1 = vec3d2;
            }
        }

        if (!newminecartbehavior_c.hasHalted) {
            vec3d2 = this.calculateHaltTrackSpeed(vec3d1, iblockdata);
            if (vec3d2.horizontalDistanceSqr() != vec3d1.horizontalDistanceSqr()) {
                newminecartbehavior_c.hasHalted = true;
                vec3d1 = vec3d2;
            }
        }

        if (newminecartbehavior_c.firstIteration) {
            vec3d1 = this.minecart.applyNaturalSlowdown(vec3d1);
            if (vec3d1.lengthSqr() > 0.0D) {
                double d0 = Math.min(vec3d1.length(), this.minecart.getMaxSpeed(worldserver));

                vec3d1 = vec3d1.normalize().scale(d0);
            }
        }

        if (!newminecartbehavior_c.hasBoosted) {
            vec3d2 = this.calculateBoostTrackSpeed(vec3d1, blockposition, iblockdata);
            if (vec3d2.horizontalDistanceSqr() != vec3d1.horizontalDistanceSqr()) {
                newminecartbehavior_c.hasBoosted = true;
                vec3d1 = vec3d2;
            }
        }

        return vec3d1;
    }

    private Vec3D calculateSlopeSpeed(Vec3D vec3d, BlockPropertyTrackPosition blockpropertytrackposition) {
        double d0 = Math.max(0.0078125D, vec3d.horizontalDistance() * 0.02D);

        if (this.minecart.isInWater()) {
            d0 *= 0.2D;
        }

        Vec3D vec3d1;

        switch (blockpropertytrackposition) {
            case ASCENDING_EAST:
                vec3d1 = vec3d.add(-d0, 0.0D, 0.0D);
                break;
            case ASCENDING_WEST:
                vec3d1 = vec3d.add(d0, 0.0D, 0.0D);
                break;
            case ASCENDING_NORTH:
                vec3d1 = vec3d.add(0.0D, 0.0D, d0);
                break;
            case ASCENDING_SOUTH:
                vec3d1 = vec3d.add(0.0D, 0.0D, -d0);
                break;
            default:
                vec3d1 = vec3d;
        }

        return vec3d1;
    }

    private Vec3D calculatePlayerInputSpeed(Vec3D vec3d) {
        Entity entity = this.minecart.getFirstPassenger();

        if (entity instanceof EntityPlayer entityplayer) {
            Vec3D vec3d1 = entityplayer.getLastClientMoveIntent();

            if (vec3d1.lengthSqr() > 0.0D) {
                Vec3D vec3d2 = vec3d1.normalize();
                double d0 = vec3d.horizontalDistanceSqr();

                if (vec3d2.lengthSqr() > 0.0D && d0 < 0.01D) {
                    return vec3d.add((new Vec3D(vec3d2.x, 0.0D, vec3d2.z)).normalize().scale(0.001D));
                }
            }

            return vec3d;
        } else {
            return vec3d;
        }
    }

    private Vec3D calculateHaltTrackSpeed(Vec3D vec3d, IBlockData iblockdata) {
        return iblockdata.is(Blocks.POWERED_RAIL) && !(Boolean) iblockdata.getValue(BlockPoweredRail.POWERED) ? (vec3d.length() < 0.03D ? Vec3D.ZERO : vec3d.scale(0.5D)) : vec3d;
    }

    private Vec3D calculateBoostTrackSpeed(Vec3D vec3d, BlockPosition blockposition, IBlockData iblockdata) {
        if (iblockdata.is(Blocks.POWERED_RAIL) && (Boolean) iblockdata.getValue(BlockPoweredRail.POWERED)) {
            if (vec3d.length() > 0.01D) {
                return vec3d.normalize().scale(vec3d.length() + 0.06D);
            } else {
                Vec3D vec3d1 = this.minecart.getRedstoneDirection(blockposition);

                return vec3d1.lengthSqr() <= 0.0D ? vec3d : vec3d1.scale(vec3d.length() + 0.2D);
            }
        } else {
            return vec3d;
        }
    }

    @Override
    public double stepAlongTrack(BlockPosition blockposition, BlockPropertyTrackPosition blockpropertytrackposition, double d0) {
        if (d0 < 9.999999747378752E-6D) {
            return 0.0D;
        } else {
            Vec3D vec3d = this.position();
            Pair<BaseBlockPosition, BaseBlockPosition> pair = EntityMinecartAbstract.exits(blockpropertytrackposition);
            BaseBlockPosition baseblockposition = (BaseBlockPosition) pair.getFirst();
            BaseBlockPosition baseblockposition1 = (BaseBlockPosition) pair.getSecond();
            Vec3D vec3d1 = this.getDeltaMovement().horizontal();

            if (vec3d1.length() < 9.999999747378752E-6D) {
                this.setDeltaMovement(Vec3D.ZERO);
                return 0.0D;
            } else {
                boolean flag = baseblockposition.getY() != baseblockposition1.getY();
                Vec3D vec3d2 = (new Vec3D(baseblockposition1)).scale(0.5D).horizontal();
                Vec3D vec3d3 = (new Vec3D(baseblockposition)).scale(0.5D).horizontal();

                if (vec3d1.dot(vec3d3) < vec3d1.dot(vec3d2)) {
                    vec3d3 = vec3d2;
                }

                Vec3D vec3d4 = blockposition.getBottomCenter().add(vec3d3).add(0.0D, 0.1D, 0.0D).add(vec3d3.normalize().scale(9.999999747378752E-6D));

                if (flag && !this.isDecending(vec3d1, blockpropertytrackposition)) {
                    vec3d4 = vec3d4.add(0.0D, 1.0D, 0.0D);
                }

                Vec3D vec3d5 = vec3d4.subtract(this.position()).normalize();

                vec3d1 = vec3d5.scale(vec3d1.length() / vec3d5.horizontalDistance());
                Vec3D vec3d6 = vec3d.add(vec3d1.normalize().scale(d0 * (double) (flag ? MathHelper.SQRT_OF_TWO : 1.0F)));

                if (vec3d.distanceToSqr(vec3d4) <= vec3d.distanceToSqr(vec3d6)) {
                    d0 = vec3d4.subtract(vec3d6).horizontalDistance();
                    vec3d6 = vec3d4;
                } else {
                    d0 = 0.0D;
                }

                this.minecart.move(EnumMoveType.SELF, vec3d6.subtract(vec3d));
                IBlockData iblockdata = this.level().getBlockState(BlockPosition.containing(vec3d6));

                if (flag) {
                    if (BlockMinecartTrackAbstract.isRail(iblockdata)) {
                        BlockPropertyTrackPosition blockpropertytrackposition1 = (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty());

                        if (this.restAtVShape(blockpropertytrackposition, blockpropertytrackposition1)) {
                            return 0.0D;
                        }
                    }

                    double d1 = vec3d4.horizontal().distanceTo(this.position().horizontal());
                    double d2 = vec3d4.y + (this.isDecending(vec3d1, blockpropertytrackposition) ? d1 : -d1);

                    if (this.position().y < d2) {
                        this.setPos(this.position().x, d2, this.position().z);
                    }
                }

                if (this.position().distanceTo(vec3d) < 9.999999747378752E-6D && vec3d6.distanceTo(vec3d) > 9.999999747378752E-6D) {
                    this.setDeltaMovement(Vec3D.ZERO);
                    return 0.0D;
                } else {
                    this.setDeltaMovement(vec3d1);
                    return d0;
                }
            }
        }
    }

    private boolean restAtVShape(BlockPropertyTrackPosition blockpropertytrackposition, BlockPropertyTrackPosition blockpropertytrackposition1) {
        if (this.getDeltaMovement().lengthSqr() < 0.005D && blockpropertytrackposition1.isSlope() && this.isDecending(this.getDeltaMovement(), blockpropertytrackposition) && !this.isDecending(this.getDeltaMovement(), blockpropertytrackposition1)) {
            this.setDeltaMovement(Vec3D.ZERO);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public double getMaxSpeed(WorldServer worldserver) {
        return (double) worldserver.getGameRules().getInt(GameRules.RULE_MINECART_MAX_SPEED) * (this.minecart.isInWater() ? 0.5D : 1.0D) / 20.0D;
    }

    private boolean isDecending(Vec3D vec3d, BlockPropertyTrackPosition blockpropertytrackposition) {
        boolean flag;

        switch (blockpropertytrackposition) {
            case ASCENDING_EAST:
                flag = vec3d.x < 0.0D;
                break;
            case ASCENDING_WEST:
                flag = vec3d.x > 0.0D;
                break;
            case ASCENDING_NORTH:
                flag = vec3d.z > 0.0D;
                break;
            case ASCENDING_SOUTH:
                flag = vec3d.z < 0.0D;
                break;
            default:
                flag = false;
        }

        return flag;
    }

    @Override
    public double getSlowdownFactor() {
        return this.minecart.isVehicle() ? 0.997D : 0.975D;
    }

    @Override
    public boolean pushAndPickupEntities() {
        boolean flag = this.pickupEntities(this.minecart.getBoundingBox().inflate(0.2D, 0.0D, 0.2D));

        if (!this.minecart.horizontalCollision && !this.minecart.verticalCollision) {
            return false;
        } else {
            boolean flag1 = this.pushEntities(this.minecart.getBoundingBox().inflate(1.0E-7D));

            return flag && !flag1;
        }
    }

    public boolean pickupEntities(AxisAlignedBB axisalignedbb) {
        if (this.minecart.isRideable() && !this.minecart.isVehicle()) {
            List<Entity> list = this.level().getEntities((Entity) this.minecart, axisalignedbb, IEntitySelector.pushableBy(this.minecart));

            if (!list.isEmpty()) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();

                    if (!(entity instanceof EntityHuman) && !(entity instanceof EntityIronGolem) && !(entity instanceof EntityMinecartAbstract) && !this.minecart.isVehicle() && !entity.isPassenger()) {
                        boolean flag = entity.startRiding(this.minecart);

                        if (flag) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean pushEntities(AxisAlignedBB axisalignedbb) {
        boolean flag = false;

        if (this.minecart.isRideable()) {
            List<Entity> list = this.level().getEntities((Entity) this.minecart, axisalignedbb, IEntitySelector.pushableBy(this.minecart));

            if (!list.isEmpty()) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();

                    if (entity instanceof EntityHuman || entity instanceof EntityIronGolem || entity instanceof EntityMinecartAbstract || this.minecart.isVehicle() || entity.isPassenger()) {
                        entity.push((Entity) this.minecart);
                        flag = true;
                    }
                }
            }
        } else {
            Iterator iterator1 = this.level().getEntities(this.minecart, axisalignedbb).iterator();

            while (iterator1.hasNext()) {
                Entity entity1 = (Entity) iterator1.next();

                if (!this.minecart.hasPassenger(entity1) && entity1.isPushable() && entity1 instanceof EntityMinecartAbstract) {
                    entity1.push((Entity) this.minecart);
                    flag = true;
                }
            }
        }

        return flag;
    }

    public static record a(Vec3D position, Vec3D movement, float yRot, float xRot, float weight) {

        public static final StreamCodec<ByteBuf, NewMinecartBehavior.a> STREAM_CODEC = StreamCodec.composite(Vec3D.STREAM_CODEC, NewMinecartBehavior.a::position, Vec3D.STREAM_CODEC, NewMinecartBehavior.a::movement, ByteBufCodecs.ROTATION_BYTE, NewMinecartBehavior.a::yRot, ByteBufCodecs.ROTATION_BYTE, NewMinecartBehavior.a::xRot, ByteBufCodecs.FLOAT, NewMinecartBehavior.a::weight, NewMinecartBehavior.a::new);
        public static NewMinecartBehavior.a ZERO = new NewMinecartBehavior.a(Vec3D.ZERO, Vec3D.ZERO, 0.0F, 0.0F, 0.0F);
    }

    private static record b(float partialTicksInStep, NewMinecartBehavior.a currentStep, NewMinecartBehavior.a previousStep) {

    }

    private static class c {

        double movementLeft = 0.0D;
        boolean firstIteration = true;
        boolean hasGainedSlopeSpeed = false;
        boolean hasHalted = false;
        boolean hasBoosted = false;

        c() {}

        public boolean shouldIterate() {
            return this.firstIteration || this.movementLeft > 9.999999747378752E-6D;
        }
    }
}
