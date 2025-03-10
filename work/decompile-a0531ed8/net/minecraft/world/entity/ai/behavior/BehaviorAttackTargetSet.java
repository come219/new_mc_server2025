package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorAttackTargetSet {

    public BehaviorAttackTargetSet() {}

    public static <E extends EntityInsentient> BehaviorControl<E> create(BehaviorAttackTargetSet.b<E> behaviorattacktargetset_b) {
        return create((worldserver, entityinsentient) -> {
            return true;
        }, behaviorattacktargetset_b);
    }

    public static <E extends EntityInsentient> BehaviorControl<E> create(BehaviorAttackTargetSet.a<E> behaviorattacktargetset_a, BehaviorAttackTargetSet.b<E> behaviorattacktargetset_b) {
        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.group(behaviorbuilder_b.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_b.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(behaviorbuilder_b, (memoryaccessor, memoryaccessor1) -> {
                return (worldserver, entityinsentient, i) -> {
                    if (!behaviorattacktargetset_a.test(worldserver, entityinsentient)) {
                        return false;
                    } else {
                        Optional<? extends EntityLiving> optional = behaviorattacktargetset_b.get(worldserver, entityinsentient);

                        if (optional.isEmpty()) {
                            return false;
                        } else {
                            EntityLiving entityliving = (EntityLiving) optional.get();

                            if (!entityinsentient.canAttack(entityliving)) {
                                return false;
                            } else {
                                memoryaccessor.set(entityliving);
                                memoryaccessor1.erase();
                                return true;
                            }
                        }
                    }
                };
            });
        });
    }

    @FunctionalInterface
    public interface a<E> {

        boolean test(WorldServer worldserver, E e0);
    }

    @FunctionalInterface
    public interface b<E> {

        Optional<? extends EntityLiving> get(WorldServer worldserver, E e0);
    }
}
