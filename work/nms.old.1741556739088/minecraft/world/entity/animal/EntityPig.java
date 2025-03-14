package net.minecraft.world.entity.animal;

import com.google.common.collect.UnmodifiableIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ISaddleable;
import net.minecraft.world.entity.ISteerable;
import net.minecraft.world.entity.SaddleStorage;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalBreed;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFollowParent;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand;
import net.minecraft.world.entity.ai.goal.PathfinderGoalTempt;
import net.minecraft.world.entity.monster.EntityPigZombie;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.DismountUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public class EntityPig extends EntityAnimal implements ISteerable, ISaddleable {

    private static final DataWatcherObject<Boolean> DATA_SADDLE_ID = DataWatcher.defineId(EntityPig.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Integer> DATA_BOOST_TIME = DataWatcher.defineId(EntityPig.class, DataWatcherRegistry.INT);
    public final SaddleStorage steering;

    public EntityPig(EntityTypes<? extends EntityPig> entitytypes, World world) {
        super(entitytypes, world);
        this.steering = new SaddleStorage(this.entityData, EntityPig.DATA_BOOST_TIME, EntityPig.DATA_SADDLE_ID);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(1, new PathfinderGoalPanic(this, 1.25D));
        this.goalSelector.addGoal(3, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.addGoal(4, new PathfinderGoalTempt(this, 1.2D, (itemstack) -> {
            return itemstack.is(Items.CARROT_ON_A_STICK);
        }, false));
        this.goalSelector.addGoal(4, new PathfinderGoalTempt(this, 1.2D, (itemstack) -> {
            return itemstack.is(TagsItem.PIG_FOOD);
        }, false));
        this.goalSelector.addGoal(5, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.addGoal(6, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.addGoal(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.addGoal(8, new PathfinderGoalRandomLookaround(this));
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityAnimal.createAnimalAttributes().add(GenericAttributes.MAX_HEALTH, 10.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.25D);
    }

    @Nullable
    @Override
    public EntityLiving getControllingPassenger() {
        if (this.isSaddled()) {
            Entity entity = this.getFirstPassenger();

            if (entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity;

                if (entityhuman.isHolding(Items.CARROT_ON_A_STICK)) {
                    return entityhuman;
                }
            }
        }

        return super.getControllingPassenger();
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (EntityPig.DATA_BOOST_TIME.equals(datawatcherobject) && this.level().isClientSide) {
            this.steering.onSynced();
        }

        super.onSyncedDataUpdated(datawatcherobject);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityPig.DATA_SADDLE_ID, false);
        datawatcher_a.define(EntityPig.DATA_BOOST_TIME, 0);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        this.steering.addAdditionalSaveData(nbttagcompound);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.steering.readAdditionalSaveData(nbttagcompound);
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.PIG_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.PIG_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.PIG_STEP, 0.15F, 1.0F);
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        boolean flag = this.isFood(entityhuman.getItemInHand(enumhand));

        if (!flag && this.isSaddled() && !this.isVehicle() && !entityhuman.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                entityhuman.startRiding(this);
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            EnumInteractionResult enuminteractionresult = super.mobInteract(entityhuman, enumhand);

            if (!enuminteractionresult.consumesAction()) {
                ItemStack itemstack = entityhuman.getItemInHand(enumhand);

                return (EnumInteractionResult) (itemstack.is(Items.SADDLE) ? itemstack.interactLivingEntity(entityhuman, this, enumhand) : EnumInteractionResult.PASS);
            } else {
                return enuminteractionresult;
            }
        }
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    protected void dropEquipment(WorldServer worldserver) {
        super.dropEquipment(worldserver);
        if (this.isSaddled()) {
            this.spawnAtLocation(worldserver, (IMaterial) Items.SADDLE);
        }

    }

    @Override
    public boolean isSaddled() {
        return this.steering.hasSaddle();
    }

    @Override
    public void equipSaddle(ItemStack itemstack, @Nullable SoundCategory soundcategory) {
        this.steering.setSaddle(true);
        if (soundcategory != null) {
            this.level().playSound((EntityHuman) null, (Entity) this, SoundEffects.PIG_SADDLE, soundcategory, 0.5F, 1.0F);
        }

    }

    @Override
    public Vec3D getDismountLocationForPassenger(EntityLiving entityliving) {
        EnumDirection enumdirection = this.getMotionDirection();

        if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
            return super.getDismountLocationForPassenger(entityliving);
        } else {
            int[][] aint = DismountUtil.offsetsForDirection(enumdirection);
            BlockPosition blockposition = this.blockPosition();
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            UnmodifiableIterator unmodifiableiterator = entityliving.getDismountPoses().iterator();

            while (unmodifiableiterator.hasNext()) {
                EntityPose entitypose = (EntityPose) unmodifiableiterator.next();
                AxisAlignedBB axisalignedbb = entityliving.getLocalBoundsForPose(entitypose);
                int[][] aint1 = aint;
                int i = aint.length;

                for (int j = 0; j < i; ++j) {
                    int[] aint2 = aint1[j];

                    blockposition_mutableblockposition.set(blockposition.getX() + aint2[0], blockposition.getY(), blockposition.getZ() + aint2[1]);
                    double d0 = this.level().getBlockFloorHeight(blockposition_mutableblockposition);

                    if (DismountUtil.isBlockFloorValid(d0)) {
                        Vec3D vec3d = Vec3D.upFromBottomCenterOf(blockposition_mutableblockposition, d0);

                        if (DismountUtil.canDismountTo(this.level(), entityliving, axisalignedbb.move(vec3d))) {
                            entityliving.setPose(entitypose);
                            return vec3d;
                        }
                    }
                }
            }

            return super.getDismountLocationForPassenger(entityliving);
        }
    }

    @Override
    public void thunderHit(WorldServer worldserver, EntityLightning entitylightning) {
        if (worldserver.getDifficulty() != EnumDifficulty.PEACEFUL) {
            EntityPigZombie entitypigzombie = (EntityPigZombie) this.convertTo(EntityTypes.ZOMBIFIED_PIGLIN, ConversionParams.single(this, false, true), (entitypigzombie1) -> {
                if (this.getMainHandItem().isEmpty()) {
                    entitypigzombie1.setItemSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                }

                entitypigzombie1.setPersistenceRequired();
            // CraftBukkit start
            }, null, null);
            if (CraftEventFactory.callPigZapEvent(this, entitylightning, entitypigzombie).isCancelled()) {
                return;
            }
            worldserver.addFreshEntity(entitypigzombie, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.LIGHTNING);
            this.discard(EntityRemoveEvent.Cause.TRANSFORMATION); // CraftBukkit - add Bukkit remove cause
            // CraftBukkit end

            if (entitypigzombie == null) {
                super.thunderHit(worldserver, entitylightning);
            }
        } else {
            super.thunderHit(worldserver, entitylightning);
        }

    }

    @Override
    protected void tickRidden(EntityHuman entityhuman, Vec3D vec3d) {
        super.tickRidden(entityhuman, vec3d);
        this.setRot(entityhuman.getYRot(), entityhuman.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.steering.tickBoost();
    }

    @Override
    protected Vec3D getRiddenInput(EntityHuman entityhuman, Vec3D vec3d) {
        return new Vec3D(0.0D, 0.0D, 1.0D);
    }

    @Override
    protected float getRiddenSpeed(EntityHuman entityhuman) {
        return (float) (this.getAttributeValue(GenericAttributes.MOVEMENT_SPEED) * 0.225D * (double) this.steering.boostFactor());
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Nullable
    @Override
    public EntityPig getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        return (EntityPig) EntityTypes.PIG.create(worldserver, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.PIG_FOOD);
    }

    @Override
    public Vec3D getLeashOffset() {
        return new Vec3D(0.0D, (double) (0.6F * this.getEyeHeight()), (double) (this.getBbWidth() * 0.4F));
    }
}
