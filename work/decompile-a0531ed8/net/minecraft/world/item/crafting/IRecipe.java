package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.World;

public interface IRecipe<T extends RecipeInput> {

    Codec<IRecipe<?>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(IRecipe::getSerializer, RecipeSerializer::codec);
    StreamCodec<RegistryFriendlyByteBuf, IRecipe<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER).dispatch(IRecipe::getSerializer, RecipeSerializer::streamCodec);

    boolean matches(T t0, World world);

    ItemStack assemble(T t0, HolderLookup.a holderlookup_a);

    default boolean isSpecial() {
        return false;
    }

    default boolean showNotification() {
        return true;
    }

    default String group() {
        return "";
    }

    RecipeSerializer<? extends IRecipe<T>> getSerializer();

    Recipes<? extends IRecipe<T>> getType();

    PlacementInfo placementInfo();

    default List<RecipeDisplay> display() {
        return List.of();
    }

    RecipeBookCategory recipeBookCategory();
}
