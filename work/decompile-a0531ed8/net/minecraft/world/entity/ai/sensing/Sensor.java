package net.minecraft.world.entity.ai.sensing;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;

public abstract class Sensor<E extends EntityLiving> {

    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    private static final int DEFAULT_SCAN_RATE = 20;
    private static final int DEFAULT_TARGETING_RANGE = 16;
    private static final PathfinderTargetCondition TARGET_CONDITIONS = PathfinderTargetCondition.forNonCombat().range(16.0D);
    private static final PathfinderTargetCondition TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = PathfinderTargetCondition.forNonCombat().range(16.0D).ignoreInvisibilityTesting();
    private static final PathfinderTargetCondition ATTACK_TARGET_CONDITIONS = PathfinderTargetCondition.forCombat().range(16.0D);
    private static final PathfinderTargetCondition ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = PathfinderTargetCondition.forCombat().range(16.0D).ignoreInvisibilityTesting();
    private static final PathfinderTargetCondition ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = PathfinderTargetCondition.forCombat().range(16.0D).ignoreLineOfSight();
    private static final PathfinderTargetCondition ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = PathfinderTargetCondition.forCombat().range(16.0D).ignoreLineOfSight().ignoreInvisibilityTesting();
    private final int scanRate;
    private long timeToTick;

    public Sensor(int i) {
        this.scanRate = i;
        this.timeToTick = (long) Sensor.RANDOM.nextInt(i);
    }

    public Sensor() {
        this(20);
    }

    public final void tick(WorldServer worldserver, E e0) {
        if (--this.timeToTick <= 0L) {
            this.timeToTick = (long) this.scanRate;
            this.updateTargetingConditionRanges(e0);
            this.doTick(worldserver, e0);
        }

    }

    private void updateTargetingConditionRanges(E e0) {
        double d0 = e0.getAttributeValue(GenericAttributes.FOLLOW_RANGE);

        Sensor.TARGET_CONDITIONS.range(d0);
        Sensor.TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range(d0);
        Sensor.ATTACK_TARGET_CONDITIONS.range(d0);
        Sensor.ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range(d0);
        Sensor.ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.range(d0);
        Sensor.ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.range(d0);
    }

    protected abstract void doTick(WorldServer worldserver, E e0);

    public abstract Set<MemoryModuleType<?>> requires();

    public static boolean isEntityTargetable(WorldServer worldserver, EntityLiving entityliving, EntityLiving entityliving1) {
        return entityliving.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, entityliving1) ? Sensor.TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(worldserver, entityliving, entityliving1) : Sensor.TARGET_CONDITIONS.test(worldserver, entityliving, entityliving1);
    }

    public static boolean isEntityAttackable(WorldServer worldserver, EntityLiving entityliving, EntityLiving entityliving1) {
        return entityliving.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, entityliving1) ? Sensor.ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(worldserver, entityliving, entityliving1) : Sensor.ATTACK_TARGET_CONDITIONS.test(worldserver, entityliving, entityliving1);
    }

    public static BiPredicate<WorldServer, EntityLiving> wasEntityAttackableLastNTicks(EntityLiving entityliving, int i) {
        return rememberPositives(i, (worldserver, entityliving1) -> {
            return isEntityAttackable(worldserver, entityliving, entityliving1);
        });
    }

    public static boolean isEntityAttackableIgnoringLineOfSight(WorldServer worldserver, EntityLiving entityliving, EntityLiving entityliving1) {
        return entityliving.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, entityliving1) ? Sensor.ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test(worldserver, entityliving, entityliving1) : Sensor.ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(worldserver, entityliving, entityliving1);
    }

    static <T, U> BiPredicate<T, U> rememberPositives(int i, BiPredicate<T, U> bipredicate) {
        AtomicInteger atomicinteger = new AtomicInteger(0);

        return (object, object1) -> {
            if (bipredicate.test(object, object1)) {
                atomicinteger.set(i);
                return true;
            } else {
                return atomicinteger.decrementAndGet() >= 0;
            }
        };
    }
}
