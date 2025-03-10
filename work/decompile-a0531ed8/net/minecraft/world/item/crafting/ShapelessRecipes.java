package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.World;

public class ShapelessRecipes implements RecipeCrafting {

    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final List<RecipeItemStack> ingredients;
    @Nullable
    private PlacementInfo placementInfo;

    public ShapelessRecipes(String s, CraftingBookCategory craftingbookcategory, ItemStack itemstack, List<RecipeItemStack> list) {
        this.group = s;
        this.category = craftingbookcategory;
        this.result = itemstack;
        this.ingredients = list;
    }

    @Override
    public RecipeSerializer<ShapelessRecipes> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public String group() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }

        return this.placementInfo;
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        return craftinginput.ingredientCount() != this.ingredients.size() ? false : (craftinginput.size() == 1 && this.ingredients.size() == 1 ? ((RecipeItemStack) this.ingredients.getFirst()).test(craftinginput.getItem(0)) : craftinginput.stackedContents().canCraft((IRecipe) this, (AutoRecipeStackManager.b) null));
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        return this.result.copy();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(this.ingredients.stream().map(RecipeItemStack::display).toList(), new SlotDisplay.f(this.result), new SlotDisplay.d(Items.CRAFTING_TABLE)));
    }

    public static class a implements RecipeSerializer<ShapelessRecipes> {

        private static final MapCodec<ShapelessRecipes> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapelessrecipes) -> {
                return shapelessrecipes.group;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapelessrecipes) -> {
                return shapelessrecipes.category;
            }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapelessrecipes) -> {
                return shapelessrecipes.result;
            }), RecipeItemStack.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter((shapelessrecipes) -> {
                return shapelessrecipes.ingredients;
            })).apply(instance, ShapelessRecipes::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipes> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, (shapelessrecipes) -> {
            return shapelessrecipes.group;
        }, CraftingBookCategory.STREAM_CODEC, (shapelessrecipes) -> {
            return shapelessrecipes.category;
        }, ItemStack.STREAM_CODEC, (shapelessrecipes) -> {
            return shapelessrecipes.result;
        }, RecipeItemStack.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), (shapelessrecipes) -> {
            return shapelessrecipes.ingredients;
        }, ShapelessRecipes::new);

        public a() {}

        @Override
        public MapCodec<ShapelessRecipes> codec() {
            return ShapelessRecipes.a.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipes> streamCodec() {
            return ShapelessRecipes.a.STREAM_CODEC;
        }
    }
}
