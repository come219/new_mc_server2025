package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBase;

public abstract class MultifaceSpreadeableBlock extends MultifaceBlock {

    public MultifaceSpreadeableBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public abstract MapCodec<? extends MultifaceSpreadeableBlock> codec();

    public abstract MultifaceSpreader getSpreader();
}
