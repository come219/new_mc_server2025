package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.level.block.BlockMinecartTrackAbstract;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;
import net.minecraft.world.phys.Vec3D;

public class MinecartDispenseItemBehavior extends DispenseBehaviorItem {

    private final DispenseBehaviorItem defaultDispenseItemBehavior = new DispenseBehaviorItem();
    private final EntityTypes<? extends EntityMinecartAbstract> entityType;

    public MinecartDispenseItemBehavior(EntityTypes<? extends EntityMinecartAbstract> entitytypes) {
        this.entityType = entitytypes;
    }

    @Override
    public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
        WorldServer worldserver = sourceblock.level();
        Vec3D vec3d = sourceblock.center();
        double d0 = vec3d.x() + (double) enumdirection.getStepX() * 1.125D;
        double d1 = Math.floor(vec3d.y()) + (double) enumdirection.getStepY();
        double d2 = vec3d.z() + (double) enumdirection.getStepZ() * 1.125D;
        BlockPosition blockposition = sourceblock.pos().relative(enumdirection);
        IBlockData iblockdata = worldserver.getBlockState(blockposition);
        double d3;

        if (iblockdata.is(TagsBlock.RAILS)) {
            if (getRailShape(iblockdata).isSlope()) {
                d3 = 0.6D;
            } else {
                d3 = 0.1D;
            }
        } else {
            if (!iblockdata.isAir()) {
                return this.defaultDispenseItemBehavior.dispense(sourceblock, itemstack);
            }

            IBlockData iblockdata1 = worldserver.getBlockState(blockposition.below());

            if (!iblockdata1.is(TagsBlock.RAILS)) {
                return this.defaultDispenseItemBehavior.dispense(sourceblock, itemstack);
            }

            if (enumdirection != EnumDirection.DOWN && getRailShape(iblockdata1).isSlope()) {
                d3 = -0.4D;
            } else {
                d3 = -0.9D;
            }
        }

        Vec3D vec3d1 = new Vec3D(d0, d1 + d3, d2);
        EntityMinecartAbstract entityminecartabstract = EntityMinecartAbstract.createMinecart(worldserver, vec3d1.x, vec3d1.y, vec3d1.z, this.entityType, EntitySpawnReason.DISPENSER, itemstack, (EntityHuman) null);

        if (entityminecartabstract != null) {
            worldserver.addFreshEntity(entityminecartabstract);
            itemstack.shrink(1);
        }

        return itemstack;
    }

    private static BlockPropertyTrackPosition getRailShape(IBlockData iblockdata) {
        Block block = iblockdata.getBlock();
        BlockPropertyTrackPosition blockpropertytrackposition;

        if (block instanceof BlockMinecartTrackAbstract blockminecarttrackabstract) {
            blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(blockminecarttrackabstract.getShapeProperty());
        } else {
            blockpropertytrackposition = BlockPropertyTrackPosition.NORTH_SOUTH;
        }

        return blockpropertytrackposition;
    }

    @Override
    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(1000, sourceblock.pos(), 0);
    }
}
