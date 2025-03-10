package net.minecraft.world.item.crafting;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RecipePropertySet {

    public static final ResourceKey<? extends IRegistry<RecipePropertySet>> TYPE_KEY = ResourceKey.createRegistryKey(MinecraftKey.withDefaultNamespace("recipe_property_set"));
    public static final ResourceKey<RecipePropertySet> SMITHING_BASE = registerVanilla("smithing_base");
    public static final ResourceKey<RecipePropertySet> SMITHING_TEMPLATE = registerVanilla("smithing_template");
    public static final ResourceKey<RecipePropertySet> SMITHING_ADDITION = registerVanilla("smithing_addition");
    public static final ResourceKey<RecipePropertySet> FURNACE_INPUT = registerVanilla("furnace_input");
    public static final ResourceKey<RecipePropertySet> BLAST_FURNACE_INPUT = registerVanilla("blast_furnace_input");
    public static final ResourceKey<RecipePropertySet> SMOKER_INPUT = registerVanilla("smoker_input");
    public static final ResourceKey<RecipePropertySet> CAMPFIRE_INPUT = registerVanilla("campfire_input");
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipePropertySet> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM).apply(ByteBufCodecs.list()).map((list) -> {
        return new RecipePropertySet(Set.copyOf(list));
    }, (recipepropertyset) -> {
        return List.copyOf(recipepropertyset.items);
    });
    public static final RecipePropertySet EMPTY = new RecipePropertySet(Set.of());
    private final Set<Holder<Item>> items;

    private RecipePropertySet(Set<Holder<Item>> set) {
        this.items = set;
    }

    private static ResourceKey<RecipePropertySet> registerVanilla(String s) {
        return ResourceKey.create(RecipePropertySet.TYPE_KEY, MinecraftKey.withDefaultNamespace(s));
    }

    public boolean test(ItemStack itemstack) {
        return this.items.contains(itemstack.getItemHolder());
    }

    static RecipePropertySet create(Collection<RecipeItemStack> collection) {
        Set<Holder<Item>> set = (Set) collection.stream().flatMap(RecipeItemStack::items).collect(Collectors.toUnmodifiableSet());

        return new RecipePropertySet(set);
    }
}
