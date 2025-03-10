package net.minecraft.world.level.redstone;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public interface NeighborUpdater {

    EnumDirection[] UPDATE_ORDER = new EnumDirection[]{EnumDirection.WEST, EnumDirection.EAST, EnumDirection.DOWN, EnumDirection.UP, EnumDirection.NORTH, EnumDirection.SOUTH};

    void shapeUpdate(EnumDirection enumdirection, IBlockData iblockdata, BlockPosition blockposition, BlockPosition blockposition1, int i, int j);

    void neighborChanged(BlockPosition blockposition, Block block, @Nullable Orientation orientation);

    void neighborChanged(IBlockData iblockdata, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag);

    default void updateNeighborsAtExceptFromFacing(BlockPosition blockposition, Block block, @Nullable EnumDirection enumdirection, @Nullable Orientation orientation) {
        EnumDirection[] aenumdirection = NeighborUpdater.UPDATE_ORDER;
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection1 = aenumdirection[j];

            if (enumdirection1 != enumdirection) {
                this.neighborChanged(blockposition.relative(enumdirection1), block, (Orientation) null);
            }
        }

    }

    static void executeShapeUpdate(GeneratorAccess generatoraccess, EnumDirection enumdirection, BlockPosition blockposition, BlockPosition blockposition1, IBlockData iblockdata, int i, int j) {
        IBlockData iblockdata1 = generatoraccess.getBlockState(blockposition);

        if ((i & 128) == 0 || !iblockdata1.is(Blocks.REDSTONE_WIRE)) {
            IBlockData iblockdata2 = iblockdata1.updateShape(generatoraccess, generatoraccess, blockposition, enumdirection, blockposition1, iblockdata, generatoraccess.getRandom());

            Block.updateOrDestroy(iblockdata1, iblockdata2, generatoraccess, blockposition, i, j);
        }
    }

    static void executeUpdate(World world, IBlockData iblockdata, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        try {
            iblockdata.handleNeighborChanged(world, blockposition, block, orientation, flag);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Block being updated");

            crashreportsystemdetails.setDetail("Source block type", () -> {
                try {
                    return String.format(Locale.ROOT, "ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                } catch (Throwable throwable1) {
                    return "ID #" + String.valueOf(BuiltInRegistries.BLOCK.getKey(block));
                }
            });
            CrashReportSystemDetails.populateBlockDetails(crashreportsystemdetails, world, blockposition, iblockdata);
            throw new ReportedException(crashreport);
        }
    }
}
