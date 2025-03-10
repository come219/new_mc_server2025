package net.minecraft.world.entity.animal;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;

public class EntitySalmon extends EntityFishSchool implements VariantHolder<EntitySalmon.a> {

    private static final String TAG_TYPE = "type";
    private static final DataWatcherObject<Integer> DATA_TYPE = DataWatcher.defineId(EntitySalmon.class, DataWatcherRegistry.INT);

    public EntitySalmon(EntityTypes<? extends EntitySalmon> entitytypes, World world) {
        super(entitytypes, world);
        this.refreshDimensions();
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.SALMON_BUCKET);
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.SALMON_AMBIENT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.SALMON_DEATH;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.SALMON_HURT;
    }

    @Override
    protected SoundEffect getFlopSound() {
        return SoundEffects.SALMON_FLOP;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntitySalmon.DATA_TYPE, EntitySalmon.a.MEDIUM.id());
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (EntitySalmon.DATA_TYPE.equals(datawatcherobject)) {
            this.refreshDimensions();
        }

    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putString("type", this.getVariant().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setVariant(EntitySalmon.a.byName(nbttagcompound.getString("type")));
    }

    @Override
    public void saveToBucketTag(ItemStack itemstack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemstack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemstack, (nbttagcompound) -> {
            nbttagcompound.putString("type", this.getVariant().getSerializedName());
        });
    }

    @Override
    public void loadFromBucketTag(NBTTagCompound nbttagcompound) {
        Bucketable.loadDefaultDataFromBucketTag(this, nbttagcompound);
        this.setVariant(EntitySalmon.a.byName(nbttagcompound.getString("type")));
    }

    public void setVariant(EntitySalmon.a entitysalmon_a) {
        this.entityData.set(EntitySalmon.DATA_TYPE, entitysalmon_a.id);
    }

    @Override
    public EntitySalmon.a getVariant() {
        return (EntitySalmon.a) EntitySalmon.a.BY_ID.apply((Integer) this.entityData.get(EntitySalmon.DATA_TYPE));
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        SimpleWeightedRandomList.a<EntitySalmon.a> simpleweightedrandomlist_a = SimpleWeightedRandomList.builder();

        simpleweightedrandomlist_a.add(EntitySalmon.a.SMALL, 30);
        simpleweightedrandomlist_a.add(EntitySalmon.a.MEDIUM, 50);
        simpleweightedrandomlist_a.add(EntitySalmon.a.LARGE, 15);
        simpleweightedrandomlist_a.build().getRandomValue(this.random).ifPresent(this::setVariant);
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    public float getSalmonScale() {
        return this.getVariant().boundingBoxScale;
    }

    @Override
    protected EntitySize getDefaultDimensions(EntityPose entitypose) {
        return super.getDefaultDimensions(entitypose).scale(this.getSalmonScale());
    }

    public static enum a implements INamable {

        SMALL("small", 0, 0.5F), MEDIUM("medium", 1, 1.0F), LARGE("large", 2, 1.5F);

        public static final INamable.a<EntitySalmon.a> CODEC = INamable.fromEnum(EntitySalmon.a::values);
        static final IntFunction<EntitySalmon.a> BY_ID = ByIdMap.continuous(EntitySalmon.a::id, values(), ByIdMap.a.CLAMP);
        private final String name;
        final int id;
        final float boundingBoxScale;

        private a(final String s, final int i, final float f) {
            this.name = s;
            this.id = i;
            this.boundingBoxScale = f;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        int id() {
            return this.id;
        }

        static EntitySalmon.a byName(String s) {
            return (EntitySalmon.a) EntitySalmon.a.CODEC.byName(s, (Enum) EntitySalmon.a.MEDIUM);
        }
    }
}
