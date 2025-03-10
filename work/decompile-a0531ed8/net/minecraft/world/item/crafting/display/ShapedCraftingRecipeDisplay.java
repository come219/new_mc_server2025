package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;

public record ShapedCraftingRecipeDisplay(int width, int height, List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    public static final MapCodec<ShapedCraftingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.INT.fieldOf("width").forGetter(ShapedCraftingRecipeDisplay::width), Codec.INT.fieldOf("height").forGetter(ShapedCraftingRecipeDisplay::height), SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(ShapedCraftingRecipeDisplay::ingredients), SlotDisplay.CODEC.fieldOf("result").forGetter(ShapedCraftingRecipeDisplay::result), SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ShapedCraftingRecipeDisplay::craftingStation)).apply(instance, ShapedCraftingRecipeDisplay::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedCraftingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ShapedCraftingRecipeDisplay::width, ByteBufCodecs.VAR_INT, ShapedCraftingRecipeDisplay::height, SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), ShapedCraftingRecipeDisplay::ingredients, SlotDisplay.STREAM_CODEC, ShapedCraftingRecipeDisplay::result, SlotDisplay.STREAM_CODEC, ShapedCraftingRecipeDisplay::craftingStation, ShapedCraftingRecipeDisplay::new);
    public static final RecipeDisplay.a<ShapedCraftingRecipeDisplay> TYPE = new RecipeDisplay.a<>(ShapedCraftingRecipeDisplay.MAP_CODEC, ShapedCraftingRecipeDisplay.STREAM_CODEC);

    public ShapedCraftingRecipeDisplay(int i, int j, List<SlotDisplay> list, SlotDisplay slotdisplay, SlotDisplay slotdisplay1) {
        if (list.size() != i * j) {
            throw new IllegalArgumentException("Invalid shaped recipe display contents");
        } else {
            this.width = i;
            this.height = j;
            this.ingredients = list;
            this.result = slotdisplay;
            this.craftingStation = slotdisplay1;
        }
    }

    @Override
    public RecipeDisplay.a<ShapedCraftingRecipeDisplay> type() {
        return ShapedCraftingRecipeDisplay.TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet featureflagset) {
        return this.ingredients.stream().allMatch((slotdisplay) -> {
            return slotdisplay.isEnabled(featureflagset);
        }) && RecipeDisplay.super.isEnabled(featureflagset);
    }
}
