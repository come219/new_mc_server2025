package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class CreakingHeartBlockEntity extends TileEntity {

    private static final int PLAYER_DETECTION_RANGE = 32;
    public static final int CREAKING_ROAMING_RADIUS = 32;
    private static final int DISTANCE_CREAKING_TOO_FAR = 34;
    private static final int SPAWN_RANGE_XZ = 16;
    private static final int SPAWN_RANGE_Y = 8;
    private static final int ATTEMPTS_PER_SPAWN = 5;
    private static final int UPDATE_TICKS = 20;
    private static final int UPDATE_TICKS_VARIANCE = 5;
    private static final int HURT_CALL_TOTAL_TICKS = 100;
    private static final int NUMBER_OF_HURT_CALLS = 10;
    private static final int HURT_CALL_INTERVAL = 10;
    private static final int HURT_CALL_PARTICLE_TICKS = 50;
    private static final int MAX_DEPTH = 2;
    private static final int MAX_COUNT = 64;
    private static final int TICKS_GRACE_PERIOD = 30;
    private static final Optional<Creaking> NO_CREAKING = Optional.empty();
    @Nullable
    private Either<Creaking, UUID> creakingInfo;
    private long ticksExisted;
    private int ticker;
    private int emitter;
    @Nullable
    private Vec3D emitterTarget;
    private int outputSignal;

    public CreakingHeartBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.CREAKING_HEART, blockposition, iblockdata);
    }

    public static void serverTick(World world, BlockPosition blockposition, IBlockData iblockdata, CreakingHeartBlockEntity creakingheartblockentity) {
        ++creakingheartblockentity.ticksExisted;
        if (world instanceof WorldServer worldserver) {
            int i = creakingheartblockentity.computeAnalogOutputSignal();

            if (creakingheartblockentity.outputSignal != i) {
                creakingheartblockentity.outputSignal = i;
                world.updateNeighbourForOutputSignal(blockposition, Blocks.CREAKING_HEART);
            }

            if (creakingheartblockentity.emitter > 0) {
                if (creakingheartblockentity.emitter > 50) {
                    creakingheartblockentity.emitParticles(worldserver, 1, true);
                    creakingheartblockentity.emitParticles(worldserver, 1, false);
                }

                if (creakingheartblockentity.emitter % 10 == 0 && creakingheartblockentity.emitterTarget != null) {
                    creakingheartblockentity.getCreakingProtector().ifPresent((creaking) -> {
                        creakingheartblockentity.emitterTarget = creaking.getBoundingBox().getCenter();
                    });
                    Vec3D vec3d = Vec3D.atCenterOf(blockposition);
                    float f = 0.2F + 0.8F * (float) (100 - creakingheartblockentity.emitter) / 100.0F;
                    Vec3D vec3d1 = vec3d.subtract(creakingheartblockentity.emitterTarget).scale((double) f).add(creakingheartblockentity.emitterTarget);
                    BlockPosition blockposition1 = BlockPosition.containing(vec3d1);
                    float f1 = (float) creakingheartblockentity.emitter / 2.0F / 100.0F + 0.5F;

                    worldserver.playSound((EntityHuman) null, blockposition1, SoundEffects.CREAKING_HEART_HURT, SoundCategory.BLOCKS, f1, 1.0F);
                }

                --creakingheartblockentity.emitter;
            }

            if (creakingheartblockentity.ticker-- < 0) {
                creakingheartblockentity.ticker = creakingheartblockentity.level == null ? 20 : creakingheartblockentity.level.random.nextInt(5) + 20;
                Creaking creaking;

                if (creakingheartblockentity.creakingInfo == null) {
                    if (!CreakingHeartBlock.hasRequiredLogs(iblockdata, world, blockposition)) {
                        world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CreakingHeartBlock.ACTIVE, false), 3);
                    } else if ((Boolean) iblockdata.getValue(CreakingHeartBlock.ACTIVE)) {
                        if (CreakingHeartBlock.isNaturalNight(world)) {
                            if (world.getDifficulty() != EnumDifficulty.PEACEFUL) {
                                if (worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                                    EntityHuman entityhuman = world.getNearestPlayer((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 32.0D, false);

                                    if (entityhuman != null) {
                                        creaking = spawnProtector(worldserver, creakingheartblockentity);
                                        if (creaking != null) {
                                            creakingheartblockentity.setCreakingInfo(creaking);
                                            creaking.makeSound(SoundEffects.CREAKING_SPAWN);
                                            world.playSound((EntityHuman) null, creakingheartblockentity.getBlockPos(), SoundEffects.CREAKING_HEART_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                        }
                                    }

                                }
                            }
                        }
                    }
                } else {
                    Optional<Creaking> optional = creakingheartblockentity.getCreakingProtector();

                    if (optional.isPresent()) {
                        creaking = (Creaking) optional.get();
                        if (!CreakingHeartBlock.isNaturalNight(world) || creakingheartblockentity.distanceToCreaking() > 34.0D || creaking.playerIsStuckInYou()) {
                            creakingheartblockentity.removeProtector((DamageSource) null);
                            return;
                        }

                        if (!CreakingHeartBlock.hasRequiredLogs(iblockdata, world, blockposition) && creakingheartblockentity.creakingInfo == null) {
                            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CreakingHeartBlock.ACTIVE, false), 3);
                        }
                    }

                }
            }
        }
    }

    private double distanceToCreaking() {
        return (Double) this.getCreakingProtector().map((creaking) -> {
            return Math.sqrt(creaking.distanceToSqr(Vec3D.atBottomCenterOf(this.getBlockPos())));
        }).orElse(0.0D);
    }

    private void clearCreakingInfo() {
        this.creakingInfo = null;
        this.setChanged();
    }

    public void setCreakingInfo(Creaking creaking) {
        this.creakingInfo = Either.left(creaking);
        this.setChanged();
    }

    public void setCreakingInfo(UUID uuid) {
        this.creakingInfo = Either.right(uuid);
        this.ticksExisted = 0L;
        this.setChanged();
    }

    private Optional<Creaking> getCreakingProtector() {
        if (this.creakingInfo == null) {
            return CreakingHeartBlockEntity.NO_CREAKING;
        } else {
            if (this.creakingInfo.left().isPresent()) {
                Creaking creaking = (Creaking) this.creakingInfo.left().get();

                if (!creaking.isRemoved()) {
                    return Optional.of(creaking);
                }

                this.setCreakingInfo(creaking.getUUID());
            }

            World world = this.level;

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                if (this.creakingInfo.right().isPresent()) {
                    UUID uuid = (UUID) this.creakingInfo.right().get();
                    Entity entity = worldserver.getEntity(uuid);

                    if (entity instanceof Creaking) {
                        Creaking creaking1 = (Creaking) entity;

                        this.setCreakingInfo(creaking1);
                        return Optional.of(creaking1);
                    }

                    if (this.ticksExisted >= 30L) {
                        this.clearCreakingInfo();
                    }

                    return CreakingHeartBlockEntity.NO_CREAKING;
                }
            }

            return CreakingHeartBlockEntity.NO_CREAKING;
        }
    }

    @Nullable
    private static Creaking spawnProtector(WorldServer worldserver, CreakingHeartBlockEntity creakingheartblockentity) {
        BlockPosition blockposition = creakingheartblockentity.getBlockPos();
        Optional<Creaking> optional = SpawnUtil.trySpawnMob(EntityTypes.CREAKING, EntitySpawnReason.SPAWNER, worldserver, blockposition, 5, 16, 8, SpawnUtil.a.ON_TOP_OF_COLLIDER_NO_LEAVES, true);

        if (optional.isEmpty()) {
            return null;
        } else {
            Creaking creaking = (Creaking) optional.get();

            worldserver.gameEvent((Entity) creaking, (Holder) GameEvent.ENTITY_PLACE, creaking.position());
            worldserver.broadcastEntityEvent(creaking, (byte) 60);
            creaking.setTransient(blockposition);
            return creaking;
        }
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public void creakingHurt() {
        Object object = this.getCreakingProtector().orElse((Object) null);

        if (object instanceof Creaking creaking) {
            World world = this.level;

            if (world instanceof WorldServer worldserver) {
                if (this.emitter <= 0) {
                    this.emitParticles(worldserver, 20, false);
                    int i = this.level.getRandom().nextIntBetweenInclusive(2, 3);

                    for (int j = 0; j < i; ++j) {
                        this.spreadResin().ifPresent((blockposition) -> {
                            this.level.playSound((EntityHuman) null, blockposition, SoundEffects.RESIN_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            this.level.gameEvent((Holder) GameEvent.BLOCK_PLACE, blockposition, GameEvent.a.of(this.level.getBlockState(blockposition)));
                        });
                    }

                    this.emitter = 100;
                    this.emitterTarget = creaking.getBoundingBox().getCenter();
                }
            }
        }
    }

    private Optional<BlockPosition> spreadResin() {
        Mutable<BlockPosition> mutable = new MutableObject((Object) null);

        BlockPosition.breadthFirstTraversal(this.worldPosition, 2, 64, (blockposition, consumer) -> {
            Iterator iterator = SystemUtils.shuffledCopy((Object[]) EnumDirection.values(), this.level.random).iterator();

            while (iterator.hasNext()) {
                EnumDirection enumdirection = (EnumDirection) iterator.next();
                BlockPosition blockposition1 = blockposition.relative(enumdirection);

                if (this.level.getBlockState(blockposition1).is(TagsBlock.PALE_OAK_LOGS)) {
                    consumer.accept(blockposition1);
                }
            }

        }, (blockposition) -> {
            if (!this.level.getBlockState(blockposition).is(TagsBlock.PALE_OAK_LOGS)) {
                return BlockPosition.b.ACCEPT;
            } else {
                Iterator iterator = SystemUtils.shuffledCopy((Object[]) EnumDirection.values(), this.level.random).iterator();

                BlockPosition blockposition1;
                IBlockData iblockdata;
                EnumDirection enumdirection;

                do {
                    if (!iterator.hasNext()) {
                        return BlockPosition.b.ACCEPT;
                    }

                    EnumDirection enumdirection1 = (EnumDirection) iterator.next();

                    blockposition1 = blockposition.relative(enumdirection1);
                    iblockdata = this.level.getBlockState(blockposition1);
                    enumdirection = enumdirection1.getOpposite();
                    if (iblockdata.isAir()) {
                        iblockdata = Blocks.RESIN_CLUMP.defaultBlockState();
                    } else if (iblockdata.is(Blocks.WATER) && iblockdata.getFluidState().isSource()) {
                        iblockdata = (IBlockData) Blocks.RESIN_CLUMP.defaultBlockState().setValue(MultifaceBlock.WATERLOGGED, true);
                    }
                } while (!iblockdata.is(Blocks.RESIN_CLUMP) || MultifaceBlock.hasFace(iblockdata, enumdirection));

                this.level.setBlock(blockposition1, (IBlockData) iblockdata.setValue(MultifaceBlock.getFaceProperty(enumdirection), true), 3);
                mutable.setValue(blockposition1);
                return BlockPosition.b.STOP;
            }
        });
        return Optional.ofNullable((BlockPosition) mutable.getValue());
    }

    private void emitParticles(WorldServer worldserver, int i, boolean flag) {
        Object object = this.getCreakingProtector().orElse((Object) null);

        if (object instanceof Creaking creaking) {
            int j = flag ? 16545810 : 6250335;
            RandomSource randomsource = worldserver.random;

            for (double d0 = 0.0D; d0 < (double) i; ++d0) {
                AxisAlignedBB axisalignedbb = creaking.getBoundingBox();
                Vec3D vec3d = axisalignedbb.getMinPosition().add(randomsource.nextDouble() * axisalignedbb.getXsize(), randomsource.nextDouble() * axisalignedbb.getYsize(), randomsource.nextDouble() * axisalignedbb.getZsize());
                Vec3D vec3d1 = Vec3D.atLowerCornerOf(this.getBlockPos()).add(randomsource.nextDouble(), randomsource.nextDouble(), randomsource.nextDouble());

                if (flag) {
                    Vec3D vec3d2 = vec3d;

                    vec3d = vec3d1;
                    vec3d1 = vec3d2;
                }

                TrailParticleOption trailparticleoption = new TrailParticleOption(vec3d1, j, randomsource.nextInt(40) + 10);

                worldserver.sendParticles(trailparticleoption, true, true, vec3d.x, vec3d.y, vec3d.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

        }
    }

    public void removeProtector(@Nullable DamageSource damagesource) {
        Object object = this.getCreakingProtector().orElse((Object) null);

        if (object instanceof Creaking creaking) {
            if (damagesource == null) {
                creaking.tearDown();
            } else {
                creaking.creakingDeathEffects(damagesource);
                creaking.setTearingDown();
                creaking.setHealth(0.0F);
            }

            this.clearCreakingInfo();
        }

    }

    public boolean isProtector(Creaking creaking) {
        return (Boolean) this.getCreakingProtector().map((creaking1) -> {
            return creaking1 == creaking;
        }).orElse(false);
    }

    public int getAnalogOutputSignal() {
        return this.outputSignal;
    }

    public int computeAnalogOutputSignal() {
        if (this.creakingInfo != null && !this.getCreakingProtector().isEmpty()) {
            double d0 = this.distanceToCreaking();
            double d1 = Math.clamp(d0, 0.0D, 32.0D) / 32.0D;

            return 15 - (int) Math.floor(d1 * 15.0D);
        } else {
            return 0;
        }
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        if (nbttagcompound.contains("creaking")) {
            this.setCreakingInfo(nbttagcompound.getUUID("creaking"));
        } else {
            this.clearCreakingInfo();
        }

    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        if (this.creakingInfo != null) {
            nbttagcompound.putUUID("creaking", (UUID) this.creakingInfo.map(Entity::getUUID, (uuid) -> {
                return uuid;
            }));
        }

    }
}
