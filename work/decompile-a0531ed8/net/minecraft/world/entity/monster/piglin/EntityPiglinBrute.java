package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.state.IBlockData;

public class EntityPiglinBrute extends EntityPiglinAbstract {

    private static final int MAX_HEALTH = 50;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35F;
    private static final int ATTACK_DAMAGE = 7;
    private static final double TARGETING_RANGE = 12.0D;
    protected static final ImmutableList<SensorType<? extends Sensor<? super EntityPiglinBrute>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_BRUTE_SPECIFIC_SENSOR);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, new MemoryModuleType[]{MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.HOME});

    public EntityPiglinBrute(EntityTypes<? extends EntityPiglinBrute> entitytypes, World world) {
        super(entitytypes, world);
        this.xpReward = 20;
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityMonster.createMonsterAttributes().add(GenericAttributes.MAX_HEALTH, 50.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.3499999940395355D).add(GenericAttributes.ATTACK_DAMAGE, 7.0D).add(GenericAttributes.FOLLOW_RANGE, 12.0D);
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        PiglinBruteAI.initMemories(this);
        this.populateDefaultEquipmentSlots(worldaccess.getRandom(), difficultydamagescaler);
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        this.setItemSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
    }

    @Override
    protected BehaviorController.b<EntityPiglinBrute> brainProvider() {
        return BehaviorController.provider(EntityPiglinBrute.MEMORY_TYPES, EntityPiglinBrute.SENSOR_TYPES);
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinBruteAI.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public BehaviorController<EntityPiglinBrute> getBrain() {
        return super.getBrain();
    }

    @Override
    public boolean canHunt() {
        return false;
    }

    @Override
    public boolean wantsToPickUp(WorldServer worldserver, ItemStack itemstack) {
        return itemstack.is(Items.GOLDEN_AXE) ? super.wantsToPickUp(worldserver, itemstack) : false;
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("piglinBruteBrain");
        this.getBrain().tick(worldserver, this);
        gameprofilerfiller.pop();
        PiglinBruteAI.updateActivity(this);
        PiglinBruteAI.maybePlayActivitySound(this);
        super.customServerAiStep(worldserver);
    }

    @Override
    public EntityPiglinArmPose getArmPose() {
        return this.isAggressive() && this.isHoldingMeleeWeapon() ? EntityPiglinArmPose.ATTACKING_WITH_MELEE_WEAPON : EntityPiglinArmPose.DEFAULT;
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        boolean flag = super.hurtServer(worldserver, damagesource, f);

        if (flag) {
            Entity entity = damagesource.getEntity();

            if (entity instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) entity;

                PiglinBruteAI.wasHurtBy(worldserver, this, entityliving);
            }
        }

        return flag;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.PIGLIN_BRUTE_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.PIGLIN_BRUTE_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.PIGLIN_BRUTE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.PIGLIN_BRUTE_STEP, 0.15F, 1.0F);
    }

    protected void playAngrySound() {
        this.makeSound(SoundEffects.PIGLIN_BRUTE_ANGRY);
    }

    @Override
    protected void playConvertedSound() {
        this.makeSound(SoundEffects.PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED);
    }
}
