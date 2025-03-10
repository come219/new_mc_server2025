package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

public class SmithingTransformRecipe implements SmithingRecipe {

    final Optional<RecipeItemStack> template;
    final Optional<RecipeItemStack> base;
    final Optional<RecipeItemStack> addition;
    final ItemStack result;
    @Nullable
    private PlacementInfo placementInfo;

    public SmithingTransformRecipe(Optional<RecipeItemStack> optional, Optional<RecipeItemStack> optional1, Optional<RecipeItemStack> optional2, ItemStack itemstack) {
        this.template = optional;
        this.base = optional1;
        this.addition = optional2;
        this.result = itemstack;
    }

    public ItemStack assemble(SmithingRecipeInput smithingrecipeinput, HolderLookup.a holderlookup_a) {
        ItemStack itemstack = smithingrecipeinput.base().transmuteCopy(this.result.getItem(), this.result.getCount());

        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
    }

    @Override
    public Optional<RecipeItemStack> templateIngredient() {
        return this.template;
    }

    @Override
    public Optional<RecipeItemStack> baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<RecipeItemStack> additionIngredient() {
        return this.addition;
    }

    @Override
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
        }

        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SmithingRecipeDisplay(RecipeItemStack.optionalIngredientToDisplay(this.template), RecipeItemStack.optionalIngredientToDisplay(this.base), RecipeItemStack.optionalIngredientToDisplay(this.addition), new SlotDisplay.f(this.result), new SlotDisplay.d(Items.SMITHING_TABLE)));
    }

    public static class a implements RecipeSerializer<SmithingTransformRecipe> {

        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(RecipeItemStack.CODEC.optionalFieldOf("template").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.template;
            }), RecipeItemStack.CODEC.optionalFieldOf("base").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.base;
            }), RecipeItemStack.CODEC.optionalFieldOf("addition").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.addition;
            }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.result;
            })).apply(instance, SmithingTransformRecipe::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.composite(RecipeItemStack.OPTIONAL_CONTENTS_STREAM_CODEC, (smithingtransformrecipe) -> {
            return smithingtransformrecipe.template;
        }, RecipeItemStack.OPTIONAL_CONTENTS_STREAM_CODEC, (smithingtransformrecipe) -> {
            return smithingtransformrecipe.base;
        }, RecipeItemStack.OPTIONAL_CONTENTS_STREAM_CODEC, (smithingtransformrecipe) -> {
            return smithingtransformrecipe.addition;
        }, ItemStack.STREAM_CODEC, (smithingtransformrecipe) -> {
            return smithingtransformrecipe.result;
        }, SmithingTransformRecipe::new);

        public a() {}

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return SmithingTransformRecipe.a.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
            return SmithingTransformRecipe.a.STREAM_CODEC;
        }
    }
}
