package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

public abstract class IProjectile extends Entity implements TraceableEntity {

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private boolean leftOwner;
    private boolean hasBeenShot;
    @Nullable
    private Entity lastDeflectedBy;

    IProjectile(EntityTypes<? extends IProjectile> entitytypes, World world) {
        super(entitytypes, world);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }

    }

    @Nullable
    @Override
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null) {
            this.cachedOwner = this.findOwner(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Nullable
    protected Entity findOwner(UUID uuid) {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            return worldserver.getEntity(uuid);
        } else {
            return null;
        }
    }

    public Entity getEffectSource() {
        return (Entity) MoreObjects.firstNonNull(this.getOwner(), this);
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        if (this.ownerUUID != null) {
            nbttagcompound.putUUID("Owner", this.ownerUUID);
        }

        if (this.leftOwner) {
            nbttagcompound.putBoolean("LeftOwner", true);
        }

        nbttagcompound.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasUUID("Owner")) {
            this.setOwnerThroughUUID(nbttagcompound.getUUID("Owner"));
        }

        this.leftOwner = nbttagcompound.getBoolean("LeftOwner");
        this.hasBeenShot = nbttagcompound.getBoolean("HasBeenShot");
    }

    protected void setOwnerThroughUUID(UUID uuid) {
        if (this.ownerUUID != uuid) {
            this.ownerUUID = uuid;
            this.cachedOwner = this.findOwner(uuid);
        }

    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof IProjectile iprojectile) {
            this.ownerUUID = iprojectile.ownerUUID;
            this.cachedOwner = iprojectile.cachedOwner;
        }

    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }

        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity entity = this.getOwner();

        if (entity != null) {
            AxisAlignedBB axisalignedbb = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D);

            return entity.getRootVehicle().getSelfAndPassengers().filter(IEntitySelector.CAN_BE_PICKED).noneMatch((entity1) -> {
                return axisalignedbb.intersects(entity1.getBoundingBox());
            });
        } else {
            return true;
        }
    }

    public Vec3D getMovementToShoot(double d0, double d1, double d2, float f, float f1) {
        return (new Vec3D(d0, d1, d2)).normalize().add(this.random.triangle(0.0D, 0.0172275D * (double) f1), this.random.triangle(0.0D, 0.0172275D * (double) f1), this.random.triangle(0.0D, 0.0172275D * (double) f1)).scale((double) f);
    }

    public void shoot(double d0, double d1, double d2, float f, float f1) {
        Vec3D vec3d = this.getMovementToShoot(d0, d1, d2, f, f1);

        this.setDeltaMovement(vec3d);
        this.hasImpulse = true;
        double d3 = vec3d.horizontalDistance();

        this.setYRot((float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D));
        this.setXRot((float) (MathHelper.atan2(vec3d.y, d3) * 57.2957763671875D));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity entity, float f, float f1, float f2, float f3, float f4) {
        float f5 = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);
        float f6 = -MathHelper.sin((f + f2) * 0.017453292F);
        float f7 = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);

        this.shoot((double) f5, (double) f6, (double) f7, f3, f4);
        Vec3D vec3d = entity.getKnownMovement();

        this.setDeltaMovement(this.getDeltaMovement().add(vec3d.x, entity.onGround() ? 0.0D : vec3d.y, vec3d.z));
    }

    public static <T extends IProjectile> T spawnProjectileFromRotation(IProjectile.a<T> iprojectile_a, WorldServer worldserver, ItemStack itemstack, EntityLiving entityliving, float f, float f1, float f2) {
        return spawnProjectile(iprojectile_a.create(worldserver, entityliving, itemstack), worldserver, itemstack, (iprojectile) -> {
            iprojectile.shootFromRotation(entityliving, entityliving.getXRot(), entityliving.getYRot(), f, f1, f2);
        });
    }

    public static <T extends IProjectile> T spawnProjectileUsingShoot(IProjectile.a<T> iprojectile_a, WorldServer worldserver, ItemStack itemstack, EntityLiving entityliving, double d0, double d1, double d2, float f, float f1) {
        return spawnProjectile(iprojectile_a.create(worldserver, entityliving, itemstack), worldserver, itemstack, (iprojectile) -> {
            iprojectile.shoot(d0, d1, d2, f, f1);
        });
    }

    public static <T extends IProjectile> T spawnProjectileUsingShoot(T t0, WorldServer worldserver, ItemStack itemstack, double d0, double d1, double d2, float f, float f1) {
        return spawnProjectile(t0, worldserver, itemstack, (iprojectile) -> {
            t0.shoot(d0, d1, d2, f, f1);
        });
    }

    public static <T extends IProjectile> T spawnProjectile(T t0, WorldServer worldserver, ItemStack itemstack) {
        return spawnProjectile(t0, worldserver, itemstack, (iprojectile) -> {
        });
    }

    public static <T extends IProjectile> T spawnProjectile(T t0, WorldServer worldserver, ItemStack itemstack, Consumer<T> consumer) {
        consumer.accept(t0);
        worldserver.addFreshEntity(t0);
        t0.applyOnProjectileSpawned(worldserver, itemstack);
        return t0;
    }

    public void applyOnProjectileSpawned(WorldServer worldserver, ItemStack itemstack) {
        EnchantmentManager.onProjectileSpawned(worldserver, itemstack, this, (item) -> {
        });
        if (this instanceof EntityArrow entityarrow) {
            ItemStack itemstack1 = entityarrow.getWeaponItem();

            if (itemstack1 != null && !itemstack1.isEmpty() && !itemstack.getItem().equals(itemstack1.getItem())) {
                Objects.requireNonNull(entityarrow);
                EnchantmentManager.onProjectileSpawned(worldserver, itemstack1, this, entityarrow::onItemBreak);
            }
        }

    }

    protected ProjectileDeflection hitTargetOrDeflectSelf(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            MovingObjectPositionEntity movingobjectpositionentity = (MovingObjectPositionEntity) movingobjectposition;
            Entity entity = movingobjectpositionentity.getEntity();
            ProjectileDeflection projectiledeflection = entity.deflection(this);

            if (projectiledeflection != ProjectileDeflection.NONE) {
                if (entity != this.lastDeflectedBy && this.deflect(projectiledeflection, entity, this.getOwner(), false)) {
                    this.lastDeflectedBy = entity;
                }

                return projectiledeflection;
            }
        } else if (this.shouldBounceOnWorldBorder() && movingobjectposition instanceof MovingObjectPositionBlock) {
            MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock) movingobjectposition;

            if (movingobjectpositionblock.isWorldBorderHit()) {
                ProjectileDeflection projectiledeflection1 = ProjectileDeflection.REVERSE;

                if (this.deflect(projectiledeflection1, (Entity) null, this.getOwner(), false)) {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
                    return projectiledeflection1;
                }
            }
        }

        this.onHit(movingobjectposition);
        return ProjectileDeflection.NONE;
    }

    protected boolean shouldBounceOnWorldBorder() {
        return false;
    }

    public boolean deflect(ProjectileDeflection projectiledeflection, @Nullable Entity entity, @Nullable Entity entity1, boolean flag) {
        projectiledeflection.deflect(this, entity, this.random);
        if (!this.level().isClientSide) {
            this.setOwner(entity1);
            this.onDeflection(entity, flag);
        }

        return true;
    }

    protected void onDeflection(@Nullable Entity entity, boolean flag) {}

    protected void onItemBreak(Item item) {}

    protected void onHit(MovingObjectPosition movingobjectposition) {
        MovingObjectPosition.EnumMovingObjectType movingobjectposition_enummovingobjecttype = movingobjectposition.getType();

        if (movingobjectposition_enummovingobjecttype == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            MovingObjectPositionEntity movingobjectpositionentity = (MovingObjectPositionEntity) movingobjectposition;
            Entity entity = movingobjectpositionentity.getEntity();

            if (entity.getType().is(TagsEntity.REDIRECTABLE_PROJECTILE) && entity instanceof IProjectile) {
                IProjectile iprojectile = (IProjectile) entity;

                iprojectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.getOwner(), true);
            }

            this.onHitEntity(movingobjectpositionentity);
            this.level().gameEvent((Holder) GameEvent.PROJECTILE_LAND, movingobjectposition.getLocation(), GameEvent.a.of(this, (IBlockData) null));
        } else if (movingobjectposition_enummovingobjecttype == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock) movingobjectposition;

            this.onHitBlock(movingobjectpositionblock);
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

            this.level().gameEvent((Holder) GameEvent.PROJECTILE_LAND, blockposition, GameEvent.a.of(this, this.level().getBlockState(blockposition)));
        }

    }

    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {}

    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        IBlockData iblockdata = this.level().getBlockState(movingobjectpositionblock.getBlockPos());

        iblockdata.onProjectileHit(this.level(), iblockdata, movingobjectpositionblock, this);
    }

    protected boolean canHitEntity(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        } else {
            Entity entity1 = this.getOwner();

            return entity1 == null || this.leftOwner || !entity1.isPassengerOfSameVehicle(entity);
        }
    }

    protected void updateRotation() {
        Vec3D vec3d = this.getDeltaMovement();
        double d0 = vec3d.horizontalDistance();

        this.setXRot(lerpRotation(this.xRotO, (float) (MathHelper.atan2(vec3d.y, d0) * 57.2957763671875D)));
        this.setYRot(lerpRotation(this.yRotO, (float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D)));
    }

    protected static float lerpRotation(float f, float f1) {
        while (f1 - f < -180.0F) {
            f -= 360.0F;
        }

        while (f1 - f >= 180.0F) {
            f += 360.0F;
        }

        return MathHelper.lerp(0.2F, f, f1);
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket(EntityTrackerEntry entitytrackerentry) {
        Entity entity = this.getOwner();

        return new PacketPlayOutSpawnEntity(this, entitytrackerentry, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        super.recreateFromPacket(packetplayoutspawnentity);
        Vec3D vec3d = new Vec3D(packetplayoutspawnentity.getXa(), packetplayoutspawnentity.getYa(), packetplayoutspawnentity.getZa());

        this.setDeltaMovement(vec3d);
        Entity entity = this.level().getEntity(packetplayoutspawnentity.getData());

        if (entity != null) {
            this.setOwner(entity);
        }

    }

    @Override
    public boolean mayInteract(WorldServer worldserver, BlockPosition blockposition) {
        Entity entity = this.getOwner();

        return entity instanceof EntityHuman ? entity.mayInteract(worldserver, blockposition) : entity == null || worldserver.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public boolean mayBreak(WorldServer worldserver) {
        return this.getType().is(TagsEntity.IMPACT_PROJECTILES) && worldserver.getGameRules().getBoolean(GameRules.RULE_PROJECTILESCANBREAKBLOCKS);
    }

    @Override
    public boolean isPickable() {
        return this.getType().is(TagsEntity.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getPickRadius() {
        return this.isPickable() ? 1.0F : 0.0F;
    }

    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(EntityLiving entityliving, DamageSource damagesource) {
        double d0 = this.getDeltaMovement().x;
        double d1 = this.getDeltaMovement().z;

        return DoubleDoubleImmutablePair.of(d0, d1);
    }

    @Override
    public int getDimensionChangingDelay() {
        return 2;
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (!this.isInvulnerableToBase(damagesource)) {
            this.markHurt();
        }

        return false;
    }

    @FunctionalInterface
    public interface a<T extends IProjectile> {

        T create(WorldServer worldserver, EntityLiving entityliving, ItemStack itemstack);
    }
}
