package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.StackedItemContents;

@FunctionalInterface
public interface AutoRecipeOutput {

    void fillStackedContents(StackedItemContents stackeditemcontents);
}
