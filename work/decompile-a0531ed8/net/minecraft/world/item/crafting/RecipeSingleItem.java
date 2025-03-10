package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public abstract class RecipeSingleItem implements IRecipe<SingleRecipeInput> {

    private final RecipeItemStack input;
    private final ItemStack result;
    private final String group;
    @Nullable
    private PlacementInfo placementInfo;

    public RecipeSingleItem(String s, RecipeItemStack recipeitemstack, ItemStack itemstack) {
        this.group = s;
        this.input = recipeitemstack;
        this.result = itemstack;
    }

    @Override
    public abstract RecipeSerializer<? extends RecipeSingleItem> getSerializer();

    @Override
    public abstract Recipes<? extends RecipeSingleItem> getType();

    public boolean matches(SingleRecipeInput singlerecipeinput, World world) {
        return this.input.test(singlerecipeinput.item());
    }

    @Override
    public String group() {
        return this.group;
    }

    public RecipeItemStack input() {
        return this.input;
    }

    protected ItemStack result() {
        return this.result;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.input);
        }

        return this.placementInfo;
    }

    public ItemStack assemble(SingleRecipeInput singlerecipeinput, HolderLookup.a holderlookup_a) {
        return this.result.copy();
    }

    @FunctionalInterface
    public interface a<T extends RecipeSingleItem> {

        T create(String s, RecipeItemStack recipeitemstack, ItemStack itemstack);
    }

    public static class b<T extends RecipeSingleItem> implements RecipeSerializer<T> {

        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        protected b(RecipeSingleItem.a<T> recipesingleitem_a) {
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                P3 p3 = instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(RecipeSingleItem::group), RecipeItemStack.CODEC.fieldOf("ingredient").forGetter(RecipeSingleItem::input), ItemStack.STRICT_CODEC.fieldOf("result").forGetter(RecipeSingleItem::result));

                Objects.requireNonNull(recipesingleitem_a);
                return p3.apply(instance, recipesingleitem_a::create);
            });
            StreamCodec streamcodec = ByteBufCodecs.STRING_UTF8;
            Function function = RecipeSingleItem::group;
            StreamCodec streamcodec1 = RecipeItemStack.CONTENTS_STREAM_CODEC;
            Function function1 = RecipeSingleItem::input;
            StreamCodec streamcodec2 = ItemStack.STREAM_CODEC;
            Function function2 = RecipeSingleItem::result;

            Objects.requireNonNull(recipesingleitem_a);
            this.streamCodec = StreamCodec.composite(streamcodec, function, streamcodec1, function1, streamcodec2, function2, recipesingleitem_a::create);
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }
    }
}
