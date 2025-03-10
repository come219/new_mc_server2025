package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.IEntityAccess;
import net.minecraft.world.phys.AxisAlignedBB;

public interface ServerEntityGetter extends IEntityAccess {

    WorldServer getLevel();

    @Nullable
    default EntityHuman getNearestPlayer(PathfinderTargetCondition pathfindertargetcondition, EntityLiving entityliving) {
        return (EntityHuman) this.getNearestEntity(this.players(), pathfindertargetcondition, entityliving, entityliving.getX(), entityliving.getY(), entityliving.getZ());
    }

    @Nullable
    default EntityHuman getNearestPlayer(PathfinderTargetCondition pathfindertargetcondition, EntityLiving entityliving, double d0, double d1, double d2) {
        return (EntityHuman) this.getNearestEntity(this.players(), pathfindertargetcondition, entityliving, d0, d1, d2);
    }

    @Nullable
    default EntityHuman getNearestPlayer(PathfinderTargetCondition pathfindertargetcondition, double d0, double d1, double d2) {
        return (EntityHuman) this.getNearestEntity(this.players(), pathfindertargetcondition, (EntityLiving) null, d0, d1, d2);
    }

    @Nullable
    default <T extends EntityLiving> T getNearestEntity(Class<? extends T> oclass, PathfinderTargetCondition pathfindertargetcondition, @Nullable EntityLiving entityliving, double d0, double d1, double d2, AxisAlignedBB axisalignedbb) {
        return this.getNearestEntity(this.getEntitiesOfClass(oclass, axisalignedbb, (entityliving1) -> {
            return true;
        }), pathfindertargetcondition, entityliving, d0, d1, d2);
    }

    @Nullable
    default <T extends EntityLiving> T getNearestEntity(List<? extends T> list, PathfinderTargetCondition pathfindertargetcondition, @Nullable EntityLiving entityliving, double d0, double d1, double d2) {
        double d3 = -1.0D;
        T t0 = null;
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            T t1 = (EntityLiving) iterator.next();

            if (pathfindertargetcondition.test(this.getLevel(), entityliving, t1)) {
                double d4 = t1.distanceToSqr(d0, d1, d2);

                if (d3 == -1.0D || d4 < d3) {
                    d3 = d4;
                    t0 = t1;
                }
            }
        }

        return t0;
    }

    default List<EntityHuman> getNearbyPlayers(PathfinderTargetCondition pathfindertargetcondition, EntityLiving entityliving, AxisAlignedBB axisalignedbb) {
        List<EntityHuman> list = new ArrayList();
        Iterator iterator = this.players().iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();

            if (axisalignedbb.contains(entityhuman.getX(), entityhuman.getY(), entityhuman.getZ()) && pathfindertargetcondition.test(this.getLevel(), entityliving, entityhuman)) {
                list.add(entityhuman);
            }
        }

        return list;
    }

    default <T extends EntityLiving> List<T> getNearbyEntities(Class<T> oclass, PathfinderTargetCondition pathfindertargetcondition, EntityLiving entityliving, AxisAlignedBB axisalignedbb) {
        List<T> list = this.getEntitiesOfClass(oclass, axisalignedbb, (entityliving1) -> {
            return true;
        });
        List<T> list1 = new ArrayList();
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            T t0 = (EntityLiving) iterator.next();

            if (pathfindertargetcondition.test(this.getLevel(), entityliving, t0)) {
                list1.add(t0);
            }
        }

        return list1;
    }
}
