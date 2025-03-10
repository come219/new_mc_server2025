package net.minecraft.tags;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.IResource;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.util.DependencySorter;
import org.slf4j.Logger;

public class TagDataPack<T> {

    private static final Logger LOGGER = LogUtils.getLogger();
    final TagDataPack.a<T> elementLookup;
    private final String directory;

    public TagDataPack(TagDataPack.a<T> tagdatapack_a, String s) {
        this.elementLookup = tagdatapack_a;
        this.directory = s;
    }

    public Map<MinecraftKey, List<TagDataPack.b>> load(IResourceManager iresourcemanager) {
        Map<MinecraftKey, List<TagDataPack.b>> map = new HashMap();
        FileToIdConverter filetoidconverter = FileToIdConverter.json(this.directory);
        Iterator iterator = filetoidconverter.listMatchingResourceStacks(iresourcemanager).entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, List<IResource>> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();
            MinecraftKey minecraftkey1 = filetoidconverter.fileToId(minecraftkey);
            Iterator iterator1 = ((List) entry.getValue()).iterator();

            while (iterator1.hasNext()) {
                IResource iresource = (IResource) iterator1.next();

                try {
                    BufferedReader bufferedreader = iresource.openAsReader();

                    try {
                        JsonElement jsonelement = JsonParser.parseReader(bufferedreader);
                        List<TagDataPack.b> list = (List) map.computeIfAbsent(minecraftkey1, (minecraftkey2) -> {
                            return new ArrayList();
                        });
                        TagFile tagfile = (TagFile) TagFile.CODEC.parse(new Dynamic(JsonOps.INSTANCE, jsonelement)).getOrThrow();

                        if (tagfile.replace()) {
                            list.clear();
                        }

                        String s = iresource.sourcePackId();

                        tagfile.entries().forEach((tagentry) -> {
                            list.add(new TagDataPack.b(tagentry, s));
                        });
                    } catch (Throwable throwable) {
                        if (bufferedreader != null) {
                            try {
                                bufferedreader.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        }

                        throw throwable;
                    }

                    if (bufferedreader != null) {
                        bufferedreader.close();
                    }
                } catch (Exception exception) {
                    TagDataPack.LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{minecraftkey1, minecraftkey, iresource.sourcePackId(), exception});
                }
            }
        }

        return map;
    }

    private Either<List<TagDataPack.b>, List<T>> tryBuildTag(TagEntry.a<T> tagentry_a, List<TagDataPack.b> list) {
        SequencedSet<T> sequencedset = new LinkedHashSet();
        List<TagDataPack.b> list1 = new ArrayList();
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            TagDataPack.b tagdatapack_b = (TagDataPack.b) iterator.next();
            TagEntry tagentry = tagdatapack_b.entry();

            Objects.requireNonNull(sequencedset);
            if (!tagentry.build(tagentry_a, sequencedset::add)) {
                list1.add(tagdatapack_b);
            }
        }

        return list1.isEmpty() ? Either.right(List.copyOf(sequencedset)) : Either.left(list1);
    }

    public Map<MinecraftKey, List<T>> build(Map<MinecraftKey, List<TagDataPack.b>> map) {
        final Map<MinecraftKey, List<T>> map1 = new HashMap();
        TagEntry.a<T> tagentry_a = new TagEntry.a<T>() {
            @Nullable
            @Override
            public T element(MinecraftKey minecraftkey, boolean flag) {
                return TagDataPack.this.elementLookup.get(minecraftkey, flag).orElse((Object) null);
            }

            @Nullable
            @Override
            public Collection<T> tag(MinecraftKey minecraftkey) {
                return (Collection) map1.get(minecraftkey);
            }
        };
        DependencySorter<MinecraftKey, TagDataPack.d> dependencysorter = new DependencySorter<>();

        map.forEach((minecraftkey, list) -> {
            dependencysorter.addEntry(minecraftkey, new TagDataPack.d(list));
        });
        dependencysorter.orderByDependencies((minecraftkey, tagdatapack_d) -> {
            this.tryBuildTag(tagentry_a, tagdatapack_d.entries).ifLeft((list) -> {
                TagDataPack.LOGGER.error("Couldn't load tag {} as it is missing following references: {}", minecraftkey, list.stream().map(Objects::toString).collect(Collectors.joining(", ")));
            }).ifRight((list) -> {
                map1.put(minecraftkey, list);
            });
        });
        return map1;
    }

    public static <T> void loadTagsFromNetwork(TagNetworkSerialization.a tagnetworkserialization_a, IRegistryWritable<T> iregistrywritable) {
        Map map = tagnetworkserialization_a.resolve(iregistrywritable).tags;

        Objects.requireNonNull(iregistrywritable);
        map.forEach(iregistrywritable::bindTag);
    }

    public static List<IRegistry.a<?>> loadTagsForExistingRegistries(IResourceManager iresourcemanager, IRegistryCustom iregistrycustom) {
        return (List) iregistrycustom.registries().map((iregistrycustom_d) -> {
            return loadPendingTags(iresourcemanager, iregistrycustom_d.value());
        }).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
    }

    public static <T> void loadTagsForRegistry(IResourceManager iresourcemanager, IRegistryWritable<T> iregistrywritable) {
        ResourceKey<? extends IRegistry<T>> resourcekey = iregistrywritable.key();
        TagDataPack<Holder<T>> tagdatapack = new TagDataPack<>(TagDataPack.a.fromWritableRegistry(iregistrywritable), Registries.tagsDirPath(resourcekey));

        tagdatapack.build(tagdatapack.load(iresourcemanager)).forEach((minecraftkey, list) -> {
            iregistrywritable.bindTag(TagKey.create(resourcekey, minecraftkey), list);
        });
    }

    private static <T> Map<TagKey<T>, List<Holder<T>>> wrapTags(ResourceKey<? extends IRegistry<T>> resourcekey, Map<MinecraftKey, List<Holder<T>>> map) {
        return (Map) map.entrySet().stream().collect(Collectors.toUnmodifiableMap((entry) -> {
            return TagKey.create(resourcekey, (MinecraftKey) entry.getKey());
        }, Entry::getValue));
    }

    private static <T> Optional<IRegistry.a<T>> loadPendingTags(IResourceManager iresourcemanager, IRegistry<T> iregistry) {
        ResourceKey<? extends IRegistry<T>> resourcekey = iregistry.key();
        TagDataPack<Holder<T>> tagdatapack = new TagDataPack<>(TagDataPack.a.fromFrozenRegistry(iregistry), Registries.tagsDirPath(resourcekey));
        TagDataPack.c<T> tagdatapack_c = new TagDataPack.c<>(resourcekey, wrapTags(iregistry.key(), tagdatapack.build(tagdatapack.load(iresourcemanager))));

        return tagdatapack_c.tags().isEmpty() ? Optional.empty() : Optional.of(iregistry.prepareTagReload(tagdatapack_c));
    }

    public static List<HolderLookup.b<?>> buildUpdatedLookups(IRegistryCustom.Dimension iregistrycustom_dimension, List<IRegistry.a<?>> list) {
        List<HolderLookup.b<?>> list1 = new ArrayList();

        iregistrycustom_dimension.registries().forEach((iregistrycustom_d) -> {
            IRegistry.a<?> iregistry_a = findTagsForRegistry(list, iregistrycustom_d.key());

            list1.add(iregistry_a != null ? iregistry_a.lookup() : iregistrycustom_d.value());
        });
        return list1;
    }

    @Nullable
    private static IRegistry.a<?> findTagsForRegistry(List<IRegistry.a<?>> list, ResourceKey<? extends IRegistry<?>> resourcekey) {
        Iterator iterator = list.iterator();

        IRegistry.a iregistry_a;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            iregistry_a = (IRegistry.a) iterator.next();
        } while (iregistry_a.key() != resourcekey);

        return iregistry_a;
    }

    public interface a<T> {

        Optional<? extends T> get(MinecraftKey minecraftkey, boolean flag);

        static <T> TagDataPack.a<? extends Holder<T>> fromFrozenRegistry(IRegistry<T> iregistry) {
            return (minecraftkey, flag) -> {
                return iregistry.get(minecraftkey);
            };
        }

        static <T> TagDataPack.a<Holder<T>> fromWritableRegistry(IRegistryWritable<T> iregistrywritable) {
            HolderGetter<T> holdergetter = iregistrywritable.createRegistrationLookup();

            return (minecraftkey, flag) -> {
                return ((HolderGetter) (flag ? holdergetter : iregistrywritable)).get(ResourceKey.create(iregistrywritable.key(), minecraftkey));
            };
        }
    }

    public static record b(TagEntry entry, String source) {

        public String toString() {
            String s = String.valueOf(this.entry);

            return s + " (from " + this.source + ")";
        }
    }

    public static record c<T>(ResourceKey<? extends IRegistry<T>> key, Map<TagKey<T>, List<Holder<T>>> tags) {

    }

    private static record d(List<TagDataPack.b> entries) implements DependencySorter.a<MinecraftKey> {

        @Override
        public void visitRequiredDependencies(Consumer<MinecraftKey> consumer) {
            this.entries.forEach((tagdatapack_b) -> {
                tagdatapack_b.entry.visitRequiredDependencies(consumer);
            });
        }

        @Override
        public void visitOptionalDependencies(Consumer<MinecraftKey> consumer) {
            this.entries.forEach((tagdatapack_b) -> {
                tagdatapack_b.entry.visitOptionalDependencies(consumer);
            });
        }
    }
}
