package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.EntityInsentient;

public class BehaviorSwim<T extends EntityInsentient> extends Behavior<T> {

    private final float chance;

    public BehaviorSwim(float f) {
        super(ImmutableMap.of());
        this.chance = f;
    }

    public static <T extends EntityInsentient> boolean shouldSwim(T t0) {
        return t0.isInWater() && t0.getFluidHeight(TagsFluid.WATER) > t0.getFluidJumpThreshold() || t0.isInLava();
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, EntityInsentient entityinsentient) {
        return shouldSwim(entityinsentient);
    }

    protected boolean canStillUse(WorldServer worldserver, EntityInsentient entityinsentient, long i) {
        return this.checkExtraStartConditions(worldserver, entityinsentient);
    }

    protected void tick(WorldServer worldserver, EntityInsentient entityinsentient, long i) {
        if (entityinsentient.getRandom().nextFloat() < this.chance) {
            entityinsentient.getJumpControl().jump();
        }

    }
}
