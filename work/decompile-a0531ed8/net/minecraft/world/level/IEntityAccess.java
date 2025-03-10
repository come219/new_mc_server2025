package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public interface IEntityAccess {

    List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb, Predicate<? super Entity> predicate);

    <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate);

    default <T extends Entity> List<T> getEntitiesOfClass(Class<T> oclass, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate) {
        return this.getEntities(EntityTypeTest.forClass(oclass), axisalignedbb, predicate);
    }

    List<? extends EntityHuman> players();

    default List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        return this.getEntities(entity, axisalignedbb, IEntitySelector.NO_SPECTATORS);
    }

    default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelshape) {
        if (voxelshape.isEmpty()) {
            return true;
        } else {
            Iterator iterator = this.getEntities(entity, voxelshape.bounds()).iterator();

            Entity entity1;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                entity1 = (Entity) iterator.next();
            } while (entity1.isRemoved() || !entity1.blocksBuilding || entity != null && entity1.isPassengerOfSameVehicle(entity) || !VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(entity1.getBoundingBox()), OperatorBoolean.AND));

            return false;
        }
    }

    default <T extends Entity> List<T> getEntitiesOfClass(Class<T> oclass, AxisAlignedBB axisalignedbb) {
        return this.getEntitiesOfClass(oclass, axisalignedbb, IEntitySelector.NO_SPECTATORS);
    }

    default List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        if (axisalignedbb.getSize() < 1.0E-7D) {
            return List.of();
        } else {
            Predicate predicate;

            if (entity == null) {
                predicate = IEntitySelector.CAN_BE_COLLIDED_WITH;
            } else {
                predicate = IEntitySelector.NO_SPECTATORS;
                Objects.requireNonNull(entity);
                predicate = predicate.and(entity::canCollideWith);
            }

            Predicate<Entity> predicate1 = predicate;
            List<Entity> list = this.getEntities(entity, axisalignedbb.inflate(1.0E-7D), predicate1);

            if (list.isEmpty()) {
                return List.of();
            } else {
                Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(list.size());
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    Entity entity1 = (Entity) iterator.next();

                    builder.add(VoxelShapes.create(entity1.getBoundingBox()));
                }

                return builder.build();
            }
        }
    }

    @Nullable
    default EntityHuman getNearestPlayer(double d0, double d1, double d2, double d3, @Nullable Predicate<Entity> predicate) {
        double d4 = -1.0D;
        EntityHuman entityhuman = null;
        Iterator iterator = this.players().iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman1 = (EntityHuman) iterator.next();

            if (predicate == null || predicate.test(entityhuman1)) {
                double d5 = entityhuman1.distanceToSqr(d0, d1, d2);

                if ((d3 < 0.0D || d5 < d3 * d3) && (d4 == -1.0D || d5 < d4)) {
                    d4 = d5;
                    entityhuman = entityhuman1;
                }
            }
        }

        return entityhuman;
    }

    @Nullable
    default EntityHuman getNearestPlayer(Entity entity, double d0) {
        return this.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), d0, false);
    }

    @Nullable
    default EntityHuman getNearestPlayer(double d0, double d1, double d2, double d3, boolean flag) {
        Predicate<Entity> predicate = flag ? IEntitySelector.NO_CREATIVE_OR_SPECTATOR : IEntitySelector.NO_SPECTATORS;

        return this.getNearestPlayer(d0, d1, d2, d3, predicate);
    }

    default boolean hasNearbyAlivePlayer(double d0, double d1, double d2, double d3) {
        Iterator iterator = this.players().iterator();

        double d4;

        do {
            EntityHuman entityhuman;

            do {
                do {
                    if (!iterator.hasNext()) {
                        return false;
                    }

                    entityhuman = (EntityHuman) iterator.next();
                } while (!IEntitySelector.NO_SPECTATORS.test(entityhuman));
            } while (!IEntitySelector.LIVING_ENTITY_STILL_ALIVE.test(entityhuman));

            d4 = entityhuman.distanceToSqr(d0, d1, d2);
        } while (d3 >= 0.0D && d4 >= d3 * d3);

        return true;
    }

    @Nullable
    default EntityHuman getPlayerByUUID(UUID uuid) {
        for (int i = 0; i < this.players().size(); ++i) {
            EntityHuman entityhuman = (EntityHuman) this.players().get(i);

            if (uuid.equals(entityhuman.getUUID())) {
                return entityhuman;
            }
        }

        return null;
    }
}
