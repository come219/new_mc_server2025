package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;

public class RecipeFireworksStar extends IRecipeComplex {

    private static final Map<Item, FireworkExplosion.a> SHAPE_BY_ITEM = Map.of(Items.FIRE_CHARGE, FireworkExplosion.a.LARGE_BALL, Items.FEATHER, FireworkExplosion.a.BURST, Items.GOLD_NUGGET, FireworkExplosion.a.STAR, Items.SKELETON_SKULL, FireworkExplosion.a.CREEPER, Items.WITHER_SKELETON_SKULL, FireworkExplosion.a.CREEPER, Items.CREEPER_HEAD, FireworkExplosion.a.CREEPER, Items.PLAYER_HEAD, FireworkExplosion.a.CREEPER, Items.DRAGON_HEAD, FireworkExplosion.a.CREEPER, Items.ZOMBIE_HEAD, FireworkExplosion.a.CREEPER, Items.PIGLIN_HEAD, FireworkExplosion.a.CREEPER);
    private static final RecipeItemStack TRAIL_INGREDIENT = RecipeItemStack.of((IMaterial) Items.DIAMOND);
    private static final RecipeItemStack TWINKLE_INGREDIENT = RecipeItemStack.of((IMaterial) Items.GLOWSTONE_DUST);
    private static final RecipeItemStack GUNPOWDER_INGREDIENT = RecipeItemStack.of((IMaterial) Items.GUNPOWDER);

    public RecipeFireworksStar(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        if (craftinginput.ingredientCount() < 2) {
            return false;
        } else {
            boolean flag = false;
            boolean flag1 = false;
            boolean flag2 = false;
            boolean flag3 = false;
            boolean flag4 = false;

            for (int i = 0; i < craftinginput.size(); ++i) {
                ItemStack itemstack = craftinginput.getItem(i);

                if (!itemstack.isEmpty()) {
                    if (RecipeFireworksStar.SHAPE_BY_ITEM.containsKey(itemstack.getItem())) {
                        if (flag2) {
                            return false;
                        }

                        flag2 = true;
                    } else if (RecipeFireworksStar.TWINKLE_INGREDIENT.test(itemstack)) {
                        if (flag4) {
                            return false;
                        }

                        flag4 = true;
                    } else if (RecipeFireworksStar.TRAIL_INGREDIENT.test(itemstack)) {
                        if (flag3) {
                            return false;
                        }

                        flag3 = true;
                    } else if (RecipeFireworksStar.GUNPOWDER_INGREDIENT.test(itemstack)) {
                        if (flag) {
                            return false;
                        }

                        flag = true;
                    } else {
                        if (!(itemstack.getItem() instanceof ItemDye)) {
                            return false;
                        }

                        flag1 = true;
                    }
                }
            }

            return flag && flag1;
        }
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        FireworkExplosion.a fireworkexplosion_a = FireworkExplosion.a.SMALL_BALL;
        boolean flag = false;
        boolean flag1 = false;
        IntArrayList intarraylist = new IntArrayList();

        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack = craftinginput.getItem(i);

            if (!itemstack.isEmpty()) {
                FireworkExplosion.a fireworkexplosion_a1 = (FireworkExplosion.a) RecipeFireworksStar.SHAPE_BY_ITEM.get(itemstack.getItem());

                if (fireworkexplosion_a1 != null) {
                    fireworkexplosion_a = fireworkexplosion_a1;
                } else if (RecipeFireworksStar.TWINKLE_INGREDIENT.test(itemstack)) {
                    flag = true;
                } else if (RecipeFireworksStar.TRAIL_INGREDIENT.test(itemstack)) {
                    flag1 = true;
                } else {
                    Item item = itemstack.getItem();

                    if (item instanceof ItemDye) {
                        ItemDye itemdye = (ItemDye) item;

                        intarraylist.add(itemdye.getDyeColor().getFireworkColor());
                    }
                }
            }
        }

        ItemStack itemstack1 = new ItemStack(Items.FIREWORK_STAR);

        itemstack1.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(fireworkexplosion_a, intarraylist, IntList.of(), flag1, flag));
        return itemstack1;
    }

    @Override
    public RecipeSerializer<RecipeFireworksStar> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR;
    }
}
