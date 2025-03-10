package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.BlockPortal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;
import org.apache.commons.lang3.mutable.MutableInt;

public class BlockPortalShape {

    private static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;
    private static final BlockBase.f FRAME = (iblockdata, iblockaccess, blockposition) -> {
        return iblockdata.is(Blocks.OBSIDIAN);
    };
    private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
    private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0D;
    private final EnumDirection.EnumAxis axis;
    private final EnumDirection rightDir;
    private final int numPortalBlocks;
    private final BlockPosition bottomLeft;
    private final int height;
    private final int width;

    private BlockPortalShape(EnumDirection.EnumAxis enumdirection_enumaxis, int i, EnumDirection enumdirection, BlockPosition blockposition, int j, int k) {
        this.axis = enumdirection_enumaxis;
        this.numPortalBlocks = i;
        this.rightDir = enumdirection;
        this.bottomLeft = blockposition;
        this.width = j;
        this.height = k;
    }

    public static Optional<BlockPortalShape> findEmptyPortalShape(GeneratorAccess generatoraccess, BlockPosition blockposition, EnumDirection.EnumAxis enumdirection_enumaxis) {
        return findPortalShape(generatoraccess, blockposition, (blockportalshape) -> {
            return blockportalshape.isValid() && blockportalshape.numPortalBlocks == 0;
        }, enumdirection_enumaxis);
    }

    public static Optional<BlockPortalShape> findPortalShape(GeneratorAccess generatoraccess, BlockPosition blockposition, Predicate<BlockPortalShape> predicate, EnumDirection.EnumAxis enumdirection_enumaxis) {
        Optional<BlockPortalShape> optional = Optional.of(findAnyShape(generatoraccess, blockposition, enumdirection_enumaxis)).filter(predicate);

        if (optional.isPresent()) {
            return optional;
        } else {
            EnumDirection.EnumAxis enumdirection_enumaxis1 = enumdirection_enumaxis == EnumDirection.EnumAxis.X ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.X;

            return Optional.of(findAnyShape(generatoraccess, blockposition, enumdirection_enumaxis1)).filter(predicate);
        }
    }

    public static BlockPortalShape findAnyShape(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection.EnumAxis enumdirection_enumaxis) {
        EnumDirection enumdirection = enumdirection_enumaxis == EnumDirection.EnumAxis.X ? EnumDirection.WEST : EnumDirection.SOUTH;
        BlockPosition blockposition1 = calculateBottomLeft(iblockaccess, enumdirection, blockposition);

        if (blockposition1 == null) {
            return new BlockPortalShape(enumdirection_enumaxis, 0, enumdirection, blockposition, 0, 0);
        } else {
            int i = calculateWidth(iblockaccess, blockposition1, enumdirection);

            if (i == 0) {
                return new BlockPortalShape(enumdirection_enumaxis, 0, enumdirection, blockposition1, 0, 0);
            } else {
                MutableInt mutableint = new MutableInt();
                int j = calculateHeight(iblockaccess, blockposition1, enumdirection, i, mutableint);

                return new BlockPortalShape(enumdirection_enumaxis, mutableint.getValue(), enumdirection, blockposition1, i, j);
            }
        }
    }

    @Nullable
    private static BlockPosition calculateBottomLeft(IBlockAccess iblockaccess, EnumDirection enumdirection, BlockPosition blockposition) {
        for (int i = Math.max(iblockaccess.getMinY(), blockposition.getY() - 21); blockposition.getY() > i && isEmpty(iblockaccess.getBlockState(blockposition.below())); blockposition = blockposition.below()) {
            ;
        }

        EnumDirection enumdirection1 = enumdirection.getOpposite();
        int j = getDistanceUntilEdgeAboveFrame(iblockaccess, blockposition, enumdirection1) - 1;

        return j < 0 ? null : blockposition.relative(enumdirection1, j);
    }

    private static int calculateWidth(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        int i = getDistanceUntilEdgeAboveFrame(iblockaccess, blockposition, enumdirection);

        return i >= 2 && i <= 21 ? i : 0;
    }

    private static int getDistanceUntilEdgeAboveFrame(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (int i = 0; i <= 21; ++i) {
            blockposition_mutableblockposition.set(blockposition).move(enumdirection, i);
            IBlockData iblockdata = iblockaccess.getBlockState(blockposition_mutableblockposition);

            if (!isEmpty(iblockdata)) {
                if (BlockPortalShape.FRAME.test(iblockdata, iblockaccess, blockposition_mutableblockposition)) {
                    return i;
                }
                break;
            }

            IBlockData iblockdata1 = iblockaccess.getBlockState(blockposition_mutableblockposition.move(EnumDirection.DOWN));

            if (!BlockPortalShape.FRAME.test(iblockdata1, iblockaccess, blockposition_mutableblockposition)) {
                break;
            }
        }

        return 0;
    }

    private static int calculateHeight(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection, int i, MutableInt mutableint) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        int j = getDistanceUntilTop(iblockaccess, blockposition, enumdirection, blockposition_mutableblockposition, i, mutableint);

        return j >= 3 && j <= 21 && hasTopFrame(iblockaccess, blockposition, enumdirection, blockposition_mutableblockposition, i, j) ? j : 0;
    }

    private static boolean hasTopFrame(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, int i, int j) {
        for (int k = 0; k < i; ++k) {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = blockposition_mutableblockposition.set(blockposition).move(EnumDirection.UP, j).move(enumdirection, k);

            if (!BlockPortalShape.FRAME.test(iblockaccess.getBlockState(blockposition_mutableblockposition1), iblockaccess, blockposition_mutableblockposition1)) {
                return false;
            }
        }

        return true;
    }

    private static int getDistanceUntilTop(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, int i, MutableInt mutableint) {
        for (int j = 0; j < 21; ++j) {
            blockposition_mutableblockposition.set(blockposition).move(EnumDirection.UP, j).move(enumdirection, -1);
            if (!BlockPortalShape.FRAME.test(iblockaccess.getBlockState(blockposition_mutableblockposition), iblockaccess, blockposition_mutableblockposition)) {
                return j;
            }

            blockposition_mutableblockposition.set(blockposition).move(EnumDirection.UP, j).move(enumdirection, i);
            if (!BlockPortalShape.FRAME.test(iblockaccess.getBlockState(blockposition_mutableblockposition), iblockaccess, blockposition_mutableblockposition)) {
                return j;
            }

            for (int k = 0; k < i; ++k) {
                blockposition_mutableblockposition.set(blockposition).move(EnumDirection.UP, j).move(enumdirection, k);
                IBlockData iblockdata = iblockaccess.getBlockState(blockposition_mutableblockposition);

                if (!isEmpty(iblockdata)) {
                    return j;
                }

                if (iblockdata.is(Blocks.NETHER_PORTAL)) {
                    mutableint.increment();
                }
            }
        }

        return 21;
    }

    private static boolean isEmpty(IBlockData iblockdata) {
        return iblockdata.isAir() || iblockdata.is(TagsBlock.FIRE) || iblockdata.is(Blocks.NETHER_PORTAL);
    }

    public boolean isValid() {
        return this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortalBlocks(GeneratorAccess generatoraccess) {
        IBlockData iblockdata = (IBlockData) Blocks.NETHER_PORTAL.defaultBlockState().setValue(BlockPortal.AXIS, this.axis);

        BlockPosition.betweenClosed(this.bottomLeft, this.bottomLeft.relative(EnumDirection.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach((blockposition) -> {
            generatoraccess.setBlock(blockposition, iblockdata, 18);
        });
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public static Vec3D getRelativePosition(BlockUtil.Rectangle blockutil_rectangle, EnumDirection.EnumAxis enumdirection_enumaxis, Vec3D vec3d, EntitySize entitysize) {
        double d0 = (double) blockutil_rectangle.axis1Size - (double) entitysize.width();
        double d1 = (double) blockutil_rectangle.axis2Size - (double) entitysize.height();
        BlockPosition blockposition = blockutil_rectangle.minCorner;
        double d2;
        double d3;

        if (d0 > 0.0D) {
            d2 = (double) blockposition.get(enumdirection_enumaxis) + (double) entitysize.width() / 2.0D;
            d3 = MathHelper.clamp(MathHelper.inverseLerp(vec3d.get(enumdirection_enumaxis) - d2, 0.0D, d0), 0.0D, 1.0D);
        } else {
            d3 = 0.5D;
        }

        EnumDirection.EnumAxis enumdirection_enumaxis1;

        if (d1 > 0.0D) {
            enumdirection_enumaxis1 = EnumDirection.EnumAxis.Y;
            d2 = MathHelper.clamp(MathHelper.inverseLerp(vec3d.get(enumdirection_enumaxis1) - (double) blockposition.get(enumdirection_enumaxis1), 0.0D, d1), 0.0D, 1.0D);
        } else {
            d2 = 0.0D;
        }

        enumdirection_enumaxis1 = enumdirection_enumaxis == EnumDirection.EnumAxis.X ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.X;
        double d4 = vec3d.get(enumdirection_enumaxis1) - ((double) blockposition.get(enumdirection_enumaxis1) + 0.5D);

        return new Vec3D(d3, d2, d4);
    }

    public static Vec3D findCollisionFreePosition(Vec3D vec3d, WorldServer worldserver, Entity entity, EntitySize entitysize) {
        if (entitysize.width() <= 4.0F && entitysize.height() <= 4.0F) {
            double d0 = (double) entitysize.height() / 2.0D;
            Vec3D vec3d1 = vec3d.add(0.0D, d0, 0.0D);
            VoxelShape voxelshape = VoxelShapes.create(AxisAlignedBB.ofSize(vec3d1, (double) entitysize.width(), 0.0D, (double) entitysize.width()).expandTowards(0.0D, 1.0D, 0.0D).inflate(1.0E-6D));
            Optional<Vec3D> optional = worldserver.findFreePosition(entity, voxelshape, vec3d1, (double) entitysize.width(), (double) entitysize.height(), (double) entitysize.width());
            Optional<Vec3D> optional1 = optional.map((vec3d2) -> {
                return vec3d2.subtract(0.0D, d0, 0.0D);
            });

            return (Vec3D) optional1.orElse(vec3d);
        } else {
            return vec3d;
        }
    }
}
