package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.World;

public class ShapedRecipes implements RecipeCrafting {

    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;
    @Nullable
    private PlacementInfo placementInfo;

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, ShapedRecipePattern shapedrecipepattern, ItemStack itemstack, boolean flag) {
        this.group = s;
        this.category = craftingbookcategory;
        this.pattern = shapedrecipepattern;
        this.result = itemstack;
        this.showNotification = flag;
    }

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, ShapedRecipePattern shapedrecipepattern, ItemStack itemstack) {
        this(s, craftingbookcategory, shapedrecipepattern, itemstack, true);
    }

    @Override
    public RecipeSerializer<? extends ShapedRecipes> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
    }

    @Override
    public String group() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @VisibleForTesting
    public List<Optional<RecipeItemStack>> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
        }

        return this.placementInfo;
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        return this.pattern.matches(craftinginput);
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        return this.result.copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(this.pattern.width(), this.pattern.height(), this.pattern.ingredients().stream().map((optional) -> {
            return (SlotDisplay) optional.map(RecipeItemStack::display).orElse(SlotDisplay.c.INSTANCE);
        }).toList(), new SlotDisplay.f(this.result), new SlotDisplay.d(Items.CRAFTING_TABLE)));
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipes> {

        public static final MapCodec<ShapedRecipes> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapedrecipes) -> {
                return shapedrecipes.group;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapedrecipes) -> {
                return shapedrecipes.category;
            }), ShapedRecipePattern.MAP_CODEC.forGetter((shapedrecipes) -> {
                return shapedrecipes.pattern;
            }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapedrecipes) -> {
                return shapedrecipes.result;
            }), Codec.BOOL.optionalFieldOf("show_notification", true).forGetter((shapedrecipes) -> {
                return shapedrecipes.showNotification;
            })).apply(instance, ShapedRecipes::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipes> STREAM_CODEC = StreamCodec.of(ShapedRecipes.Serializer::toNetwork, ShapedRecipes.Serializer::fromNetwork);

        public Serializer() {}

        @Override
        public MapCodec<ShapedRecipes> codec() {
            return ShapedRecipes.Serializer.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipes> streamCodec() {
            return ShapedRecipes.Serializer.STREAM_CODEC;
        }

        private static ShapedRecipes fromNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            String s = registryfriendlybytebuf.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) registryfriendlybytebuf.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = (ShapedRecipePattern) ShapedRecipePattern.STREAM_CODEC.decode(registryfriendlybytebuf);
            ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(registryfriendlybytebuf);
            boolean flag = registryfriendlybytebuf.readBoolean();

            return new ShapedRecipes(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf, ShapedRecipes shapedrecipes) {
            registryfriendlybytebuf.writeUtf(shapedrecipes.group);
            registryfriendlybytebuf.writeEnum(shapedrecipes.category);
            ShapedRecipePattern.STREAM_CODEC.encode(registryfriendlybytebuf, shapedrecipes.pattern);
            ItemStack.STREAM_CODEC.encode(registryfriendlybytebuf, shapedrecipes.result);
            registryfriendlybytebuf.writeBoolean(shapedrecipes.showNotification);
        }
    }
}
