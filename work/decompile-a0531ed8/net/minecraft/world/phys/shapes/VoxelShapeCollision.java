package net.minecraft.world.phys.shapes;

import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public interface VoxelShapeCollision {

    static VoxelShapeCollision empty() {
        return VoxelShapeCollisionEntity.EMPTY;
    }

    static VoxelShapeCollision of(Entity entity) {
        Objects.requireNonNull(entity);
        byte b0 = 0;
        Object object;

        //$FF: b0->value
        //0->net/minecraft/world/entity/vehicle/EntityMinecartAbstract
        switch (entity.typeSwitch<invokedynamic>(entity, b0)) {
            case 0:
                EntityMinecartAbstract entityminecartabstract = (EntityMinecartAbstract)entity;

                object = EntityMinecartAbstract.useExperimentalMovement(entityminecartabstract.level()) ? new MinecartCollisionContext(entityminecartabstract, false) : new VoxelShapeCollisionEntity(entity, false);
                break;
            default:
                object = new VoxelShapeCollisionEntity(entity, false);
        }

        return (VoxelShapeCollision)object;
    }

    static VoxelShapeCollision of(Entity entity, boolean flag) {
        return new VoxelShapeCollisionEntity(entity, flag);
    }

    boolean isDescending();

    boolean isAbove(VoxelShape voxelshape, BlockPosition blockposition, boolean flag);

    boolean isHoldingItem(Item item);

    boolean canStandOnFluid(Fluid fluid, Fluid fluid1);

    VoxelShape getCollisionShape(IBlockData iblockdata, ICollisionAccess icollisionaccess, BlockPosition blockposition);
}
