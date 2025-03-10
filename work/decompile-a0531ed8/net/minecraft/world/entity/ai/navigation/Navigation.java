package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Pathfinder;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.phys.Vec3D;

public class Navigation extends NavigationAbstract {

    private boolean avoidSun;

    public Navigation(EntityInsentient entityinsentient, World world) {
        super(entityinsentient, world);
    }

    @Override
    protected Pathfinder createPathFinder(int i) {
        this.nodeEvaluator = new PathfinderNormal();
        return new Pathfinder(this.nodeEvaluator, i);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.mob.isInLiquid() || this.mob.isPassenger();
    }

    @Override
    protected Vec3D getTempMobPos() {
        return new Vec3D(this.mob.getX(), (double) this.getSurfaceY(), this.mob.getZ());
    }

    @Override
    public PathEntity createPath(BlockPosition blockposition, int i) {
        Chunk chunk = this.level.getChunkSource().getChunkNow(SectionPosition.blockToSectionCoord(((BlockPosition) blockposition).getX()), SectionPosition.blockToSectionCoord(((BlockPosition) blockposition).getZ()));

        if (chunk == null) {
            return null;
        } else {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition;

            if (chunk.getBlockState((BlockPosition) blockposition).isAir()) {
                blockposition_mutableblockposition = ((BlockPosition) blockposition).mutable().move(EnumDirection.DOWN);

                while (blockposition_mutableblockposition.getY() > this.level.getMinY() && chunk.getBlockState(blockposition_mutableblockposition).isAir()) {
                    blockposition_mutableblockposition.move(EnumDirection.DOWN);
                }

                if (blockposition_mutableblockposition.getY() > this.level.getMinY()) {
                    return super.createPath(blockposition_mutableblockposition.above(), i);
                }

                blockposition_mutableblockposition.setY(((BlockPosition) blockposition).getY() + 1);

                while (blockposition_mutableblockposition.getY() <= this.level.getMaxY() && chunk.getBlockState(blockposition_mutableblockposition).isAir()) {
                    blockposition_mutableblockposition.move(EnumDirection.UP);
                }

                blockposition = blockposition_mutableblockposition;
            }

            if (!chunk.getBlockState((BlockPosition) blockposition).isSolid()) {
                return super.createPath((BlockPosition) blockposition, i);
            } else {
                blockposition_mutableblockposition = ((BlockPosition) blockposition).mutable().move(EnumDirection.UP);

                while (blockposition_mutableblockposition.getY() <= this.level.getMaxY() && chunk.getBlockState(blockposition_mutableblockposition).isSolid()) {
                    blockposition_mutableblockposition.move(EnumDirection.UP);
                }

                return super.createPath(blockposition_mutableblockposition.immutable(), i);
            }
        }
    }

    @Override
    public PathEntity createPath(Entity entity, int i) {
        return this.createPath(entity.blockPosition(), i);
    }

    private int getSurfaceY() {
        if (this.mob.isInWater() && this.canFloat()) {
            int i = this.mob.getBlockY();
            IBlockData iblockdata = this.level.getBlockState(BlockPosition.containing(this.mob.getX(), (double) i, this.mob.getZ()));
            int j = 0;

            do {
                if (!iblockdata.is(Blocks.WATER)) {
                    return i;
                }

                ++i;
                iblockdata = this.level.getBlockState(BlockPosition.containing(this.mob.getX(), (double) i, this.mob.getZ()));
                ++j;
            } while (j <= 16);

            return this.mob.getBlockY();
        } else {
            return MathHelper.floor(this.mob.getY() + 0.5D);
        }
    }

    @Override
    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(BlockPosition.containing(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ()))) {
                return;
            }

            for (int i = 0; i < this.path.getNodeCount(); ++i) {
                PathPoint pathpoint = this.path.getNode(i);

                if (this.level.canSeeSky(new BlockPosition(pathpoint.x, pathpoint.y, pathpoint.z))) {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }

    }

    protected boolean hasValidPathType(PathType pathtype) {
        return pathtype == PathType.WATER ? false : (pathtype == PathType.LAVA ? false : pathtype != PathType.OPEN);
    }

    public void setCanOpenDoors(boolean flag) {
        this.nodeEvaluator.setCanOpenDoors(flag);
    }

    public void setAvoidSun(boolean flag) {
        this.avoidSun = flag;
    }

    public void setCanWalkOverFences(boolean flag) {
        this.nodeEvaluator.setCanWalkOverFences(flag);
    }
}
