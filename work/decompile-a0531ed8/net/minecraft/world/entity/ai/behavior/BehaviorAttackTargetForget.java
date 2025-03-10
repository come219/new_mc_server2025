package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorAttackTargetForget {

    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    public BehaviorAttackTargetForget() {}

    public static <E extends EntityInsentient> BehaviorControl<E> create(BehaviorAttackTargetForget.b<E> behaviorattacktargetforget_b) {
        return create((worldserver, entityliving) -> {
            return false;
        }, behaviorattacktargetforget_b, true);
    }

    public static <E extends EntityInsentient> BehaviorControl<E> create(BehaviorAttackTargetForget.a behaviorattacktargetforget_a) {
        return create(behaviorattacktargetforget_a, (worldserver, entityinsentient, entityliving) -> {
        }, true);
    }

    public static <E extends EntityInsentient> BehaviorControl<E> create() {
        return create((worldserver, entityliving) -> {
            return false;
        }, (worldserver, entityinsentient, entityliving) -> {
        }, true);
    }

    public static <E extends EntityInsentient> BehaviorControl<E> create(BehaviorAttackTargetForget.a behaviorattacktargetforget_a, BehaviorAttackTargetForget.b<E> behaviorattacktargetforget_b, boolean flag) {
        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.group(behaviorbuilder_b.present(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_b.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(behaviorbuilder_b, (memoryaccessor, memoryaccessor1) -> {
                return (worldserver, entityinsentient, i) -> {
                    EntityLiving entityliving = (EntityLiving) behaviorbuilder_b.get(memoryaccessor);

                    if (entityinsentient.canAttack(entityliving) && (!flag || !isTiredOfTryingToReachTarget(entityinsentient, behaviorbuilder_b.tryGet(memoryaccessor1))) && entityliving.isAlive() && entityliving.level() == entityinsentient.level() && !behaviorattacktargetforget_a.test(worldserver, entityliving)) {
                        return true;
                    } else {
                        behaviorattacktargetforget_b.accept(worldserver, entityinsentient, entityliving);
                        memoryaccessor.erase();
                        return true;
                    }
                };
            });
        });
    }

    private static boolean isTiredOfTryingToReachTarget(EntityLiving entityliving, Optional<Long> optional) {
        return optional.isPresent() && entityliving.level().getGameTime() - (Long) optional.get() > 200L;
    }

    @FunctionalInterface
    public interface a {

        boolean test(WorldServer worldserver, EntityLiving entityliving);
    }

    @FunctionalInterface
    public interface b<E> {

        void accept(WorldServer worldserver, E e0, EntityLiving entityliving);
    }
}
