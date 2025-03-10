package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.phys.AxisAlignedBB;

public class PathfinderGoalNearestAttackableTarget<T extends EntityLiving> extends PathfinderGoalTarget {

    private static final int DEFAULT_RANDOM_INTERVAL = 10;
    protected final Class<T> targetType;
    protected final int randomInterval;
    @Nullable
    protected EntityLiving target;
    protected PathfinderTargetCondition targetConditions;

    public PathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, boolean flag) {
        this(entityinsentient, oclass, 10, flag, false, (PathfinderTargetCondition.a) null);
    }

    public PathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, boolean flag, PathfinderTargetCondition.a pathfindertargetcondition_a) {
        this(entityinsentient, oclass, 10, flag, false, pathfindertargetcondition_a);
    }

    public PathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, boolean flag, boolean flag1) {
        this(entityinsentient, oclass, 10, flag, flag1, (PathfinderTargetCondition.a) null);
    }

    public PathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable PathfinderTargetCondition.a pathfindertargetcondition_a) {
        super(entityinsentient, flag, flag1);
        this.targetType = oclass;
        this.randomInterval = reducedTickDelay(i);
        this.setFlags(EnumSet.of(PathfinderGoal.Type.TARGET));
        this.targetConditions = PathfinderTargetCondition.forCombat().range(this.getFollowDistance()).selector(pathfindertargetcondition_a);
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }
    }

    protected AxisAlignedBB getTargetSearchArea(double d0) {
        return this.mob.getBoundingBox().inflate(d0, d0, d0);
    }

    protected void findTarget() {
        WorldServer worldserver = getServerLevel((Entity) this.mob);

        if (this.targetType != EntityHuman.class && this.targetType != EntityPlayer.class) {
            this.target = worldserver.getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (entityliving) -> {
                return true;
            }), this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            this.target = worldserver.getNearestPlayer(this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable EntityLiving entityliving) {
        this.target = entityliving;
    }

    private PathfinderTargetCondition getTargetConditions() {
        return this.targetConditions.range(this.getFollowDistance());
    }
}
