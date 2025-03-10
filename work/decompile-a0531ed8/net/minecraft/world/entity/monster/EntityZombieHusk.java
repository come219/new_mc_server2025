package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;

public class EntityZombieHusk extends EntityZombie {

    public EntityZombieHusk(EntityTypes<? extends EntityZombieHusk> entitytypes, World world) {
        super(entitytypes, world);
    }

    public static boolean checkHuskSpawnRules(EntityTypes<EntityZombieHusk> entitytypes, WorldAccess worldaccess, EntitySpawnReason entityspawnreason, BlockPosition blockposition, RandomSource randomsource) {
        return checkMonsterSpawnRules(entitytypes, worldaccess, entityspawnreason, blockposition, randomsource) && (EntitySpawnReason.isSpawner(entityspawnreason) || worldaccess.canSeeSky(blockposition));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.HUSK_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.HUSK_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.HUSK_DEATH;
    }

    @Override
    protected SoundEffect getStepSound() {
        return SoundEffects.HUSK_STEP;
    }

    @Override
    public boolean doHurtTarget(WorldServer worldserver, Entity entity) {
        boolean flag = super.doHurtTarget(worldserver, entity);

        if (flag && this.getMainHandItem().isEmpty() && entity instanceof EntityLiving) {
            float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();

            ((EntityLiving) entity).addEffect(new MobEffect(MobEffects.HUNGER, 140 * (int) f), this);
        }

        return flag;
    }

    @Override
    protected boolean convertsInWater() {
        return true;
    }

    @Override
    protected void doUnderWaterConversion() {
        this.convertToZombieType(EntityTypes.ZOMBIE);
        if (!this.isSilent()) {
            this.level().levelEvent((EntityHuman) null, 1041, this.blockPosition(), 0);
        }

    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }
}
