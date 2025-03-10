package net.minecraft.world.entity.vehicle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class VehicleEntity extends Entity {

    protected static final DataWatcherObject<Integer> DATA_ID_HURT = DataWatcher.defineId(VehicleEntity.class, DataWatcherRegistry.INT);
    protected static final DataWatcherObject<Integer> DATA_ID_HURTDIR = DataWatcher.defineId(VehicleEntity.class, DataWatcherRegistry.INT);
    protected static final DataWatcherObject<Float> DATA_ID_DAMAGE = DataWatcher.defineId(VehicleEntity.class, DataWatcherRegistry.FLOAT);

    public VehicleEntity(EntityTypes<?> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    public boolean hurtClient(DamageSource damagesource) {
        return true;
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (this.isRemoved()) {
            return true;
        } else if (this.isInvulnerableToBase(damagesource)) {
            return false;
        } else {
            boolean flag;
            label32:
            {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.markHurt();
                this.setDamage(this.getDamage() + f * 10.0F);
                this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
                Entity entity = damagesource.getEntity();

                if (entity instanceof EntityHuman) {
                    EntityHuman entityhuman = (EntityHuman) entity;

                    if (entityhuman.getAbilities().instabuild) {
                        flag = true;
                        break label32;
                    }
                }

                flag = false;
            }

            boolean flag1 = flag;

            if ((flag1 || this.getDamage() <= 40.0F) && !this.shouldSourceDestroy(damagesource)) {
                if (flag1) {
                    this.discard();
                }
            } else {
                this.destroy(worldserver, damagesource);
            }

            return true;
        }
    }

    boolean shouldSourceDestroy(DamageSource damagesource) {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return explosion.getIndirectSourceEntity() instanceof EntityInsentient && !explosion.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public void destroy(WorldServer worldserver, Item item) {
        this.kill(worldserver);
        if (worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemstack = new ItemStack(item);

            itemstack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
            this.spawnAtLocation(worldserver, itemstack);
        }
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(VehicleEntity.DATA_ID_HURT, 0);
        datawatcher_a.define(VehicleEntity.DATA_ID_HURTDIR, 1);
        datawatcher_a.define(VehicleEntity.DATA_ID_DAMAGE, 0.0F);
    }

    public void setHurtTime(int i) {
        this.entityData.set(VehicleEntity.DATA_ID_HURT, i);
    }

    public void setHurtDir(int i) {
        this.entityData.set(VehicleEntity.DATA_ID_HURTDIR, i);
    }

    public void setDamage(float f) {
        this.entityData.set(VehicleEntity.DATA_ID_DAMAGE, f);
    }

    public float getDamage() {
        return (Float) this.entityData.get(VehicleEntity.DATA_ID_DAMAGE);
    }

    public int getHurtTime() {
        return (Integer) this.entityData.get(VehicleEntity.DATA_ID_HURT);
    }

    public int getHurtDir() {
        return (Integer) this.entityData.get(VehicleEntity.DATA_ID_HURTDIR);
    }

    protected void destroy(WorldServer worldserver, DamageSource damagesource) {
        this.destroy(worldserver, this.getDropItem());
    }

    @Override
    public int getDimensionChangingDelay() {
        return 10;
    }

    protected abstract Item getDropItem();
}
