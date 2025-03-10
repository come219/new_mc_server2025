package net.minecraft.world.level;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.NextTickListEntry;
import net.minecraft.world.ticks.TickListPriority;

public interface ScheduledTickAccess {

    <T> NextTickListEntry<T> createTick(BlockPosition blockposition, T t0, int i, TickListPriority ticklistpriority);

    <T> NextTickListEntry<T> createTick(BlockPosition blockposition, T t0, int i);

    LevelTickAccess<Block> getBlockTicks();

    default void scheduleTick(BlockPosition blockposition, Block block, int i, TickListPriority ticklistpriority) {
        this.getBlockTicks().schedule(this.createTick(blockposition, block, i, ticklistpriority));
    }

    default void scheduleTick(BlockPosition blockposition, Block block, int i) {
        this.getBlockTicks().schedule(this.createTick(blockposition, block, i));
    }

    LevelTickAccess<FluidType> getFluidTicks();

    default void scheduleTick(BlockPosition blockposition, FluidType fluidtype, int i, TickListPriority ticklistpriority) {
        this.getFluidTicks().schedule(this.createTick(blockposition, fluidtype, i, ticklistpriority));
    }

    default void scheduleTick(BlockPosition blockposition, FluidType fluidtype, int i) {
        this.getFluidTicks().schedule(this.createTick(blockposition, fluidtype, i));
    }
}
