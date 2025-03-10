package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;

// CraftBukkit start
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
// CraftBukkit end

public class RecipeMap {

    // CraftBukkit start - ordered
    public static final RecipeMap EMPTY = new RecipeMap(ImmutableMultimap.of(), Maps.newLinkedHashMap());
    public final Multimap<Recipes<?>, RecipeHolder<?>> byType;
    private final LinkedHashMap<ResourceKey<IRecipe<?>>, RecipeHolder<?>> byKey;

    private RecipeMap(Multimap<Recipes<?>, RecipeHolder<?>> multimap, LinkedHashMap<ResourceKey<IRecipe<?>>, RecipeHolder<?>> map) {
        // CraftBukkit end
        this.byType = multimap;
        this.byKey = map;
    }

    public static RecipeMap create(Iterable<RecipeHolder<?>> iterable) {
        Builder<Recipes<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
        com.google.common.collect.ImmutableMap.Builder<ResourceKey<IRecipe<?>>, RecipeHolder<?>> com_google_common_collect_immutablemap_builder = ImmutableMap.builder();
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            RecipeHolder<?> recipeholder = (RecipeHolder) iterator.next();

            builder.put(recipeholder.value().getType(), recipeholder);
            com_google_common_collect_immutablemap_builder.put(recipeholder.id(), recipeholder);
        }

        // CraftBukkit start - mutable, ordered
        return new RecipeMap(LinkedHashMultimap.create(builder.build()), Maps.newLinkedHashMap(com_google_common_collect_immutablemap_builder.build()));
    }

    public void addRecipe(RecipeHolder<?> irecipe) {
        Collection<RecipeHolder<?>> map = this.byType.get(irecipe.value().getType());

        if (byKey.containsKey(irecipe.id())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + irecipe.id());
        } else {
            map.add(irecipe);
            byKey.putFirst(irecipe.id(), irecipe); // CraftBukkit - ordered
        }
    }

    public boolean removeRecipe(ResourceKey<IRecipe<?>> mcKey) {
        boolean removed = false;
        Iterator<RecipeHolder<?>> iter = byType.values().iterator();
        while (iter.hasNext()) {
            RecipeHolder<?> recipe = iter.next();
            if (recipe.id().equals(mcKey)) {
                iter.remove();
                removed = true;
            }
        }
        removed |= byKey.remove(mcKey) != null;

        return removed;
    }
    // CraftBukkit end

    public <I extends RecipeInput, T extends IRecipe<I>> Collection<RecipeHolder<T>> byType(Recipes<T> recipes) {
        return (Collection) this.byType.get(recipes); // CraftBukkit - decompile error
    }

    public Collection<RecipeHolder<?>> values() {
        return this.byKey.values();
    }

    @Nullable
    public RecipeHolder<?> byKey(ResourceKey<IRecipe<?>> resourcekey) {
        return (RecipeHolder) this.byKey.get(resourcekey);
    }

    public <I extends RecipeInput, T extends IRecipe<I>> Stream<RecipeHolder<T>> getRecipesFor(Recipes<T> recipes, I i0, World world) {
        return i0.isEmpty() ? Stream.empty() : this.byType(recipes).stream().filter((recipeholder) -> {
            return recipeholder.value().matches(i0, world);
        });
    }
}
