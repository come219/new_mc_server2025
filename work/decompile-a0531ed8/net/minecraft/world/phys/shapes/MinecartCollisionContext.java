package net.minecraft.world.phys.shapes;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.block.BlockMinecartTrackAbstract;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;

public class MinecartCollisionContext extends VoxelShapeCollisionEntity {

    @Nullable
    private BlockPosition ingoreBelow;
    @Nullable
    private BlockPosition slopeIgnore;

    protected MinecartCollisionContext(EntityMinecartAbstract entityminecartabstract, boolean flag) {
        super(entityminecartabstract, flag);
        this.setupContext(entityminecartabstract);
    }

    private void setupContext(EntityMinecartAbstract entityminecartabstract) {
        BlockPosition blockposition = entityminecartabstract.getCurrentBlockPosOrRailBelow();
        IBlockData iblockdata = entityminecartabstract.level().getBlockState(blockposition);
        boolean flag = BlockMinecartTrackAbstract.isRail(iblockdata);

        if (flag) {
            this.ingoreBelow = blockposition.below();
            BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(((BlockMinecartTrackAbstract) iblockdata.getBlock()).getShapeProperty());

            if (blockpropertytrackposition.isSlope()) {
                BlockPosition blockposition1;

                switch (blockpropertytrackposition) {
                    case ASCENDING_EAST:
                        blockposition1 = blockposition.east();
                        break;
                    case ASCENDING_WEST:
                        blockposition1 = blockposition.west();
                        break;
                    case ASCENDING_NORTH:
                        blockposition1 = blockposition.north();
                        break;
                    case ASCENDING_SOUTH:
                        blockposition1 = blockposition.south();
                        break;
                    default:
                        blockposition1 = null;
                }

                this.slopeIgnore = blockposition1;
            }
        }

    }

    @Override
    public VoxelShape getCollisionShape(IBlockData iblockdata, ICollisionAccess icollisionaccess, BlockPosition blockposition) {
        return !blockposition.equals(this.ingoreBelow) && !blockposition.equals(this.slopeIgnore) ? super.getCollisionShape(iblockdata, icollisionaccess, blockposition) : VoxelShapes.empty();
    }
}
