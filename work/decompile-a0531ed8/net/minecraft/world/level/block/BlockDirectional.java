package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;

public abstract class BlockDirectional extends Block {

    public static final BlockStateEnum<EnumDirection> FACING = BlockProperties.FACING;

    protected BlockDirectional(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected abstract MapCodec<? extends BlockDirectional> codec();
}
