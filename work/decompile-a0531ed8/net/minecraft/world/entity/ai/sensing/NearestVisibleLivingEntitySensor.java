package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public abstract class NearestVisibleLivingEntitySensor extends Sensor<EntityLiving> {

    public NearestVisibleLivingEntitySensor() {}

    protected abstract boolean isMatchingEntity(WorldServer worldserver, EntityLiving entityliving, EntityLiving entityliving1);

    protected abstract MemoryModuleType<EntityLiving> getMemory();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(this.getMemory());
    }

    @Override
    protected void doTick(WorldServer worldserver, EntityLiving entityliving) {
        entityliving.getBrain().setMemory(this.getMemory(), this.getNearestEntity(worldserver, entityliving));
    }

    private Optional<EntityLiving> getNearestEntity(WorldServer worldserver, EntityLiving entityliving) {
        return this.getVisibleEntities(entityliving).flatMap((nearestvisiblelivingentities) -> {
            return nearestvisiblelivingentities.findClosest((entityliving1) -> {
                return this.isMatchingEntity(worldserver, entityliving, entityliving1);
            });
        });
    }

    protected Optional<NearestVisibleLivingEntities> getVisibleEntities(EntityLiving entityliving) {
        return entityliving.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}
