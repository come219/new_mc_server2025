package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.phys.Vec3D;

public interface Explosion {

    static DamageSource getDefaultDamageSource(World world, @Nullable Entity entity) {
        return world.damageSources().explosion(entity, getIndirectSourceEntity(entity));
    }

    @Nullable
    static EntityLiving getIndirectSourceEntity(@Nullable Entity entity) {
        Entity entity1 = entity;
        byte b0 = 0;

        while(true) {
            EntityLiving entityliving;

            //$FF: b0->value
            //0->net/minecraft/world/entity/item/EntityTNTPrimed
            //1->net/minecraft/world/entity/EntityLiving
            //2->net/minecraft/world/entity/projectile/IProjectile
            switch (entity1.typeSwitch<invokedynamic>(entity1, b0)) {
                case -1:
                default:
                    entityliving = null;
                    return entityliving;
                case 0:
                    EntityTNTPrimed entitytntprimed = (EntityTNTPrimed)entity1;

                    entityliving = entitytntprimed.getOwner();
                    return entityliving;
                case 1:
                    EntityLiving entityliving1 = (EntityLiving)entity1;

                    entityliving = entityliving1;
                    return entityliving;
                case 2:
                    IProjectile iprojectile = (IProjectile)entity1;
                    Entity entity2 = iprojectile.getOwner();

                    if (entity2 instanceof EntityLiving entityliving2) {
                        entityliving = entityliving2;
                        return entityliving;
                    }

                    b0 = 3;
            }
        }
    }

    WorldServer level();

    Explosion.Effect getBlockInteraction();

    @Nullable
    EntityLiving getIndirectSourceEntity();

    @Nullable
    Entity getDirectSourceEntity();

    float radius();

    Vec3D center();

    boolean canTriggerBlocks();

    boolean shouldAffectBlocklikeEntities();

    public static enum Effect {

        KEEP(false), DESTROY(true), DESTROY_WITH_DECAY(true), TRIGGER_BLOCK(false);

        private final boolean shouldAffectBlocklikeEntities;

        private Effect(final boolean flag) {
            this.shouldAffectBlocklikeEntities = flag;
        }

        public boolean shouldAffectBlocklikeEntities() {
            return this.shouldAffectBlocklikeEntities;
        }
    }
}
