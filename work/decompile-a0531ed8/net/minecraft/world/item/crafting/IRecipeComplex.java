package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class IRecipeComplex implements RecipeCrafting {

    private final CraftingBookCategory category;

    public IRecipeComplex(CraftingBookCategory craftingbookcategory) {
        this.category = craftingbookcategory;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public abstract RecipeSerializer<? extends IRecipeComplex> getSerializer();

    public static class Serializer<T extends RecipeCrafting> implements RecipeSerializer<T> {

        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(IRecipeComplex.Serializer.Factory<T> irecipecomplex_serializer_factory) {
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                P1 p1 = instance.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(RecipeCrafting::category));

                Objects.requireNonNull(irecipecomplex_serializer_factory);
                return p1.apply(instance, irecipecomplex_serializer_factory::create);
            });
            StreamCodec streamcodec = CraftingBookCategory.STREAM_CODEC;
            Function function = RecipeCrafting::category;

            Objects.requireNonNull(irecipecomplex_serializer_factory);
            this.streamCodec = StreamCodec.composite(streamcodec, function, irecipecomplex_serializer_factory::create);
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }

        @FunctionalInterface
        public interface Factory<T extends RecipeCrafting> {

            T create(CraftingBookCategory craftingbookcategory);
        }
    }
}
