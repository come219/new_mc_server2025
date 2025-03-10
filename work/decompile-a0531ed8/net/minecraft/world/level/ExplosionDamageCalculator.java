package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3D;

public class ExplosionDamageCalculator {

    public ExplosionDamageCalculator() {}

    public Optional<Float> getBlockExplosionResistance(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
        return iblockdata.isAir() && fluid.isEmpty() ? Optional.empty() : Optional.of(Math.max(iblockdata.getBlock().getExplosionResistance(), fluid.getExplosionResistance()));
    }

    public boolean shouldBlockExplode(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, float f) {
        return true;
    }

    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        return true;
    }

    public float getKnockbackMultiplier(Entity entity) {
        return 1.0F;
    }

    public float getEntityDamageAmount(Explosion explosion, Entity entity, float f) {
        float f1 = explosion.radius() * 2.0F;
        Vec3D vec3d = explosion.center();
        double d0 = Math.sqrt(entity.distanceToSqr(vec3d)) / (double) f1;
        double d1 = (1.0D - d0) * (double) f;

        return (float) ((d1 * d1 + d1) / 2.0D * 7.0D * (double) f1 + 1.0D);
    }
}
