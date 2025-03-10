package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.raid.EntityRaider;

public class PathfinderGoalNearestHealableRaider<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {

    private static final int DEFAULT_COOLDOWN = 200;
    private int cooldown = 0;

    public PathfinderGoalNearestHealableRaider(EntityRaider entityraider, Class<T> oclass, boolean flag, @Nullable PathfinderTargetCondition.a pathfindertargetcondition_a) {
        super(entityraider, oclass, 500, flag, false, pathfindertargetcondition_a);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void decrementCooldown() {
        --this.cooldown;
    }

    @Override
    public boolean canUse() {
        if (this.cooldown <= 0 && this.mob.getRandom().nextBoolean()) {
            if (!((EntityRaider) this.mob).hasActiveRaid()) {
                return false;
            } else {
                this.findTarget();
                return this.target != null;
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.cooldown = reducedTickDelay(200);
        super.start();
    }
}
