package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleParamItem;
import net.minecraft.core.particles.Particles;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.EntityChicken;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionEntity;

// CraftBukkit start
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
// CraftBukkit end

public class EntityEgg extends EntityProjectileThrowable {

    private static final EntitySize ZERO_SIZED_DIMENSIONS = EntitySize.fixed(0.0F, 0.0F);

    public EntityEgg(EntityTypes<? extends EntityEgg> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityEgg(World world, EntityLiving entityliving, ItemStack itemstack) {
        super(EntityTypes.EGG, entityliving, world, itemstack);
    }

    public EntityEgg(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(EntityTypes.EGG, d0, d1, d2, world, itemstack);
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 3) {
            double d0 = 0.08D;

            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new ParticleParamItem(Particles.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }

    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        movingobjectpositionentity.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            // CraftBukkit start
            boolean hatching = this.random.nextInt(8) == 0;
            if (true) {
            // CraftBukkit end
                byte b0 = 1;

                if (this.random.nextInt(32) == 0) {
                    b0 = 4;
                }

                // CraftBukkit start
                EntityType hatchingType = EntityType.CHICKEN;

                Entity shooter = this.getOwner();
                if (!hatching) {
                    b0 = 0;
                }
                if (shooter instanceof EntityPlayer) {
                    PlayerEggThrowEvent event = new PlayerEggThrowEvent((Player) shooter.getBukkitEntity(), (org.bukkit.entity.Egg) this.getBukkitEntity(), hatching, b0, hatchingType);
                    this.level().getCraftServer().getPluginManager().callEvent(event);

                    b0 = event.getNumHatches();
                    hatching = event.isHatching();
                    hatchingType = event.getHatchingType();
                    // If hatching is set to false, ensure child count is 0
                    if (!hatching) {
                        b0 = 0;
                    }
                }
                // CraftBukkit end

                for (int i = 0; i < b0; ++i) {
                    Entity entitychicken = this.level().getWorld().makeEntity(new org.bukkit.Location(this.level().getWorld(), this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F), hatchingType.getEntityClass()); // CraftBukkit

                    if (entitychicken != null) {
                        // CraftBukkit start
                        if (entitychicken.getBukkitEntity() instanceof Ageable) {
                            ((Ageable) entitychicken.getBukkitEntity()).setBaby();
                        }
                        // CraftBukkit end
                        entitychicken.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                        if (!entitychicken.fudgePositionAfterSizeChange(EntityEgg.ZERO_SIZED_DIMENSIONS)) {
                            break;
                        }

                        this.level().addFreshEntity(entitychicken, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.EGG); // CraftBukkit
                    }
                }
            }

            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard(EntityRemoveEvent.Cause.HIT); // CraftBukkit - add Bukkit remove cause
        }

    }

    @Override
    protected Item getDefaultItem() {
        return Items.EGG;
    }
}
