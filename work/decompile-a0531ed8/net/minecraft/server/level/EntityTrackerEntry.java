package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.decoration.EntityItemFrame;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityFireball;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemWorldMap;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class EntityTrackerEntry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TOLERANCE_LEVEL_ROTATION = 1;
    private static final double TOLERANCE_LEVEL_POSITION = 7.62939453125E-6D;
    public static final int FORCED_POS_UPDATE_PERIOD = 60;
    private static final int FORCED_TELEPORT_PERIOD = 400;
    private final WorldServer level;
    private final Entity entity;
    private final int updateInterval;
    private final boolean trackDelta;
    private final Consumer<Packet<?>> broadcast;
    private final VecDeltaCodec positionCodec = new VecDeltaCodec();
    private byte lastSentYRot;
    private byte lastSentXRot;
    private byte lastSentYHeadRot;
    private Vec3D lastSentMovement;
    private int tickCount;
    private int teleportDelay;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean wasRiding;
    private boolean wasOnGround;
    @Nullable
    private List<DataWatcher.c<?>> trackedDataValues;

    public EntityTrackerEntry(WorldServer worldserver, Entity entity, int i, boolean flag, Consumer<Packet<?>> consumer) {
        this.level = worldserver;
        this.broadcast = consumer;
        this.entity = entity;
        this.updateInterval = i;
        this.trackDelta = flag;
        this.positionCodec.setBase(entity.trackingPosition());
        this.lastSentMovement = entity.getDeltaMovement();
        this.lastSentYRot = MathHelper.packDegrees(entity.getYRot());
        this.lastSentXRot = MathHelper.packDegrees(entity.getXRot());
        this.lastSentYHeadRot = MathHelper.packDegrees(entity.getYHeadRot());
        this.wasOnGround = entity.onGround();
        this.trackedDataValues = entity.getEntityData().getNonDefaultValues();
    }

    public void sendChanges() {
        List<Entity> list = this.entity.getPassengers();

        if (!list.equals(this.lastPassengers)) {
            this.broadcast.accept(new PacketPlayOutMount(this.entity));
            removedPassengers(list, this.lastPassengers).forEach((entity) -> {
                if (entity instanceof EntityPlayer entityplayer) {
                    entityplayer.connection.teleport(entityplayer.getX(), entityplayer.getY(), entityplayer.getZ(), entityplayer.getYRot(), entityplayer.getXRot());
                }

            });
            this.lastPassengers = list;
        }

        Entity entity = this.entity;

        if (entity instanceof EntityItemFrame entityitemframe) {
            if (this.tickCount % 10 == 0) {
                ItemStack itemstack = entityitemframe.getItem();

                if (itemstack.getItem() instanceof ItemWorldMap) {
                    MapId mapid = (MapId) itemstack.get(DataComponents.MAP_ID);
                    WorldMap worldmap = ItemWorldMap.getSavedData(mapid, this.level);

                    if (worldmap != null) {
                        Iterator iterator = this.level.players().iterator();

                        while (iterator.hasNext()) {
                            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                            worldmap.tickCarriedBy(entityplayer, itemstack);
                            Packet<?> packet = worldmap.getUpdatePacket(mapid, entityplayer);

                            if (packet != null) {
                                entityplayer.connection.send(packet);
                            }
                        }
                    }
                }

                this.sendDirtyEntityData();
            }
        }

        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            byte b0 = MathHelper.packDegrees(this.entity.getYRot());
            byte b1 = MathHelper.packDegrees(this.entity.getXRot());
            boolean flag = Math.abs(b0 - this.lastSentYRot) >= 1 || Math.abs(b1 - this.lastSentXRot) >= 1;

            if (this.entity.isPassenger()) {
                if (flag) {
                    this.broadcast.accept(new PacketPlayOutEntity.PacketPlayOutEntityLook(this.entity.getId(), b0, b1, this.entity.onGround()));
                    this.lastSentYRot = b0;
                    this.lastSentXRot = b1;
                }

                this.positionCodec.setBase(this.entity.trackingPosition());
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                label186:
                {
                    Entity entity1 = this.entity;

                    if (entity1 instanceof EntityMinecartAbstract) {
                        EntityMinecartAbstract entityminecartabstract = (EntityMinecartAbstract) entity1;
                        MinecartBehavior minecartbehavior = entityminecartabstract.getBehavior();

                        if (minecartbehavior instanceof NewMinecartBehavior) {
                            NewMinecartBehavior newminecartbehavior = (NewMinecartBehavior) minecartbehavior;

                            this.handleMinecartPosRot(newminecartbehavior, b0, b1, flag);
                            break label186;
                        }
                    }

                    ++this.teleportDelay;
                    Vec3D vec3d = this.entity.trackingPosition();
                    boolean flag1 = this.positionCodec.delta(vec3d).lengthSqr() >= 7.62939453125E-6D;
                    Packet<?> packet1 = null;
                    boolean flag2 = flag1 || this.tickCount % 60 == 0;
                    boolean flag3 = false;
                    boolean flag4 = false;
                    long i = this.positionCodec.encodeX(vec3d);
                    long j = this.positionCodec.encodeY(vec3d);
                    long k = this.positionCodec.encodeZ(vec3d);
                    boolean flag5 = i < -32768L || i > 32767L || j < -32768L || j > 32767L || k < -32768L || k > 32767L;

                    if (!flag5 && this.teleportDelay <= 400 && !this.wasRiding && this.wasOnGround == this.entity.onGround()) {
                        if ((!flag2 || !flag) && !(this.entity instanceof EntityArrow)) {
                            if (flag2) {
                                packet1 = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(this.entity.getId(), (short) ((int) i), (short) ((int) j), (short) ((int) k), this.entity.onGround());
                                flag3 = true;
                            } else if (flag) {
                                packet1 = new PacketPlayOutEntity.PacketPlayOutEntityLook(this.entity.getId(), b0, b1, this.entity.onGround());
                                flag4 = true;
                            }
                        } else {
                            packet1 = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(this.entity.getId(), (short) ((int) i), (short) ((int) j), (short) ((int) k), b0, b1, this.entity.onGround());
                            flag3 = true;
                            flag4 = true;
                        }
                    } else {
                        this.wasOnGround = this.entity.onGround();
                        this.teleportDelay = 0;
                        packet1 = ClientboundEntityPositionSyncPacket.of(this.entity);
                        flag3 = true;
                        flag4 = true;
                    }

                    if (this.entity.hasImpulse || this.trackDelta || this.entity instanceof EntityLiving && ((EntityLiving) this.entity).isFallFlying()) {
                        Vec3D vec3d1 = this.entity.getDeltaMovement();
                        double d0 = vec3d1.distanceToSqr(this.lastSentMovement);

                        if (d0 > 1.0E-7D || d0 > 0.0D && vec3d1.lengthSqr() == 0.0D) {
                            this.lastSentMovement = vec3d1;
                            Entity entity2 = this.entity;

                            if (entity2 instanceof EntityFireball) {
                                EntityFireball entityfireball = (EntityFireball) entity2;

                                this.broadcast.accept(new ClientboundBundlePacket(List.of(new PacketPlayOutEntityVelocity(this.entity.getId(), this.lastSentMovement), new ClientboundProjectilePowerPacket(entityfireball.getId(), entityfireball.accelerationPower))));
                            } else {
                                this.broadcast.accept(new PacketPlayOutEntityVelocity(this.entity.getId(), this.lastSentMovement));
                            }
                        }
                    }

                    if (packet1 != null) {
                        this.broadcast.accept(packet1);
                    }

                    this.sendDirtyEntityData();
                    if (flag3) {
                        this.positionCodec.setBase(vec3d);
                    }

                    if (flag4) {
                        this.lastSentYRot = b0;
                        this.lastSentXRot = b1;
                    }

                    this.wasRiding = false;
                }
            }

            byte b2 = MathHelper.packDegrees(this.entity.getYHeadRot());

            if (Math.abs(b2 - this.lastSentYHeadRot) >= 1) {
                this.broadcast.accept(new PacketPlayOutEntityHeadRotation(this.entity, b2));
                this.lastSentYHeadRot = b2;
            }

            this.entity.hasImpulse = false;
        }

        ++this.tickCount;
        if (this.entity.hurtMarked) {
            this.entity.hurtMarked = false;
            this.broadcastAndSend(new PacketPlayOutEntityVelocity(this.entity));
        }

    }

    private void handleMinecartPosRot(NewMinecartBehavior newminecartbehavior, byte b0, byte b1, boolean flag) {
        this.sendDirtyEntityData();
        if (newminecartbehavior.lerpSteps.isEmpty()) {
            Vec3D vec3d = this.entity.getDeltaMovement();
            double d0 = vec3d.distanceToSqr(this.lastSentMovement);
            Vec3D vec3d1 = this.entity.trackingPosition();
            boolean flag1 = this.positionCodec.delta(vec3d1).lengthSqr() >= 7.62939453125E-6D;
            boolean flag2 = flag1 || this.tickCount % 60 == 0;

            if (flag2 || flag || d0 > 1.0E-7D) {
                this.broadcast.accept(new ClientboundMoveMinecartPacket(this.entity.getId(), List.of(new NewMinecartBehavior.a(this.entity.position(), this.entity.getDeltaMovement(), this.entity.getYRot(), this.entity.getXRot(), 1.0F))));
            }
        } else {
            this.broadcast.accept(new ClientboundMoveMinecartPacket(this.entity.getId(), List.copyOf(newminecartbehavior.lerpSteps)));
            newminecartbehavior.lerpSteps.clear();
        }

        this.lastSentYRot = b0;
        this.lastSentXRot = b1;
        this.positionCodec.setBase(this.entity.position());
    }

    private static Stream<Entity> removedPassengers(List<Entity> list, List<Entity> list1) {
        return list1.stream().filter((entity) -> {
            return !list.contains(entity);
        });
    }

    public void removePairing(EntityPlayer entityplayer) {
        this.entity.stopSeenByPlayer(entityplayer);
        entityplayer.connection.send(new PacketPlayOutEntityDestroy(new int[]{this.entity.getId()}));
    }

    public void addPairing(EntityPlayer entityplayer) {
        List<Packet<? super PacketListenerPlayOut>> list = new ArrayList();

        Objects.requireNonNull(list);
        this.sendPairingData(entityplayer, list::add);
        entityplayer.connection.send(new ClientboundBundlePacket(list));
        this.entity.startSeenByPlayer(entityplayer);
    }

    public void sendPairingData(EntityPlayer entityplayer, Consumer<Packet<PacketListenerPlayOut>> consumer) {
        if (this.entity.isRemoved()) {
            EntityTrackerEntry.LOGGER.warn("Fetching packet for removed entity {}", this.entity);
        }

        Packet<PacketListenerPlayOut> packet = this.entity.getAddEntityPacket(this);

        consumer.accept(packet);
        if (this.trackedDataValues != null) {
            consumer.accept(new PacketPlayOutEntityMetadata(this.entity.getId(), this.trackedDataValues));
        }

        boolean flag = this.trackDelta;

        if (this.entity instanceof EntityLiving) {
            Collection<AttributeModifiable> collection = ((EntityLiving) this.entity).getAttributes().getSyncableAttributes();

            if (!collection.isEmpty()) {
                consumer.accept(new PacketPlayOutUpdateAttributes(this.entity.getId(), collection));
            }

            if (((EntityLiving) this.entity).isFallFlying()) {
                flag = true;
            }
        }

        if (flag && !(this.entity instanceof EntityLiving)) {
            consumer.accept(new PacketPlayOutEntityVelocity(this.entity.getId(), this.lastSentMovement));
        }

        Entity entity = this.entity;

        if (entity instanceof EntityLiving entityliving) {
            List<Pair<EnumItemSlot, ItemStack>> list = Lists.newArrayList();
            Iterator iterator = EnumItemSlot.VALUES.iterator();

            while (iterator.hasNext()) {
                EnumItemSlot enumitemslot = (EnumItemSlot) iterator.next();
                ItemStack itemstack = entityliving.getItemBySlot(enumitemslot);

                if (!itemstack.isEmpty()) {
                    list.add(Pair.of(enumitemslot, itemstack.copy()));
                }
            }

            if (!list.isEmpty()) {
                consumer.accept(new PacketPlayOutEntityEquipment(this.entity.getId(), list));
            }
        }

        if (!this.entity.getPassengers().isEmpty()) {
            consumer.accept(new PacketPlayOutMount(this.entity));
        }

        if (this.entity.isPassenger()) {
            consumer.accept(new PacketPlayOutMount(this.entity.getVehicle()));
        }

        entity = this.entity;
        if (entity instanceof Leashable leashable) {
            if (leashable.isLeashed()) {
                consumer.accept(new PacketPlayOutAttachEntity(this.entity, leashable.getLeashHolder()));
            }
        }

    }

    public Vec3D getPositionBase() {
        return this.positionCodec.getBase();
    }

    public Vec3D getLastSentMovement() {
        return this.lastSentMovement;
    }

    public float getLastSentXRot() {
        return MathHelper.unpackDegrees(this.lastSentXRot);
    }

    public float getLastSentYRot() {
        return MathHelper.unpackDegrees(this.lastSentYRot);
    }

    public float getLastSentYHeadRot() {
        return MathHelper.unpackDegrees(this.lastSentYHeadRot);
    }

    private void sendDirtyEntityData() {
        DataWatcher datawatcher = this.entity.getEntityData();
        List<DataWatcher.c<?>> list = datawatcher.packDirty();

        if (list != null) {
            this.trackedDataValues = datawatcher.getNonDefaultValues();
            this.broadcastAndSend(new PacketPlayOutEntityMetadata(this.entity.getId(), list));
        }

        if (this.entity instanceof EntityLiving) {
            Set<AttributeModifiable> set = ((EntityLiving) this.entity).getAttributes().getAttributesToSync();

            if (!set.isEmpty()) {
                this.broadcastAndSend(new PacketPlayOutUpdateAttributes(this.entity.getId(), set));
            }

            set.clear();
        }

    }

    private void broadcastAndSend(Packet<?> packet) {
        this.broadcast.accept(packet);
        if (this.entity instanceof EntityPlayer) {
            ((EntityPlayer) this.entity).connection.send(packet);
        }

    }
}
