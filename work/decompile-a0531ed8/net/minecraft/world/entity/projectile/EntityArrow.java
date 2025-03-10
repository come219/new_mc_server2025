package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class EntityArrow extends IProjectile {

    private static final double ARROW_BASE_DAMAGE = 2.0D;
    private static final int SHAKE_TIME = 7;
    private static final float WATER_INERTIA = 0.6F;
    private static final float INERTIA = 0.99F;
    private static final DataWatcherObject<Byte> ID_FLAGS = DataWatcher.defineId(EntityArrow.class, DataWatcherRegistry.BYTE);
    private static final DataWatcherObject<Byte> PIERCE_LEVEL = DataWatcher.defineId(EntityArrow.class, DataWatcherRegistry.BYTE);
    private static final DataWatcherObject<Boolean> IN_GROUND = DataWatcher.defineId(EntityArrow.class, DataWatcherRegistry.BOOLEAN);
    private static final int FLAG_CRIT = 1;
    private static final int FLAG_NOPHYSICS = 2;
    @Nullable
    private IBlockData lastState;
    protected int inGroundTime;
    public EntityArrow.PickupStatus pickup;
    public int shakeTime;
    public int life;
    private double baseDamage;
    private SoundEffect soundEvent;
    @Nullable
    private IntOpenHashSet piercingIgnoreEntityIds;
    @Nullable
    private List<Entity> piercedAndKilledEntities;
    public ItemStack pickupItemStack;
    @Nullable
    public ItemStack firedFromWeapon;

    protected EntityArrow(EntityTypes<? extends EntityArrow> entitytypes, World world) {
        super(entitytypes, world);
        this.pickup = EntityArrow.PickupStatus.DISALLOWED;
        this.baseDamage = 2.0D;
        this.soundEvent = this.getDefaultHitGroundSoundEvent();
        this.pickupItemStack = this.getDefaultPickupItem();
        this.firedFromWeapon = null;
    }

    protected EntityArrow(EntityTypes<? extends EntityArrow> entitytypes, double d0, double d1, double d2, World world, ItemStack itemstack, @Nullable ItemStack itemstack1) {
        this(entitytypes, world);
        this.pickupItemStack = itemstack.copy();
        this.setCustomName((IChatBaseComponent) itemstack.get(DataComponents.CUSTOM_NAME));
        Unit unit = (Unit) itemstack.remove(DataComponents.INTANGIBLE_PROJECTILE);

        if (unit != null) {
            this.pickup = EntityArrow.PickupStatus.CREATIVE_ONLY;
        }

        this.setPos(d0, d1, d2);
        if (itemstack1 != null && world instanceof WorldServer worldserver) {
            if (itemstack1.isEmpty()) {
                throw new IllegalArgumentException("Invalid weapon firing an arrow");
            }

            this.firedFromWeapon = itemstack1.copy();
            int i = EnchantmentManager.getPiercingCount(worldserver, itemstack1, this.pickupItemStack);

            if (i > 0) {
                this.setPierceLevel((byte) i);
            }
        }

    }

    protected EntityArrow(EntityTypes<? extends EntityArrow> entitytypes, EntityLiving entityliving, World world, ItemStack itemstack, @Nullable ItemStack itemstack1) {
        this(entitytypes, entityliving.getX(), entityliving.getEyeY() - 0.10000000149011612D, entityliving.getZ(), world, itemstack, itemstack1);
        this.setOwner(entityliving);
    }

    public void setSoundEvent(SoundEffect soundeffect) {
        this.soundEvent = soundeffect;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        double d1 = this.getBoundingBox().getSize() * 10.0D;

        if (Double.isNaN(d1)) {
            d1 = 1.0D;
        }

        d1 *= 64.0D * getViewScale();
        return d0 < d1 * d1;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityArrow.ID_FLAGS, (byte) 0);
        datawatcher_a.define(EntityArrow.PIERCE_LEVEL, (byte) 0);
        datawatcher_a.define(EntityArrow.IN_GROUND, false);
    }

    @Override
    public void shoot(double d0, double d1, double d2, float f, float f1) {
        super.shoot(d0, d1, d2, f, f1);
        this.life = 0;
    }

    @Override
    public void lerpTo(double d0, double d1, double d2, float f, float f1, int i) {
        this.setPos(d0, d1, d2);
        this.setRot(f, f1);
    }

    @Override
    public void lerpMotion(double d0, double d1, double d2) {
        super.lerpMotion(d0, d1, d2);
        this.life = 0;
        if (this.isInGround() && MathHelper.lengthSquared(d0, d1, d2) > 0.0D) {
            this.setInGround(false);
        }

    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (!this.firstTick && this.shakeTime <= 0 && datawatcherobject.equals(EntityArrow.IN_GROUND) && this.isInGround()) {
            this.shakeTime = 7;
        }

    }

    @Override
    public void tick() {
        boolean flag = !this.isNoPhysics();
        Vec3D vec3d = this.getDeltaMovement();
        BlockPosition blockposition = this.blockPosition();
        IBlockData iblockdata = this.level().getBlockState(blockposition);

        if (!iblockdata.isAir() && flag) {
            VoxelShape voxelshape = iblockdata.getCollisionShape(this.level(), blockposition);

            if (!voxelshape.isEmpty()) {
                Vec3D vec3d1 = this.position();
                Iterator iterator = voxelshape.toAabbs().iterator();

                while (iterator.hasNext()) {
                    AxisAlignedBB axisalignedbb = (AxisAlignedBB) iterator.next();

                    if (axisalignedbb.move(blockposition).contains(vec3d1)) {
                        this.setInGround(true);
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain() || iblockdata.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.isInGround() && flag) {
            if (!this.level().isClientSide()) {
                if (this.lastState != iblockdata && this.shouldFall()) {
                    this.startFalling();
                } else {
                    this.tickDespawn();
                }
            }

            ++this.inGroundTime;
            if (this.isAlive()) {
                this.applyEffectsFromBlocks();
            }

        } else {
            this.inGroundTime = 0;
            Vec3D vec3d2 = this.position();

            if (this.isInWater()) {
                this.applyInertia(this.getWaterInertia());
                this.addBubbleParticles(vec3d2);
            }

            if (this.isCritArrow()) {
                for (int i = 0; i < 4; ++i) {
                    this.level().addParticle(Particles.CRIT, vec3d2.x + vec3d.x * (double) i / 4.0D, vec3d2.y + vec3d.y * (double) i / 4.0D, vec3d2.z + vec3d.z * (double) i / 4.0D, -vec3d.x, -vec3d.y + 0.2D, -vec3d.z);
                }
            }

            float f;

            if (!flag) {
                f = (float) (MathHelper.atan2(-vec3d.x, -vec3d.z) * 57.2957763671875D);
            } else {
                f = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D);
            }

            float f1 = (float) (MathHelper.atan2(vec3d.y, vec3d.horizontalDistance()) * 57.2957763671875D);

            this.setXRot(lerpRotation(this.getXRot(), f1));
            this.setYRot(lerpRotation(this.getYRot(), f));
            if (flag) {
                MovingObjectPositionBlock movingobjectpositionblock = this.level().clipIncludingBorder(new RayTrace(vec3d2, vec3d2.add(vec3d), RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, this));

                this.stepMoveAndHit(movingobjectpositionblock);
            } else {
                this.setPos(vec3d2.add(vec3d));
                this.applyEffectsFromBlocks();
            }

            if (!this.isInWater()) {
                this.applyInertia(0.99F);
            }

            if (flag && !this.isInGround()) {
                this.applyGravity();
            }

            super.tick();
        }
    }

    private void stepMoveAndHit(MovingObjectPositionBlock movingobjectpositionblock) {
        while (true) {
            if (this.isAlive()) {
                Vec3D vec3d = this.position();
                MovingObjectPositionEntity movingobjectpositionentity = this.findHitEntity(vec3d, movingobjectpositionblock.getLocation());
                Vec3D vec3d1 = ((MovingObjectPosition) Objects.requireNonNullElse(movingobjectpositionentity, movingobjectpositionblock)).getLocation();

                this.setPos(vec3d1);
                this.applyEffectsFromBlocks(vec3d, vec3d1);
                if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
                    this.handlePortal();
                }

                if (movingobjectpositionentity == null) {
                    if (this.isAlive() && movingobjectpositionblock.getType() != MovingObjectPosition.EnumMovingObjectType.MISS) {
                        this.hitTargetOrDeflectSelf(movingobjectpositionblock);
                        this.hasImpulse = true;
                    }
                } else {
                    if (!this.isAlive() || this.noPhysics) {
                        continue;
                    }

                    ProjectileDeflection projectiledeflection = this.hitTargetOrDeflectSelf(movingobjectpositionentity);

                    this.hasImpulse = true;
                    if (this.getPierceLevel() > 0 && projectiledeflection == ProjectileDeflection.NONE) {
                        continue;
                    }
                }
            }

            return;
        }
    }

    private void applyInertia(float f) {
        Vec3D vec3d = this.getDeltaMovement();

        this.setDeltaMovement(vec3d.scale((double) f));
    }

    private void addBubbleParticles(Vec3D vec3d) {
        Vec3D vec3d1 = this.getDeltaMovement();

        for (int i = 0; i < 4; ++i) {
            float f = 0.25F;

            this.level().addParticle(Particles.BUBBLE, vec3d.x - vec3d1.x * 0.25D, vec3d.y - vec3d1.y * 0.25D, vec3d.z - vec3d1.z * 0.25D, vec3d1.x, vec3d1.y, vec3d1.z);
        }

    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    private boolean shouldFall() {
        return this.isInGround() && this.level().noCollision((new AxisAlignedBB(this.position(), this.position())).inflate(0.06D));
    }

    private void startFalling() {
        this.setInGround(false);
        Vec3D vec3d = this.getDeltaMovement();

        this.setDeltaMovement(vec3d.multiply((double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F)));
        this.life = 0;
    }

    public boolean isInGround() {
        return (Boolean) this.entityData.get(EntityArrow.IN_GROUND);
    }

    protected void setInGround(boolean flag) {
        this.entityData.set(EntityArrow.IN_GROUND, flag);
    }

    @Override
    public void move(EnumMoveType enummovetype, Vec3D vec3d) {
        super.move(enummovetype, vec3d);
        if (enummovetype != EnumMoveType.SELF && this.shouldFall()) {
            this.startFalling();
        }

    }

    protected void tickDespawn() {
        ++this.life;
        if (this.life >= 1200) {
            this.discard();
        }

    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }

        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }

    }

    @Override
    protected void onItemBreak(Item item) {
        this.firedFromWeapon = null;
    }

    @Override
    public void onInsideBubbleColumn(boolean flag) {
        if (!this.isInGround()) {
            super.onInsideBubbleColumn(flag);
        }
    }

    @Override
    public void push(double d0, double d1, double d2) {
        if (!this.isInGround()) {
            super.push(d0, d1, d2);
        }
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        Entity entity = movingobjectpositionentity.getEntity();
        float f = (float) this.getDeltaMovement().length();
        double d0 = this.baseDamage;
        Entity entity1 = this.getOwner();
        DamageSource damagesource = this.damageSources().arrow(this, (Entity) (entity1 != null ? entity1 : this));

        if (this.getWeaponItem() != null) {
            World world = this.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                d0 = (double) EnchantmentManager.modifyDamage(worldserver, this.getWeaponItem(), entity, damagesource, (float) d0);
            }
        }

        int i = MathHelper.ceil(MathHelper.clamp((double) f * d0, 0.0D, 2.147483647E9D));

        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = (long) this.random.nextInt(i / 2 + 2);

            i = (int) Math.min(j + (long) i, 2147483647L);
        }

        if (entity1 instanceof EntityLiving entityliving) {
            entityliving.setLastHurtMob(entity);
        }

        boolean flag = entity.getType() == EntityTypes.ENDERMAN;
        int k = entity.getRemainingFireTicks();

        if (this.isOnFire() && !flag) {
            entity.igniteForSeconds(5.0F);
        }

        if (entity.hurtOrSimulate(damagesource, (float) i)) {
            if (flag) {
                return;
            }

            if (entity instanceof EntityLiving) {
                EntityLiving entityliving1 = (EntityLiving) entity;

                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    entityliving1.setArrowCount(entityliving1.getArrowCount() + 1);
                }

                this.doKnockback(entityliving1, damagesource);
                World world1 = this.level();

                if (world1 instanceof WorldServer) {
                    WorldServer worldserver1 = (WorldServer) world1;

                    EnchantmentManager.doPostAttackEffectsWithItemSource(worldserver1, entityliving1, damagesource, this.getWeaponItem());
                }

                this.doPostHurtEffects(entityliving1);
                if (entityliving1 != entity1 && entityliving1 instanceof EntityHuman && entity1 instanceof EntityPlayer && !this.isSilent()) {
                    ((EntityPlayer) entity1).connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(entityliving1);
                }

                if (!this.level().isClientSide && entity1 instanceof EntityPlayer) {
                    EntityPlayer entityplayer = (EntityPlayer) entity1;

                    if (this.piercedAndKilledEntities != null) {
                        CriterionTriggers.KILLED_BY_ARROW.trigger(entityplayer, this.piercedAndKilledEntities, this.firedFromWeapon);
                    } else if (!entity.isAlive()) {
                        CriterionTriggers.KILLED_BY_ARROW.trigger(entityplayer, List.of(entity), this.firedFromWeapon);
                    }
                }
            }

            this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
            World world2 = this.level();

            if (world2 instanceof WorldServer) {
                WorldServer worldserver2 = (WorldServer) world2;

                if (this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                    if (this.pickup == EntityArrow.PickupStatus.ALLOWED) {
                        this.spawnAtLocation(worldserver2, this.getPickupItem(), 0.1F);
                    }

                    this.discard();
                }
            }
        }

    }

    protected void doKnockback(EntityLiving entityliving, DamageSource damagesource) {
        float f;
        label18:
        {
            if (this.firedFromWeapon != null) {
                World world = this.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    f = EnchantmentManager.modifyKnockback(worldserver, this.firedFromWeapon, entityliving, damagesource, 0.0F);
                    break label18;
                }
            }

            f = 0.0F;
        }

        double d0 = (double) f;

        if (d0 > 0.0D) {
            double d1 = Math.max(0.0D, 1.0D - entityliving.getAttributeValue(GenericAttributes.KNOCKBACK_RESISTANCE));
            Vec3D vec3d = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(d0 * 0.6D * d1);

            if (vec3d.lengthSqr() > 0.0D) {
                entityliving.push(vec3d.x, 0.1D, vec3d.z);
            }
        }

    }

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        this.lastState = this.level().getBlockState(movingobjectpositionblock.getBlockPos());
        super.onHitBlock(movingobjectpositionblock);
        ItemStack itemstack = this.getWeaponItem();
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (itemstack != null) {
                this.hitBlockEnchantmentEffects(worldserver, movingobjectpositionblock, itemstack);
            }
        }

        Vec3D vec3d = this.getDeltaMovement();
        Vec3D vec3d1 = new Vec3D(Math.signum(vec3d.x), Math.signum(vec3d.y), Math.signum(vec3d.z));
        Vec3D vec3d2 = vec3d1.scale(0.05000000074505806D);

        this.setPos(this.position().subtract(vec3d2));
        this.setDeltaMovement(Vec3D.ZERO);
        this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.setInGround(true);
        this.shakeTime = 7;
        this.setCritArrow(false);
        this.setPierceLevel((byte) 0);
        this.setSoundEvent(SoundEffects.ARROW_HIT);
        this.resetPiercedEntities();
    }

    protected void hitBlockEnchantmentEffects(WorldServer worldserver, MovingObjectPositionBlock movingobjectpositionblock, ItemStack itemstack) {
        Vec3D vec3d = movingobjectpositionblock.getBlockPos().clampLocationWithin(movingobjectpositionblock.getLocation());
        Entity entity = this.getOwner();
        EntityLiving entityliving;

        if (entity instanceof EntityLiving entityliving1) {
            entityliving = entityliving1;
        } else {
            entityliving = null;
        }

        EnchantmentManager.onHitBlock(worldserver, itemstack, entityliving, this, (EnumItemSlot) null, vec3d, worldserver.getBlockState(movingobjectpositionblock.getBlockPos()), (item) -> {
            this.firedFromWeapon = null;
        });
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.firedFromWeapon;
    }

    protected SoundEffect getDefaultHitGroundSoundEvent() {
        return SoundEffects.ARROW_HIT;
    }

    protected final SoundEffect getHitGroundSoundEvent() {
        return this.soundEvent;
    }

    protected void doPostHurtEffects(EntityLiving entityliving) {}

    @Nullable
    protected MovingObjectPositionEntity findHitEntity(Vec3D vec3d, Vec3D vec3d1) {
        return ProjectileHelper.getEntityHitResult(this.level(), this, vec3d, vec3d1, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof EntityHuman) {
            Entity entity1 = this.getOwner();

            if (entity1 instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity1;

                if (!entityhuman.canHarmPlayer((EntityHuman) entity)) {
                    return false;
                }
            }
        }

        return super.canHitEntity(entity) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(entity.getId()));
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putShort("life", (short) this.life);
        if (this.lastState != null) {
            nbttagcompound.put("inBlockState", GameProfileSerializer.writeBlockState(this.lastState));
        }

        nbttagcompound.putByte("shake", (byte) this.shakeTime);
        nbttagcompound.putBoolean("inGround", this.isInGround());
        nbttagcompound.putByte("pickup", (byte) this.pickup.ordinal());
        nbttagcompound.putDouble("damage", this.baseDamage);
        nbttagcompound.putBoolean("crit", this.isCritArrow());
        nbttagcompound.putByte("PierceLevel", this.getPierceLevel());
        nbttagcompound.putString("SoundEvent", BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent).toString());
        nbttagcompound.put("item", this.pickupItemStack.save(this.registryAccess()));
        if (this.firedFromWeapon != null) {
            nbttagcompound.put("weapon", this.firedFromWeapon.save(this.registryAccess(), new NBTTagCompound()));
        }

    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.life = nbttagcompound.getShort("life");
        if (nbttagcompound.contains("inBlockState", 10)) {
            this.lastState = GameProfileSerializer.readBlockState(this.level().holderLookup(Registries.BLOCK), nbttagcompound.getCompound("inBlockState"));
        }

        this.shakeTime = nbttagcompound.getByte("shake") & 255;
        this.setInGround(nbttagcompound.getBoolean("inGround"));
        if (nbttagcompound.contains("damage", 99)) {
            this.baseDamage = nbttagcompound.getDouble("damage");
        }

        this.pickup = EntityArrow.PickupStatus.byOrdinal(nbttagcompound.getByte("pickup"));
        this.setCritArrow(nbttagcompound.getBoolean("crit"));
        this.setPierceLevel(nbttagcompound.getByte("PierceLevel"));
        if (nbttagcompound.contains("SoundEvent", 8)) {
            this.soundEvent = (SoundEffect) BuiltInRegistries.SOUND_EVENT.getOptional(MinecraftKey.parse(nbttagcompound.getString("SoundEvent"))).orElse(this.getDefaultHitGroundSoundEvent());
        }

        if (nbttagcompound.contains("item", 10)) {
            this.setPickupItemStack((ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound.getCompound("item")).orElse(this.getDefaultPickupItem()));
        } else {
            this.setPickupItemStack(this.getDefaultPickupItem());
        }

        if (nbttagcompound.contains("weapon", 10)) {
            this.firedFromWeapon = (ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound.getCompound("weapon")).orElse((Object) null);
        } else {
            this.firedFromWeapon = null;
        }

    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        Entity entity1 = entity;
        byte b0 = 0;

        EntityArrow.PickupStatus entityarrow_pickupstatus;

        label16:
        while(true) {
            //$FF: b0->value
            //0->net/minecraft/world/entity/player/EntityHuman
            //1->net/minecraft/world/entity/OminousItemSpawner
            switch (entity1.typeSwitch<invokedynamic>(entity1, b0)) {
                case -1:
                default:
                    entityarrow_pickupstatus = this.pickup;
                    break label16;
                case 0:
                    EntityHuman entityhuman = (EntityHuman)entity1;

                    if (this.pickup != EntityArrow.PickupStatus.DISALLOWED) {
                        b0 = 1;
                        break;
                    }

                    entityarrow_pickupstatus = EntityArrow.PickupStatus.ALLOWED;
                    break label16;
                case 1:
                    OminousItemSpawner ominousitemspawner = (OminousItemSpawner)entity1;

                    entityarrow_pickupstatus = EntityArrow.PickupStatus.DISALLOWED;
                    break label16;
            }
        }

        this.pickup = entityarrow_pickupstatus;
    }

    @Override
    public void playerTouch(EntityHuman entityhuman) {
        if (!this.level().isClientSide && (this.isInGround() || this.isNoPhysics()) && this.shakeTime <= 0) {
            if (this.tryPickup(entityhuman)) {
                entityhuman.take(this, 1);
                this.discard();
            }

        }
    }

    protected boolean tryPickup(EntityHuman entityhuman) {
        boolean flag;

        switch (this.pickup.ordinal()) {
            case 0:
                flag = false;
                break;
            case 1:
                flag = entityhuman.getInventory().add(this.getPickupItem());
                break;
            case 2:
                flag = entityhuman.hasInfiniteMaterials();
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return flag;
    }

    protected ItemStack getPickupItem() {
        return this.pickupItemStack.copy();
    }

    protected abstract ItemStack getDefaultPickupItem();

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    public ItemStack getPickupItemStackOrigin() {
        return this.pickupItemStack;
    }

    public void setBaseDamage(double d0) {
        this.baseDamage = d0;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    @Override
    public boolean isAttackable() {
        return this.getType().is(TagsEntity.REDIRECTABLE_PROJECTILE);
    }

    public void setCritArrow(boolean flag) {
        this.setFlag(1, flag);
    }

    public void setPierceLevel(byte b0) {
        this.entityData.set(EntityArrow.PIERCE_LEVEL, b0);
    }

    private void setFlag(int i, boolean flag) {
        byte b0 = (Byte) this.entityData.get(EntityArrow.ID_FLAGS);

        if (flag) {
            this.entityData.set(EntityArrow.ID_FLAGS, (byte) (b0 | i));
        } else {
            this.entityData.set(EntityArrow.ID_FLAGS, (byte) (b0 & ~i));
        }

    }

    protected void setPickupItemStack(ItemStack itemstack) {
        if (!itemstack.isEmpty()) {
            this.pickupItemStack = itemstack;
        } else {
            this.pickupItemStack = this.getDefaultPickupItem();
        }

    }

    public boolean isCritArrow() {
        byte b0 = (Byte) this.entityData.get(EntityArrow.ID_FLAGS);

        return (b0 & 1) != 0;
    }

    public byte getPierceLevel() {
        return (Byte) this.entityData.get(EntityArrow.PIERCE_LEVEL);
    }

    public void setBaseDamageFromMob(float f) {
        this.setBaseDamage((double) (f * 2.0F) + this.random.triangle((double) this.level().getDifficulty().getId() * 0.11D, 0.57425D));
    }

    protected float getWaterInertia() {
        return 0.6F;
    }

    public void setNoPhysics(boolean flag) {
        this.noPhysics = flag;
        this.setFlag(2, flag);
    }

    public boolean isNoPhysics() {
        return !this.level().isClientSide ? this.noPhysics : ((Byte) this.entityData.get(EntityArrow.ID_FLAGS) & 2) != 0;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.isInGround();
    }

    @Override
    public SlotAccess getSlot(int i) {
        return i == 0 ? SlotAccess.of(this::getPickupItemStackOrigin, this::setPickupItemStack) : super.getSlot(i);
    }

    @Override
    protected boolean shouldBounceOnWorldBorder() {
        return true;
    }

    public static enum PickupStatus {

        DISALLOWED, ALLOWED, CREATIVE_ONLY;

        private PickupStatus() {}

        public static EntityArrow.PickupStatus byOrdinal(int i) {
            if (i < 0 || i > values().length) {
                i = 0;
            }

            return values()[i];
        }
    }
}
