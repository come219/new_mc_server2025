package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.ARGB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.IShearable;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalBreed;
import net.minecraft.world.entity.ai.goal.PathfinderGoalEatTile;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFollowParent;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand;
import net.minecraft.world.entity.ai.goal.PathfinderGoalTempt;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTables;

public class EntitySheep extends EntityAnimal implements IShearable {

    private static final int EAT_ANIMATION_TICKS = 40;
    private static final DataWatcherObject<Byte> DATA_WOOL_ID = DataWatcher.defineId(EntitySheep.class, DataWatcherRegistry.BYTE);
    private static final Map<EnumColor, Integer> COLOR_BY_DYE = Maps.newEnumMap((Map) Arrays.stream(EnumColor.values()).collect(Collectors.toMap((enumcolor) -> {
        return enumcolor;
    }, EntitySheep::createSheepColor)));
    private int eatAnimationTick;
    private PathfinderGoalEatTile eatBlockGoal;

    private static int createSheepColor(EnumColor enumcolor) {
        if (enumcolor == EnumColor.WHITE) {
            return -1644826;
        } else {
            int i = enumcolor.getTextureDiffuseColor();
            float f = 0.75F;

            return ARGB.color(255, MathHelper.floor((float) ARGB.red(i) * 0.75F), MathHelper.floor((float) ARGB.green(i) * 0.75F), MathHelper.floor((float) ARGB.blue(i) * 0.75F));
        }
    }

    public static int getColor(EnumColor enumcolor) {
        return (Integer) EntitySheep.COLOR_BY_DYE.get(enumcolor);
    }

    public EntitySheep(EntityTypes<? extends EntitySheep> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new PathfinderGoalEatTile(this);
        this.goalSelector.addGoal(0, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(1, new PathfinderGoalPanic(this, 1.25D));
        this.goalSelector.addGoal(2, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.addGoal(3, new PathfinderGoalTempt(this, 1.1D, (itemstack) -> {
            return itemstack.is(TagsItem.SHEEP_FOOD);
        }, false));
        this.goalSelector.addGoal(4, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.addGoal(5, this.eatBlockGoal);
        this.goalSelector.addGoal(6, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.addGoal(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.addGoal(8, new PathfinderGoalRandomLookaround(this));
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.SHEEP_FOOD);
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep(worldserver);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }

        super.aiStep();
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityAnimal.createAnimalAttributes().add(GenericAttributes.MAX_HEALTH, 8.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.23000000417232513D);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntitySheep.DATA_WOOL_ID, (byte) 0);
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(b0);
        }

    }

    public float getHeadEatPositionScale(float f) {
        return this.eatAnimationTick <= 0 ? 0.0F : (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36 ? 1.0F : (this.eatAnimationTick < 4 ? ((float) this.eatAnimationTick - f) / 4.0F : -((float) (this.eatAnimationTick - 40) - f) / 4.0F));
    }

    public float getHeadEatAngleScale(float f) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            float f1 = ((float) (this.eatAnimationTick - 4) - f) / 32.0F;

            return 0.62831855F + 0.21991149F * MathHelper.sin(f1 * 28.7F);
        } else {
            return this.eatAnimationTick > 0 ? 0.62831855F : this.getXRot() * 0.017453292F;
        }
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (itemstack.is(Items.SHEARS)) {
            World world = this.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                if (this.readyForShearing()) {
                    this.shear(worldserver, SoundCategory.PLAYERS, itemstack);
                    this.gameEvent(GameEvent.SHEAR, entityhuman);
                    itemstack.hurtAndBreak(1, entityhuman, getSlotForHand(enumhand));
                    return EnumInteractionResult.SUCCESS_SERVER;
                }
            }

            return EnumInteractionResult.CONSUME;
        } else {
            return super.mobInteract(entityhuman, enumhand);
        }
    }

    @Override
    public void shear(WorldServer worldserver, SoundCategory soundcategory, ItemStack itemstack) {
        worldserver.playSound((EntityHuman) null, (Entity) this, SoundEffects.SHEEP_SHEAR, soundcategory, 1.0F, 1.0F);
        this.dropFromShearingLootTable(worldserver, LootTables.SHEAR_SHEEP, itemstack, (worldserver1, itemstack1) -> {
            for (int i = 0; i < itemstack1.getCount(); ++i) {
                EntityItem entityitem = this.spawnAtLocation(worldserver1, itemstack1.copyWithCount(1), 1.0F);

                if (entityitem != null) {
                    entityitem.setDeltaMovement(entityitem.getDeltaMovement().add((double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double) (this.random.nextFloat() * 0.05F), (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)));
                }
            }

        });
        this.setSheared(true);
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putBoolean("Sheared", this.isSheared());
        nbttagcompound.putByte("Color", (byte) this.getColor().getId());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setSheared(nbttagcompound.getBoolean("Sheared"));
        this.setColor(EnumColor.byId(nbttagcompound.getByte("Color")));
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.SHEEP_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.SHEEP_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.SHEEP_STEP, 0.15F, 1.0F);
    }

    public EnumColor getColor() {
        return EnumColor.byId((Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID) & 15);
    }

    public void setColor(EnumColor enumcolor) {
        byte b0 = (Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID);

        this.entityData.set(EntitySheep.DATA_WOOL_ID, (byte) (b0 & 240 | enumcolor.getId() & 15));
    }

    public boolean isSheared() {
        return ((Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID) & 16) != 0;
    }

    public void setSheared(boolean flag) {
        byte b0 = (Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID);

        if (flag) {
            this.entityData.set(EntitySheep.DATA_WOOL_ID, (byte) (b0 | 16));
        } else {
            this.entityData.set(EntitySheep.DATA_WOOL_ID, (byte) (b0 & -17));
        }

    }

    public static EnumColor getRandomSheepColor(RandomSource randomsource) {
        int i = randomsource.nextInt(100);

        return i < 5 ? EnumColor.BLACK : (i < 10 ? EnumColor.GRAY : (i < 15 ? EnumColor.LIGHT_GRAY : (i < 18 ? EnumColor.BROWN : (randomsource.nextInt(500) == 0 ? EnumColor.PINK : EnumColor.WHITE))));
    }

    @Nullable
    @Override
    public EntitySheep getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        EntitySheep entitysheep = (EntitySheep) EntityTypes.SHEEP.create(worldserver, EntitySpawnReason.BREEDING);

        if (entitysheep != null) {
            EnumColor enumcolor = this.getColor();
            EnumColor enumcolor1 = ((EntitySheep) entityageable).getColor();

            entitysheep.setColor(EnumColor.getMixedColor(worldserver, enumcolor, enumcolor1));
        }

        return entitysheep;
    }

    @Override
    public void ate() {
        super.ate();
        this.setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }

    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        this.setColor(getRandomSheepColor(worldaccess.getRandom()));
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }
}
