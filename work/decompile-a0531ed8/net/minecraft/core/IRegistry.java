package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagDataPack;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface IRegistry<T> extends Keyable, HolderLookup.b<T>, Registry<T> {

    @Override
    ResourceKey<? extends IRegistry<T>> key();

    default Codec<T> byNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap(Holder.c::value, (object) -> {
            return this.safeCastToReference(this.wrapAsHolder(object));
        });
    }

    default Codec<Holder<T>> holderByNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap((holder_c) -> {
            return holder_c;
        }, this::safeCastToReference);
    }

    private Codec<Holder.c<T>> referenceHolderWithLifecycle() {
        Codec<Holder.c<T>> codec = MinecraftKey.CODEC.comapFlatMap((minecraftkey) -> {
            return (DataResult) this.get(minecraftkey).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    String s = String.valueOf(this.key());

                    return "Unknown registry key in " + s + ": " + String.valueOf(minecraftkey);
                });
            });
        }, (holder_c) -> {
            return holder_c.key().location();
        });

        return ExtraCodecs.overrideLifecycle(codec, (holder_c) -> {
            return (Lifecycle) this.registrationInfo(holder_c.key()).map(RegistrationInfo::lifecycle).orElse(Lifecycle.experimental());
        });
    }

    private DataResult<Holder.c<T>> safeCastToReference(Holder<T> holder) {
        DataResult dataresult;

        if (holder instanceof Holder.c<T> holder_c) {
            dataresult = DataResult.success(holder_c);
        } else {
            dataresult = DataResult.error(() -> {
                String s = String.valueOf(this.key());

                return "Unregistered holder in " + s + ": " + String.valueOf(holder);
            });
        }

        return dataresult;
    }

    default <U> Stream<U> keys(DynamicOps<U> dynamicops) {
        return this.keySet().stream().map((minecraftkey) -> {
            return dynamicops.createString(minecraftkey.toString());
        });
    }

    @Nullable
    MinecraftKey getKey(T t0);

    Optional<ResourceKey<T>> getResourceKey(T t0);

    @Override
    int getId(@Nullable T t0);

    @Nullable
    T getValue(@Nullable ResourceKey<T> resourcekey);

    @Nullable
    T getValue(@Nullable MinecraftKey minecraftkey);

    Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourcekey);

    default Optional<T> getOptional(@Nullable MinecraftKey minecraftkey) {
        return Optional.ofNullable(this.getValue(minecraftkey));
    }

    default Optional<T> getOptional(@Nullable ResourceKey<T> resourcekey) {
        return Optional.ofNullable(this.getValue(resourcekey));
    }

    Optional<Holder.c<T>> getAny();

    default T getValueOrThrow(ResourceKey<T> resourcekey) {
        T t0 = this.getValue(resourcekey);

        if (t0 == null) {
            String s = String.valueOf(this.key());

            throw new IllegalStateException("Missing key in " + s + ": " + String.valueOf(resourcekey));
        } else {
            return t0;
        }
    }

    Set<MinecraftKey> keySet();

    Set<Entry<ResourceKey<T>, T>> entrySet();

    Set<ResourceKey<T>> registryKeySet();

    Optional<Holder.c<T>> getRandom(RandomSource randomsource);

    default Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    boolean containsKey(MinecraftKey minecraftkey);

    boolean containsKey(ResourceKey<T> resourcekey);

    static <T> T register(IRegistry<? super T> iregistry, String s, T t0) {
        return register(iregistry, MinecraftKey.parse(s), t0);
    }

    static <V, T extends V> T register(IRegistry<V> iregistry, MinecraftKey minecraftkey, T t0) {
        return register(iregistry, ResourceKey.create(iregistry.key(), minecraftkey), t0);
    }

    static <V, T extends V> T register(IRegistry<V> iregistry, ResourceKey<V> resourcekey, T t0) {
        ((IRegistryWritable) iregistry).register(resourcekey, t0, RegistrationInfo.BUILT_IN);
        return t0;
    }

    static <T> Holder.c<T> registerForHolder(IRegistry<T> iregistry, ResourceKey<T> resourcekey, T t0) {
        return ((IRegistryWritable) iregistry).register(resourcekey, t0, RegistrationInfo.BUILT_IN);
    }

    static <T> Holder.c<T> registerForHolder(IRegistry<T> iregistry, MinecraftKey minecraftkey, T t0) {
        return registerForHolder(iregistry, ResourceKey.create(iregistry.key(), minecraftkey), t0);
    }

    IRegistry<T> freeze();

    Holder.c<T> createIntrusiveHolder(T t0);

    Optional<Holder.c<T>> get(int i);

    Optional<Holder.c<T>> get(MinecraftKey minecraftkey);

    Holder<T> wrapAsHolder(T t0);

    default Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagkey) {
        return (Iterable) DataFixUtils.orElse(this.get(tagkey), List.of());
    }

    default Optional<Holder<T>> getRandomElementOf(TagKey<T> tagkey, RandomSource randomsource) {
        return this.get(tagkey).flatMap((holderset_named) -> {
            return holderset_named.getRandomElement(randomsource);
        });
    }

    Stream<HolderSet.Named<T>> getTags();

    default Registry<Holder<T>> asHolderIdMap() {
        return new Registry<Holder<T>>() {
            public int getId(Holder<T> holder) {
                return IRegistry.this.getId(holder.value());
            }

            @Nullable
            @Override
            public Holder<T> byId(int i) {
                return (Holder) IRegistry.this.get(i).orElse((Object) null);
            }

            @Override
            public int size() {
                return IRegistry.this.size();
            }

            public Iterator<Holder<T>> iterator() {
                return IRegistry.this.listElements().map((holder_c) -> {
                    return holder_c;
                }).iterator();
            }
        };
    }

    IRegistry.a<T> prepareTagReload(TagDataPack.c<T> tagdatapack_c);

    public interface a<T> {

        ResourceKey<? extends IRegistry<? extends T>> key();

        HolderLookup.b<T> lookup();

        void apply();

        int size();
    }
}
