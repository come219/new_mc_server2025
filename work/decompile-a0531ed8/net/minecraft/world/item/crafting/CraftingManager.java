package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceDataAbstract;
import net.minecraft.server.packs.resources.ResourceDataJson;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.World;
import org.slf4j.Logger;

public class CraftingManager extends ResourceDataAbstract<RecipeMap> implements RecipeAccess {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceKey<RecipePropertySet>, CraftingManager.c> RECIPE_PROPERTY_SETS = Map.of(RecipePropertySet.SMITHING_ADDITION, (irecipe) -> {
        Optional optional;

        if (irecipe instanceof SmithingRecipe smithingrecipe) {
            optional = smithingrecipe.additionIngredient();
        } else {
            optional = Optional.empty();
        }

        return optional;
    }, RecipePropertySet.SMITHING_BASE, (irecipe) -> {
        Optional optional;

        if (irecipe instanceof SmithingRecipe smithingrecipe) {
            optional = smithingrecipe.baseIngredient();
        } else {
            optional = Optional.empty();
        }

        return optional;
    }, RecipePropertySet.SMITHING_TEMPLATE, (irecipe) -> {
        Optional optional;

        if (irecipe instanceof SmithingRecipe smithingrecipe) {
            optional = smithingrecipe.templateIngredient();
        } else {
            optional = Optional.empty();
        }

        return optional;
    }, RecipePropertySet.FURNACE_INPUT, forSingleInput(Recipes.SMELTING), RecipePropertySet.BLAST_FURNACE_INPUT, forSingleInput(Recipes.BLASTING), RecipePropertySet.SMOKER_INPUT, forSingleInput(Recipes.SMOKING), RecipePropertySet.CAMPFIRE_INPUT, forSingleInput(Recipes.CAMPFIRE_COOKING));
    private static final FileToIdConverter RECIPE_LISTER = FileToIdConverter.registry(Registries.RECIPE);
    private final HolderLookup.a registries;
    public RecipeMap recipes;
    private Map<ResourceKey<RecipePropertySet>, RecipePropertySet> propertySets;
    private SelectableRecipe.b<RecipeStonecutting> stonecutterRecipes;
    private List<CraftingManager.d> allDisplays;
    private Map<ResourceKey<IRecipe<?>>, List<CraftingManager.d>> recipeToDisplay;

    public CraftingManager(HolderLookup.a holderlookup_a) {
        this.recipes = RecipeMap.EMPTY;
        this.propertySets = Map.of();
        this.stonecutterRecipes = SelectableRecipe.b.empty();
        this.allDisplays = List.of();
        this.recipeToDisplay = Map.of();
        this.registries = holderlookup_a;
    }

    @Override
    protected RecipeMap prepare(IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        SortedMap<MinecraftKey, IRecipe<?>> sortedmap = new TreeMap();

        ResourceDataJson.scanDirectory(iresourcemanager, CraftingManager.RECIPE_LISTER, this.registries.createSerializationContext(JsonOps.INSTANCE), IRecipe.CODEC, sortedmap);
        List<RecipeHolder<?>> list = new ArrayList(sortedmap.size());

        sortedmap.forEach((minecraftkey, irecipe) -> {
            ResourceKey<IRecipe<?>> resourcekey = ResourceKey.create(Registries.RECIPE, minecraftkey);
            RecipeHolder<?> recipeholder = new RecipeHolder<>(resourcekey, irecipe);

            list.add(recipeholder);
        });
        return RecipeMap.create(list);
    }

    protected void apply(RecipeMap recipemap, IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        this.recipes = recipemap;
        CraftingManager.LOGGER.info("Loaded {} recipes", recipemap.values().size());
    }

    public void finalizeRecipeLoading(FeatureFlagSet featureflagset) {
        List<SelectableRecipe.a<RecipeStonecutting>> list = new ArrayList();
        List<CraftingManager.b> list1 = CraftingManager.RECIPE_PROPERTY_SETS.entrySet().stream().map((entry) -> {
            return new CraftingManager.b((ResourceKey) entry.getKey(), (CraftingManager.c) entry.getValue());
        }).toList();

        this.recipes.values().forEach((recipeholder) -> {
            IRecipe<?> irecipe = recipeholder.value();

            if (!irecipe.isSpecial() && irecipe.placementInfo().isImpossibleToPlace()) {
                CraftingManager.LOGGER.warn("Recipe {} can't be placed due to empty ingredients and will be ignored", recipeholder.id().location());
            } else {
                list1.forEach((craftingmanager_b) -> {
                    craftingmanager_b.accept(irecipe);
                });
                if (irecipe instanceof RecipeStonecutting) {
                    RecipeStonecutting recipestonecutting = (RecipeStonecutting) irecipe;

                    if (isIngredientEnabled(featureflagset, recipestonecutting.input()) && recipestonecutting.resultDisplay().isEnabled(featureflagset)) {
                        list.add(new SelectableRecipe.a<>(recipestonecutting.input(), new SelectableRecipe<>(recipestonecutting.resultDisplay(), Optional.of(recipeholder))));
                    }
                }

            }
        });
        this.propertySets = (Map) list1.stream().collect(Collectors.toUnmodifiableMap((craftingmanager_b) -> {
            return craftingmanager_b.key;
        }, (craftingmanager_b) -> {
            return craftingmanager_b.asPropertySet(featureflagset);
        }));
        this.stonecutterRecipes = new SelectableRecipe.b<>(list);
        this.allDisplays = unpackRecipeInfo(this.recipes.values(), featureflagset);
        this.recipeToDisplay = (Map) this.allDisplays.stream().collect(Collectors.groupingBy((craftingmanager_d) -> {
            return craftingmanager_d.parent.id();
        }, IdentityHashMap::new, Collectors.toList()));
    }

    static List<RecipeItemStack> filterDisabled(FeatureFlagSet featureflagset, List<RecipeItemStack> list) {
        list.removeIf((recipeitemstack) -> {
            return !isIngredientEnabled(featureflagset, recipeitemstack);
        });
        return list;
    }

    private static boolean isIngredientEnabled(FeatureFlagSet featureflagset, RecipeItemStack recipeitemstack) {
        return recipeitemstack.items().allMatch((holder) -> {
            return ((Item) holder.value()).isEnabled(featureflagset);
        });
    }

    public <I extends RecipeInput, T extends IRecipe<I>> Optional<RecipeHolder<T>> getRecipeFor(Recipes<T> recipes, I i0, World world, @Nullable ResourceKey<IRecipe<?>> resourcekey) {
        RecipeHolder<T> recipeholder = resourcekey != null ? this.byKeyTyped(recipes, resourcekey) : null;

        return this.getRecipeFor(recipes, i0, world, recipeholder);
    }

    public <I extends RecipeInput, T extends IRecipe<I>> Optional<RecipeHolder<T>> getRecipeFor(Recipes<T> recipes, I i0, World world, @Nullable RecipeHolder<T> recipeholder) {
        return recipeholder != null && recipeholder.value().matches(i0, world) ? Optional.of(recipeholder) : this.getRecipeFor(recipes, i0, world);
    }

    public <I extends RecipeInput, T extends IRecipe<I>> Optional<RecipeHolder<T>> getRecipeFor(Recipes<T> recipes, I i0, World world) {
        return this.recipes.getRecipesFor(recipes, i0, world).findFirst();
    }

    public Optional<RecipeHolder<?>> byKey(ResourceKey<IRecipe<?>> resourcekey) {
        return Optional.ofNullable(this.recipes.byKey(resourcekey));
    }

    @Nullable
    private <T extends IRecipe<?>> RecipeHolder<T> byKeyTyped(Recipes<T> recipes, ResourceKey<IRecipe<?>> resourcekey) {
        RecipeHolder<?> recipeholder = this.recipes.byKey(resourcekey);

        return recipeholder != null && recipeholder.value().getType().equals(recipes) ? recipeholder : null;
    }

    public Map<ResourceKey<RecipePropertySet>, RecipePropertySet> getSynchronizedItemProperties() {
        return this.propertySets;
    }

    public SelectableRecipe.b<RecipeStonecutting> getSynchronizedStonecutterRecipes() {
        return this.stonecutterRecipes;
    }

    @Override
    public RecipePropertySet propertySet(ResourceKey<RecipePropertySet> resourcekey) {
        return (RecipePropertySet) this.propertySets.getOrDefault(resourcekey, RecipePropertySet.EMPTY);
    }

    @Override
    public SelectableRecipe.b<RecipeStonecutting> stonecutterRecipes() {
        return this.stonecutterRecipes;
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        return this.recipes.values();
    }

    @Nullable
    public CraftingManager.d getRecipeFromDisplay(RecipeDisplayId recipedisplayid) {
        return (CraftingManager.d) this.allDisplays.get(recipedisplayid.index());
    }

    public void listDisplaysForRecipe(ResourceKey<IRecipe<?>> resourcekey, Consumer<RecipeDisplayEntry> consumer) {
        List<CraftingManager.d> list = (List) this.recipeToDisplay.get(resourcekey);

        if (list != null) {
            list.forEach((craftingmanager_d) -> {
                consumer.accept(craftingmanager_d.display);
            });
        }

    }

    @VisibleForTesting
    protected static RecipeHolder<?> fromJson(ResourceKey<IRecipe<?>> resourcekey, JsonObject jsonobject, HolderLookup.a holderlookup_a) {
        IRecipe<?> irecipe = (IRecipe) IRecipe.CODEC.parse(holderlookup_a.createSerializationContext(JsonOps.INSTANCE), jsonobject).getOrThrow(JsonParseException::new);

        return new RecipeHolder<>(resourcekey, irecipe);
    }

    public static <I extends RecipeInput, T extends IRecipe<I>> CraftingManager.a<I, T> createCheck(final Recipes<T> recipes) {
        return new CraftingManager.a<I, T>() {
            @Nullable
            private ResourceKey<IRecipe<?>> lastRecipe;

            @Override
            public Optional<RecipeHolder<T>> getRecipeFor(I i0, WorldServer worldserver) {
                CraftingManager craftingmanager = worldserver.recipeAccess();
                Optional<RecipeHolder<T>> optional = craftingmanager.getRecipeFor(recipes, i0, worldserver, this.lastRecipe);

                if (optional.isPresent()) {
                    RecipeHolder<T> recipeholder = (RecipeHolder) optional.get();

                    this.lastRecipe = recipeholder.id();
                    return Optional.of(recipeholder);
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    private static List<CraftingManager.d> unpackRecipeInfo(Iterable<RecipeHolder<?>> iterable, FeatureFlagSet featureflagset) {
        List<CraftingManager.d> list = new ArrayList();
        Object2IntMap<String> object2intmap = new Object2IntOpenHashMap();
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            RecipeHolder<?> recipeholder = (RecipeHolder) iterator.next();
            IRecipe<?> irecipe = recipeholder.value();
            OptionalInt optionalint;

            if (irecipe.group().isEmpty()) {
                optionalint = OptionalInt.empty();
            } else {
                optionalint = OptionalInt.of(object2intmap.computeIfAbsent(irecipe.group(), (object) -> {
                    return object2intmap.size();
                }));
            }

            Optional optional;

            if (irecipe.isSpecial()) {
                optional = Optional.empty();
            } else {
                optional = Optional.of(irecipe.placementInfo().ingredients());
            }

            Iterator iterator1 = irecipe.display().iterator();

            while (iterator1.hasNext()) {
                RecipeDisplay recipedisplay = (RecipeDisplay) iterator1.next();

                if (recipedisplay.isEnabled(featureflagset)) {
                    int i = list.size();
                    RecipeDisplayId recipedisplayid = new RecipeDisplayId(i);
                    RecipeDisplayEntry recipedisplayentry = new RecipeDisplayEntry(recipedisplayid, recipedisplay, optionalint, irecipe.recipeBookCategory(), optional);

                    list.add(new CraftingManager.d(recipedisplayentry, recipeholder));
                }
            }
        }

        return list;
    }

    private static CraftingManager.c forSingleInput(Recipes<? extends RecipeSingleItem> recipes) {
        return (irecipe) -> {
            Optional optional;

            if (irecipe.getType() == recipes && irecipe instanceof RecipeSingleItem recipesingleitem) {
                optional = Optional.of(recipesingleitem.input());
            } else {
                optional = Optional.empty();
            }

            return optional;
        };
    }

    public static record d(RecipeDisplayEntry display, RecipeHolder<?> parent) {

    }

    @FunctionalInterface
    public interface c {

        Optional<RecipeItemStack> apply(IRecipe<?> irecipe);
    }

    public static class b implements Consumer<IRecipe<?>> {

        final ResourceKey<RecipePropertySet> key;
        private final CraftingManager.c extractor;
        private final List<RecipeItemStack> ingredients = new ArrayList();

        protected b(ResourceKey<RecipePropertySet> resourcekey, CraftingManager.c craftingmanager_c) {
            this.key = resourcekey;
            this.extractor = craftingmanager_c;
        }

        public void accept(IRecipe<?> irecipe) {
            Optional optional = this.extractor.apply(irecipe);
            List list = this.ingredients;

            Objects.requireNonNull(this.ingredients);
            optional.ifPresent(list::add);
        }

        public RecipePropertySet asPropertySet(FeatureFlagSet featureflagset) {
            return RecipePropertySet.create(CraftingManager.filterDisabled(featureflagset, this.ingredients));
        }
    }

    public interface a<I extends RecipeInput, T extends IRecipe<I>> {

        Optional<RecipeHolder<T>> getRecipeFor(I i0, WorldServer worldserver);
    }
}
