package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public abstract class RecipeCooking extends RecipeSingleItem {

    private final CookingBookCategory category;
    private final float experience;
    private final int cookingTime;

    public RecipeCooking(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        super(s, recipeitemstack, itemstack);
        this.category = cookingbookcategory;
        this.experience = f;
        this.cookingTime = i;
    }

    @Override
    public abstract RecipeSerializer<? extends RecipeCooking> getSerializer();

    @Override
    public abstract Recipes<? extends RecipeCooking> getType();

    public float experience() {
        return this.experience;
    }

    public int cookingTime() {
        return this.cookingTime;
    }

    public CookingBookCategory category() {
        return this.category;
    }

    protected abstract Item furnaceIcon();

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new FurnaceRecipeDisplay(this.input().display(), SlotDisplay.a.INSTANCE, new SlotDisplay.f(this.result()), new SlotDisplay.d(this.furnaceIcon()), this.cookingTime, this.experience));
    }

    @FunctionalInterface
    public interface a<T extends RecipeCooking> {

        T create(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i);
    }

    public static class b<T extends RecipeCooking> implements RecipeSerializer<T> {

        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public b(RecipeCooking.a<T> recipecooking_a, int i) {
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                P6 p6 = instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(RecipeSingleItem::group), CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(RecipeCooking::category), RecipeItemStack.CODEC.fieldOf("ingredient").forGetter(RecipeSingleItem::input), ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(RecipeSingleItem::result), Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(RecipeCooking::experience), Codec.INT.fieldOf("cookingtime").orElse(i).forGetter(RecipeCooking::cookingTime));

                Objects.requireNonNull(recipecooking_a);
                return p6.apply(instance, recipecooking_a::create);
            });
            StreamCodec streamcodec = ByteBufCodecs.STRING_UTF8;
            Function function = RecipeSingleItem::group;
            StreamCodec streamcodec1 = CookingBookCategory.STREAM_CODEC;
            Function function1 = RecipeCooking::category;
            StreamCodec streamcodec2 = RecipeItemStack.CONTENTS_STREAM_CODEC;
            Function function2 = RecipeSingleItem::input;
            StreamCodec streamcodec3 = ItemStack.STREAM_CODEC;
            Function function3 = RecipeSingleItem::result;
            StreamCodec streamcodec4 = ByteBufCodecs.FLOAT;
            Function function4 = RecipeCooking::experience;
            StreamCodec streamcodec5 = ByteBufCodecs.INT;
            Function function5 = RecipeCooking::cookingTime;

            Objects.requireNonNull(recipecooking_a);
            this.streamCodec = StreamCodec.composite(streamcodec, function, streamcodec1, function1, streamcodec2, function2, streamcodec3, function3, streamcodec4, function4, streamcodec5, function5, recipecooking_a::create);
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
