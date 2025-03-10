package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record StonecutterRecipeDisplay(SlotDisplay input, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    public static final MapCodec<StonecutterRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(SlotDisplay.CODEC.fieldOf("input").forGetter(StonecutterRecipeDisplay::input), SlotDisplay.CODEC.fieldOf("result").forGetter(StonecutterRecipeDisplay::result), SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(StonecutterRecipeDisplay::craftingStation)).apply(instance, StonecutterRecipeDisplay::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, StonecutterRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, StonecutterRecipeDisplay::input, SlotDisplay.STREAM_CODEC, StonecutterRecipeDisplay::result, SlotDisplay.STREAM_CODEC, StonecutterRecipeDisplay::craftingStation, StonecutterRecipeDisplay::new);
    public static final RecipeDisplay.a<StonecutterRecipeDisplay> TYPE = new RecipeDisplay.a<>(StonecutterRecipeDisplay.MAP_CODEC, StonecutterRecipeDisplay.STREAM_CODEC);

    @Override
    public RecipeDisplay.a<StonecutterRecipeDisplay> type() {
        return StonecutterRecipeDisplay.TYPE;
    }
}
