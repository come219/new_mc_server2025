package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockChestTrapped;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

public class TileEntityChestTrapped extends TileEntityChest {

    public TileEntityChestTrapped(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.TRAPPED_CHEST, blockposition, iblockdata);
    }

    @Override
    protected void signalOpenCount(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        super.signalOpenCount(world, blockposition, iblockdata, i, j);
        if (i != j) {
            Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(world, ((EnumDirection) iblockdata.getValue(BlockChestTrapped.FACING)).getOpposite(), EnumDirection.UP);
            Block block = iblockdata.getBlock();

            world.updateNeighborsAt(blockposition, block, orientation);
            world.updateNeighborsAt(blockposition.below(), block, orientation);
        }

    }
}
