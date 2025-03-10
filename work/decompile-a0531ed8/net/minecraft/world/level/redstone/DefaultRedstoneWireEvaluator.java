package net.minecraft.world.level.redstone;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockRedstoneWire;
import net.minecraft.world.level.block.state.IBlockData;

public class DefaultRedstoneWireEvaluator extends RedstoneWireEvaluator {

    public DefaultRedstoneWireEvaluator(BlockRedstoneWire blockredstonewire) {
        super(blockredstonewire);
    }

    @Override
    public void updatePowerStrength(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable Orientation orientation, boolean flag) {
        int i = this.calculateTargetStrength(world, blockposition);

        if ((Integer) iblockdata.getValue(BlockRedstoneWire.POWER) != i) {
            if (world.getBlockState(blockposition) == iblockdata) {
                world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockRedstoneWire.POWER, i), 2);
            }

            Set<BlockPosition> set = Sets.newHashSet();

            set.add(blockposition);
            EnumDirection[] aenumdirection = EnumDirection.values();
            int j = aenumdirection.length;

            for (int k = 0; k < j; ++k) {
                EnumDirection enumdirection = aenumdirection[k];

                set.add(blockposition.relative(enumdirection));
            }

            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition1 = (BlockPosition) iterator.next();

                world.updateNeighborsAt(blockposition1, this.wireBlock);
            }
        }

    }

    private int calculateTargetStrength(World world, BlockPosition blockposition) {
        int i = this.getBlockSignal(world, blockposition);

        return i == 15 ? i : Math.max(i, this.getIncomingWireSignal(world, blockposition));
    }
}
