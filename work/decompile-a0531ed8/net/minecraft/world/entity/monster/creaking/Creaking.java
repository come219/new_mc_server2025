package net.minecraft.world.entity.monster.creaking;

import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerJump;
import net.minecraft.world.entity.ai.control.ControllerLook;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.entity.ai.control.EntityAIBodyControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Pathfinder;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class Creaking extends EntityMonster {

    private static final DataWatcherObject<Boolean> CAN_MOVE = DataWatcher.defineId(Creaking.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Boolean> IS_ACTIVE = DataWatcher.defineId(Creaking.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Boolean> IS_TEARING_DOWN = DataWatcher.defineId(Creaking.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Optional<BlockPosition>> HOME_POS = DataWatcher.defineId(Creaking.class, DataWatcherRegistry.OPTIONAL_BLOCK_POS);
    private static final int ATTACK_ANIMATION_DURATION = 15;
    private static final int MAX_HEALTH = 1;
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float FOLLOW_RANGE = 32.0F;
    private static final float ACTIVATION_RANGE_SQ = 144.0F;
    public static final int ATTACK_INTERVAL = 40;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.4F;
    public static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.3F;
    public static final int CREAKING_ORANGE = 16545810;
    public static final int CREAKING_GRAY = 6250335;
    public static final int INVULNERABILITY_ANIMATION_DURATION = 8;
    public static final int TWITCH_DEATH_DURATION = 45;
    private static final int MAX_PLAYER_STUCK_COUNTER = 4;
    private int attackAnimationRemainingTicks;
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState invulnerabilityAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    private int invulnerabilityAnimationRemainingTicks;
    private boolean eyesGlowing;
    private int nextFlickerTime;
    private int playerStuckCounter;

    public Creaking(EntityTypes<? extends Creaking> entitytypes, World world) {
        super(entitytypes, world);
        this.lookControl = new Creaking.c(this);
        this.moveControl = new Creaking.d(this);
        this.jumpControl = new Creaking.b(this);
        Navigation navigation = (Navigation) this.getNavigation();

        navigation.setCanFloat(true);
        this.xpReward = 0;
    }

    public void setTransient(BlockPosition blockposition) {
        this.setHomePos(blockposition);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0F);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public boolean isHeartBound() {
        return this.getHomePos() != null;
    }

    @Override
    protected EntityAIBodyControl createBodyControl() {
        return new Creaking.a(this);
    }

    @Override
    protected BehaviorController.b<Creaking> brainProvider() {
        return CreakingAi.brainProvider();
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return CreakingAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(Creaking.CAN_MOVE, true);
        datawatcher_a.define(Creaking.IS_ACTIVE, false);
        datawatcher_a.define(Creaking.IS_TEARING_DOWN, false);
        datawatcher_a.define(Creaking.HOME_POS, Optional.empty());
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityMonster.createMonsterAttributes().add(GenericAttributes.MAX_HEALTH, 1.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.4000000059604645D).add(GenericAttributes.ATTACK_DAMAGE, 3.0D).add(GenericAttributes.FOLLOW_RANGE, 32.0D).add(GenericAttributes.STEP_HEIGHT, 1.0625D);
    }

    public boolean canMove() {
        return (Boolean) this.entityData.get(Creaking.CAN_MOVE);
    }

    @Override
    public boolean doHurtTarget(WorldServer worldserver, Entity entity) {
        if (!(entity instanceof EntityLiving)) {
            return false;
        } else {
            this.attackAnimationRemainingTicks = 15;
            this.level().broadcastEntityEvent(this, (byte) 4);
            return super.doHurtTarget(worldserver, entity);
        }
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        BlockPosition blockposition = this.getHomePos();

        if (blockposition != null && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!this.isInvulnerableTo(worldserver, damagesource) && this.invulnerabilityAnimationRemainingTicks <= 0 && !this.isDeadOrDying()) {
                EntityHuman entityhuman = this.blameSourceForDamage(damagesource);
                Entity entity = damagesource.getDirectEntity();

                if (!(entity instanceof EntityLiving) && !(entity instanceof IProjectile) && entityhuman == null) {
                    return false;
                } else {
                    this.invulnerabilityAnimationRemainingTicks = 8;
                    this.level().broadcastEntityEvent(this, (byte) 66);
                    TileEntity tileentity = this.level().getBlockEntity(blockposition);

                    if (tileentity instanceof CreakingHeartBlockEntity) {
                        CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) tileentity;

                        if (creakingheartblockentity.isProtector(this)) {
                            if (entityhuman != null) {
                                creakingheartblockentity.creakingHurt();
                            }

                            this.playHurtSound(damagesource);
                        }
                    }

                    return true;
                }
            } else {
                return false;
            }
        } else {
            return super.hurtServer(worldserver, damagesource, f);
        }
    }

    public EntityHuman blameSourceForDamage(DamageSource damagesource) {
        this.resolveMobResponsibleForDamage(damagesource);
        return this.resolvePlayerResponsibleForDamage(damagesource);
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && this.canMove();
    }

    @Override
    public void push(double d0, double d1, double d2) {
        if (this.canMove()) {
            super.push(d0, d1, d2);
        }
    }

    @Override
    public BehaviorController<Creaking> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("creakingBrain");
        this.getBrain().tick((WorldServer) this.level(), this);
        gameprofilerfiller.pop();
        CreakingAi.updateActivity(this);
    }

    @Override
    public void aiStep() {
        if (this.invulnerabilityAnimationRemainingTicks > 0) {
            --this.invulnerabilityAnimationRemainingTicks;
        }

        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }

        if (!this.level().isClientSide) {
            boolean flag = (Boolean) this.entityData.get(Creaking.CAN_MOVE);
            boolean flag1 = this.checkCanMove();

            if (flag1 != flag) {
                this.gameEvent(GameEvent.ENTITY_ACTION);
                if (flag1) {
                    this.makeSound(SoundEffects.CREAKING_UNFREEZE);
                } else {
                    this.stopInPlace();
                    this.makeSound(SoundEffects.CREAKING_FREEZE);
                }
            }

            this.entityData.set(Creaking.CAN_MOVE, flag1);
        }

        super.aiStep();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            BlockPosition blockposition = this.getHomePos();

            if (blockposition != null) {
                boolean flag;
                label21:
                {
                    TileEntity tileentity = this.level().getBlockEntity(blockposition);

                    if (tileentity instanceof CreakingHeartBlockEntity) {
                        CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) tileentity;

                        if (creakingheartblockentity.isProtector(this)) {
                            flag = true;
                            break label21;
                        }
                    }

                    flag = false;
                }

                boolean flag1 = flag;

                if (!flag1) {
                    this.setHealth(0.0F);
                }
            }
        }

        super.tick();
        if (this.level().isClientSide) {
            this.setupAnimationStates();
            this.checkEyeBlink();
        }

    }

    @Override
    protected void tickDeath() {
        if (this.isHeartBound() && this.isTearingDown()) {
            ++this.deathTime;
            if (!this.level().isClientSide() && this.deathTime > 45 && !this.isRemoved()) {
                this.tearDown();
            }
        } else {
            super.tickDeath();
        }

    }

    @Override
    protected void updateWalkAnimation(float f) {
        float f1 = Math.min(f * 25.0F, 3.0F);

        this.walkAnimation.update(f1, 0.4F, 1.0F);
    }

    private void setupAnimationStates() {
        this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
        this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
        this.deathAnimationState.animateWhen(this.isTearingDown(), this.tickCount);
    }

    public void tearDown() {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            AxisAlignedBB axisalignedbb = this.getBoundingBox();
            Vec3D vec3d = axisalignedbb.getCenter();
            double d0 = axisalignedbb.getXsize() * 0.3D;
            double d1 = axisalignedbb.getYsize() * 0.3D;
            double d2 = axisalignedbb.getZsize() * 0.3D;

            worldserver.sendParticles(new ParticleParamBlock(Particles.BLOCK_CRUMBLE, Blocks.PALE_OAK_WOOD.defaultBlockState()), vec3d.x, vec3d.y, vec3d.z, 100, d0, d1, d2, 0.0D);
            worldserver.sendParticles(new ParticleParamBlock(Particles.BLOCK_CRUMBLE, (IBlockData) Blocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.ACTIVE, true)), vec3d.x, vec3d.y, vec3d.z, 10, d0, d1, d2, 0.0D);
        }

        this.makeSound(this.getDeathSound());
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    public void creakingDeathEffects(DamageSource damagesource) {
        this.blameSourceForDamage(damagesource);
        this.die(damagesource);
        this.makeSound(SoundEffects.CREAKING_TWITCH);
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 66) {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.playHurtSound(this.damageSources().generic());
        } else if (b0 == 4) {
            this.attackAnimationRemainingTicks = 15;
            this.playAttackSound();
        } else {
            super.handleEntityEvent(b0);
        }

    }

    @Override
    public boolean fireImmune() {
        return this.isHeartBound() || super.fireImmune();
    }

    @Override
    public boolean canBeNameTagged() {
        return !this.isHeartBound() && super.canBeNameTagged();
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return !this.isHeartBound() && super.canAddPassenger(entity);
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return !this.isHeartBound() && super.couldAcceptPassenger();
    }

    @Override
    protected void addPassenger(Entity entity) {
        if (this.isHeartBound()) {
            throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
        }
    }

    @Override
    public boolean canUsePortal(boolean flag) {
        return !this.isHeartBound() && super.canUsePortal(flag);
    }

    @Override
    protected NavigationAbstract createNavigation(World world) {
        return new Creaking.e(this, world);
    }

    public boolean playerIsStuckInYou() {
        List<EntityHuman> list = (List) this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());

        if (list.isEmpty()) {
            this.playerStuckCounter = 0;
            return false;
        } else {
            AxisAlignedBB axisalignedbb = this.getBoundingBox();
            Iterator iterator = list.iterator();

            EntityHuman entityhuman;

            do {
                if (!iterator.hasNext()) {
                    this.playerStuckCounter = 0;
                    return false;
                }

                entityhuman = (EntityHuman) iterator.next();
            } while (!axisalignedbb.contains(entityhuman.getEyePosition()));

            ++this.playerStuckCounter;
            return this.playerStuckCounter > 4;
        }
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        if (nbttagcompound.contains("home_pos")) {
            this.setTransient((BlockPosition) GameProfileSerializer.readBlockPos(nbttagcompound, "home_pos").orElseThrow());
        }

    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        BlockPosition blockposition = this.getHomePos();

        if (blockposition != null) {
            nbttagcompound.put("home_pos", GameProfileSerializer.writeBlockPos(blockposition));
        }

    }

    public void setHomePos(BlockPosition blockposition) {
        this.entityData.set(Creaking.HOME_POS, Optional.of(blockposition));
    }

    @Nullable
    public BlockPosition getHomePos() {
        return (BlockPosition) ((Optional) this.entityData.get(Creaking.HOME_POS)).orElse((Object) null);
    }

    public void setTearingDown() {
        this.entityData.set(Creaking.IS_TEARING_DOWN, true);
    }

    public boolean isTearingDown() {
        return (Boolean) this.entityData.get(Creaking.IS_TEARING_DOWN);
    }

    public boolean hasGlowingEyes() {
        return this.eyesGlowing;
    }

    public void checkEyeBlink() {
        if (this.deathTime > this.nextFlickerTime) {
            this.nextFlickerTime = this.deathTime + this.getRandom().nextIntBetweenInclusive(this.eyesGlowing ? 2 : this.deathTime / 4, this.eyesGlowing ? 8 : this.deathTime / 2);
            this.eyesGlowing = !this.eyesGlowing;
        }

    }

    @Override
    public void playAttackSound() {
        this.makeSound(SoundEffects.CREAKING_ATTACK);
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return this.isActive() ? null : SoundEffects.CREAKING_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return this.isHeartBound() ? SoundEffects.CREAKING_SWAY : super.getHurtSound(damagesource);
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.CREAKING_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.CREAKING_STEP, 0.15F, 1.0F);
    }

    @Nullable
    @Override
    public EntityLiving getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
    }

    @Override
    public void knockback(double d0, double d1, double d2) {
        if (this.canMove()) {
            super.knockback(d0, d1, d2);
        }
    }

    public boolean checkCanMove() {
        List<EntityHuman> list = (List) this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        boolean flag = this.isActive();

        if (list.isEmpty()) {
            if (flag) {
                this.deactivate();
            }

            return true;
        } else {
            boolean flag1 = false;
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (this.canAttack(entityhuman) && !this.isAlliedTo((Entity) entityhuman)) {
                    flag1 = true;
                    if ((!flag || EntityLiving.PLAYER_NOT_WEARING_DISGUISE_ITEM.test(entityhuman)) && this.isLookingAtMe(entityhuman, 0.5D, false, true, new double[]{this.getEyeY(), this.getY() + 0.5D * (double) this.getScale(), (this.getEyeY() + this.getY()) / 2.0D})) {
                        if (flag) {
                            return false;
                        }

                        if (entityhuman.distanceToSqr((Entity) this) < 144.0D) {
                            this.activate(entityhuman);
                            return false;
                        }
                    }
                }
            }

            if (!flag1 && flag) {
                this.deactivate();
            }

            return true;
        }
    }

    public void activate(EntityHuman entityhuman) {
        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, (Object) entityhuman);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEffects.CREAKING_ACTIVATE);
        this.setIsActive(true);
    }

    public void deactivate() {
        this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEffects.CREAKING_DEACTIVATE);
        this.setIsActive(false);
    }

    public void setIsActive(boolean flag) {
        this.entityData.set(Creaking.IS_ACTIVE, flag);
    }

    public boolean isActive() {
        return (Boolean) this.entityData.get(Creaking.IS_ACTIVE);
    }

    @Override
    public float getWalkTargetValue(BlockPosition blockposition, IWorldReader iworldreader) {
        return 0.0F;
    }

    private class c extends ControllerLook {

        public c(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }

        }
    }

    private class d extends ControllerMove {

        public d(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }

        }
    }

    private class b extends ControllerJump {

        public b(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            } else {
                Creaking.this.setJumping(false);
            }

        }
    }

    private class a extends EntityAIBodyControl {

        public a(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void clientTick() {
            if (Creaking.this.canMove()) {
                super.clientTick();
            }

        }
    }

    private class e extends Navigation {

        e(final Creaking creaking, final World world) {
            super(creaking, world);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }

        }

        @Override
        protected Pathfinder createPathFinder(int i) {
            this.nodeEvaluator = Creaking.this.new f();
            this.nodeEvaluator.setCanPassDoors(true);
            return new Pathfinder(this.nodeEvaluator, i);
        }
    }

    private class f extends PathfinderNormal {

        private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

        f() {}

        @Override
        public PathType getPathType(PathfindingContext pathfindingcontext, int i, int j, int k) {
            BlockPosition blockposition = Creaking.this.getHomePos();

            if (blockposition == null) {
                return super.getPathType(pathfindingcontext, i, j, k);
            } else {
                double d0 = blockposition.distSqr(new BaseBlockPosition(i, j, k));

                return d0 > 1024.0D && d0 >= blockposition.distSqr(pathfindingcontext.mobPosition()) ? PathType.BLOCKED : super.getPathType(pathfindingcontext, i, j, k);
            }
        }
    }
}
