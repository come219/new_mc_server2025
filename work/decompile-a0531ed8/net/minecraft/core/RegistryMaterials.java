package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagDataPack;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

public class RegistryMaterials<T> implements IRegistryWritable<T> {

    private final ResourceKey<? extends IRegistry<T>> key;
    private final ObjectList<Holder.c<T>> byId;
    private final Reference2IntMap<T> toId;
    private final Map<MinecraftKey, Holder.c<T>> byLocation;
    private final Map<ResourceKey<T>, Holder.c<T>> byKey;
    private final Map<T, Holder.c<T>> byValue;
    private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos;
    private Lifecycle registryLifecycle;
    private final Map<TagKey<T>, HolderSet.Named<T>> frozenTags;
    RegistryMaterials.a<T> allTags;
    private boolean frozen;
    @Nullable
    private Map<T, Holder.c<T>> unregisteredIntrusiveHolders;

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return this.getTags();
    }

    public RegistryMaterials(ResourceKey<? extends IRegistry<T>> resourcekey, Lifecycle lifecycle) {
        this(resourcekey, lifecycle, false);
    }

    public RegistryMaterials(ResourceKey<? extends IRegistry<T>> resourcekey, Lifecycle lifecycle, boolean flag) {
        this.byId = new ObjectArrayList(256);
        this.toId = (Reference2IntMap) SystemUtils.make(new Reference2IntOpenHashMap(), (reference2intopenhashmap) -> {
            reference2intopenhashmap.defaultReturnValue(-1);
        });
        this.byLocation = new HashMap();
        this.byKey = new HashMap();
        this.byValue = new IdentityHashMap();
        this.registrationInfos = new IdentityHashMap();
        this.frozenTags = new IdentityHashMap();
        this.allTags = RegistryMaterials.a.unbound();
        this.key = resourcekey;
        this.registryLifecycle = lifecycle;
        if (flag) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap();
        }

    }

    @Override
    public ResourceKey<? extends IRegistry<T>> key() {
        return this.key;
    }

    public String toString() {
        String s = String.valueOf(this.key);

        return "Registry[" + s + " (" + String.valueOf(this.registryLifecycle) + ")]";
    }

    private void validateWrite() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void validateWrite(ResourceKey<T> resourcekey) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + String.valueOf(resourcekey) + ")");
        }
    }

    @Override
    public Holder.c<T> register(ResourceKey<T> resourcekey, T t0, RegistrationInfo registrationinfo) {
        this.validateWrite(resourcekey);
        Objects.requireNonNull(resourcekey);
        Objects.requireNonNull(t0);
        if (this.byLocation.containsKey(resourcekey.location())) {
            throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException("Adding duplicate key '" + String.valueOf(resourcekey) + "' to registry"));
        } else if (this.byValue.containsKey(t0)) {
            throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException("Adding duplicate value '" + String.valueOf(t0) + "' to registry"));
        } else {
            Holder.c holder_c;

            if (this.unregisteredIntrusiveHolders != null) {
                holder_c = (Holder.c) this.unregisteredIntrusiveHolders.remove(t0);
                if (holder_c == null) {
                    String s = String.valueOf(resourcekey);

                    throw new AssertionError("Missing intrusive holder for " + s + ":" + String.valueOf(t0));
                }

                holder_c.bindKey(resourcekey);
            } else {
                holder_c = (Holder.c) this.byKey.computeIfAbsent(resourcekey, (resourcekey1) -> {
                    return Holder.c.createStandAlone(this, resourcekey1);
                });
            }

            this.byKey.put(resourcekey, holder_c);
            this.byLocation.put(resourcekey.location(), holder_c);
            this.byValue.put(t0, holder_c);
            int i = this.byId.size();

            this.byId.add(holder_c);
            this.toId.put(t0, i);
            this.registrationInfos.put(resourcekey, registrationinfo);
            this.registryLifecycle = this.registryLifecycle.add(registrationinfo.lifecycle());
            return holder_c;
        }
    }

    @Nullable
    @Override
    public MinecraftKey getKey(T t0) {
        Holder.c<T> holder_c = (Holder.c) this.byValue.get(t0);

        return holder_c != null ? holder_c.key().location() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T t0) {
        return Optional.ofNullable((Holder.c) this.byValue.get(t0)).map(Holder.c::key);
    }

    @Override
    public int getId(@Nullable T t0) {
        return this.toId.getInt(t0);
    }

    @Nullable
    @Override
    public T getValue(@Nullable ResourceKey<T> resourcekey) {
        return getValueFromNullable((Holder.c) this.byKey.get(resourcekey));
    }

    @Nullable
    @Override
    public T byId(int i) {
        return i >= 0 && i < this.byId.size() ? ((Holder.c) this.byId.get(i)).value() : null;
    }

    @Override
    public Optional<Holder.c<T>> get(int i) {
        return i >= 0 && i < this.byId.size() ? Optional.ofNullable((Holder.c) this.byId.get(i)) : Optional.empty();
    }

    @Override
    public Optional<Holder.c<T>> get(MinecraftKey minecraftkey) {
        return Optional.ofNullable((Holder.c) this.byLocation.get(minecraftkey));
    }

    @Override
    public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey) {
        return Optional.ofNullable((Holder.c) this.byKey.get(resourcekey));
    }

    @Override
    public Optional<Holder.c<T>> getAny() {
        return this.byId.isEmpty() ? Optional.empty() : Optional.of((Holder.c) this.byId.getFirst());
    }

    @Override
    public Holder<T> wrapAsHolder(T t0) {
        Holder.c<T> holder_c = (Holder.c) this.byValue.get(t0);

        return (Holder) (holder_c != null ? holder_c : Holder.direct(t0));
    }

    Holder.c<T> getOrCreateHolderOrThrow(ResourceKey<T> resourcekey) {
        return (Holder.c) this.byKey.computeIfAbsent(resourcekey, (resourcekey1) -> {
            if (this.unregisteredIntrusiveHolders != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            } else {
                this.validateWrite(resourcekey1);
                return Holder.c.createStandAlone(this, resourcekey1);
            }
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourcekey) {
        return Optional.ofNullable((RegistrationInfo) this.registrationInfos.get(resourcekey));
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.registryLifecycle;
    }

    public Iterator<T> iterator() {
        return Iterators.transform(this.byId.iterator(), Holder::value);
    }

    @Nullable
    @Override
    public T getValue(@Nullable MinecraftKey minecraftkey) {
        Holder.c<T> holder_c = (Holder.c) this.byLocation.get(minecraftkey);

        return getValueFromNullable(holder_c);
    }

    @Nullable
    private static <T> T getValueFromNullable(@Nullable Holder.c<T> holder_c) {
        return holder_c != null ? holder_c.value() : null;
    }

    @Override
    public Set<MinecraftKey> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Collections.unmodifiableSet(this.byKey.keySet());
    }

    @Override
    public Set<Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.c<T>> listElements() {
        return this.byId.stream();
    }

    @Override
    public Stream<HolderSet.Named<T>> getTags() {
        return this.allTags.getTags();
    }

    HolderSet.Named<T> getOrCreateTagForRegistration(TagKey<T> tagkey) {
        return (HolderSet.Named) this.frozenTags.computeIfAbsent(tagkey, this::createTag);
    }

    private HolderSet.Named<T> createTag(TagKey<T> tagkey) {
        return new HolderSet.Named<>(this, tagkey);
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder.c<T>> getRandom(RandomSource randomsource) {
        return SystemUtils.getRandomSafe(this.byId, randomsource);
    }

    @Override
    public boolean containsKey(MinecraftKey minecraftkey) {
        return this.byLocation.containsKey(minecraftkey);
    }

    @Override
    public boolean containsKey(ResourceKey<T> resourcekey) {
        return this.byKey.containsKey(resourcekey);
    }

    @Override
    public IRegistry<T> freeze() {
        if (this.frozen) {
            return this;
        } else {
            this.frozen = true;
            this.byValue.forEach((object, holder_c) -> {
                holder_c.bindValue(object);
            });
            List<MinecraftKey> list = this.byKey.entrySet().stream().filter((entry) -> {
                return !((Holder.c) entry.getValue()).isBound();
            }).map((entry) -> {
                return ((ResourceKey) entry.getKey()).location();
            }).sorted().toList();
            String s;

            if (!list.isEmpty()) {
                s = String.valueOf(this.key());
                throw new IllegalStateException("Unbound values in registry " + s + ": " + String.valueOf(list));
            } else {
                if (this.unregisteredIntrusiveHolders != null) {
                    if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                        throw new IllegalStateException("Some intrusive holders were not registered: " + String.valueOf(this.unregisteredIntrusiveHolders.values()));
                    }

                    this.unregisteredIntrusiveHolders = null;
                }

                if (this.allTags.isBound()) {
                    throw new IllegalStateException("Tags already present before freezing");
                } else {
                    List<MinecraftKey> list1 = this.frozenTags.entrySet().stream().filter((entry) -> {
                        return !((HolderSet.Named) entry.getValue()).isBound();
                    }).map((entry) -> {
                        return ((TagKey) entry.getKey()).location();
                    }).sorted().toList();

                    if (!list1.isEmpty()) {
                        s = String.valueOf(this.key());
                        throw new IllegalStateException("Unbound tags in registry " + s + ": " + String.valueOf(list1));
                    } else {
                        this.allTags = RegistryMaterials.a.fromMap(this.frozenTags);
                        this.refreshTagsInHolders();
                        return this;
                    }
                }
            }
        }
    }

    @Override
    public Holder.c<T> createIntrusiveHolder(T t0) {
        if (this.unregisteredIntrusiveHolders == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        } else {
            this.validateWrite();
            return (Holder.c) this.unregisteredIntrusiveHolders.computeIfAbsent(t0, (object) -> {
                return Holder.c.createIntrusive(this, object);
            });
        }
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
        return this.allTags.get(tagkey);
    }

    private Holder.c<T> validateAndUnwrapTagElement(TagKey<T> tagkey, Holder<T> holder) {
        String s;

        if (!holder.canSerializeIn(this)) {
            s = String.valueOf(tagkey);
            throw new IllegalStateException("Can't create named set " + s + " containing value " + String.valueOf(holder) + " from outside registry " + String.valueOf(this));
        } else if (holder instanceof Holder.c) {
            Holder.c<T> holder_c = (Holder.c) holder;

            return holder_c;
        } else {
            s = String.valueOf(holder);
            throw new IllegalStateException("Found direct holder " + s + " value in tag " + String.valueOf(tagkey));
        }
    }

    @Override
    public void bindTag(TagKey<T> tagkey, List<Holder<T>> list) {
        this.validateWrite();
        this.getOrCreateTagForRegistration(tagkey).bind(list);
    }

    void refreshTagsInHolders() {
        Map<Holder.c<T>, List<TagKey<T>>> map = new IdentityHashMap();

        this.byKey.values().forEach((holder_c) -> {
            map.put(holder_c, new ArrayList());
        });
        this.allTags.forEach((tagkey, holderset_named) -> {
            Iterator iterator = holderset_named.iterator();

            while (iterator.hasNext()) {
                Holder<T> holder = (Holder) iterator.next();
                Holder.c<T> holder_c = this.validateAndUnwrapTagElement(tagkey, holder);

                ((List) map.get(holder_c)).add(tagkey);
            }

        });
        map.forEach(Holder.c::bindTags);
    }

    public void bindAllTagsToEmpty() {
        this.validateWrite();
        this.frozenTags.values().forEach((holderset_named) -> {
            holderset_named.bind(List.of());
        });
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        this.validateWrite();
        return new HolderGetter<T>() {
            @Override
            public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey) {
                return Optional.of(this.getOrThrow(resourcekey));
            }

            @Override
            public Holder.c<T> getOrThrow(ResourceKey<T> resourcekey) {
                return RegistryMaterials.this.getOrCreateHolderOrThrow(resourcekey);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                return Optional.of(this.getOrThrow(tagkey));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> tagkey) {
                return RegistryMaterials.this.getOrCreateTagForRegistration(tagkey);
            }
        };
    }

    @Override
    public IRegistry.a<T> prepareTagReload(TagDataPack.c<T> tagdatapack_c) {
        if (!this.frozen) {
            throw new IllegalStateException("Invalid method used for tag loading");
        } else {
            Builder<TagKey<T>, HolderSet.Named<T>> builder = ImmutableMap.builder();
            final Map<TagKey<T>, List<Holder<T>>> map = new HashMap();

            tagdatapack_c.tags().forEach((tagkey, list) -> {
                HolderSet.Named<T> holderset_named = (HolderSet.Named) this.frozenTags.get(tagkey);

                if (holderset_named == null) {
                    holderset_named = this.createTag(tagkey);
                }

                builder.put(tagkey, holderset_named);
                map.put(tagkey, List.copyOf(list));
            });
            final ImmutableMap<TagKey<T>, HolderSet.Named<T>> immutablemap = builder.build();
            final HolderLookup.b<T> holderlookup_b = new HolderLookup.b.a<T>() {
                @Override
                public HolderLookup.b<T> parent() {
                    return RegistryMaterials.this;
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                    return Optional.ofNullable((HolderSet.Named) immutablemap.get(tagkey));
                }

                @Override
                public Stream<HolderSet.Named<T>> listTags() {
                    return immutablemap.values().stream();
                }
            };

            return new IRegistry.a<T>() {
                @Override
                public ResourceKey<? extends IRegistry<? extends T>> key() {
                    return RegistryMaterials.this.key();
                }

                @Override
                public int size() {
                    return map.size();
                }

                @Override
                public HolderLookup.b<T> lookup() {
                    return holderlookup_b;
                }

                @Override
                public void apply() {
                    immutablemap.forEach((tagkey, holderset_named) -> {
                        List<Holder<T>> list = (List) map.getOrDefault(tagkey, List.of());

                        holderset_named.bind(list);
                    });
                    RegistryMaterials.this.allTags = RegistryMaterials.a.fromMap(immutablemap);
                    RegistryMaterials.this.refreshTagsInHolders();
                }
            };
        }
    }

    private interface a<T> {

        static <T> RegistryMaterials.a<T> unbound() {
            return new RegistryMaterials.a<T>() {
                @Override
                public boolean isBound() {
                    return false;
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                    throw new IllegalStateException("Tags not bound, trying to access " + String.valueOf(tagkey));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biconsumer) {
                    throw new IllegalStateException("Tags not bound");
                }

                @Override
                public Stream<HolderSet.Named<T>> getTags() {
                    throw new IllegalStateException("Tags not bound");
                }
            };
        }

        static <T> RegistryMaterials.a<T> fromMap(final Map<TagKey<T>, HolderSet.Named<T>> map) {
            return new RegistryMaterials.a<T>() {
                @Override
                public boolean isBound() {
                    return true;
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                    return Optional.ofNullable((HolderSet.Named) map.get(tagkey));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biconsumer) {
                    map.forEach(biconsumer);
                }

                @Override
                public Stream<HolderSet.Named<T>> getTags() {
                    return map.values().stream();
                }
            };
        }

        boolean isBound();

        Optional<HolderSet.Named<T>> get(TagKey<T> tagkey);

        void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biconsumer);

        Stream<HolderSet.Named<T>> getTags();
    }
}
