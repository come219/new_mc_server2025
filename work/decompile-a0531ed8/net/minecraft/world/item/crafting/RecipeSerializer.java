package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface RecipeSerializer<T extends IRecipe<?>> {

    RecipeSerializer<ShapedRecipes> SHAPED_RECIPE = register("crafting_shaped", new ShapedRecipes.Serializer());
    RecipeSerializer<ShapelessRecipes> SHAPELESS_RECIPE = register("crafting_shapeless", new ShapelessRecipes.a());
    RecipeSerializer<RecipeArmorDye> ARMOR_DYE = register("crafting_special_armordye", new IRecipeComplex.Serializer<>(RecipeArmorDye::new));
    RecipeSerializer<RecipeBookClone> BOOK_CLONING = register("crafting_special_bookcloning", new IRecipeComplex.Serializer<>(RecipeBookClone::new));
    RecipeSerializer<RecipeMapClone> MAP_CLONING = register("crafting_special_mapcloning", new IRecipeComplex.Serializer<>(RecipeMapClone::new));
    RecipeSerializer<RecipeMapExtend> MAP_EXTENDING = register("crafting_special_mapextending", new IRecipeComplex.Serializer<>(RecipeMapExtend::new));
    RecipeSerializer<RecipeFireworks> FIREWORK_ROCKET = register("crafting_special_firework_rocket", new IRecipeComplex.Serializer<>(RecipeFireworks::new));
    RecipeSerializer<RecipeFireworksStar> FIREWORK_STAR = register("crafting_special_firework_star", new IRecipeComplex.Serializer<>(RecipeFireworksStar::new));
    RecipeSerializer<RecipeFireworksFade> FIREWORK_STAR_FADE = register("crafting_special_firework_star_fade", new IRecipeComplex.Serializer<>(RecipeFireworksFade::new));
    RecipeSerializer<RecipeTippedArrow> TIPPED_ARROW = register("crafting_special_tippedarrow", new IRecipeComplex.Serializer<>(RecipeTippedArrow::new));
    RecipeSerializer<RecipeBannerDuplicate> BANNER_DUPLICATE = register("crafting_special_bannerduplicate", new IRecipeComplex.Serializer<>(RecipeBannerDuplicate::new));
    RecipeSerializer<RecipiesShield> SHIELD_DECORATION = register("crafting_special_shielddecoration", new IRecipeComplex.Serializer<>(RecipiesShield::new));
    RecipeSerializer<TransmuteRecipe> TRANSMUTE = register("crafting_transmute", new TransmuteRecipe.a());
    RecipeSerializer<RecipeRepair> REPAIR_ITEM = register("crafting_special_repairitem", new IRecipeComplex.Serializer<>(RecipeRepair::new));
    RecipeSerializer<FurnaceRecipe> SMELTING_RECIPE = register("smelting", new RecipeCooking.b<>(FurnaceRecipe::new, 200));
    RecipeSerializer<RecipeBlasting> BLASTING_RECIPE = register("blasting", new RecipeCooking.b<>(RecipeBlasting::new, 100));
    RecipeSerializer<RecipeSmoking> SMOKING_RECIPE = register("smoking", new RecipeCooking.b<>(RecipeSmoking::new, 100));
    RecipeSerializer<RecipeCampfire> CAMPFIRE_COOKING_RECIPE = register("campfire_cooking", new RecipeCooking.b<>(RecipeCampfire::new, 100));
    RecipeSerializer<RecipeStonecutting> STONECUTTER = register("stonecutting", new RecipeSingleItem.b<>(RecipeStonecutting::new));
    RecipeSerializer<SmithingTransformRecipe> SMITHING_TRANSFORM = register("smithing_transform", new SmithingTransformRecipe.a());
    RecipeSerializer<SmithingTrimRecipe> SMITHING_TRIM = register("smithing_trim", new SmithingTrimRecipe.a());
    RecipeSerializer<DecoratedPotRecipe> DECORATED_POT_RECIPE = register("crafting_decorated_pot", new IRecipeComplex.Serializer<>(DecoratedPotRecipe::new));

    MapCodec<T> codec();

    /** @deprecated */
    @Deprecated
    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

    static <S extends RecipeSerializer<T>, T extends IRecipe<?>> S register(String s, S s0) {
        return (RecipeSerializer) IRegistry.register(BuiltInRegistries.RECIPE_SERIALIZER, s, s0);
    }
}
