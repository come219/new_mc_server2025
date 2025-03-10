package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public class SmithingTrimRecipe implements SmithingRecipe {

    final Optional<RecipeItemStack> template;
    final Optional<RecipeItemStack> base;
    final Optional<RecipeItemStack> addition;
    @Nullable
    private PlacementInfo placementInfo;

    public SmithingTrimRecipe(Optional<RecipeItemStack> optional, Optional<RecipeItemStack> optional1, Optional<RecipeItemStack> optional2) {
        this.template = optional;
        this.base = optional1;
        this.addition = optional2;
    }

    public ItemStack assemble(SmithingRecipeInput smithingrecipeinput, HolderLookup.a holderlookup_a) {
        return applyTrim(holderlookup_a, smithingrecipeinput.base(), smithingrecipeinput.addition(), smithingrecipeinput.template());
    }

    public static ItemStack applyTrim(HolderLookup.a holderlookup_a, ItemStack itemstack, ItemStack itemstack1, ItemStack itemstack2) {
        Optional<Holder.c<TrimMaterial>> optional = TrimMaterials.getFromIngredient(holderlookup_a, itemstack1);
        Optional<Holder.c<TrimPattern>> optional1 = TrimPatterns.getFromTemplate(holderlookup_a, itemstack2);

        if (optional.isPresent() && optional1.isPresent()) {
            ArmorTrim armortrim = (ArmorTrim) itemstack.get(DataComponents.TRIM);

            if (armortrim != null && armortrim.hasPatternAndMaterial((Holder) optional1.get(), (Holder) optional.get())) {
                return ItemStack.EMPTY;
            } else {
                ItemStack itemstack3 = itemstack.copyWithCount(1);

                itemstack3.set(DataComponents.TRIM, new ArmorTrim((Holder) optional.get(), (Holder) optional1.get()));
                return itemstack3;
            }
        } else {
            return ItemStack.EMPTY;
        }
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
    public RecipeSerializer<SmithingTrimRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
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
        SlotDisplay slotdisplay = RecipeItemStack.optionalIngredientToDisplay(this.base);
        SlotDisplay slotdisplay1 = RecipeItemStack.optionalIngredientToDisplay(this.addition);
        SlotDisplay slotdisplay2 = RecipeItemStack.optionalIngredientToDisplay(this.template);

        return List.of(new SmithingRecipeDisplay(slotdisplay2, slotdisplay, slotdisplay1, new SlotDisplay.g(slotdisplay, slotdisplay1, slotdisplay2), new SlotDisplay.d(Items.SMITHING_TABLE)));
    }

    public static class a implements RecipeSerializer<SmithingTrimRecipe> {

        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(RecipeItemStack.CODEC.optionalFieldOf("template").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.template;
            }), RecipeItemStack.CODEC.optionalFieldOf("base").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.base;
            }), RecipeItemStack.CODEC.optionalFieldOf("addition").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.addition;
            })).apply(instance, SmithingTrimRecipe::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.composite(RecipeItemStack.OPTIONAL_CONTENTS_STREAM_CODEC, (smithingtrimrecipe) -> {
            return smithingtrimrecipe.template;
        }, RecipeItemStack.OPTIONAL_CONTENTS_STREAM_CODEC, (smithingtrimrecipe) -> {
            return smithingtrimrecipe.base;
        }, RecipeItemStack.OPTIONAL_CONTENTS_STREAM_CODEC, (smithingtrimrecipe) -> {
            return smithingtrimrecipe.addition;
        }, SmithingTrimRecipe::new);

        public a() {}

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return SmithingTrimRecipe.a.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
            return SmithingTrimRecipe.a.STREAM_CODEC;
        }
    }
}
