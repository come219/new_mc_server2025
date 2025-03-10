package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.raid.EntityRaider;

public class PathfinderGoalNearestAttackableTargetWitch<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {

    private boolean canAttack = true;

    public PathfinderGoalNearestAttackableTargetWitch(EntityRaider entityraider, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable PathfinderTargetCondition.a pathfindertargetcondition_a) {
        super(entityraider, oclass, i, flag, flag1, pathfindertargetcondition_a);
    }

    public void setCanAttack(boolean flag) {
        this.canAttack = flag;
    }

    @Override
    public boolean canUse() {
        return this.canAttack && super.canUse();
    }
}
