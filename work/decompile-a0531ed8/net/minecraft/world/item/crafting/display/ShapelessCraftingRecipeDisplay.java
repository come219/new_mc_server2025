package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;

public record ShapelessCraftingRecipeDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    public static final MapCodec<ShapelessCraftingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(ShapelessCraftingRecipeDisplay::ingredients), SlotDisplay.CODEC.fieldOf("result").forGetter(ShapelessCraftingRecipeDisplay::result), SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ShapelessCraftingRecipeDisplay::craftingStation)).apply(instance, ShapelessCraftingRecipeDisplay::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessCraftingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), ShapelessCraftingRecipeDisplay::ingredients, SlotDisplay.STREAM_CODEC, ShapelessCraftingRecipeDisplay::result, SlotDisplay.STREAM_CODEC, ShapelessCraftingRecipeDisplay::craftingStation, ShapelessCraftingRecipeDisplay::new);
    public static final RecipeDisplay.a<ShapelessCraftingRecipeDisplay> TYPE = new RecipeDisplay.a<>(ShapelessCraftingRecipeDisplay.MAP_CODEC, ShapelessCraftingRecipeDisplay.STREAM_CODEC);

    @Override
    public RecipeDisplay.a<ShapelessCraftingRecipeDisplay> type() {
        return ShapelessCraftingRecipeDisplay.TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet featureflagset) {
        return this.ingredients.stream().allMatch((slotdisplay) -> {
            return slotdisplay.isEnabled(featureflagset);
        }) && RecipeDisplay.super.isEnabled(featureflagset);
    }
}
