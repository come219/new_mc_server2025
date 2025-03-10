package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

// CraftBukkit start
import net.minecraft.world.item.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftComplexRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.inventory.Recipe;
// CraftBukkit end

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

    // CraftBukkit start
    @Override
    public Recipe toBukkitRecipe(NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(ItemStack.EMPTY);

        CraftComplexRecipe recipe = new CraftComplexRecipe(id, result, this);
        recipe.setGroup(this.group());
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        return recipe;
    }
    // CraftBukkit end

    public static class Serializer<T extends RecipeCrafting> implements RecipeSerializer<T> {

        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(IRecipeComplex.Serializer.Factory<T> irecipecomplex_serializer_factory) {
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                P1<RecordCodecBuilder.Mu<T>, CraftingBookCategory> p1 = instance.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(RecipeCrafting::category)); // CraftBukkit - decompile error

                Objects.requireNonNull(irecipecomplex_serializer_factory);
                return p1.apply(instance, irecipecomplex_serializer_factory::create);
            });
            StreamCodec streamcodec = CraftingBookCategory.STREAM_CODEC;
            Function<RecipeCrafting, CraftingBookCategory> function = RecipeCrafting::category; // CraftBukkit - decompile error

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
