package net.minecraft.world.inventory;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class ContainerRecipeBook extends Container {

    public ContainerRecipeBook(Containers<?> containers, int i) {
        super(containers, i);
    }

    public abstract ContainerRecipeBook.a handlePlacement(boolean flag, boolean flag1, RecipeHolder<?> recipeholder, WorldServer worldserver, PlayerInventory playerinventory);

    public abstract void fillCraftSlotsStackedContents(StackedItemContents stackeditemcontents);

    public abstract RecipeBookType getRecipeBookType();

    public static enum a {

        NOTHING, PLACE_GHOST_RECIPE;

        private a() {}
    }
}
