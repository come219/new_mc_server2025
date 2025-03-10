package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.crafting.CraftingManager;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.World;
import org.slf4j.Logger;

public class ItemKnowledgeBook extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ItemKnowledgeBook(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        List<ResourceKey<IRecipe<?>>> list = (List) itemstack.getOrDefault(DataComponents.RECIPES, List.of());

        itemstack.consume(1, entityhuman);
        if (list.isEmpty()) {
            return EnumInteractionResult.FAIL;
        } else {
            if (!world.isClientSide) {
                CraftingManager craftingmanager = world.getServer().getRecipeManager();
                List<RecipeHolder<?>> list1 = new ArrayList(list.size());
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    ResourceKey<IRecipe<?>> resourcekey = (ResourceKey) iterator.next();
                    Optional<RecipeHolder<?>> optional = craftingmanager.byKey(resourcekey);

                    if (!optional.isPresent()) {
                        ItemKnowledgeBook.LOGGER.error("Invalid recipe: {}", resourcekey);
                        return EnumInteractionResult.FAIL;
                    }

                    list1.add((RecipeHolder) optional.get());
                }

                entityhuman.awardRecipes(list1);
                entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            }

            return EnumInteractionResult.SUCCESS;
        }
    }
}
