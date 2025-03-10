package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.ParticleParamItem;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutRemoveEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsEntity;
import net.minecraft.tags.TagsFluid;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.CombatMath;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeDefaults;
import net.minecraft.world.entity.ai.attributes.AttributeMapBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.animal.EntityBird;
import net.minecraft.world.entity.animal.EntityWolf;
import net.minecraft.world.entity.boss.wither.EntityWither;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemAxe;
import net.minecraft.world.item.ItemShield;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.BlockHoney;
import net.minecraft.world.level.block.BlockLadder;
import net.minecraft.world.level.block.BlockTrapdoor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import org.slf4j.Logger;

public abstract class EntityLiving extends Entity implements Attackable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_ACTIVE_EFFECTS = "active_effects";
    private static final MinecraftKey SPEED_MODIFIER_POWDER_SNOW_ID = MinecraftKey.withDefaultNamespace("powder_snow");
    private static final MinecraftKey SPRINTING_MODIFIER_ID = MinecraftKey.withDefaultNamespace("sprinting");
    private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(EntityLiving.SPRINTING_MODIFIER_ID, 0.30000001192092896D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final int HAND_SLOTS = 2;
    public static final int ARMOR_SLOTS = 4;
    public static final int EQUIPMENT_SLOT_OFFSET = 98;
    public static final int ARMOR_SLOT_OFFSET = 100;
    public static final int BODY_ARMOR_OFFSET = 105;
    public static final int SWING_DURATION = 6;
    public static final int PLAYER_HURT_EXPERIENCE_TIME = 100;
    private static final int DAMAGE_SOURCE_TIMEOUT = 40;
    public static final double MIN_MOVEMENT_DISTANCE = 0.003D;
    public static final double DEFAULT_BASE_GRAVITY = 0.08D;
    public static final int DEATH_DURATION = 20;
    private static final int TICKS_PER_ELYTRA_FREE_FALL_EVENT = 10;
    private static final int FREE_FALL_EVENTS_PER_ELYTRA_BREAK = 2;
    public static final float BASE_JUMP_POWER = 0.42F;
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 128.0D;
    protected static final int LIVING_ENTITY_FLAG_IS_USING = 1;
    protected static final int LIVING_ENTITY_FLAG_OFF_HAND = 2;
    public static final int LIVING_ENTITY_FLAG_SPIN_ATTACK = 4;
    protected static final DataWatcherObject<Byte> DATA_LIVING_ENTITY_FLAGS = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.BYTE);
    public static final DataWatcherObject<Float> DATA_HEALTH_ID = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.FLOAT);
    private static final DataWatcherObject<List<ParticleParam>> DATA_EFFECT_PARTICLES = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.PARTICLES);
    private static final DataWatcherObject<Boolean> DATA_EFFECT_AMBIENCE_ID = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.BOOLEAN);
    public static final DataWatcherObject<Integer> DATA_ARROW_COUNT_ID = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<Integer> DATA_STINGER_COUNT_ID = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<Optional<BlockPosition>> SLEEPING_POS_ID = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.OPTIONAL_BLOCK_POS);
    private static final int PARTICLE_FREQUENCY_WHEN_INVISIBLE = 15;
    protected static final EntitySize SLEEPING_DIMENSIONS = EntitySize.fixed(0.2F, 0.2F).withEyeHeight(0.2F);
    public static final float EXTRA_RENDER_CULLING_SIZE_WITH_BIG_HAT = 0.5F;
    public static final float DEFAULT_BABY_SCALE = 0.5F;
    public static final String ATTRIBUTES_FIELD = "attributes";
    public static final Predicate<EntityLiving> PLAYER_NOT_WEARING_DISGUISE_ITEM = (entityliving) -> {
        if (entityliving instanceof EntityHuman entityhuman) {
            ItemStack itemstack = entityhuman.getItemBySlot(EnumItemSlot.HEAD);

            return !itemstack.is(TagsItem.GAZE_DISGUISE_EQUIPMENT);
        } else {
            return true;
        }
    };
    private final AttributeMapBase attributes;
    public CombatTracker combatTracker = new CombatTracker(this);
    public final Map<Holder<MobEffectList>, MobEffect> activeEffects = Maps.newHashMap();
    private final NonNullList<ItemStack> lastHandItemStacks;
    private final NonNullList<ItemStack> lastArmorItemStacks;
    private ItemStack lastBodyItemStack;
    public boolean swinging;
    private boolean discardFriction;
    public EnumHand swingingArm;
    public int swingTime;
    public int removeArrowTime;
    public int removeStingerTime;
    public int hurtTime;
    public int hurtDuration;
    public int deathTime;
    public float oAttackAnim;
    public float attackAnim;
    protected int attackStrengthTicker;
    public final WalkAnimationState walkAnimation;
    public int invulnerableDuration;
    public final float timeOffs;
    public final float rotA;
    public float yBodyRot;
    public float yBodyRotO;
    public float yHeadRot;
    public float yHeadRotO;
    public final ElytraAnimationState elytraAnimationState;
    @Nullable
    public EntityHuman lastHurtByPlayer;
    protected int lastHurtByPlayerTime;
    protected boolean dead;
    protected int noActionTime;
    protected float oRun;
    protected float run;
    protected float animStep;
    protected float animStepO;
    protected float rotOffs;
    public float lastHurt;
    protected boolean jumping;
    public float xxa;
    public float yya;
    public float zza;
    protected int lerpSteps;
    protected double lerpX;
    protected double lerpY;
    protected double lerpZ;
    protected double lerpYRot;
    protected double lerpXRot;
    protected double lerpYHeadRot;
    protected int lerpHeadSteps;
    public boolean effectsDirty;
    @Nullable
    public EntityLiving lastHurtByMob;
    public int lastHurtByMobTimestamp;
    @Nullable
    private EntityLiving lastHurtMob;
    private int lastHurtMobTimestamp;
    private float speed;
    private int noJumpDelay;
    private float absorptionAmount;
    protected ItemStack useItem;
    public int useItemRemaining;
    protected int fallFlyTicks;
    private BlockPosition lastPos;
    private Optional<BlockPosition> lastClimbablePos;
    @Nullable
    private DamageSource lastDamageSource;
    private long lastDamageStamp;
    protected int autoSpinAttackTicks;
    protected float autoSpinAttackDmg;
    @Nullable
    protected ItemStack autoSpinAttackItemStack;
    private float swimAmount;
    private float swimAmountO;
    protected BehaviorController<?> brain;
    protected boolean skipDropExperience;
    private final EnumMap<EnumItemSlot, Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>>> activeLocationDependentEnchantments;
    protected float appliedScale;

    protected EntityLiving(EntityTypes<? extends EntityLiving> entitytypes, World world) {
        super(entitytypes, world);
        this.lastHandItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
        this.lastArmorItemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
        this.lastBodyItemStack = ItemStack.EMPTY;
        this.discardFriction = false;
        this.walkAnimation = new WalkAnimationState();
        this.invulnerableDuration = 20;
        this.elytraAnimationState = new ElytraAnimationState(this);
        this.effectsDirty = true;
        this.useItem = ItemStack.EMPTY;
        this.lastClimbablePos = Optional.empty();
        this.activeLocationDependentEnchantments = new EnumMap(EnumItemSlot.class);
        this.appliedScale = 1.0F;
        this.attributes = new AttributeMapBase(AttributeDefaults.getSupplier(entitytypes));
        this.setHealth(this.getMaxHealth());
        this.blocksBuilding = true;
        this.rotA = (float) ((Math.random() + 1.0D) * 0.009999999776482582D);
        this.reapplyPosition();
        this.timeOffs = (float) Math.random() * 12398.0F;
        this.setYRot((float) (Math.random() * 6.2831854820251465D));
        this.yHeadRot = this.getYRot();
        DynamicOpsNBT dynamicopsnbt = DynamicOpsNBT.INSTANCE;

        this.brain = this.makeBrain(new Dynamic(dynamicopsnbt, (NBTBase) dynamicopsnbt.createMap((Map) ImmutableMap.of(dynamicopsnbt.createString("memories"), (NBTBase) dynamicopsnbt.emptyMap()))));
    }

    public BehaviorController<?> getBrain() {
        return this.brain;
    }

    protected BehaviorController.b<?> brainProvider() {
        return BehaviorController.provider(ImmutableList.of(), ImmutableList.of());
    }

    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return this.brainProvider().makeBrain(dynamic);
    }

    @Override
    public void kill(WorldServer worldserver) {
        this.hurtServer(worldserver, this.damageSources().genericKill(), Float.MAX_VALUE);
    }

    public boolean canAttackType(EntityTypes<?> entitytypes) {
        return true;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityLiving.DATA_LIVING_ENTITY_FLAGS, (byte) 0);
        datawatcher_a.define(EntityLiving.DATA_EFFECT_PARTICLES, List.of());
        datawatcher_a.define(EntityLiving.DATA_EFFECT_AMBIENCE_ID, false);
        datawatcher_a.define(EntityLiving.DATA_ARROW_COUNT_ID, 0);
        datawatcher_a.define(EntityLiving.DATA_STINGER_COUNT_ID, 0);
        datawatcher_a.define(EntityLiving.DATA_HEALTH_ID, 1.0F);
        datawatcher_a.define(EntityLiving.SLEEPING_POS_ID, Optional.empty());
    }

    public static AttributeProvider.Builder createLivingAttributes() {
        return AttributeProvider.builder().add(GenericAttributes.MAX_HEALTH).add(GenericAttributes.KNOCKBACK_RESISTANCE).add(GenericAttributes.MOVEMENT_SPEED).add(GenericAttributes.ARMOR).add(GenericAttributes.ARMOR_TOUGHNESS).add(GenericAttributes.MAX_ABSORPTION).add(GenericAttributes.STEP_HEIGHT).add(GenericAttributes.SCALE).add(GenericAttributes.GRAVITY).add(GenericAttributes.SAFE_FALL_DISTANCE).add(GenericAttributes.FALL_DAMAGE_MULTIPLIER).add(GenericAttributes.JUMP_STRENGTH).add(GenericAttributes.OXYGEN_BONUS).add(GenericAttributes.BURNING_TIME).add(GenericAttributes.EXPLOSION_KNOCKBACK_RESISTANCE).add(GenericAttributes.WATER_MOVEMENT_EFFICIENCY).add(GenericAttributes.MOVEMENT_EFFICIENCY).add(GenericAttributes.ATTACK_KNOCKBACK);
    }

    @Override
    protected void checkFallDamage(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {
        if (!this.isInWater()) {
            this.updateInWaterStateAndDoWaterCurrentPushing();
        }

        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (flag && this.fallDistance > 0.0F) {
                this.onChangedBlock(worldserver, blockposition);
                double d1 = this.getAttributeValue(GenericAttributes.SAFE_FALL_DISTANCE);

                if ((double) this.fallDistance > d1 && !iblockdata.isAir()) {
                    double d2 = this.getX();
                    double d3 = this.getY();
                    double d4 = this.getZ();
                    BlockPosition blockposition1 = this.blockPosition();

                    if (blockposition.getX() != blockposition1.getX() || blockposition.getZ() != blockposition1.getZ()) {
                        double d5 = d2 - (double) blockposition.getX() - 0.5D;
                        double d6 = d4 - (double) blockposition.getZ() - 0.5D;
                        double d7 = Math.max(Math.abs(d5), Math.abs(d6));

                        d2 = (double) blockposition.getX() + 0.5D + d5 / d7 * 0.5D;
                        d4 = (double) blockposition.getZ() + 0.5D + d6 / d7 * 0.5D;
                    }

                    float f = (float) MathHelper.ceil((double) this.fallDistance - d1);
                    double d8 = Math.min((double) (0.2F + f / 15.0F), 2.5D);
                    int i = (int) (150.0D * d8);

                    worldserver.sendParticles(new ParticleParamBlock(Particles.BLOCK, iblockdata), d2, d3, d4, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D);
                }
            }
        }

        super.checkFallDamage(d0, flag, iblockdata, blockposition);
        if (flag) {
            this.lastClimbablePos = Optional.empty();
        }

    }

    public final boolean canBreatheUnderwater() {
        return this.getType().is(TagsEntity.CAN_BREATHE_UNDER_WATER);
    }

    public float getSwimAmount(float f) {
        return MathHelper.lerp(f, this.swimAmountO, this.swimAmount);
    }

    public boolean hasLandedInLiquid() {
        return this.getDeltaMovement().y() < 9.999999747378752E-6D && this.isInLiquid();
    }

    @Override
    public void baseTick() {
        this.oAttackAnim = this.attackAnim;
        if (this.firstTick) {
            this.getSleepingPos().ifPresent(this::setPosToBed);
        }

        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            EnchantmentManager.tickEffects(worldserver, this);
        }

        super.baseTick();
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("livingEntityBaseTick");
        if (this.fireImmune() || this.level().isClientSide) {
            this.clearFire();
        }

        if (this.isAlive()) {
            boolean flag = this instanceof EntityHuman;
            World world1 = this.level();
            WorldServer worldserver1;
            double d0;

            if (world1 instanceof WorldServer) {
                worldserver1 = (WorldServer) world1;
                if (this.isInWall()) {
                    this.hurtServer(worldserver1, this.damageSources().inWall(), 1.0F);
                } else if (flag && !this.level().getWorldBorder().isWithinBounds(this.getBoundingBox())) {
                    double d1 = this.level().getWorldBorder().getDistanceToBorder(this) + this.level().getWorldBorder().getDamageSafeZone();

                    if (d1 < 0.0D) {
                        d0 = this.level().getWorldBorder().getDamagePerBlock();
                        if (d0 > 0.0D) {
                            this.hurtServer(worldserver1, this.damageSources().outOfBorder(), (float) Math.max(1, MathHelper.floor(-d1 * d0)));
                        }
                    }
                }
            }

            if (this.isEyeInFluid(TagsFluid.WATER) && !this.level().getBlockState(BlockPosition.containing(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
                boolean flag1 = !this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && (!flag || !((EntityHuman) this).getAbilities().invulnerable);

                if (flag1) {
                    this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
                    if (this.getAirSupply() == -20) {
                        this.setAirSupply(0);
                        Vec3D vec3d = this.getDeltaMovement();

                        for (int i = 0; i < 8; ++i) {
                            d0 = this.random.nextDouble() - this.random.nextDouble();
                            double d2 = this.random.nextDouble() - this.random.nextDouble();
                            double d3 = this.random.nextDouble() - this.random.nextDouble();

                            this.level().addParticle(Particles.BUBBLE, this.getX() + d0, this.getY() + d2, this.getZ() + d3, vec3d.x, vec3d.y, vec3d.z);
                        }

                        this.hurt(this.damageSources().drown(), 2.0F);
                    }
                } else if (this.getAirSupply() < this.getMaxAirSupply()) {
                    this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
                }

                if (!this.level().isClientSide && this.isPassenger() && this.getVehicle() != null && this.getVehicle().dismountsUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            }

            world1 = this.level();
            if (world1 instanceof WorldServer) {
                worldserver1 = (WorldServer) world1;
                BlockPosition blockposition = this.blockPosition();

                if (!Objects.equal(this.lastPos, blockposition)) {
                    this.lastPos = blockposition;
                    this.onChangedBlock(worldserver1, blockposition);
                }
            }
        }

        if (this.isAlive() && (this.isInWaterRainOrBubble() || this.isInPowderSnow)) {
            this.extinguishFire();
        }

        if (this.hurtTime > 0) {
            --this.hurtTime;
        }

        if (this.invulnerableTime > 0 && !(this instanceof EntityPlayer)) {
            --this.invulnerableTime;
        }

        if (this.isDeadOrDying() && this.level().shouldTickDeath(this)) {
            this.tickDeath();
        }

        if (this.lastHurtByPlayerTime > 0) {
            --this.lastHurtByPlayerTime;
        } else {
            this.lastHurtByPlayer = null;
        }

        if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
            this.lastHurtMob = null;
        }

        if (this.lastHurtByMob != null) {
            if (!this.lastHurtByMob.isAlive()) {
                this.setLastHurtByMob((EntityLiving) null);
            } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                this.setLastHurtByMob((EntityLiving) null);
            }
        }

        this.tickEffects();
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        gameprofilerfiller.pop();
    }

    @Override
    protected float getBlockSpeedFactor() {
        return MathHelper.lerp((float) this.getAttributeValue(GenericAttributes.MOVEMENT_EFFICIENCY), super.getBlockSpeedFactor(), 1.0F);
    }

    protected void removeFrost() {
        AttributeModifiable attributemodifiable = this.getAttribute(GenericAttributes.MOVEMENT_SPEED);

        if (attributemodifiable != null) {
            if (attributemodifiable.getModifier(EntityLiving.SPEED_MODIFIER_POWDER_SNOW_ID) != null) {
                attributemodifiable.removeModifier(EntityLiving.SPEED_MODIFIER_POWDER_SNOW_ID);
            }

        }
    }

    protected void tryAddFrost() {
        if (!this.getBlockStateOnLegacy().isAir()) {
            int i = this.getTicksFrozen();

            if (i > 0) {
                AttributeModifiable attributemodifiable = this.getAttribute(GenericAttributes.MOVEMENT_SPEED);

                if (attributemodifiable == null) {
                    return;
                }

                float f = -0.05F * this.getPercentFrozen();

                attributemodifiable.addTransientModifier(new AttributeModifier(EntityLiving.SPEED_MODIFIER_POWDER_SNOW_ID, (double) f, AttributeModifier.Operation.ADD_VALUE));
            }
        }

    }

    protected void onChangedBlock(WorldServer worldserver, BlockPosition blockposition) {
        EnchantmentManager.runLocationChangedEffects(worldserver, this);
    }

    public boolean isBaby() {
        return false;
    }

    public float getAgeScale() {
        return this.isBaby() ? 0.5F : 1.0F;
    }

    public final float getScale() {
        AttributeMapBase attributemapbase = this.getAttributes();

        return attributemapbase == null ? 1.0F : this.sanitizeScale((float) attributemapbase.getValue(GenericAttributes.SCALE));
    }

    protected float sanitizeScale(float f) {
        return f;
    }

    protected boolean isAffectedByFluids() {
        return true;
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }

    }

    public boolean shouldDropExperience() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot() {
        return !this.isBaby();
    }

    protected int decreaseAirSupply(int i) {
        AttributeModifiable attributemodifiable = this.getAttribute(GenericAttributes.OXYGEN_BONUS);
        double d0;

        if (attributemodifiable != null) {
            d0 = attributemodifiable.getValue();
        } else {
            d0 = 0.0D;
        }

        return d0 > 0.0D && this.random.nextDouble() >= 1.0D / (d0 + 1.0D) ? i : i - 1;
    }

    protected int increaseAirSupply(int i) {
        return Math.min(i + 4, this.getMaxAirSupply());
    }

    public final int getExperienceReward(WorldServer worldserver, @Nullable Entity entity) {
        return EnchantmentManager.processMobExperience(worldserver, entity, this, this.getBaseExperienceReward(worldserver));
    }

    protected int getBaseExperienceReward(WorldServer worldserver) {
        return 0;
    }

    protected boolean isAlwaysExperienceDropper() {
        return false;
    }

    @Nullable
    public EntityLiving getLastHurtByMob() {
        return this.lastHurtByMob;
    }

    @Override
    public EntityLiving getLastAttacker() {
        return this.getLastHurtByMob();
    }

    public int getLastHurtByMobTimestamp() {
        return this.lastHurtByMobTimestamp;
    }

    public void setLastHurtByPlayer(@Nullable EntityHuman entityhuman) {
        this.lastHurtByPlayer = entityhuman;
        this.lastHurtByPlayerTime = this.tickCount;
    }

    public void setLastHurtByMob(@Nullable EntityLiving entityliving) {
        this.lastHurtByMob = entityliving;
        this.lastHurtByMobTimestamp = this.tickCount;
    }

    @Nullable
    public EntityLiving getLastHurtMob() {
        return this.lastHurtMob;
    }

    public int getLastHurtMobTimestamp() {
        return this.lastHurtMobTimestamp;
    }

    public void setLastHurtMob(Entity entity) {
        if (entity instanceof EntityLiving) {
            this.lastHurtMob = (EntityLiving) entity;
        } else {
            this.lastHurtMob = null;
        }

        this.lastHurtMobTimestamp = this.tickCount;
    }

    public int getNoActionTime() {
        return this.noActionTime;
    }

    public void setNoActionTime(int i) {
        this.noActionTime = i;
    }

    public boolean shouldDiscardFriction() {
        return this.discardFriction;
    }

    public void setDiscardFriction(boolean flag) {
        this.discardFriction = flag;
    }

    protected boolean doesEmitEquipEvent(EnumItemSlot enumitemslot) {
        return true;
    }

    public void onEquipItem(EnumItemSlot enumitemslot, ItemStack itemstack, ItemStack itemstack1) {
        if (!this.level().isClientSide() && !this.isSpectator()) {
            boolean flag = itemstack1.isEmpty() && itemstack.isEmpty();

            if (!flag && !ItemStack.isSameItemSameComponents(itemstack, itemstack1) && !this.firstTick) {
                Equippable equippable = (Equippable) itemstack1.get(DataComponents.EQUIPPABLE);

                if (!this.isSilent() && equippable != null && enumitemslot == equippable.slot()) {
                    this.level().playSeededSound((EntityHuman) null, this.getX(), this.getY(), this.getZ(), equippable.equipSound(), this.getSoundSource(), 1.0F, 1.0F, this.random.nextLong());
                }

                if (this.doesEmitEquipEvent(enumitemslot)) {
                    this.gameEvent(equippable != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
                }

            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason entity_removalreason) {
        if (entity_removalreason == Entity.RemovalReason.KILLED || entity_removalreason == Entity.RemovalReason.DISCARDED) {
            World world = this.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                this.triggerOnDeathMobEffects(worldserver, entity_removalreason);
            }
        }

        super.remove(entity_removalreason);
        this.brain.clearMemories();
    }

    protected void triggerOnDeathMobEffects(WorldServer worldserver, Entity.RemovalReason entity_removalreason) {
        Iterator iterator = this.getActiveEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            mobeffect.onMobRemoved(worldserver, this, entity_removalreason);
        }

        this.activeEffects.clear();
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.putFloat("Health", this.getHealth());
        nbttagcompound.putShort("HurtTime", (short) this.hurtTime);
        nbttagcompound.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
        nbttagcompound.putShort("DeathTime", (short) this.deathTime);
        nbttagcompound.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        nbttagcompound.put("attributes", this.getAttributes().save());
        if (!this.activeEffects.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            Iterator iterator = this.activeEffects.values().iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                nbttaglist.add(mobeffect.save());
            }

            nbttagcompound.put("active_effects", nbttaglist);
        }

        nbttagcompound.putBoolean("FallFlying", this.isFallFlying());
        this.getSleepingPos().ifPresent((blockposition) -> {
            nbttagcompound.putInt("SleepingX", blockposition.getX());
            nbttagcompound.putInt("SleepingY", blockposition.getY());
            nbttagcompound.putInt("SleepingZ", blockposition.getZ());
        });
        DataResult<NBTBase> dataresult = this.brain.serializeStart(DynamicOpsNBT.INSTANCE);
        Logger logger = EntityLiving.LOGGER;

        java.util.Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
            nbttagcompound.put("Brain", nbtbase);
        });
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.internalSetAbsorptionAmount(nbttagcompound.getFloat("AbsorptionAmount"));
        if (nbttagcompound.contains("attributes", 9) && this.level() != null && !this.level().isClientSide) {
            this.getAttributes().load(nbttagcompound.getList("attributes", 10));
        }

        if (nbttagcompound.contains("active_effects", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("active_effects", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompound(i);
                MobEffect mobeffect = MobEffect.load(nbttagcompound1);

                if (mobeffect != null) {
                    this.activeEffects.put(mobeffect.getEffect(), mobeffect);
                }
            }
        }

        if (nbttagcompound.contains("Health", 99)) {
            this.setHealth(nbttagcompound.getFloat("Health"));
        }

        this.hurtTime = nbttagcompound.getShort("HurtTime");
        this.deathTime = nbttagcompound.getShort("DeathTime");
        this.lastHurtByMobTimestamp = nbttagcompound.getInt("HurtByTimestamp");
        if (nbttagcompound.contains("Team", 8)) {
            String s = nbttagcompound.getString("Team");
            Scoreboard scoreboard = this.level().getScoreboard();
            ScoreboardTeam scoreboardteam = scoreboard.getPlayerTeam(s);
            boolean flag = scoreboardteam != null && scoreboard.addPlayerToTeam(this.getStringUUID(), scoreboardteam);

            if (!flag) {
                EntityLiving.LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", s);
            }
        }

        if (nbttagcompound.getBoolean("FallFlying")) {
            this.setSharedFlag(7, true);
        }

        if (nbttagcompound.contains("SleepingX", 99) && nbttagcompound.contains("SleepingY", 99) && nbttagcompound.contains("SleepingZ", 99)) {
            BlockPosition blockposition = new BlockPosition(nbttagcompound.getInt("SleepingX"), nbttagcompound.getInt("SleepingY"), nbttagcompound.getInt("SleepingZ"));

            this.setSleepingPos(blockposition);
            this.entityData.set(EntityLiving.DATA_POSE, EntityPose.SLEEPING);
            if (!this.firstTick) {
                this.setPosToBed(blockposition);
            }
        }

        if (nbttagcompound.contains("Brain", 10)) {
            this.brain = this.makeBrain(new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound.get("Brain")));
        }

    }

    protected void tickEffects() {
        Iterator<Holder<MobEffectList>> iterator = this.activeEffects.keySet().iterator();

        try {
            while (iterator.hasNext()) {
                Holder<MobEffectList> holder = (Holder) iterator.next();
                MobEffect mobeffect = (MobEffect) this.activeEffects.get(holder);

                if (!mobeffect.tick(this, () -> {
                    this.onEffectUpdated(mobeffect, true, (Entity) null);
                })) {
                    if (!this.level().isClientSide) {
                        iterator.remove();
                        this.onEffectsRemoved(List.of(mobeffect));
                    }
                } else if (mobeffect.getDuration() % 600 == 0) {
                    this.onEffectUpdated(mobeffect, false, (Entity) null);
                }
            }
        } catch (ConcurrentModificationException concurrentmodificationexception) {
            ;
        }

        if (this.effectsDirty) {
            if (!this.level().isClientSide) {
                this.updateInvisibilityStatus();
                this.updateGlowingStatus();
            }

            this.effectsDirty = false;
        }

        List<ParticleParam> list = (List) this.entityData.get(EntityLiving.DATA_EFFECT_PARTICLES);

        if (!list.isEmpty()) {
            boolean flag = (Boolean) this.entityData.get(EntityLiving.DATA_EFFECT_AMBIENCE_ID);
            int i = this.isInvisible() ? 15 : 4;
            int j = flag ? 5 : 1;

            if (this.random.nextInt(i * j) == 0) {
                this.level().addParticle((ParticleParam) SystemUtils.getRandom(list, this.random), this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 1.0D, 1.0D, 1.0D);
            }
        }

    }

    protected void updateInvisibilityStatus() {
        if (this.activeEffects.isEmpty()) {
            this.removeEffectParticles();
            this.setInvisible(false);
        } else {
            this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
            this.updateSynchronizedMobEffectParticles();
        }
    }

    private void updateSynchronizedMobEffectParticles() {
        List<ParticleParam> list = this.activeEffects.values().stream().filter(MobEffect::isVisible).map(MobEffect::getParticleOptions).toList();

        this.entityData.set(EntityLiving.DATA_EFFECT_PARTICLES, list);
        this.entityData.set(EntityLiving.DATA_EFFECT_AMBIENCE_ID, areAllEffectsAmbient(this.activeEffects.values()));
    }

    private void updateGlowingStatus() {
        boolean flag = this.isCurrentlyGlowing();

        if (this.getSharedFlag(6) != flag) {
            this.setSharedFlag(6, flag);
        }

    }

    public double getVisibilityPercent(@Nullable Entity entity) {
        double d0 = 1.0D;

        if (this.isDiscrete()) {
            d0 *= 0.8D;
        }

        if (this.isInvisible()) {
            float f = this.getArmorCoverPercentage();

            if (f < 0.1F) {
                f = 0.1F;
            }

            d0 *= 0.7D * (double) f;
        }

        if (entity != null) {
            ItemStack itemstack = this.getItemBySlot(EnumItemSlot.HEAD);
            EntityTypes<?> entitytypes = entity.getType();

            if (entitytypes == EntityTypes.SKELETON && itemstack.is(Items.SKELETON_SKULL) || entitytypes == EntityTypes.ZOMBIE && itemstack.is(Items.ZOMBIE_HEAD) || entitytypes == EntityTypes.PIGLIN && itemstack.is(Items.PIGLIN_HEAD) || entitytypes == EntityTypes.PIGLIN_BRUTE && itemstack.is(Items.PIGLIN_HEAD) || entitytypes == EntityTypes.CREEPER && itemstack.is(Items.CREEPER_HEAD)) {
                d0 *= 0.5D;
            }
        }

        return d0;
    }

    public boolean canAttack(EntityLiving entityliving) {
        return entityliving instanceof EntityHuman && this.level().getDifficulty() == EnumDifficulty.PEACEFUL ? false : entityliving.canBeSeenAsEnemy();
    }

    public boolean canBeSeenAsEnemy() {
        return !this.isInvulnerable() && this.canBeSeenByAnyone();
    }

    public boolean canBeSeenByAnyone() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean areAllEffectsAmbient(Collection<MobEffect> collection) {
        Iterator iterator = collection.iterator();

        MobEffect mobeffect;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            mobeffect = (MobEffect) iterator.next();
        } while (!mobeffect.isVisible() || mobeffect.isAmbient());

        return false;
    }

    protected void removeEffectParticles() {
        this.entityData.set(EntityLiving.DATA_EFFECT_PARTICLES, List.of());
    }

    public boolean removeAllEffects() {
        if (this.level().isClientSide) {
            return false;
        } else if (this.activeEffects.isEmpty()) {
            return false;
        } else {
            Map<Holder<MobEffectList>, MobEffect> map = Maps.newHashMap(this.activeEffects);

            this.activeEffects.clear();
            this.onEffectsRemoved(map.values());
            return true;
        }
    }

    public Collection<MobEffect> getActiveEffects() {
        return this.activeEffects.values();
    }

    public Map<Holder<MobEffectList>, MobEffect> getActiveEffectsMap() {
        return this.activeEffects;
    }

    public boolean hasEffect(Holder<MobEffectList> holder) {
        return this.activeEffects.containsKey(holder);
    }

    @Nullable
    public MobEffect getEffect(Holder<MobEffectList> holder) {
        return (MobEffect) this.activeEffects.get(holder);
    }

    public final boolean addEffect(MobEffect mobeffect) {
        return this.addEffect(mobeffect, (Entity) null);
    }

    public boolean addEffect(MobEffect mobeffect, @Nullable Entity entity) {
        if (!this.canBeAffected(mobeffect)) {
            return false;
        } else {
            MobEffect mobeffect1 = (MobEffect) this.activeEffects.get(mobeffect.getEffect());
            boolean flag = false;

            if (mobeffect1 == null) {
                this.activeEffects.put(mobeffect.getEffect(), mobeffect);
                this.onEffectAdded(mobeffect, entity);
                flag = true;
                mobeffect.onEffectAdded(this);
            } else if (mobeffect1.update(mobeffect)) {
                this.onEffectUpdated(mobeffect1, true, entity);
                flag = true;
            }

            mobeffect.onEffectStarted(this);
            return flag;
        }
    }

    public boolean canBeAffected(MobEffect mobeffect) {
        return this.getType().is(TagsEntity.IMMUNE_TO_INFESTED) ? !mobeffect.is(MobEffects.INFESTED) : (this.getType().is(TagsEntity.IMMUNE_TO_OOZING) ? !mobeffect.is(MobEffects.OOZING) : (!this.getType().is(TagsEntity.IGNORES_POISON_AND_REGEN) ? true : !mobeffect.is(MobEffects.REGENERATION) && !mobeffect.is(MobEffects.POISON)));
    }

    public void forceAddEffect(MobEffect mobeffect, @Nullable Entity entity) {
        if (this.canBeAffected(mobeffect)) {
            MobEffect mobeffect1 = (MobEffect) this.activeEffects.put(mobeffect.getEffect(), mobeffect);

            if (mobeffect1 == null) {
                this.onEffectAdded(mobeffect, entity);
            } else {
                mobeffect.copyBlendState(mobeffect1);
                this.onEffectUpdated(mobeffect, true, entity);
            }

        }
    }

    public boolean isInvertedHealAndHarm() {
        return this.getType().is(TagsEntity.INVERTED_HEALING_AND_HARM);
    }

    @Nullable
    public MobEffect removeEffectNoUpdate(Holder<MobEffectList> holder) {
        return (MobEffect) this.activeEffects.remove(holder);
    }

    public boolean removeEffect(Holder<MobEffectList> holder) {
        MobEffect mobeffect = this.removeEffectNoUpdate(holder);

        if (mobeffect != null) {
            this.onEffectsRemoved(List.of(mobeffect));
            return true;
        } else {
            return false;
        }
    }

    protected void onEffectAdded(MobEffect mobeffect, @Nullable Entity entity) {
        this.effectsDirty = true;
        if (!this.level().isClientSide) {
            ((MobEffectList) mobeffect.getEffect().value()).addAttributeModifiers(this.getAttributes(), mobeffect.getAmplifier());
            this.sendEffectToPassengers(mobeffect);
        }

    }

    public void sendEffectToPassengers(MobEffect mobeffect) {
        Iterator iterator = this.getPassengers().iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (entity instanceof EntityPlayer entityplayer) {
                entityplayer.connection.send(new PacketPlayOutEntityEffect(this.getId(), mobeffect, false));
            }
        }

    }

    protected void onEffectUpdated(MobEffect mobeffect, boolean flag, @Nullable Entity entity) {
        this.effectsDirty = true;
        if (flag && !this.level().isClientSide) {
            MobEffectList mobeffectlist = (MobEffectList) mobeffect.getEffect().value();

            mobeffectlist.removeAttributeModifiers(this.getAttributes());
            mobeffectlist.addAttributeModifiers(this.getAttributes(), mobeffect.getAmplifier());
            this.refreshDirtyAttributes();
        }

        if (!this.level().isClientSide) {
            this.sendEffectToPassengers(mobeffect);
        }

    }

    protected void onEffectsRemoved(Collection<MobEffect> collection) {
        this.effectsDirty = true;
        if (!this.level().isClientSide) {
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                ((MobEffectList) mobeffect.getEffect().value()).removeAttributeModifiers(this.getAttributes());
                Iterator iterator1 = this.getPassengers().iterator();

                while (iterator1.hasNext()) {
                    Entity entity = (Entity) iterator1.next();

                    if (entity instanceof EntityPlayer) {
                        EntityPlayer entityplayer = (EntityPlayer) entity;

                        entityplayer.connection.send(new PacketPlayOutRemoveEntityEffect(this.getId(), mobeffect.getEffect()));
                    }
                }
            }

            this.refreshDirtyAttributes();
        }

    }

    private void refreshDirtyAttributes() {
        Set<AttributeModifiable> set = this.getAttributes().getAttributesToUpdate();
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            AttributeModifiable attributemodifiable = (AttributeModifiable) iterator.next();

            this.onAttributeUpdated(attributemodifiable.getAttribute());
        }

        set.clear();
    }

    protected void onAttributeUpdated(Holder<AttributeBase> holder) {
        float f;

        if (holder.is(GenericAttributes.MAX_HEALTH)) {
            f = this.getMaxHealth();
            if (this.getHealth() > f) {
                this.setHealth(f);
            }
        } else if (holder.is(GenericAttributes.MAX_ABSORPTION)) {
            f = this.getMaxAbsorption();
            if (this.getAbsorptionAmount() > f) {
                this.setAbsorptionAmount(f);
            }
        }

    }

    public void heal(float f) {
        float f1 = this.getHealth();

        if (f1 > 0.0F) {
            this.setHealth(f1 + f);
        }

    }

    public float getHealth() {
        return (Float) this.entityData.get(EntityLiving.DATA_HEALTH_ID);
    }

    public void setHealth(float f) {
        this.entityData.set(EntityLiving.DATA_HEALTH_ID, MathHelper.clamp(f, 0.0F, this.getMaxHealth()));
    }

    public boolean isDeadOrDying() {
        return this.getHealth() <= 0.0F;
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (this.isInvulnerableTo(worldserver, damagesource)) {
            return false;
        } else if (this.isDeadOrDying()) {
            return false;
        } else if (damagesource.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (this.isSleeping()) {
                this.stopSleeping();
            }

            this.noActionTime = 0;
            if (f < 0.0F) {
                f = 0.0F;
            }

            float f1 = f;
            boolean flag = false;
            float f2 = 0.0F;

            if (f > 0.0F && this.isDamageSourceBlocked(damagesource)) {
                this.hurtCurrentlyUsedShield(f);
                f2 = f;
                f = 0.0F;
                if (!damagesource.is(DamageTypeTags.IS_PROJECTILE)) {
                    Entity entity = damagesource.getDirectEntity();

                    if (entity instanceof EntityLiving) {
                        EntityLiving entityliving = (EntityLiving) entity;

                        this.blockUsingShield(entityliving);
                    }
                }

                flag = true;
            }

            if (damagesource.is(DamageTypeTags.IS_FREEZING) && this.getType().is(TagsEntity.FREEZE_HURTS_EXTRA_TYPES)) {
                f *= 5.0F;
            }

            if (damagesource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EnumItemSlot.HEAD).isEmpty()) {
                this.hurtHelmet(damagesource, f);
                f *= 0.75F;
            }

            this.walkAnimation.setSpeed(1.5F);
            if (Float.isNaN(f) || Float.isInfinite(f)) {
                f = Float.MAX_VALUE;
            }

            boolean flag1 = true;

            if ((float) this.invulnerableTime > 10.0F && !damagesource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
                if (f <= this.lastHurt) {
                    return false;
                }

                this.actuallyHurt(worldserver, damagesource, f - this.lastHurt);
                this.lastHurt = f;
                flag1 = false;
            } else {
                this.lastHurt = f;
                this.invulnerableTime = 20;
                this.actuallyHurt(worldserver, damagesource, f);
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
            }

            this.resolveMobResponsibleForDamage(damagesource);
            this.resolvePlayerResponsibleForDamage(damagesource);
            if (flag1) {
                if (flag) {
                    worldserver.broadcastEntityEvent(this, (byte) 29);
                } else {
                    worldserver.broadcastDamageEvent(this, damagesource);
                }

                if (!damagesource.is(DamageTypeTags.NO_IMPACT) && (!flag || f > 0.0F)) {
                    this.markHurt();
                }

                if (!damagesource.is(DamageTypeTags.NO_KNOCKBACK)) {
                    double d0 = 0.0D;
                    double d1 = 0.0D;
                    Entity entity1 = damagesource.getDirectEntity();

                    if (entity1 instanceof IProjectile) {
                        IProjectile iprojectile = (IProjectile) entity1;
                        DoubleDoubleImmutablePair doubledoubleimmutablepair = iprojectile.calculateHorizontalHurtKnockbackDirection(this, damagesource);

                        d0 = -doubledoubleimmutablepair.leftDouble();
                        d1 = -doubledoubleimmutablepair.rightDouble();
                    } else if (damagesource.getSourcePosition() != null) {
                        d0 = damagesource.getSourcePosition().x() - this.getX();
                        d1 = damagesource.getSourcePosition().z() - this.getZ();
                    }

                    this.knockback(0.4000000059604645D, d0, d1);
                    if (!flag) {
                        this.indicateDamage(d0, d1);
                    }
                }
            }

            if (this.isDeadOrDying()) {
                if (!this.checkTotemDeathProtection(damagesource)) {
                    if (flag1) {
                        this.makeSound(this.getDeathSound());
                    }

                    this.die(damagesource);
                }
            } else if (flag1) {
                this.playHurtSound(damagesource);
            }

            boolean flag2 = !flag || f > 0.0F;

            if (flag2) {
                this.lastDamageSource = damagesource;
                this.lastDamageStamp = this.level().getGameTime();
                Iterator iterator = this.getActiveEffects().iterator();

                while (iterator.hasNext()) {
                    MobEffect mobeffect = (MobEffect) iterator.next();

                    mobeffect.onMobHurt(worldserver, this, damagesource, f);
                }
            }

            EntityPlayer entityplayer;

            if (this instanceof EntityPlayer) {
                entityplayer = (EntityPlayer) this;
                CriterionTriggers.ENTITY_HURT_PLAYER.trigger(entityplayer, damagesource, f1, f, flag);
                if (f2 > 0.0F && f2 < 3.4028235E37F) {
                    entityplayer.awardStat(StatisticList.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f2 * 10.0F));
                }
            }

            Entity entity2 = damagesource.getEntity();

            if (entity2 instanceof EntityPlayer) {
                entityplayer = (EntityPlayer) entity2;
                CriterionTriggers.PLAYER_HURT_ENTITY.trigger(entityplayer, this, damagesource, f1, f, flag);
            }

            return flag2;
        }
    }

    protected void resolveMobResponsibleForDamage(DamageSource damagesource) {
        Entity entity = damagesource.getEntity();

        if (entity instanceof EntityLiving entityliving) {
            if (!damagesource.is(DamageTypeTags.NO_ANGER) && (!damagesource.is(DamageTypes.WIND_CHARGE) || !this.getType().is(TagsEntity.NO_ANGER_FROM_WIND_CHARGE))) {
                this.setLastHurtByMob(entityliving);
            }
        }

    }

    @Nullable
    protected EntityHuman resolvePlayerResponsibleForDamage(DamageSource damagesource) {
        Entity entity = damagesource.getEntity();

        if (entity instanceof EntityHuman entityhuman) {
            this.lastHurtByPlayerTime = 100;
            this.lastHurtByPlayer = entityhuman;
            return entityhuman;
        } else {
            if (entity instanceof EntityWolf entitywolf) {
                if (entitywolf.isTame()) {
                    this.lastHurtByPlayerTime = 100;
                    EntityLiving entityliving = entitywolf.getOwner();

                    if (entityliving instanceof EntityHuman) {
                        EntityHuman entityhuman1 = (EntityHuman) entityliving;

                        this.lastHurtByPlayer = entityhuman1;
                    } else {
                        this.lastHurtByPlayer = null;
                    }

                    return this.lastHurtByPlayer;
                }
            }

            return null;
        }
    }

    protected void blockUsingShield(EntityLiving entityliving) {
        entityliving.blockedByShield(this);
    }

    protected void blockedByShield(EntityLiving entityliving) {
        entityliving.knockback(0.5D, entityliving.getX() - this.getX(), entityliving.getZ() - this.getZ());
    }

    private boolean checkTotemDeathProtection(DamageSource damagesource) {
        if (damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        } else {
            ItemStack itemstack = null;
            DeathProtection deathprotection = null;
            EnumHand[] aenumhand = EnumHand.values();
            int i = aenumhand.length;

            for (int j = 0; j < i; ++j) {
                EnumHand enumhand = aenumhand[j];
                ItemStack itemstack1 = this.getItemInHand(enumhand);

                deathprotection = (DeathProtection) itemstack1.get(DataComponents.DEATH_PROTECTION);
                if (deathprotection != null) {
                    itemstack = itemstack1.copy();
                    itemstack1.shrink(1);
                    break;
                }
            }

            if (itemstack != null) {
                if (this instanceof EntityPlayer) {
                    EntityPlayer entityplayer = (EntityPlayer) this;

                    entityplayer.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
                    CriterionTriggers.USED_TOTEM.trigger(entityplayer, itemstack);
                    this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                }

                this.setHealth(1.0F);
                deathprotection.applyEffects(itemstack, this);
                this.level().broadcastEntityEvent(this, (byte) 35);
            }

            return deathprotection != null;
        }
    }

    @Nullable
    public DamageSource getLastDamageSource() {
        if (this.level().getGameTime() - this.lastDamageStamp > 40L) {
            this.lastDamageSource = null;
        }

        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource damagesource) {
        this.makeSound(this.getHurtSound(damagesource));
    }

    public void makeSound(@Nullable SoundEffect soundeffect) {
        if (soundeffect != null) {
            this.playSound(soundeffect, this.getSoundVolume(), this.getVoicePitch());
        }

    }

    public boolean isDamageSourceBlocked(DamageSource damagesource) {
        Entity entity = damagesource.getDirectEntity();
        boolean flag = false;

        if (entity instanceof EntityArrow entityarrow) {
            if (entityarrow.getPierceLevel() > 0) {
                flag = true;
            }
        }

        ItemStack itemstack = this.getItemBlockingWith();

        if (!damagesource.is(DamageTypeTags.BYPASSES_SHIELD) && itemstack != null && itemstack.getItem() instanceof ItemShield && !flag) {
            Vec3D vec3d = damagesource.getSourcePosition();

            if (vec3d != null) {
                Vec3D vec3d1 = this.calculateViewVector(0.0F, this.getYHeadRot());
                Vec3D vec3d2 = vec3d.vectorTo(this.position());

                vec3d2 = (new Vec3D(vec3d2.x, 0.0D, vec3d2.z)).normalize();
                return vec3d2.dot(vec3d1) < 0.0D;
            }
        }

        return false;
    }

    private void breakItem(ItemStack itemstack) {
        if (!itemstack.isEmpty()) {
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), itemstack.getBreakingSound(), this.getSoundSource(), 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F, false);
            }

            this.spawnItemParticles(itemstack, 5);
        }

    }

    public void die(DamageSource damagesource) {
        if (!this.isRemoved() && !this.dead) {
            Entity entity = damagesource.getEntity();
            EntityLiving entityliving = this.getKillCredit();

            if (entityliving != null) {
                entityliving.awardKillScore(this, damagesource);
            }

            if (this.isSleeping()) {
                this.stopSleeping();
            }

            if (!this.level().isClientSide && this.hasCustomName()) {
                EntityLiving.LOGGER.info("Named entity {} died: {}", this, this.getCombatTracker().getDeathMessage().getString());
            }

            this.dead = true;
            this.getCombatTracker().recheckStatus();
            World world = this.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                if (entity == null || entity.killedEntity(worldserver, this)) {
                    this.gameEvent(GameEvent.ENTITY_DIE);
                    this.dropAllDeathLoot(worldserver, damagesource);
                    this.createWitherRose(entityliving);
                }

                this.level().broadcastEntityEvent(this, (byte) 3);
            }

            this.setPose(EntityPose.DYING);
        }
    }

    protected void createWitherRose(@Nullable EntityLiving entityliving) {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            boolean flag = false;

            if (entityliving instanceof EntityWither) {
                if (worldserver.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    BlockPosition blockposition = this.blockPosition();
                    IBlockData iblockdata = Blocks.WITHER_ROSE.defaultBlockState();

                    if (this.level().getBlockState(blockposition).isAir() && iblockdata.canSurvive(this.level(), blockposition)) {
                        this.level().setBlock(blockposition, iblockdata, 3);
                        flag = true;
                    }
                }

                if (!flag) {
                    EntityItem entityitem = new EntityItem(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));

                    this.level().addFreshEntity(entityitem);
                }
            }

        }
    }

    protected void dropAllDeathLoot(WorldServer worldserver, DamageSource damagesource) {
        boolean flag = this.lastHurtByPlayerTime > 0;

        if (this.shouldDropLoot() && worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.dropFromLootTable(worldserver, damagesource, flag);
            this.dropCustomDeathLoot(worldserver, damagesource, flag);
        }

        this.dropEquipment(worldserver);
        this.dropExperience(worldserver, damagesource.getEntity());
    }

    protected void dropEquipment(WorldServer worldserver) {}

    protected void dropExperience(WorldServer worldserver, @Nullable Entity entity) {
        if (!this.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
            EntityExperienceOrb.award(worldserver, this.position(), this.getExperienceReward(worldserver, entity));
        }

    }

    protected void dropCustomDeathLoot(WorldServer worldserver, DamageSource damagesource, boolean flag) {}

    public long getLootTableSeed() {
        return 0L;
    }

    protected float getKnockback(Entity entity, DamageSource damagesource) {
        float f = (float) this.getAttributeValue(GenericAttributes.ATTACK_KNOCKBACK);
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            return EnchantmentManager.modifyKnockback(worldserver, this.getWeaponItem(), entity, damagesource, f);
        } else {
            return f;
        }
    }

    protected void dropFromLootTable(WorldServer worldserver, DamageSource damagesource, boolean flag) {
        Optional<ResourceKey<LootTable>> optional = this.getLootTable();

        if (!optional.isEmpty()) {
            LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable((ResourceKey) optional.get());
            LootParams.a lootparams_a = (new LootParams.a(worldserver)).withParameter(LootContextParameters.THIS_ENTITY, this).withParameter(LootContextParameters.ORIGIN, this.position()).withParameter(LootContextParameters.DAMAGE_SOURCE, damagesource).withOptionalParameter(LootContextParameters.ATTACKING_ENTITY, damagesource.getEntity()).withOptionalParameter(LootContextParameters.DIRECT_ATTACKING_ENTITY, damagesource.getDirectEntity());

            if (flag && this.lastHurtByPlayer != null) {
                lootparams_a = lootparams_a.withParameter(LootContextParameters.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
            }

            LootParams lootparams = lootparams_a.create(LootContextParameterSets.ENTITY);

            loottable.getRandomItems(lootparams, this.getLootTableSeed(), (itemstack) -> {
                this.spawnAtLocation(worldserver, itemstack);
            });
        }
    }

    public boolean dropFromGiftLootTable(WorldServer worldserver, ResourceKey<LootTable> resourcekey, BiConsumer<WorldServer, ItemStack> biconsumer) {
        return this.dropFromLootTable(worldserver, resourcekey, (lootparams_a) -> {
            return lootparams_a.withParameter(LootContextParameters.ORIGIN, this.position()).withParameter(LootContextParameters.THIS_ENTITY, this).create(LootContextParameterSets.GIFT);
        }, biconsumer);
    }

    protected void dropFromShearingLootTable(WorldServer worldserver, ResourceKey<LootTable> resourcekey, ItemStack itemstack, BiConsumer<WorldServer, ItemStack> biconsumer) {
        this.dropFromLootTable(worldserver, resourcekey, (lootparams_a) -> {
            return lootparams_a.withParameter(LootContextParameters.ORIGIN, this.position()).withParameter(LootContextParameters.THIS_ENTITY, this).withParameter(LootContextParameters.TOOL, itemstack).create(LootContextParameterSets.SHEARING);
        }, biconsumer);
    }

    protected boolean dropFromLootTable(WorldServer worldserver, ResourceKey<LootTable> resourcekey, Function<LootParams.a, LootParams> function, BiConsumer<WorldServer, ItemStack> biconsumer) {
        LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(resourcekey);
        LootParams lootparams = (LootParams) function.apply(new LootParams.a(worldserver));
        List<ItemStack> list = loottable.getRandomItems(lootparams);

        if (!list.isEmpty()) {
            list.forEach((itemstack) -> {
                biconsumer.accept(worldserver, itemstack);
            });
            return true;
        } else {
            return false;
        }
    }

    public void knockback(double d0, double d1, double d2) {
        d0 *= 1.0D - this.getAttributeValue(GenericAttributes.KNOCKBACK_RESISTANCE);
        if (d0 > 0.0D) {
            this.hasImpulse = true;

            Vec3D vec3d;

            for (vec3d = this.getDeltaMovement(); d1 * d1 + d2 * d2 < 9.999999747378752E-6D; d2 = (Math.random() - Math.random()) * 0.01D) {
                d1 = (Math.random() - Math.random()) * 0.01D;
            }

            Vec3D vec3d1 = (new Vec3D(d1, 0.0D, d2)).normalize().scale(d0);

            this.setDeltaMovement(vec3d.x / 2.0D - vec3d1.x, this.onGround() ? Math.min(0.4D, vec3d.y / 2.0D + d0) : vec3d.y, vec3d.z / 2.0D - vec3d1.z);
        }
    }

    public void indicateDamage(double d0, double d1) {}

    @Nullable
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.GENERIC_HURT;
    }

    @Nullable
    protected SoundEffect getDeathSound() {
        return SoundEffects.GENERIC_DEATH;
    }

    private SoundEffect getFallDamageSound(int i) {
        return i > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void skipDropExperience() {
        this.skipDropExperience = true;
    }

    public boolean wasExperienceConsumed() {
        return this.skipDropExperience;
    }

    public float getHurtDir() {
        return 0.0F;
    }

    protected AxisAlignedBB getHitbox() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        Entity entity = this.getVehicle();

        if (entity != null) {
            Vec3D vec3d = entity.getPassengerRidingPosition(this);

            return axisalignedbb.setMinY(Math.max(vec3d.y, axisalignedbb.minY));
        } else {
            return axisalignedbb;
        }
    }

    public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> activeLocationDependentEnchantments(EnumItemSlot enumitemslot) {
        return (Map) this.activeLocationDependentEnchantments.computeIfAbsent(enumitemslot, (enumitemslot1) -> {
            return new Reference2ObjectArrayMap();
        });
    }

    public boolean canBeNameTagged() {
        return true;
    }

    public EntityLiving.a getFallSounds() {
        return new EntityLiving.a(SoundEffects.GENERIC_SMALL_FALL, SoundEffects.GENERIC_BIG_FALL);
    }

    public Optional<BlockPosition> getLastClimbablePos() {
        return this.lastClimbablePos;
    }

    public boolean onClimbable() {
        if (this.isSpectator()) {
            return false;
        } else {
            BlockPosition blockposition = this.blockPosition();
            IBlockData iblockdata = this.getInBlockState();

            if (iblockdata.is(TagsBlock.CLIMBABLE)) {
                this.lastClimbablePos = Optional.of(blockposition);
                return true;
            } else if (iblockdata.getBlock() instanceof BlockTrapdoor && this.trapdoorUsableAsLadder(blockposition, iblockdata)) {
                this.lastClimbablePos = Optional.of(blockposition);
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean trapdoorUsableAsLadder(BlockPosition blockposition, IBlockData iblockdata) {
        if (!(Boolean) iblockdata.getValue(BlockTrapdoor.OPEN)) {
            return false;
        } else {
            IBlockData iblockdata1 = this.level().getBlockState(blockposition.below());

            return iblockdata1.is(Blocks.LADDER) && iblockdata1.getValue(BlockLadder.FACING) == iblockdata.getValue(BlockTrapdoor.FACING);
        }
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0F;
    }

    public boolean isLookingAtMe(EntityLiving entityliving, double d0, boolean flag, boolean flag1, double... adouble) {
        Vec3D vec3d = entityliving.getViewVector(1.0F).normalize();
        double[] adouble1 = adouble;
        int i = adouble.length;

        for (int j = 0; j < i; ++j) {
            double d1 = adouble1[j];
            Vec3D vec3d1 = new Vec3D(this.getX() - entityliving.getX(), d1 - entityliving.getEyeY(), this.getZ() - entityliving.getZ());
            double d2 = vec3d1.length();

            vec3d1 = vec3d1.normalize();
            double d3 = vec3d.dot(vec3d1);

            if (d3 > 1.0D - d0 / (flag ? d2 : 1.0D) && entityliving.hasLineOfSight(this, flag1 ? RayTrace.BlockCollisionOption.VISUAL : RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, d1)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getMaxFallDistance() {
        return this.getComfortableFallDistance(0.0F);
    }

    protected final int getComfortableFallDistance(float f) {
        return MathHelper.floor(f + 3.0F);
    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
        boolean flag = super.causeFallDamage(f, f1, damagesource);
        int i = this.calculateFallDamage(f, f1);

        if (i > 0) {
            this.playSound(this.getFallDamageSound(i), 1.0F, 1.0F);
            this.playBlockFallSound();
            this.hurt(damagesource, (float) i);
            return true;
        } else {
            return flag;
        }
    }

    protected int calculateFallDamage(float f, float f1) {
        if (this.getType().is(TagsEntity.FALL_DAMAGE_IMMUNE)) {
            return 0;
        } else {
            float f2 = (float) this.getAttributeValue(GenericAttributes.SAFE_FALL_DISTANCE);
            float f3 = f - f2;

            return MathHelper.ceil((double) (f3 * f1) * this.getAttributeValue(GenericAttributes.FALL_DAMAGE_MULTIPLIER));
        }
    }

    protected void playBlockFallSound() {
        if (!this.isSilent()) {
            int i = MathHelper.floor(this.getX());
            int j = MathHelper.floor(this.getY() - 0.20000000298023224D);
            int k = MathHelper.floor(this.getZ());
            IBlockData iblockdata = this.level().getBlockState(new BlockPosition(i, j, k));

            if (!iblockdata.isAir()) {
                SoundEffectType soundeffecttype = iblockdata.getSoundType();

                this.playSound(soundeffecttype.getFallSound(), soundeffecttype.getVolume() * 0.5F, soundeffecttype.getPitch() * 0.75F);
            }

        }
    }

    @Override
    public void animateHurt(float f) {
        this.hurtDuration = 10;
        this.hurtTime = this.hurtDuration;
    }

    public int getArmorValue() {
        return MathHelper.floor(this.getAttributeValue(GenericAttributes.ARMOR));
    }

    protected void hurtArmor(DamageSource damagesource, float f) {}

    protected void hurtHelmet(DamageSource damagesource, float f) {}

    protected void hurtCurrentlyUsedShield(float f) {}

    protected void doHurtEquipment(DamageSource damagesource, float f, EnumItemSlot... aenumitemslot) {
        if (f > 0.0F) {
            int i = (int) Math.max(1.0F, f / 4.0F);
            EnumItemSlot[] aenumitemslot1 = aenumitemslot;
            int j = aenumitemslot.length;

            for (int k = 0; k < j; ++k) {
                EnumItemSlot enumitemslot = aenumitemslot1[k];
                ItemStack itemstack = this.getItemBySlot(enumitemslot);
                Equippable equippable = (Equippable) itemstack.get(DataComponents.EQUIPPABLE);

                if (equippable != null && equippable.damageOnHurt() && itemstack.isDamageableItem() && itemstack.canBeHurtBy(damagesource)) {
                    itemstack.hurtAndBreak(i, this, enumitemslot);
                }
            }

        }
    }

    protected float getDamageAfterArmorAbsorb(DamageSource damagesource, float f) {
        if (!damagesource.is(DamageTypeTags.BYPASSES_ARMOR)) {
            this.hurtArmor(damagesource, f);
            f = CombatMath.getDamageAfterAbsorb(this, f, damagesource, (float) this.getArmorValue(), (float) this.getAttributeValue(GenericAttributes.ARMOR_TOUGHNESS));
        }

        return f;
    }

    protected float getDamageAfterMagicAbsorb(DamageSource damagesource, float f) {
        if (damagesource.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return f;
        } else {
            if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !damagesource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                int i = (this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f1 = f * (float) j;
                float f2 = f;

                f = Math.max(f1 / 25.0F, 0.0F);
                float f3 = f2 - f;

                if (f3 > 0.0F && f3 < 3.4028235E37F) {
                    if (this instanceof EntityPlayer) {
                        ((EntityPlayer) this).awardStat(StatisticList.DAMAGE_RESISTED, Math.round(f3 * 10.0F));
                    } else if (damagesource.getEntity() instanceof EntityPlayer) {
                        ((EntityPlayer) damagesource.getEntity()).awardStat(StatisticList.DAMAGE_DEALT_RESISTED, Math.round(f3 * 10.0F));
                    }
                }
            }

            if (f <= 0.0F) {
                return 0.0F;
            } else if (damagesource.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
                return f;
            } else {
                World world = this.level();
                float f4;

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    f4 = EnchantmentManager.getDamageProtection(worldserver, this, damagesource);
                } else {
                    f4 = 0.0F;
                }

                if (f4 > 0.0F) {
                    f = CombatMath.getDamageAfterMagicAbsorb(f, f4);
                }

                return f;
            }
        }
    }

    protected void actuallyHurt(WorldServer worldserver, DamageSource damagesource, float f) {
        if (!this.isInvulnerableTo(worldserver, damagesource)) {
            f = this.getDamageAfterArmorAbsorb(damagesource, f);
            f = this.getDamageAfterMagicAbsorb(damagesource, f);
            float f1 = f;

            f = Math.max(f - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (f1 - f));
            float f2 = f1 - f;

            if (f2 > 0.0F && f2 < 3.4028235E37F) {
                Entity entity = damagesource.getEntity();

                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityplayer = (EntityPlayer) entity;

                    entityplayer.awardStat(StatisticList.DAMAGE_DEALT_ABSORBED, Math.round(f2 * 10.0F));
                }
            }

            if (f != 0.0F) {
                this.getCombatTracker().recordDamage(damagesource, f);
                this.setHealth(this.getHealth() - f);
                this.setAbsorptionAmount(this.getAbsorptionAmount() - f);
                this.gameEvent(GameEvent.ENTITY_DAMAGE);
            }
        }
    }

    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    @Nullable
    public EntityLiving getKillCredit() {
        return (EntityLiving) (this.lastHurtByPlayer != null ? this.lastHurtByPlayer : (this.lastHurtByMob != null ? this.lastHurtByMob : null));
    }

    public final float getMaxHealth() {
        return (float) this.getAttributeValue(GenericAttributes.MAX_HEALTH);
    }

    public final float getMaxAbsorption() {
        return (float) this.getAttributeValue(GenericAttributes.MAX_ABSORPTION);
    }

    public final int getArrowCount() {
        return (Integer) this.entityData.get(EntityLiving.DATA_ARROW_COUNT_ID);
    }

    public final void setArrowCount(int i) {
        this.entityData.set(EntityLiving.DATA_ARROW_COUNT_ID, i);
    }

    public final int getStingerCount() {
        return (Integer) this.entityData.get(EntityLiving.DATA_STINGER_COUNT_ID);
    }

    public final void setStingerCount(int i) {
        this.entityData.set(EntityLiving.DATA_STINGER_COUNT_ID, i);
    }

    private int getCurrentSwingDuration() {
        return MobEffectUtil.hasDigSpeed(this) ? 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this)) : (this.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6);
    }

    public void swing(EnumHand enumhand) {
        this.swing(enumhand, false);
    }

    public void swing(EnumHand enumhand, boolean flag) {
        if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
            this.swingTime = -1;
            this.swinging = true;
            this.swingingArm = enumhand;
            if (this.level() instanceof WorldServer) {
                PacketPlayOutAnimation packetplayoutanimation = new PacketPlayOutAnimation(this, enumhand == EnumHand.MAIN_HAND ? 0 : 3);
                ChunkProviderServer chunkproviderserver = ((WorldServer) this.level()).getChunkSource();

                if (flag) {
                    chunkproviderserver.broadcastAndSend(this, packetplayoutanimation);
                } else {
                    chunkproviderserver.broadcast(this, packetplayoutanimation);
                }
            }
        }

    }

    @Override
    public void handleDamageEvent(DamageSource damagesource) {
        this.walkAnimation.setSpeed(1.5F);
        this.invulnerableTime = 20;
        this.hurtDuration = 10;
        this.hurtTime = this.hurtDuration;
        SoundEffect soundeffect = this.getHurtSound(damagesource);

        if (soundeffect != null) {
            this.playSound(soundeffect, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        }

        this.lastDamageSource = damagesource;
        this.lastDamageStamp = this.level().getGameTime();
    }

    @Override
    public void handleEntityEvent(byte b0) {
        switch (b0) {
            case 3:
                SoundEffect soundeffect = this.getDeathSound();

                if (soundeffect != null) {
                    this.playSound(soundeffect, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }

                if (!(this instanceof EntityHuman)) {
                    this.setHealth(0.0F);
                    this.die(this.damageSources().generic());
                }
                break;
            case 29:
                this.playSound(SoundEffects.SHIELD_BLOCK, 1.0F, 0.8F + this.level().random.nextFloat() * 0.4F);
                break;
            case 30:
                this.playSound(SoundEffects.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
                break;
            case 46:
                boolean flag = true;

                for (int i = 0; i < 128; ++i) {
                    double d0 = (double) i / 127.0D;
                    float f = (this.random.nextFloat() - 0.5F) * 0.2F;
                    float f1 = (this.random.nextFloat() - 0.5F) * 0.2F;
                    float f2 = (this.random.nextFloat() - 0.5F) * 0.2F;
                    double d1 = MathHelper.lerp(d0, this.xo, this.getX()) + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth() * 2.0D;
                    double d2 = MathHelper.lerp(d0, this.yo, this.getY()) + this.random.nextDouble() * (double) this.getBbHeight();
                    double d3 = MathHelper.lerp(d0, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth() * 2.0D;

                    this.level().addParticle(Particles.PORTAL, d1, d2, d3, (double) f, (double) f1, (double) f2);
                }

                return;
            case 47:
                this.breakItem(this.getItemBySlot(EnumItemSlot.MAINHAND));
                break;
            case 48:
                this.breakItem(this.getItemBySlot(EnumItemSlot.OFFHAND));
                break;
            case 49:
                this.breakItem(this.getItemBySlot(EnumItemSlot.HEAD));
                break;
            case 50:
                this.breakItem(this.getItemBySlot(EnumItemSlot.CHEST));
                break;
            case 51:
                this.breakItem(this.getItemBySlot(EnumItemSlot.LEGS));
                break;
            case 52:
                this.breakItem(this.getItemBySlot(EnumItemSlot.FEET));
                break;
            case 54:
                BlockHoney.showJumpParticles(this);
                break;
            case 55:
                this.swapHandItems();
                break;
            case 60:
                this.makePoofParticles();
                break;
            case 65:
                this.breakItem(this.getItemBySlot(EnumItemSlot.BODY));
                break;
            default:
                super.handleEntityEvent(b0);
        }

    }

    public void makePoofParticles() {
        for (int i = 0; i < 20; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            double d3 = 10.0D;

            this.level().addParticle(Particles.POOF, this.getRandomX(1.0D) - d0 * 10.0D, this.getRandomY() - d1 * 10.0D, this.getRandomZ(1.0D) - d2 * 10.0D, d0, d1, d2);
        }

    }

    private void swapHandItems() {
        ItemStack itemstack = this.getItemBySlot(EnumItemSlot.OFFHAND);

        this.setItemSlot(EnumItemSlot.OFFHAND, this.getItemBySlot(EnumItemSlot.MAINHAND));
        this.setItemSlot(EnumItemSlot.MAINHAND, itemstack);
    }

    @Override
    protected void onBelowWorld() {
        this.hurt(this.damageSources().fellOutOfWorld(), 4.0F);
    }

    protected void updateSwingTime() {
        int i = this.getCurrentSwingDuration();

        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= i) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }

        this.attackAnim = (float) this.swingTime / (float) i;
    }

    @Nullable
    public AttributeModifiable getAttribute(Holder<AttributeBase> holder) {
        return this.getAttributes().getInstance(holder);
    }

    public double getAttributeValue(Holder<AttributeBase> holder) {
        return this.getAttributes().getValue(holder);
    }

    public double getAttributeBaseValue(Holder<AttributeBase> holder) {
        return this.getAttributes().getBaseValue(holder);
    }

    public AttributeMapBase getAttributes() {
        return this.attributes;
    }

    public ItemStack getMainHandItem() {
        return this.getItemBySlot(EnumItemSlot.MAINHAND);
    }

    public ItemStack getOffhandItem() {
        return this.getItemBySlot(EnumItemSlot.OFFHAND);
    }

    public ItemStack getItemHeldByArm(EnumMainHand enummainhand) {
        return this.getMainArm() == enummainhand ? this.getMainHandItem() : this.getOffhandItem();
    }

    @Nonnull
    @Override
    public ItemStack getWeaponItem() {
        return this.getMainHandItem();
    }

    public boolean isHolding(Item item) {
        return this.isHolding((itemstack) -> {
            return itemstack.is(item);
        });
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandItem()) || predicate.test(this.getOffhandItem());
    }

    public ItemStack getItemInHand(EnumHand enumhand) {
        if (enumhand == EnumHand.MAIN_HAND) {
            return this.getItemBySlot(EnumItemSlot.MAINHAND);
        } else if (enumhand == EnumHand.OFF_HAND) {
            return this.getItemBySlot(EnumItemSlot.OFFHAND);
        } else {
            throw new IllegalArgumentException("Invalid hand " + String.valueOf(enumhand));
        }
    }

    public void setItemInHand(EnumHand enumhand, ItemStack itemstack) {
        if (enumhand == EnumHand.MAIN_HAND) {
            this.setItemSlot(EnumItemSlot.MAINHAND, itemstack);
        } else {
            if (enumhand != EnumHand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + String.valueOf(enumhand));
            }

            this.setItemSlot(EnumItemSlot.OFFHAND, itemstack);
        }

    }

    public boolean hasItemInSlot(EnumItemSlot enumitemslot) {
        return !this.getItemBySlot(enumitemslot).isEmpty();
    }

    public boolean canUseSlot(EnumItemSlot enumitemslot) {
        return false;
    }

    public abstract Iterable<ItemStack> getArmorSlots();

    public abstract ItemStack getItemBySlot(EnumItemSlot enumitemslot);

    public abstract void setItemSlot(EnumItemSlot enumitemslot, ItemStack itemstack);

    public Iterable<ItemStack> getHandSlots() {
        return List.of();
    }

    public Iterable<ItemStack> getArmorAndBodyArmorSlots() {
        return this.getArmorSlots();
    }

    public Iterable<ItemStack> getAllSlots() {
        return Iterables.concat(this.getHandSlots(), this.getArmorAndBodyArmorSlots());
    }

    protected void verifyEquippedItem(ItemStack itemstack) {
        itemstack.getItem().verifyComponentsAfterLoad(itemstack);
    }

    public float getArmorCoverPercentage() {
        Iterable<ItemStack> iterable = this.getArmorSlots();
        int i = 0;
        int j = 0;

        for (Iterator iterator = iterable.iterator(); iterator.hasNext(); ++i) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (!itemstack.isEmpty()) {
                ++j;
            }
        }

        return i > 0 ? (float) j / (float) i : 0.0F;
    }

    @Override
    public void setSprinting(boolean flag) {
        super.setSprinting(flag);
        AttributeModifiable attributemodifiable = this.getAttribute(GenericAttributes.MOVEMENT_SPEED);

        attributemodifiable.removeModifier(EntityLiving.SPEED_MODIFIER_SPRINTING.id());
        if (flag) {
            attributemodifiable.addTransientModifier(EntityLiving.SPEED_MODIFIER_SPRINTING);
        }

    }

    protected float getSoundVolume() {
        return 1.0F;
    }

    public float getVoicePitch() {
        return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isSleeping()) {
            super.push(entity);
        }

    }

    private void dismountVehicle(Entity entity) {
        Vec3D vec3d;

        if (this.isRemoved()) {
            vec3d = this.position();
        } else if (!entity.isRemoved() && !this.level().getBlockState(entity.blockPosition()).is(TagsBlock.PORTALS)) {
            vec3d = entity.getDismountLocationForPassenger(this);
        } else {
            double d0 = Math.max(this.getY(), entity.getY());

            vec3d = new Vec3D(this.getX(), d0, this.getZ());
            boolean flag = this.getBbWidth() <= 4.0F && this.getBbHeight() <= 4.0F;

            if (flag) {
                double d1 = (double) this.getBbHeight() / 2.0D;
                Vec3D vec3d1 = vec3d.add(0.0D, d1, 0.0D);
                VoxelShape voxelshape = VoxelShapes.create(AxisAlignedBB.ofSize(vec3d1, (double) this.getBbWidth(), (double) this.getBbHeight(), (double) this.getBbWidth()));

                vec3d = (Vec3D) this.level().findFreePosition(this, voxelshape, vec3d1, (double) this.getBbWidth(), (double) this.getBbHeight(), (double) this.getBbWidth()).map((vec3d2) -> {
                    return vec3d2.add(0.0D, -d1, 0.0D);
                }).orElse(vec3d);
            }
        }

        this.dismountTo(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public boolean shouldShowName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpPower() {
        return this.getJumpPower(1.0F);
    }

    protected float getJumpPower(float f) {
        return (float) this.getAttributeValue(GenericAttributes.JUMP_STRENGTH) * f * this.getBlockJumpFactor() + this.getJumpBoostPower();
    }

    public float getJumpBoostPower() {
        return this.hasEffect(MobEffects.JUMP) ? 0.1F * ((float) this.getEffect(MobEffects.JUMP).getAmplifier() + 1.0F) : 0.0F;
    }

    @VisibleForTesting
    public void jumpFromGround() {
        float f = this.getJumpPower();

        if (f > 1.0E-5F) {
            Vec3D vec3d = this.getDeltaMovement();

            this.setDeltaMovement(vec3d.x, Math.max((double) f, vec3d.y), vec3d.z);
            if (this.isSprinting()) {
                float f1 = this.getYRot() * 0.017453292F;

                this.addDeltaMovement(new Vec3D((double) (-MathHelper.sin(f1)) * 0.2D, 0.0D, (double) MathHelper.cos(f1) * 0.2D));
            }

            this.hasImpulse = true;
        }
    }

    protected void goDownInWater() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03999999910593033D, 0.0D));
    }

    protected void jumpInLiquid(TagKey<FluidType> tagkey) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.03999999910593033D, 0.0D));
    }

    protected float getWaterSlowDown() {
        return 0.8F;
    }

    public boolean canStandOnFluid(Fluid fluid) {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return this.getAttributeValue(GenericAttributes.GRAVITY);
    }

    protected double getEffectiveGravity() {
        boolean flag = this.getDeltaMovement().y <= 0.0D;

        return flag && this.hasEffect(MobEffects.SLOW_FALLING) ? Math.min(this.getGravity(), 0.01D) : this.getGravity();
    }

    public void travel(Vec3D vec3d) {
        if (this.isControlledByLocalInstance()) {
            Fluid fluid = this.level().getFluidState(this.blockPosition());

            if ((this.isInWater() || this.isInLava()) && this.isAffectedByFluids() && !this.canStandOnFluid(fluid)) {
                this.travelInFluid(vec3d);
            } else if (this.isFallFlying()) {
                this.travelFallFlying();
            } else {
                this.travelInAir(vec3d);
            }

        }
    }

    private void travelInAir(Vec3D vec3d) {
        BlockPosition blockposition = this.getBlockPosBelowThatAffectsMyMovement();
        float f = this.onGround() ? this.level().getBlockState(blockposition).getBlock().getFriction() : 1.0F;
        float f1 = f * 0.91F;
        Vec3D vec3d1 = this.handleRelativeFrictionAndCalculateMovement(vec3d, f);
        double d0 = vec3d1.y;
        MobEffect mobeffect = this.getEffect(MobEffects.LEVITATION);

        if (mobeffect != null) {
            d0 += (0.05D * (double) (mobeffect.getAmplifier() + 1) - vec3d1.y) * 0.2D;
        } else if (this.level().isClientSide && !this.level().hasChunkAt(blockposition)) {
            if (this.getY() > (double) this.level().getMinY()) {
                d0 = -0.1D;
            } else {
                d0 = 0.0D;
            }
        } else {
            d0 -= this.getEffectiveGravity();
        }

        if (this.shouldDiscardFriction()) {
            this.setDeltaMovement(vec3d1.x, d0, vec3d1.z);
        } else {
            float f2 = this instanceof EntityBird ? f1 : 0.98F;

            this.setDeltaMovement(vec3d1.x * (double) f1, d0 * (double) f2, vec3d1.z * (double) f1);
        }

    }

    private void travelInFluid(Vec3D vec3d) {
        boolean flag = this.getDeltaMovement().y <= 0.0D;
        double d0 = this.getY();
        double d1 = this.getEffectiveGravity();
        Vec3D vec3d1;

        if (this.isInWater()) {
            float f = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
            float f1 = 0.02F;
            float f2 = (float) this.getAttributeValue(GenericAttributes.WATER_MOVEMENT_EFFICIENCY);

            if (!this.onGround()) {
                f2 *= 0.5F;
            }

            if (f2 > 0.0F) {
                f += (0.54600006F - f) * f2;
                f1 += (this.getSpeed() - f1) * f2;
            }

            if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                f = 0.96F;
            }

            this.moveRelative(f1, vec3d);
            this.move(EnumMoveType.SELF, this.getDeltaMovement());
            Vec3D vec3d2 = this.getDeltaMovement();

            if (this.horizontalCollision && this.onClimbable()) {
                vec3d2 = new Vec3D(vec3d2.x, 0.2D, vec3d2.z);
            }

            vec3d2 = vec3d2.multiply((double) f, 0.800000011920929D, (double) f);
            this.setDeltaMovement(this.getFluidFallingAdjustedMovement(d1, flag, vec3d2));
        } else {
            this.moveRelative(0.02F, vec3d);
            this.move(EnumMoveType.SELF, this.getDeltaMovement());
            if (this.getFluidHeight(TagsFluid.LAVA) <= this.getFluidJumpThreshold()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.800000011920929D, 0.5D));
                vec3d1 = this.getFluidFallingAdjustedMovement(d1, flag, this.getDeltaMovement());
                this.setDeltaMovement(vec3d1);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            }

            if (d1 != 0.0D) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -d1 / 4.0D, 0.0D));
            }
        }

        vec3d1 = this.getDeltaMovement();
        if (this.horizontalCollision && this.isFree(vec3d1.x, vec3d1.y + 0.6000000238418579D - this.getY() + d0, vec3d1.z)) {
            this.setDeltaMovement(vec3d1.x, 0.30000001192092896D, vec3d1.z);
        }

    }

    private void travelFallFlying() {
        Vec3D vec3d = this.getDeltaMovement();
        double d0 = vec3d.horizontalDistance();

        this.setDeltaMovement(this.updateFallFlyingMovement(vec3d));
        this.move(EnumMoveType.SELF, this.getDeltaMovement());
        if (!this.level().isClientSide) {
            double d1 = this.getDeltaMovement().horizontalDistance();

            this.handleFallFlyingCollisions(d0, d1);
        }

    }

    private Vec3D updateFallFlyingMovement(Vec3D vec3d) {
        Vec3D vec3d1 = this.getLookAngle();
        float f = this.getXRot() * 0.017453292F;
        double d0 = Math.sqrt(vec3d1.x * vec3d1.x + vec3d1.z * vec3d1.z);
        double d1 = vec3d.horizontalDistance();
        double d2 = this.getEffectiveGravity();
        double d3 = MathHelper.square(Math.cos((double) f));

        vec3d = vec3d.add(0.0D, d2 * (-1.0D + d3 * 0.75D), 0.0D);
        double d4;

        if (vec3d.y < 0.0D && d0 > 0.0D) {
            d4 = vec3d.y * -0.1D * d3;
            vec3d = vec3d.add(vec3d1.x * d4 / d0, d4, vec3d1.z * d4 / d0);
        }

        if (f < 0.0F && d0 > 0.0D) {
            d4 = d1 * (double) (-MathHelper.sin(f)) * 0.04D;
            vec3d = vec3d.add(-vec3d1.x * d4 / d0, d4 * 3.2D, -vec3d1.z * d4 / d0);
        }

        if (d0 > 0.0D) {
            vec3d = vec3d.add((vec3d1.x / d0 * d1 - vec3d.x) * 0.1D, 0.0D, (vec3d1.z / d0 * d1 - vec3d.z) * 0.1D);
        }

        return vec3d.multiply(0.9900000095367432D, 0.9800000190734863D, 0.9900000095367432D);
    }

    private void handleFallFlyingCollisions(double d0, double d1) {
        if (this.horizontalCollision) {
            double d2 = d0 - d1;
            float f = (float) (d2 * 10.0D - 3.0D);

            if (f > 0.0F) {
                this.playSound(this.getFallDamageSound((int) f), 1.0F, 1.0F);
                this.hurt(this.damageSources().flyIntoWall(), f);
            }
        }

    }

    private void travelRidden(EntityHuman entityhuman, Vec3D vec3d) {
        Vec3D vec3d1 = this.getRiddenInput(entityhuman, vec3d);

        this.tickRidden(entityhuman, vec3d1);
        if (this.isControlledByLocalInstance()) {
            this.setSpeed(this.getRiddenSpeed(entityhuman));
            this.travel(vec3d1);
        } else {
            this.setDeltaMovement(Vec3D.ZERO);
        }

    }

    protected void tickRidden(EntityHuman entityhuman, Vec3D vec3d) {}

    protected Vec3D getRiddenInput(EntityHuman entityhuman, Vec3D vec3d) {
        return vec3d;
    }

    protected float getRiddenSpeed(EntityHuman entityhuman) {
        return this.getSpeed();
    }

    public void calculateEntityAnimation(boolean flag) {
        float f = (float) MathHelper.length(this.getX() - this.xo, flag ? this.getY() - this.yo : 0.0D, this.getZ() - this.zo);

        if (!this.isPassenger() && this.isAlive()) {
            this.updateWalkAnimation(f);
        } else {
            this.walkAnimation.stop();
        }

    }

    protected void updateWalkAnimation(float f) {
        float f1 = Math.min(f * 4.0F, 1.0F);

        this.walkAnimation.update(f1, 0.4F, this.isBaby() ? 3.0F : 1.0F);
    }

    private Vec3D handleRelativeFrictionAndCalculateMovement(Vec3D vec3d, float f) {
        this.moveRelative(this.getFrictionInfluencedSpeed(f), vec3d);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(EnumMoveType.SELF, this.getDeltaMovement());
        Vec3D vec3d1 = this.getDeltaMovement();

        if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.getInBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
            vec3d1 = new Vec3D(vec3d1.x, 0.2D, vec3d1.z);
        }

        return vec3d1;
    }

    public Vec3D getFluidFallingAdjustedMovement(double d0, boolean flag, Vec3D vec3d) {
        if (d0 != 0.0D && !this.isSprinting()) {
            double d1;

            if (flag && Math.abs(vec3d.y - 0.005D) >= 0.003D && Math.abs(vec3d.y - d0 / 16.0D) < 0.003D) {
                d1 = -0.003D;
            } else {
                d1 = vec3d.y - d0 / 16.0D;
            }

            return new Vec3D(vec3d.x, d1, vec3d.z);
        } else {
            return vec3d;
        }
    }

    private Vec3D handleOnClimbable(Vec3D vec3d) {
        if (this.onClimbable()) {
            this.resetFallDistance();
            float f = 0.15F;
            double d0 = MathHelper.clamp(vec3d.x, -0.15000000596046448D, 0.15000000596046448D);
            double d1 = MathHelper.clamp(vec3d.z, -0.15000000596046448D, 0.15000000596046448D);
            double d2 = Math.max(vec3d.y, -0.15000000596046448D);

            if (d2 < 0.0D && !this.getInBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof EntityHuman) {
                d2 = 0.0D;
            }

            vec3d = new Vec3D(d0, d2, d1);
        }

        return vec3d;
    }

    private float getFrictionInfluencedSpeed(float f) {
        return this.onGround() ? this.getSpeed() * (0.21600002F / (f * f * f)) : this.getFlyingSpeed();
    }

    protected float getFlyingSpeed() {
        return this.getControllingPassenger() instanceof EntityHuman ? this.getSpeed() * 0.1F : 0.02F;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float f) {
        this.speed = f;
    }

    public boolean doHurtTarget(WorldServer worldserver, Entity entity) {
        this.setLastHurtMob(entity);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.updatingUsingItem();
        this.updateSwimAmount();
        if (!this.level().isClientSide) {
            int i = this.getArrowCount();

            if (i > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - i);
                }

                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount(i - 1);
                }
            }

            int j = this.getStingerCount();

            if (j > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - j);
                }

                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount(j - 1);
                }
            }

            this.detectEquipmentUpdates();
            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }

            if (this.isSleeping() && !this.checkBedExists()) {
                this.stopSleeping();
            }
        }

        if (!this.isRemoved()) {
            this.aiStep();
        }

        double d0 = this.getX() - this.xo;
        double d1 = this.getZ() - this.zo;
        float f = (float) (d0 * d0 + d1 * d1);
        float f1 = this.yBodyRot;
        float f2 = 0.0F;

        this.oRun = this.run;
        float f3 = 0.0F;
        float f4;

        if (f > 0.0025000002F) {
            f3 = 1.0F;
            f2 = (float) Math.sqrt((double) f) * 3.0F;
            float f5 = (float) MathHelper.atan2(d1, d0) * 57.295776F - 90.0F;

            f4 = MathHelper.abs(MathHelper.wrapDegrees(this.getYRot()) - f5);
            if (95.0F < f4 && f4 < 265.0F) {
                f1 = f5 - 180.0F;
            } else {
                f1 = f5;
            }
        }

        if (this.attackAnim > 0.0F) {
            f1 = this.getYRot();
        }

        if (!this.onGround()) {
            f3 = 0.0F;
        }

        this.run += (f3 - this.run) * 0.3F;
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("headTurn");
        f2 = this.tickHeadTurn(f1, f2);
        gameprofilerfiller.pop();
        gameprofilerfiller.push("rangeChecks");

        while (this.getYRot() - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while (this.getYRot() - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        while (this.yBodyRot - this.yBodyRotO < -180.0F) {
            this.yBodyRotO -= 360.0F;
        }

        while (this.yBodyRot - this.yBodyRotO >= 180.0F) {
            this.yBodyRotO += 360.0F;
        }

        while (this.getXRot() - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while (this.getXRot() - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while (this.yHeadRot - this.yHeadRotO < -180.0F) {
            this.yHeadRotO -= 360.0F;
        }

        while (this.yHeadRot - this.yHeadRotO >= 180.0F) {
            this.yHeadRotO += 360.0F;
        }

        gameprofilerfiller.pop();
        this.animStep += f2;
        if (this.isFallFlying()) {
            ++this.fallFlyTicks;
        } else {
            this.fallFlyTicks = 0;
        }

        if (this.isSleeping()) {
            this.setXRot(0.0F);
        }

        this.refreshDirtyAttributes();
        f4 = this.getScale();
        if (f4 != this.appliedScale) {
            this.appliedScale = f4;
            this.refreshDimensions();
        }

        this.elytraAnimationState.tick();
    }

    public void detectEquipmentUpdates() {
        Map<EnumItemSlot, ItemStack> map = this.collectEquipmentChanges();

        if (map != null) {
            this.handleHandSwap(map);
            if (!map.isEmpty()) {
                this.handleEquipmentChanges(map);
            }
        }

    }

    @Nullable
    private Map<EnumItemSlot, ItemStack> collectEquipmentChanges() {
        Map<EnumItemSlot, ItemStack> map = null;
        Iterator iterator = EnumItemSlot.VALUES.iterator();

        ItemStack itemstack;

        while (iterator.hasNext()) {
            EnumItemSlot enumitemslot = (EnumItemSlot) iterator.next();
            ItemStack itemstack1;

            switch (enumitemslot.getType()) {
                case HAND:
                    itemstack1 = this.getLastHandItem(enumitemslot);
                    break;
                case HUMANOID_ARMOR:
                    itemstack1 = this.getLastArmorItem(enumitemslot);
                    break;
                case ANIMAL_ARMOR:
                    itemstack1 = this.lastBodyItemStack;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            ItemStack itemstack2 = itemstack1;

            itemstack = this.getItemBySlot(enumitemslot);
            if (this.equipmentHasChanged(itemstack2, itemstack)) {
                if (map == null) {
                    map = Maps.newEnumMap(EnumItemSlot.class);
                }

                map.put(enumitemslot, itemstack);
                AttributeMapBase attributemapbase = this.getAttributes();

                if (!itemstack2.isEmpty()) {
                    this.stopLocationBasedEffects(itemstack2, enumitemslot, attributemapbase);
                }
            }
        }

        if (map != null) {
            iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<EnumItemSlot, ItemStack> entry = (Entry) iterator.next();
                EnumItemSlot enumitemslot1 = (EnumItemSlot) entry.getKey();

                itemstack = (ItemStack) entry.getValue();
                if (!itemstack.isEmpty() && !itemstack.isBroken()) {
                    itemstack.forEachModifier(enumitemslot1, (holder, attributemodifier) -> {
                        AttributeModifiable attributemodifiable = this.attributes.getInstance(holder);

                        if (attributemodifiable != null) {
                            attributemodifiable.removeModifier(attributemodifier.id());
                            attributemodifiable.addTransientModifier(attributemodifier);
                        }

                    });
                    World world = this.level();

                    if (world instanceof WorldServer) {
                        WorldServer worldserver = (WorldServer) world;

                        EnchantmentManager.runLocationChangedEffects(worldserver, itemstack, this, enumitemslot1);
                    }
                }
            }
        }

        return map;
    }

    public boolean equipmentHasChanged(ItemStack itemstack, ItemStack itemstack1) {
        return !ItemStack.matches(itemstack1, itemstack);
    }

    private void handleHandSwap(Map<EnumItemSlot, ItemStack> map) {
        ItemStack itemstack = (ItemStack) map.get(EnumItemSlot.MAINHAND);
        ItemStack itemstack1 = (ItemStack) map.get(EnumItemSlot.OFFHAND);

        if (itemstack != null && itemstack1 != null && ItemStack.matches(itemstack, this.getLastHandItem(EnumItemSlot.OFFHAND)) && ItemStack.matches(itemstack1, this.getLastHandItem(EnumItemSlot.MAINHAND))) {
            ((WorldServer) this.level()).getChunkSource().broadcast(this, new PacketPlayOutEntityStatus(this, (byte) 55));
            map.remove(EnumItemSlot.MAINHAND);
            map.remove(EnumItemSlot.OFFHAND);
            this.setLastHandItem(EnumItemSlot.MAINHAND, itemstack.copy());
            this.setLastHandItem(EnumItemSlot.OFFHAND, itemstack1.copy());
        }

    }

    private void handleEquipmentChanges(Map<EnumItemSlot, ItemStack> map) {
        List<Pair<EnumItemSlot, ItemStack>> list = Lists.newArrayListWithCapacity(map.size());

        map.forEach((enumitemslot, itemstack) -> {
            ItemStack itemstack1 = itemstack.copy();

            list.add(Pair.of(enumitemslot, itemstack1));
            switch (enumitemslot.getType()) {
                case HAND:
                    this.setLastHandItem(enumitemslot, itemstack1);
                    break;
                case HUMANOID_ARMOR:
                    this.setLastArmorItem(enumitemslot, itemstack1);
                    break;
                case ANIMAL_ARMOR:
                    this.lastBodyItemStack = itemstack1;
            }

        });
        ((WorldServer) this.level()).getChunkSource().broadcast(this, new PacketPlayOutEntityEquipment(this.getId(), list));
    }

    private ItemStack getLastArmorItem(EnumItemSlot enumitemslot) {
        return (ItemStack) this.lastArmorItemStacks.get(enumitemslot.getIndex());
    }

    private void setLastArmorItem(EnumItemSlot enumitemslot, ItemStack itemstack) {
        this.lastArmorItemStacks.set(enumitemslot.getIndex(), itemstack);
    }

    private ItemStack getLastHandItem(EnumItemSlot enumitemslot) {
        return (ItemStack) this.lastHandItemStacks.get(enumitemslot.getIndex());
    }

    private void setLastHandItem(EnumItemSlot enumitemslot, ItemStack itemstack) {
        this.lastHandItemStacks.set(enumitemslot.getIndex(), itemstack);
    }

    protected float tickHeadTurn(float f, float f1) {
        float f2 = MathHelper.wrapDegrees(f - this.yBodyRot);

        this.yBodyRot += f2 * 0.3F;
        float f3 = MathHelper.wrapDegrees(this.getYRot() - this.yBodyRot);
        float f4 = this.getMaxHeadRotationRelativeToBody();

        if (Math.abs(f3) > f4) {
            this.yBodyRot += f3 - (float) MathHelper.sign((double) f3) * f4;
        }

        boolean flag = f3 < -90.0F || f3 >= 90.0F;

        if (flag) {
            f1 *= -1.0F;
        }

        return f1;
    }

    protected float getMaxHeadRotationRelativeToBody() {
        return 50.0F;
    }

    public void aiStep() {
        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }

        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            --this.lerpSteps;
        } else if (!this.isEffectiveAi()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        }

        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            --this.lerpHeadSteps;
        }

        Vec3D vec3d = this.getDeltaMovement();
        double d0 = vec3d.x;
        double d1 = vec3d.y;
        double d2 = vec3d.z;

        if (Math.abs(vec3d.x) < 0.003D) {
            d0 = 0.0D;
        }

        if (Math.abs(vec3d.y) < 0.003D) {
            d1 = 0.0D;
        }

        if (Math.abs(vec3d.z) < 0.003D) {
            d2 = 0.0D;
        }

        this.setDeltaMovement(d0, d1, d2);
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        } else if (this.isEffectiveAi()) {
            gameprofilerfiller.push("newAi");
            this.serverAiStep();
            gameprofilerfiller.pop();
        }

        gameprofilerfiller.pop();
        gameprofilerfiller.push("jump");
        if (this.jumping && this.isAffectedByFluids()) {
            double d3;

            if (this.isInLava()) {
                d3 = this.getFluidHeight(TagsFluid.LAVA);
            } else {
                d3 = this.getFluidHeight(TagsFluid.WATER);
            }

            boolean flag = this.isInWater() && d3 > 0.0D;
            double d4 = this.getFluidJumpThreshold();

            if (flag && (!this.onGround() || d3 > d4)) {
                this.jumpInLiquid(TagsFluid.WATER);
            } else if (this.isInLava() && (!this.onGround() || d3 > d4)) {
                this.jumpInLiquid(TagsFluid.LAVA);
            } else if ((this.onGround() || flag && d3 <= d4) && this.noJumpDelay == 0) {
                this.jumpFromGround();
                this.noJumpDelay = 10;
            }
        } else {
            this.noJumpDelay = 0;
        }

        gameprofilerfiller.pop();
        gameprofilerfiller.push("travel");
        this.xxa *= 0.98F;
        this.zza *= 0.98F;
        if (this.isFallFlying()) {
            this.updateFallFlying();
        }

        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        Vec3D vec3d1 = new Vec3D((double) this.xxa, (double) this.yya, (double) this.zza);

        if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
            this.resetFallDistance();
        }

        label112:
        {
            EntityLiving entityliving = this.getControllingPassenger();

            if (entityliving instanceof EntityHuman entityhuman) {
                if (this.isAlive()) {
                    this.travelRidden(entityhuman, vec3d1);
                    break label112;
                }
            }

            this.travel(vec3d1);
        }

        if (!this.level().isClientSide() || this.isControlledByLocalInstance()) {
            this.applyEffectsFromBlocks();
        }

        this.calculateEntityAnimation(this instanceof EntityBird);
        gameprofilerfiller.pop();
        gameprofilerfiller.push("freezing");
        if (!this.level().isClientSide && !this.isDeadOrDying()) {
            int i = this.getTicksFrozen();

            if (this.isInPowderSnow && this.canFreeze()) {
                this.setTicksFrozen(Math.min(this.getTicksRequiredToFreeze(), i + 1));
            } else {
                this.setTicksFrozen(Math.max(0, i - 2));
            }
        }

        this.removeFrost();
        this.tryAddFrost();
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
                this.hurtServer(worldserver, this.damageSources().freeze(), 1.0F);
            }
        }

        gameprofilerfiller.pop();
        gameprofilerfiller.push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            this.checkAutoSpinAttack(axisalignedbb, this.getBoundingBox());
        }

        this.pushEntities();
        gameprofilerfiller.pop();
        world = this.level();
        if (world instanceof WorldServer worldserver) {
            if (this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
                this.hurtServer(worldserver, this.damageSources().drown(), 1.0F);
            }
        }

    }

    public boolean isSensitiveToWater() {
        return false;
    }

    protected void updateFallFlying() {
        this.checkSlowFallDistance();
        if (!this.level().isClientSide) {
            if (!this.canGlide()) {
                this.setSharedFlag(7, false);
                return;
            }

            int i = this.fallFlyTicks + 1;

            if (i % 10 == 0) {
                int j = i / 10;

                if (j % 2 == 0) {
                    List<EnumItemSlot> list = EnumItemSlot.VALUES.stream().filter((enumitemslot) -> {
                        return canGlideUsing(this.getItemBySlot(enumitemslot), enumitemslot);
                    }).toList();
                    EnumItemSlot enumitemslot = (EnumItemSlot) SystemUtils.getRandom(list, this.random);

                    this.getItemBySlot(enumitemslot).hurtAndBreak(1, this, enumitemslot);
                }

                this.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }

    }

    protected boolean canGlide() {
        if (!this.onGround() && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
            Iterator iterator = EnumItemSlot.VALUES.iterator();

            EnumItemSlot enumitemslot;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                enumitemslot = (EnumItemSlot) iterator.next();
            } while (!canGlideUsing(this.getItemBySlot(enumitemslot), enumitemslot));

            return true;
        } else {
            return false;
        }
    }

    protected void serverAiStep() {}

    protected void pushEntities() {
        World world = this.level();

        if (!(world instanceof WorldServer worldserver)) {
            this.level().getEntities(EntityTypeTest.forClass(EntityHuman.class), this.getBoundingBox(), IEntitySelector.pushableBy(this)).forEach(this::doPush);
        } else {
            List list = this.level().getEntities((Entity) this, this.getBoundingBox(), IEntitySelector.pushableBy(this));

            if (!list.isEmpty()) {
                int i = worldserver.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);

                if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                    int j = 0;
                    Iterator iterator = list.iterator();

                    while (iterator.hasNext()) {
                        Entity entity = (Entity) iterator.next();

                        if (!entity.isPassenger()) {
                            ++j;
                        }
                    }

                    if (j > i - 1) {
                        this.hurtServer(worldserver, this.damageSources().cramming(), 6.0F);
                    }
                }

                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    Entity entity1 = (Entity) iterator1.next();

                    this.doPush(entity1);
                }
            }

        }
    }

    protected void checkAutoSpinAttack(AxisAlignedBB axisalignedbb, AxisAlignedBB axisalignedbb1) {
        AxisAlignedBB axisalignedbb2 = axisalignedbb.minmax(axisalignedbb1);
        List<Entity> list = this.level().getEntities(this, axisalignedbb2);

        if (!list.isEmpty()) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                if (entity instanceof EntityLiving) {
                    this.doAutoAttackOnTouch((EntityLiving) entity);
                    this.autoSpinAttackTicks = 0;
                    this.setDeltaMovement(this.getDeltaMovement().scale(-0.2D));
                    break;
                }
            }
        } else if (this.horizontalCollision) {
            this.autoSpinAttackTicks = 0;
        }

        if (!this.level().isClientSide && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
            this.autoSpinAttackDmg = 0.0F;
            this.autoSpinAttackItemStack = null;
        }

    }

    protected void doPush(Entity entity) {
        entity.push((Entity) this);
    }

    protected void doAutoAttackOnTouch(EntityLiving entityliving) {}

    public boolean isAutoSpinAttack() {
        return ((Byte) this.entityData.get(EntityLiving.DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity entity = this.getVehicle();

        super.stopRiding();
        if (entity != null && entity != this.getVehicle() && !this.level().isClientSide) {
            this.dismountVehicle(entity);
        }

    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.oRun = this.run;
        this.run = 0.0F;
        this.resetFallDistance();
    }

    @Override
    public void cancelLerp() {
        this.lerpSteps = 0;
    }

    @Override
    public void lerpTo(double d0, double d1, double d2, float f, float f1, int i) {
        this.lerpX = d0;
        this.lerpY = d1;
        this.lerpZ = d2;
        this.lerpYRot = (double) f;
        this.lerpXRot = (double) f1;
        this.lerpSteps = i;
    }

    @Override
    public double lerpTargetX() {
        return this.lerpSteps > 0 ? this.lerpX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.lerpSteps > 0 ? this.lerpY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.lerpSteps > 0 ? (float) this.lerpXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.lerpSteps > 0 ? (float) this.lerpYRot : this.getYRot();
    }

    @Override
    public void lerpHeadTo(float f, int i) {
        this.lerpYHeadRot = (double) f;
        this.lerpHeadSteps = i;
    }

    public void setJumping(boolean flag) {
        this.jumping = flag;
    }

    public void onItemPickup(EntityItem entityitem) {
        Entity entity = entityitem.getOwner();

        if (entity instanceof EntityPlayer) {
            CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((EntityPlayer) entity, entityitem.getItem(), this);
        }

    }

    public void take(Entity entity, int i) {
        if (!entity.isRemoved() && !this.level().isClientSide && (entity instanceof EntityItem || entity instanceof EntityArrow || entity instanceof EntityExperienceOrb)) {
            ((WorldServer) this.level()).getChunkSource().broadcast(entity, new PacketPlayOutCollect(entity.getId(), this.getId(), i));
        }

    }

    public boolean hasLineOfSight(Entity entity) {
        return this.hasLineOfSight(entity, RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, entity.getEyeY());
    }

    public boolean hasLineOfSight(Entity entity, RayTrace.BlockCollisionOption raytrace_blockcollisionoption, RayTrace.FluidCollisionOption raytrace_fluidcollisionoption, double d0) {
        if (entity.level() != this.level()) {
            return false;
        } else {
            Vec3D vec3d = new Vec3D(this.getX(), this.getEyeY(), this.getZ());
            Vec3D vec3d1 = new Vec3D(entity.getX(), d0, entity.getZ());

            return vec3d1.distanceTo(vec3d) > 128.0D ? false : this.level().clip(new RayTrace(vec3d, vec3d1, raytrace_blockcollisionoption, raytrace_fluidcollisionoption, this)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS;
        }
    }

    @Override
    public float getViewYRot(float f) {
        return f == 1.0F ? this.yHeadRot : MathHelper.rotLerp(f, this.yHeadRotO, this.yHeadRot);
    }

    public float getAttackAnim(float f) {
        float f1 = this.attackAnim - this.oAttackAnim;

        if (f1 < 0.0F) {
            ++f1;
        }

        return this.oAttackAnim + f1 * f;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.onClimbable();
    }

    @Override
    public float getYHeadRot() {
        return this.yHeadRot;
    }

    @Override
    public void setYHeadRot(float f) {
        this.yHeadRot = f;
    }

    @Override
    public void setYBodyRot(float f) {
        this.yBodyRot = f;
    }

    @Override
    public Vec3D getRelativePortalPosition(EnumDirection.EnumAxis enumdirection_enumaxis, BlockUtil.Rectangle blockutil_rectangle) {
        return resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(enumdirection_enumaxis, blockutil_rectangle));
    }

    public static Vec3D resetForwardDirectionOfRelativePortalPosition(Vec3D vec3d) {
        return new Vec3D(vec3d.x, vec3d.y, 0.0D);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public final void setAbsorptionAmount(float f) {
        this.internalSetAbsorptionAmount(MathHelper.clamp(f, 0.0F, this.getMaxAbsorption()));
    }

    protected void internalSetAbsorptionAmount(float f) {
        this.absorptionAmount = f;
    }

    public void onEnterCombat() {}

    public void onLeaveCombat() {}

    protected void updateEffectVisibility() {
        this.effectsDirty = true;
    }

    public abstract EnumMainHand getMainArm();

    public boolean isUsingItem() {
        return ((Byte) this.entityData.get(EntityLiving.DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
    }

    public EnumHand getUsedItemHand() {
        return ((Byte) this.entityData.get(EntityLiving.DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
    }

    private void updatingUsingItem() {
        if (this.isUsingItem()) {
            if (ItemStack.isSameItem(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                this.updateUsingItem(this.useItem);
            } else {
                this.stopUsingItem();
            }
        }

    }

    protected void updateUsingItem(ItemStack itemstack) {
        itemstack.onUseTick(this.level(), this, this.getUseItemRemainingTicks());
        if (--this.useItemRemaining == 0 && !this.level().isClientSide && !itemstack.useOnRelease()) {
            this.completeUsingItem();
        }

    }

    private void updateSwimAmount() {
        this.swimAmountO = this.swimAmount;
        if (this.isVisuallySwimming()) {
            this.swimAmount = Math.min(1.0F, this.swimAmount + 0.09F);
        } else {
            this.swimAmount = Math.max(0.0F, this.swimAmount - 0.09F);
        }

    }

    public void setLivingEntityFlag(int i, boolean flag) {
        int j = (Byte) this.entityData.get(EntityLiving.DATA_LIVING_ENTITY_FLAGS);

        if (flag) {
            j |= i;
        } else {
            j &= ~i;
        }

        this.entityData.set(EntityLiving.DATA_LIVING_ENTITY_FLAGS, (byte) j);
    }

    public void startUsingItem(EnumHand enumhand) {
        ItemStack itemstack = this.getItemInHand(enumhand);

        if (!itemstack.isEmpty() && !this.isUsingItem()) {
            this.useItem = itemstack;
            this.useItemRemaining = itemstack.getUseDuration(this);
            if (!this.level().isClientSide) {
                this.setLivingEntityFlag(1, true);
                this.setLivingEntityFlag(2, enumhand == EnumHand.OFF_HAND);
                this.gameEvent(GameEvent.ITEM_INTERACT_START);
            }

        }
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (EntityLiving.SLEEPING_POS_ID.equals(datawatcherobject)) {
            if (this.level().isClientSide) {
                this.getSleepingPos().ifPresent(this::setPosToBed);
            }
        } else if (EntityLiving.DATA_LIVING_ENTITY_FLAGS.equals(datawatcherobject) && this.level().isClientSide) {
            if (this.isUsingItem() && this.useItem.isEmpty()) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                if (!this.useItem.isEmpty()) {
                    this.useItemRemaining = this.useItem.getUseDuration(this);
                }
            } else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
                this.useItem = ItemStack.EMPTY;
                this.useItemRemaining = 0;
            }
        }

    }

    @Override
    public void lookAt(ArgumentAnchor.Anchor argumentanchor_anchor, Vec3D vec3d) {
        super.lookAt(argumentanchor_anchor, vec3d);
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRot = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
    }

    @Override
    public float getPreciseBodyRotation(float f) {
        return MathHelper.lerp(f, this.yBodyRotO, this.yBodyRot);
    }

    public void spawnItemParticles(ItemStack itemstack, int i) {
        for (int j = 0; j < i; ++j) {
            Vec3D vec3d = new Vec3D(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);

            vec3d = vec3d.xRot(-this.getXRot() * 0.017453292F);
            vec3d = vec3d.yRot(-this.getYRot() * 0.017453292F);
            double d0 = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3D vec3d1 = new Vec3D(((double) this.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);

            vec3d1 = vec3d1.xRot(-this.getXRot() * 0.017453292F);
            vec3d1 = vec3d1.yRot(-this.getYRot() * 0.017453292F);
            vec3d1 = vec3d1.add(this.getX(), this.getEyeY(), this.getZ());
            this.level().addParticle(new ParticleParamItem(Particles.ITEM, itemstack), vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
        }

    }

    protected void completeUsingItem() {
        if (!this.level().isClientSide || this.isUsingItem()) {
            EnumHand enumhand = this.getUsedItemHand();

            if (!this.useItem.equals(this.getItemInHand(enumhand))) {
                this.releaseUsingItem();
            } else {
                if (!this.useItem.isEmpty() && this.isUsingItem()) {
                    ItemStack itemstack = this.useItem.finishUsingItem(this.level(), this);

                    if (itemstack != this.useItem) {
                        this.setItemInHand(enumhand, itemstack);
                    }

                    this.stopUsingItem();
                }

            }
        }
    }

    public void handleExtraItemsCreatedOnUse(ItemStack itemstack) {}

    public ItemStack getUseItem() {
        return this.useItem;
    }

    public int getUseItemRemainingTicks() {
        return this.useItemRemaining;
    }

    public int getTicksUsingItem() {
        return this.isUsingItem() ? this.useItem.getUseDuration(this) - this.getUseItemRemainingTicks() : 0;
    }

    public void releaseUsingItem() {
        if (!this.useItem.isEmpty()) {
            this.useItem.releaseUsing(this.level(), this, this.getUseItemRemainingTicks());
            if (this.useItem.useOnRelease()) {
                this.updatingUsingItem();
            }
        }

        this.stopUsingItem();
    }

    public void stopUsingItem() {
        if (!this.level().isClientSide) {
            boolean flag = this.isUsingItem();

            this.setLivingEntityFlag(1, false);
            if (flag) {
                this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
        }

        this.useItem = ItemStack.EMPTY;
        this.useItemRemaining = 0;
    }

    public boolean isBlocking() {
        return this.getItemBlockingWith() != null;
    }

    @Nullable
    public ItemStack getItemBlockingWith() {
        if (this.isUsingItem() && !this.useItem.isEmpty()) {
            Item item = this.useItem.getItem();

            return item.getUseAnimation(this.useItem) != ItemUseAnimation.BLOCK ? null : (item.getUseDuration(this.useItem, this) - this.useItemRemaining < 5 ? null : this.useItem);
        } else {
            return null;
        }
    }

    public boolean isSuppressingSlidingDownLadder() {
        return this.isShiftKeyDown();
    }

    public boolean isFallFlying() {
        return this.getSharedFlag(7);
    }

    @Override
    public boolean isVisuallySwimming() {
        return super.isVisuallySwimming() || !this.isFallFlying() && this.hasPose(EntityPose.FALL_FLYING);
    }

    public int getFallFlyingTicks() {
        return this.fallFlyTicks;
    }

    public boolean randomTeleport(double d0, double d1, double d2, boolean flag) {
        double d3 = this.getX();
        double d4 = this.getY();
        double d5 = this.getZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockposition = BlockPosition.containing(d0, d1, d2);
        World world = this.level();

        if (world.hasChunkAt(blockposition)) {
            boolean flag2 = false;

            while (!flag2 && blockposition.getY() > world.getMinY()) {
                BlockPosition blockposition1 = blockposition.below();
                IBlockData iblockdata = world.getBlockState(blockposition1);

                if (iblockdata.blocksMotion()) {
                    flag2 = true;
                } else {
                    --d6;
                    blockposition = blockposition1;
                }
            }

            if (flag2) {
                this.teleportTo(d0, d6, d2);
                if (world.noCollision((Entity) this) && !world.containsAnyLiquid(this.getBoundingBox())) {
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.teleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEvent(this, (byte) 46);
            }

            if (this instanceof EntityCreature) {
                EntityCreature entitycreature = (EntityCreature) this;

                entitycreature.getNavigation().stop();
            }

            return true;
        }
    }

    public boolean isAffectedByPotions() {
        return !this.isDeadOrDying();
    }

    public boolean attackable() {
        return true;
    }

    public void setRecordPlayingNearby(BlockPosition blockposition, boolean flag) {}

    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public final EntitySize getDimensions(EntityPose entitypose) {
        return entitypose == EntityPose.SLEEPING ? EntityLiving.SLEEPING_DIMENSIONS : this.getDefaultDimensions(entitypose).scale(this.getScale());
    }

    protected EntitySize getDefaultDimensions(EntityPose entitypose) {
        return this.getType().getDimensions().scale(this.getAgeScale());
    }

    public ImmutableList<EntityPose> getDismountPoses() {
        return ImmutableList.of(EntityPose.STANDING);
    }

    public AxisAlignedBB getLocalBoundsForPose(EntityPose entitypose) {
        EntitySize entitysize = this.getDimensions(entitypose);

        return new AxisAlignedBB((double) (-entitysize.width() / 2.0F), 0.0D, (double) (-entitysize.width() / 2.0F), (double) (entitysize.width() / 2.0F), (double) entitysize.height(), (double) (entitysize.width() / 2.0F));
    }

    protected boolean wouldNotSuffocateAtTargetPose(EntityPose entitypose) {
        AxisAlignedBB axisalignedbb = this.getDimensions(entitypose).makeBoundingBox(this.position());

        return this.level().noBlockCollision(this, axisalignedbb);
    }

    @Override
    public boolean canUsePortal(boolean flag) {
        return super.canUsePortal(flag) && !this.isSleeping();
    }

    public Optional<BlockPosition> getSleepingPos() {
        return (Optional) this.entityData.get(EntityLiving.SLEEPING_POS_ID);
    }

    public void setSleepingPos(BlockPosition blockposition) {
        this.entityData.set(EntityLiving.SLEEPING_POS_ID, Optional.of(blockposition));
    }

    public void clearSleepingPos() {
        this.entityData.set(EntityLiving.SLEEPING_POS_ID, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPos().isPresent();
    }

    public void startSleeping(BlockPosition blockposition) {
        if (this.isPassenger()) {
            this.stopRiding();
        }

        IBlockData iblockdata = this.level().getBlockState(blockposition);

        if (iblockdata.getBlock() instanceof BlockBed) {
            this.level().setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockBed.OCCUPIED, true), 3);
        }

        this.setPose(EntityPose.SLEEPING);
        this.setPosToBed(blockposition);
        this.setSleepingPos(blockposition);
        this.setDeltaMovement(Vec3D.ZERO);
        this.hasImpulse = true;
    }

    private void setPosToBed(BlockPosition blockposition) {
        this.setPos((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.6875D, (double) blockposition.getZ() + 0.5D);
    }

    private boolean checkBedExists() {
        return (Boolean) this.getSleepingPos().map((blockposition) -> {
            return this.level().getBlockState(blockposition).getBlock() instanceof BlockBed;
        }).orElse(false);
    }

    public void stopSleeping() {
        Optional optional = this.getSleepingPos();
        World world = this.level();

        java.util.Objects.requireNonNull(world);
        optional.filter(world::hasChunkAt).ifPresent((blockposition) -> {
            IBlockData iblockdata = this.level().getBlockState(blockposition);

            if (iblockdata.getBlock() instanceof BlockBed) {
                EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockBed.FACING);

                this.level().setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockBed.OCCUPIED, false), 3);
                Vec3D vec3d = (Vec3D) BlockBed.findStandUpPosition(this.getType(), this.level(), blockposition, enumdirection, this.getYRot()).orElseGet(() -> {
                    BlockPosition blockposition1 = blockposition.above();

                    return new Vec3D((double) blockposition1.getX() + 0.5D, (double) blockposition1.getY() + 0.1D, (double) blockposition1.getZ() + 0.5D);
                });
                Vec3D vec3d1 = Vec3D.atBottomCenterOf(blockposition).subtract(vec3d).normalize();
                float f = (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D - 90.0D);

                this.setPos(vec3d.x, vec3d.y, vec3d.z);
                this.setYRot(f);
                this.setXRot(0.0F);
            }

        });
        Vec3D vec3d = this.position();

        this.setPose(EntityPose.STANDING);
        this.setPos(vec3d.x, vec3d.y, vec3d.z);
        this.clearSleepingPos();
    }

    @Nullable
    public EnumDirection getBedOrientation() {
        BlockPosition blockposition = (BlockPosition) this.getSleepingPos().orElse((Object) null);

        return blockposition != null ? BlockBed.getBedOrientation(this.level(), blockposition) : null;
    }

    @Override
    public boolean isInWall() {
        return !this.isSleeping() && super.isInWall();
    }

    public ItemStack getProjectile(ItemStack itemstack) {
        return ItemStack.EMPTY;
    }

    private static byte entityEventForEquipmentBreak(EnumItemSlot enumitemslot) {
        byte b0;

        switch (enumitemslot) {
            case MAINHAND:
                b0 = 47;
                break;
            case OFFHAND:
                b0 = 48;
                break;
            case HEAD:
                b0 = 49;
                break;
            case CHEST:
                b0 = 50;
                break;
            case FEET:
                b0 = 52;
                break;
            case LEGS:
                b0 = 51;
                break;
            case BODY:
                b0 = 65;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return b0;
    }

    public void onEquippedItemBroken(Item item, EnumItemSlot enumitemslot) {
        this.level().broadcastEntityEvent(this, entityEventForEquipmentBreak(enumitemslot));
        this.stopLocationBasedEffects(this.getItemBySlot(enumitemslot), enumitemslot, this.attributes);
    }

    private void stopLocationBasedEffects(ItemStack itemstack, EnumItemSlot enumitemslot, AttributeMapBase attributemapbase) {
        itemstack.forEachModifier(enumitemslot, (holder, attributemodifier) -> {
            AttributeModifiable attributemodifiable = attributemapbase.getInstance(holder);

            if (attributemodifiable != null) {
                attributemodifiable.removeModifier(attributemodifier);
            }

        });
        EnchantmentManager.stopLocationBasedEffects(itemstack, this, enumitemslot);
    }

    public static EnumItemSlot getSlotForHand(EnumHand enumhand) {
        return enumhand == EnumHand.MAIN_HAND ? EnumItemSlot.MAINHAND : EnumItemSlot.OFFHAND;
    }

    public final boolean canEquipWithDispenser(ItemStack itemstack) {
        if (this.isAlive() && !this.isSpectator()) {
            Equippable equippable = (Equippable) itemstack.get(DataComponents.EQUIPPABLE);

            if (equippable != null && equippable.dispensable()) {
                EnumItemSlot enumitemslot = equippable.slot();

                return this.canUseSlot(enumitemslot) && equippable.canBeEquippedBy(this.getType()) ? this.getItemBySlot(enumitemslot).isEmpty() && this.canDispenserEquipIntoSlot(enumitemslot) : false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected boolean canDispenserEquipIntoSlot(EnumItemSlot enumitemslot) {
        return true;
    }

    public final EnumItemSlot getEquipmentSlotForItem(ItemStack itemstack) {
        Equippable equippable = (Equippable) itemstack.get(DataComponents.EQUIPPABLE);

        return equippable != null && this.canUseSlot(equippable.slot()) ? equippable.slot() : EnumItemSlot.MAINHAND;
    }

    public final boolean isEquippableInSlot(ItemStack itemstack, EnumItemSlot enumitemslot) {
        Equippable equippable = (Equippable) itemstack.get(DataComponents.EQUIPPABLE);

        return equippable == null ? enumitemslot == EnumItemSlot.MAINHAND && this.canUseSlot(EnumItemSlot.MAINHAND) : enumitemslot == equippable.slot() && this.canUseSlot(equippable.slot()) && equippable.canBeEquippedBy(this.getType());
    }

    private static SlotAccess createEquipmentSlotAccess(EntityLiving entityliving, EnumItemSlot enumitemslot) {
        return enumitemslot != EnumItemSlot.HEAD && enumitemslot != EnumItemSlot.MAINHAND && enumitemslot != EnumItemSlot.OFFHAND ? SlotAccess.forEquipmentSlot(entityliving, enumitemslot, (itemstack) -> {
            return itemstack.isEmpty() || entityliving.getEquipmentSlotForItem(itemstack) == enumitemslot;
        }) : SlotAccess.forEquipmentSlot(entityliving, enumitemslot);
    }

    @Nullable
    private static EnumItemSlot getEquipmentSlot(int i) {
        return i == 100 + EnumItemSlot.HEAD.getIndex() ? EnumItemSlot.HEAD : (i == 100 + EnumItemSlot.CHEST.getIndex() ? EnumItemSlot.CHEST : (i == 100 + EnumItemSlot.LEGS.getIndex() ? EnumItemSlot.LEGS : (i == 100 + EnumItemSlot.FEET.getIndex() ? EnumItemSlot.FEET : (i == 98 ? EnumItemSlot.MAINHAND : (i == 99 ? EnumItemSlot.OFFHAND : (i == 105 ? EnumItemSlot.BODY : null))))));
    }

    @Override
    public SlotAccess getSlot(int i) {
        EnumItemSlot enumitemslot = getEquipmentSlot(i);

        return enumitemslot != null ? createEquipmentSlotAccess(this, enumitemslot) : super.getSlot(i);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        } else {
            boolean flag = !this.getItemBySlot(EnumItemSlot.HEAD).is(TagsItem.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EnumItemSlot.CHEST).is(TagsItem.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EnumItemSlot.LEGS).is(TagsItem.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EnumItemSlot.FEET).is(TagsItem.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EnumItemSlot.BODY).is(TagsItem.FREEZE_IMMUNE_WEARABLES);

            return flag && super.canFreeze();
        }
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return !this.level().isClientSide() && this.hasEffect(MobEffects.GLOWING) || super.isCurrentlyGlowing();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.yBodyRot;
    }

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        double d0 = packetplayoutspawnentity.getX();
        double d1 = packetplayoutspawnentity.getY();
        double d2 = packetplayoutspawnentity.getZ();
        float f = packetplayoutspawnentity.getYRot();
        float f1 = packetplayoutspawnentity.getXRot();

        this.syncPacketPositionCodec(d0, d1, d2);
        this.yBodyRot = packetplayoutspawnentity.getYHeadRot();
        this.yHeadRot = packetplayoutspawnentity.getYHeadRot();
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.setId(packetplayoutspawnentity.getId());
        this.setUUID(packetplayoutspawnentity.getUUID());
        this.absMoveTo(d0, d1, d2, f, f1);
        this.setDeltaMovement(packetplayoutspawnentity.getXa(), packetplayoutspawnentity.getYa(), packetplayoutspawnentity.getZa());
    }

    public boolean canDisableShield() {
        return this.getWeaponItem().getItem() instanceof ItemAxe;
    }

    @Override
    public float maxUpStep() {
        float f = (float) this.getAttributeValue(GenericAttributes.STEP_HEIGHT);

        return this.getControllingPassenger() instanceof EntityHuman ? Math.max(f, 1.0F) : f;
    }

    @Override
    public Vec3D getPassengerRidingPosition(Entity entity) {
        return this.position().add(this.getPassengerAttachmentPoint(entity, this.getDimensions(this.getPose()), this.getScale() * this.getAgeScale()));
    }

    protected void lerpHeadRotationStep(int i, double d0) {
        this.yHeadRot = (float) MathHelper.rotLerp(1.0D / (double) i, (double) this.yHeadRot, d0);
    }

    @Override
    public void igniteForTicks(int i) {
        super.igniteForTicks(MathHelper.ceil((double) i * this.getAttributeValue(GenericAttributes.BURNING_TIME)));
    }

    public boolean hasInfiniteMaterials() {
        return false;
    }

    public boolean isInvulnerableTo(WorldServer worldserver, DamageSource damagesource) {
        return this.isInvulnerableToBase(damagesource) || EnchantmentManager.isImmuneToDamage(worldserver, this, damagesource);
    }

    public static boolean canGlideUsing(ItemStack itemstack, EnumItemSlot enumitemslot) {
        if (!itemstack.has(DataComponents.GLIDER)) {
            return false;
        } else {
            Equippable equippable = (Equippable) itemstack.get(DataComponents.EQUIPPABLE);

            return equippable != null && enumitemslot == equippable.slot() && !itemstack.nextDamageWillBreak();
        }
    }

    @VisibleForTesting
    public int getLastHurtByPlayerTime() {
        return this.lastHurtByPlayerTime;
    }

    public static record a(SoundEffect small, SoundEffect big) {

    }
}
