package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;

public interface RecipeCraftingHolder {

    void setRecipeUsed(@Nullable RecipeHolder<?> recipeholder);

    @Nullable
    RecipeHolder<?> getRecipeUsed();

    default void awardUsedRecipes(EntityHuman entityhuman, List<ItemStack> list) {
        RecipeHolder<?> recipeholder = this.getRecipeUsed();

        if (recipeholder != null) {
            entityhuman.triggerRecipeCrafted(recipeholder, list);
            if (!recipeholder.value().isSpecial()) {
                entityhuman.awardRecipes(Collections.singleton(recipeholder));
                this.setRecipeUsed((RecipeHolder) null);
            }
        }

    }

    default boolean setRecipeUsed(EntityPlayer entityplayer, RecipeHolder<?> recipeholder) {
        if (!recipeholder.value().isSpecial() && entityplayer.serverLevel().getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !entityplayer.getRecipeBook().contains(recipeholder.id())) {
            return false;
        } else {
            this.setRecipeUsed(recipeholder);
            return true;
        }
    }
}
