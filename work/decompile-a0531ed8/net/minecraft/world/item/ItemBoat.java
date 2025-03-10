package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class ItemBoat extends Item {

    private final EntityTypes<? extends AbstractBoat> entityType;

    public ItemBoat(EntityTypes<? extends AbstractBoat> entitytypes, Item.Info item_info) {
        super(item_info);
        this.entityType = entitytypes;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        MovingObjectPositionBlock movingobjectpositionblock = getPlayerPOVHitResult(world, entityhuman, RayTrace.FluidCollisionOption.ANY);

        if (movingobjectpositionblock.getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
            return EnumInteractionResult.PASS;
        } else {
            Vec3D vec3d = entityhuman.getViewVector(1.0F);
            double d0 = 5.0D;
            List<Entity> list = world.getEntities((Entity) entityhuman, entityhuman.getBoundingBox().expandTowards(vec3d.scale(5.0D)).inflate(1.0D), IEntitySelector.CAN_BE_PICKED);

            if (!list.isEmpty()) {
                Vec3D vec3d1 = entityhuman.getEyePosition();
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();
                    AxisAlignedBB axisalignedbb = entity.getBoundingBox().inflate((double) entity.getPickRadius());

                    if (axisalignedbb.contains(vec3d1)) {
                        return EnumInteractionResult.PASS;
                    }
                }
            }

            if (movingobjectpositionblock.getType() == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                AbstractBoat abstractboat = this.getBoat(world, movingobjectpositionblock, itemstack, entityhuman);

                if (abstractboat == null) {
                    return EnumInteractionResult.FAIL;
                } else {
                    abstractboat.setYRot(entityhuman.getYRot());
                    if (!world.noCollision(abstractboat, abstractboat.getBoundingBox())) {
                        return EnumInteractionResult.FAIL;
                    } else {
                        if (!world.isClientSide) {
                            world.addFreshEntity(abstractboat);
                            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.ENTITY_PLACE, movingobjectpositionblock.getLocation());
                            itemstack.consume(1, entityhuman);
                        }

                        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                        return EnumInteractionResult.SUCCESS;
                    }
                }
            } else {
                return EnumInteractionResult.PASS;
            }
        }
    }

    @Nullable
    private AbstractBoat getBoat(World world, MovingObjectPosition movingobjectposition, ItemStack itemstack, EntityHuman entityhuman) {
        AbstractBoat abstractboat = (AbstractBoat) this.entityType.create(world, EntitySpawnReason.SPAWN_ITEM_USE);

        if (abstractboat != null) {
            Vec3D vec3d = movingobjectposition.getLocation();

            abstractboat.setInitialPos(vec3d.x, vec3d.y, vec3d.z);
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                EntityTypes.createDefaultStackConfig(worldserver, itemstack, entityhuman).accept(abstractboat);
            }
        }

        return abstractboat;
    }
}
