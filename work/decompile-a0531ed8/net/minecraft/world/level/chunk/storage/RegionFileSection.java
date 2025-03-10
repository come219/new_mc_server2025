package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import org.slf4j.Logger;

public class RegionFileSection<R, P> implements AutoCloseable {

    static final Logger LOGGER = LogUtils.getLogger();
    private static final String SECTIONS_TAG = "Sections";
    private final SimpleRegionStorage simpleRegionStorage;
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap();
    private final LongLinkedOpenHashSet dirtyChunks = new LongLinkedOpenHashSet();
    private final Codec<P> codec;
    private final Function<R, P> packer;
    private final BiFunction<P, Runnable, R> unpacker;
    private final Function<Runnable, R> factory;
    private final IRegistryCustom registryAccess;
    private final ChunkIOErrorReporter errorReporter;
    protected final LevelHeightAccessor levelHeightAccessor;
    private final LongSet loadedChunks = new LongOpenHashSet();
    private final Long2ObjectMap<CompletableFuture<Optional<RegionFileSection.a<P>>>> pendingLoads = new Long2ObjectOpenHashMap();
    private final Object loadLock = new Object();

    public RegionFileSection(SimpleRegionStorage simpleregionstorage, Codec<P> codec, Function<R, P> function, BiFunction<P, Runnable, R> bifunction, Function<Runnable, R> function1, IRegistryCustom iregistrycustom, ChunkIOErrorReporter chunkioerrorreporter, LevelHeightAccessor levelheightaccessor) {
        this.simpleRegionStorage = simpleregionstorage;
        this.codec = codec;
        this.packer = function;
        this.unpacker = bifunction;
        this.factory = function1;
        this.registryAccess = iregistrycustom;
        this.errorReporter = chunkioerrorreporter;
        this.levelHeightAccessor = levelheightaccessor;
    }

    protected void tick(BooleanSupplier booleansupplier) {
        LongListIterator longlistiterator = this.dirtyChunks.iterator();

        while (longlistiterator.hasNext() && booleansupplier.getAsBoolean()) {
            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(longlistiterator.nextLong());

            longlistiterator.remove();
            this.writeChunk(chunkcoordintpair);
        }

        this.unpackPendingLoads();
    }

    private void unpackPendingLoads() {
        Object object = this.loadLock;

        synchronized (this.loadLock) {
            Iterator<Entry<CompletableFuture<Optional<RegionFileSection.a<P>>>>> iterator = Long2ObjectMaps.fastIterator(this.pendingLoads);

            while (iterator.hasNext()) {
                Entry<CompletableFuture<Optional<RegionFileSection.a<P>>>> entry = (Entry) iterator.next();
                Optional<RegionFileSection.a<P>> optional = (Optional) ((CompletableFuture) entry.getValue()).getNow((Object) null);

                if (optional != null) {
                    long i = entry.getLongKey();

                    this.unpackChunk(new ChunkCoordIntPair(i), (RegionFileSection.a) optional.orElse((Object) null));
                    iterator.remove();
                    this.loadedChunks.add(i);
                }
            }

        }
    }

    public void flushAll() {
        if (!this.dirtyChunks.isEmpty()) {
            this.dirtyChunks.forEach((i) -> {
                this.writeChunk(new ChunkCoordIntPair(i));
            });
            this.dirtyChunks.clear();
        }

    }

    public boolean hasWork() {
        return !this.dirtyChunks.isEmpty();
    }

    @Nullable
    protected Optional<R> get(long i) {
        return (Optional) this.storage.get(i);
    }

    protected Optional<R> getOrLoad(long i) {
        if (this.outsideStoredRange(i)) {
            return Optional.empty();
        } else {
            Optional<R> optional = this.get(i);

            if (optional != null) {
                return optional;
            } else {
                this.unpackChunk(SectionPosition.of(i).chunk());
                optional = this.get(i);
                if (optional == null) {
                    throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException());
                } else {
                    return optional;
                }
            }
        }
    }

    protected boolean outsideStoredRange(long i) {
        int j = SectionPosition.sectionToBlockCoord(SectionPosition.y(i));

        return this.levelHeightAccessor.isOutsideBuildHeight(j);
    }

    protected R getOrCreate(long i) {
        if (this.outsideStoredRange(i)) {
            throw (IllegalArgumentException) SystemUtils.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
        } else {
            Optional<R> optional = this.getOrLoad(i);

            if (optional.isPresent()) {
                return optional.get();
            } else {
                R r0 = this.factory.apply(() -> {
                    this.setDirty(i);
                });

                this.storage.put(i, Optional.of(r0));
                return r0;
            }
        }
    }

    public CompletableFuture<?> prefetch(ChunkCoordIntPair chunkcoordintpair) {
        Object object = this.loadLock;

        synchronized (this.loadLock) {
            long i = chunkcoordintpair.toLong();

            return this.loadedChunks.contains(i) ? CompletableFuture.completedFuture((Object) null) : (CompletableFuture) this.pendingLoads.computeIfAbsent(i, (j) -> {
                return this.tryRead(chunkcoordintpair);
            });
        }
    }

    private void unpackChunk(ChunkCoordIntPair chunkcoordintpair) {
        long i = chunkcoordintpair.toLong();
        Object object = this.loadLock;
        CompletableFuture completablefuture;

        synchronized (this.loadLock) {
            if (!this.loadedChunks.add(i)) {
                return;
            }

            completablefuture = (CompletableFuture) this.pendingLoads.computeIfAbsent(i, (j) -> {
                return this.tryRead(chunkcoordintpair);
            });
        }

        this.unpackChunk(chunkcoordintpair, (RegionFileSection.a) ((Optional) completablefuture.join()).orElse((Object) null));
        object = this.loadLock;
        synchronized (this.loadLock) {
            this.pendingLoads.remove(i);
        }
    }

    private CompletableFuture<Optional<RegionFileSection.a<P>>> tryRead(ChunkCoordIntPair chunkcoordintpair) {
        RegistryOps<NBTBase> registryops = this.registryAccess.createSerializationContext(DynamicOpsNBT.INSTANCE);

        return this.simpleRegionStorage.read(chunkcoordintpair).thenApplyAsync((optional) -> {
            return optional.map((nbttagcompound) -> {
                return RegionFileSection.a.parse(this.codec, registryops, nbttagcompound, this.simpleRegionStorage, this.levelHeightAccessor);
            });
        }, SystemUtils.backgroundExecutor().forName("parseSection")).exceptionally((throwable) -> {
            if (throwable instanceof CompletionException) {
                throwable = throwable.getCause();
            }

            if (throwable instanceof IOException ioexception) {
                RegionFileSection.LOGGER.error("Error reading chunk {} data from disk", chunkcoordintpair, ioexception);
                this.errorReporter.reportChunkLoadFailure(ioexception, this.simpleRegionStorage.storageInfo(), chunkcoordintpair);
                return Optional.empty();
            } else {
                throw new CompletionException(throwable);
            }
        });
    }

    private void unpackChunk(ChunkCoordIntPair chunkcoordintpair, @Nullable RegionFileSection.a<P> regionfilesection_a) {
        if (regionfilesection_a == null) {
            for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); ++i) {
                this.storage.put(getKey(chunkcoordintpair, i), Optional.empty());
            }
        } else {
            boolean flag = regionfilesection_a.versionChanged();

            for (int j = this.levelHeightAccessor.getMinSectionY(); j <= this.levelHeightAccessor.getMaxSectionY(); ++j) {
                long k = getKey(chunkcoordintpair, j);
                Optional<R> optional = Optional.ofNullable(regionfilesection_a.sectionsByY.get(j)).map((object) -> {
                    return this.unpacker.apply(object, () -> {
                        this.setDirty(k);
                    });
                });

                this.storage.put(k, optional);
                optional.ifPresent((object) -> {
                    this.onSectionLoad(k);
                    if (flag) {
                        this.setDirty(k);
                    }

                });
            }
        }

    }

    private void writeChunk(ChunkCoordIntPair chunkcoordintpair) {
        RegistryOps<NBTBase> registryops = this.registryAccess.createSerializationContext(DynamicOpsNBT.INSTANCE);
        Dynamic<NBTBase> dynamic = this.writeChunk(chunkcoordintpair, registryops);
        NBTBase nbtbase = (NBTBase) dynamic.getValue();

        if (nbtbase instanceof NBTTagCompound) {
            this.simpleRegionStorage.write(chunkcoordintpair, (NBTTagCompound) nbtbase).exceptionally((throwable) -> {
                this.errorReporter.reportChunkSaveFailure(throwable, this.simpleRegionStorage.storageInfo(), chunkcoordintpair);
                return null;
            });
        } else {
            RegionFileSection.LOGGER.error("Expected compound tag, got {}", nbtbase);
        }

    }

    private <T> Dynamic<T> writeChunk(ChunkCoordIntPair chunkcoordintpair, DynamicOps<T> dynamicops) {
        Map<T, T> map = Maps.newHashMap();

        for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); ++i) {
            long j = getKey(chunkcoordintpair, i);
            Optional<R> optional = (Optional) this.storage.get(j);

            if (optional != null && !optional.isEmpty()) {
                DataResult<T> dataresult = this.codec.encodeStart(dynamicops, this.packer.apply(optional.get()));
                String s = Integer.toString(i);
                Logger logger = RegionFileSection.LOGGER;

                Objects.requireNonNull(logger);
                dataresult.resultOrPartial(logger::error).ifPresent((object) -> {
                    map.put(dynamicops.createString(s), object);
                });
            }
        }

        return new Dynamic(dynamicops, dynamicops.createMap(ImmutableMap.of(dynamicops.createString("Sections"), dynamicops.createMap(map), dynamicops.createString("DataVersion"), dynamicops.createInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion()))));
    }

    private static long getKey(ChunkCoordIntPair chunkcoordintpair, int i) {
        return SectionPosition.asLong(chunkcoordintpair.x, i, chunkcoordintpair.z);
    }

    protected void onSectionLoad(long i) {}

    protected void setDirty(long i) {
        Optional<R> optional = (Optional) this.storage.get(i);

        if (optional != null && !optional.isEmpty()) {
            this.dirtyChunks.add(ChunkCoordIntPair.asLong(SectionPosition.x(i), SectionPosition.z(i)));
        } else {
            RegionFileSection.LOGGER.warn("No data for position: {}", SectionPosition.of(i));
        }
    }

    static int getVersion(Dynamic<?> dynamic) {
        return dynamic.get("DataVersion").asInt(1945);
    }

    public void flush(ChunkCoordIntPair chunkcoordintpair) {
        if (this.dirtyChunks.remove(chunkcoordintpair.toLong())) {
            this.writeChunk(chunkcoordintpair);
        }

    }

    public void close() throws IOException {
        this.simpleRegionStorage.close();
    }

    private static record a<T>(Int2ObjectMap<T> sectionsByY, boolean versionChanged) {

        public static <T> RegionFileSection.a<T> parse(Codec<T> codec, DynamicOps<NBTBase> dynamicops, NBTBase nbtbase, SimpleRegionStorage simpleregionstorage, LevelHeightAccessor levelheightaccessor) {
            Dynamic<NBTBase> dynamic = new Dynamic(dynamicops, nbtbase);
            int i = RegionFileSection.getVersion(dynamic);
            int j = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            boolean flag = i != j;
            Dynamic<NBTBase> dynamic1 = simpleregionstorage.upgradeChunkTag(dynamic, i);
            OptionalDynamic<NBTBase> optionaldynamic = dynamic1.get("Sections");
            Int2ObjectMap<T> int2objectmap = new Int2ObjectOpenHashMap();

            for (int k = levelheightaccessor.getMinSectionY(); k <= levelheightaccessor.getMaxSectionY(); ++k) {
                Optional<T> optional = optionaldynamic.get(Integer.toString(k)).result().flatMap((dynamic2) -> {
                    DataResult dataresult = codec.parse(dynamic2);
                    Logger logger = RegionFileSection.LOGGER;

                    Objects.requireNonNull(logger);
                    return dataresult.resultOrPartial(logger::error);
                });

                if (optional.isPresent()) {
                    int2objectmap.put(k, optional.get());
                }
            }

            return new RegionFileSection.a<>(int2objectmap, flag);
        }
    }
}
