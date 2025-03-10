package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.PairedQueue;
import net.minecraft.util.thread.PriorityConsecutiveExecutor;
import net.minecraft.world.level.ChunkCoordIntPair;
import org.slf4j.Logger;

public class IOWorker implements ChunkScanAccess, AutoCloseable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final PriorityConsecutiveExecutor consecutiveExecutor;
    private final RegionFileCache storage;
    private final SequencedMap<ChunkCoordIntPair, IOWorker.a> pendingWrites = new LinkedHashMap();
    private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap();
    private static final int REGION_CACHE_SIZE = 1024;

    protected IOWorker(RegionStorageInfo regionstorageinfo, Path path, boolean flag) {
        this.storage = new RegionFileCache(regionstorageinfo, path, flag);
        this.consecutiveExecutor = new PriorityConsecutiveExecutor(IOWorker.Priority.values().length, SystemUtils.ioPool(), "IOWorker-" + regionstorageinfo.type());
    }

    public boolean isOldChunkAround(ChunkCoordIntPair chunkcoordintpair, int i) {
        ChunkCoordIntPair chunkcoordintpair1 = new ChunkCoordIntPair(chunkcoordintpair.x - i, chunkcoordintpair.z - i);
        ChunkCoordIntPair chunkcoordintpair2 = new ChunkCoordIntPair(chunkcoordintpair.x + i, chunkcoordintpair.z + i);

        for (int j = chunkcoordintpair1.getRegionX(); j <= chunkcoordintpair2.getRegionX(); ++j) {
            for (int k = chunkcoordintpair1.getRegionZ(); k <= chunkcoordintpair2.getRegionZ(); ++k) {
                BitSet bitset = (BitSet) this.getOrCreateOldDataForRegion(j, k).join();

                if (!bitset.isEmpty()) {
                    ChunkCoordIntPair chunkcoordintpair3 = ChunkCoordIntPair.minFromRegion(j, k);
                    int l = Math.max(chunkcoordintpair1.x - chunkcoordintpair3.x, 0);
                    int i1 = Math.max(chunkcoordintpair1.z - chunkcoordintpair3.z, 0);
                    int j1 = Math.min(chunkcoordintpair2.x - chunkcoordintpair3.x, 31);
                    int k1 = Math.min(chunkcoordintpair2.z - chunkcoordintpair3.z, 31);

                    for (int l1 = l; l1 <= j1; ++l1) {
                        for (int i2 = i1; i2 <= k1; ++i2) {
                            int j2 = i2 * 32 + l1;

                            if (bitset.get(j2)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int i, int j) {
        long k = ChunkCoordIntPair.asLong(i, j);
        Long2ObjectLinkedOpenHashMap long2objectlinkedopenhashmap = this.regionCacheForBlender;

        synchronized (this.regionCacheForBlender) {
            CompletableFuture<BitSet> completablefuture = (CompletableFuture) this.regionCacheForBlender.getAndMoveToFirst(k);

            if (completablefuture == null) {
                completablefuture = this.createOldDataForRegion(i, j);
                this.regionCacheForBlender.putAndMoveToFirst(k, completablefuture);
                if (this.regionCacheForBlender.size() > 1024) {
                    this.regionCacheForBlender.removeLast();
                }
            }

            return completablefuture;
        }
    }

    private CompletableFuture<BitSet> createOldDataForRegion(int i, int j) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkCoordIntPair chunkcoordintpair = ChunkCoordIntPair.minFromRegion(i, j);
            ChunkCoordIntPair chunkcoordintpair1 = ChunkCoordIntPair.maxFromRegion(i, j);
            BitSet bitset = new BitSet();

            ChunkCoordIntPair.rangeClosed(chunkcoordintpair, chunkcoordintpair1).forEach((chunkcoordintpair2) -> {
                CollectFields collectfields = new CollectFields(new FieldSelector[]{new FieldSelector(NBTTagInt.TYPE, "DataVersion"), new FieldSelector(NBTTagCompound.TYPE, "blending_data")});

                try {
                    this.scanChunk(chunkcoordintpair2, collectfields).join();
                } catch (Exception exception) {
                    IOWorker.LOGGER.warn("Failed to scan chunk {}", chunkcoordintpair2, exception);
                    return;
                }

                NBTBase nbtbase = collectfields.getResult();

                if (nbtbase instanceof NBTTagCompound nbttagcompound) {
                    if (this.isOldChunk(nbttagcompound)) {
                        int k = chunkcoordintpair2.getRegionLocalZ() * 32 + chunkcoordintpair2.getRegionLocalX();

                        bitset.set(k);
                    }
                }

            });
            return bitset;
        }, SystemUtils.backgroundExecutor());
    }

    private boolean isOldChunk(NBTTagCompound nbttagcompound) {
        return nbttagcompound.contains("DataVersion", 99) && nbttagcompound.getInt("DataVersion") >= 4185 ? nbttagcompound.contains("blending_data", 10) : true;
    }

    public CompletableFuture<Void> store(ChunkCoordIntPair chunkcoordintpair, @Nullable NBTTagCompound nbttagcompound) {
        return this.store(chunkcoordintpair, () -> {
            return nbttagcompound;
        });
    }

    public CompletableFuture<Void> store(ChunkCoordIntPair chunkcoordintpair, Supplier<NBTTagCompound> supplier) {
        return this.submitTask(() -> {
            NBTTagCompound nbttagcompound = (NBTTagCompound) supplier.get();
            IOWorker.a ioworker_a = (IOWorker.a) this.pendingWrites.computeIfAbsent(chunkcoordintpair, (chunkcoordintpair1) -> {
                return new IOWorker.a(nbttagcompound);
            });

            ioworker_a.data = nbttagcompound;
            return ioworker_a.result;
        }).thenCompose(Function.identity());
    }

    public CompletableFuture<Optional<NBTTagCompound>> loadAsync(ChunkCoordIntPair chunkcoordintpair) {
        return this.submitThrowingTask(() -> {
            IOWorker.a ioworker_a = (IOWorker.a) this.pendingWrites.get(chunkcoordintpair);

            if (ioworker_a != null) {
                return Optional.ofNullable(ioworker_a.copyData());
            } else {
                try {
                    NBTTagCompound nbttagcompound = this.storage.read(chunkcoordintpair);

                    return Optional.ofNullable(nbttagcompound);
                } catch (Exception exception) {
                    IOWorker.LOGGER.warn("Failed to read chunk {}", chunkcoordintpair, exception);
                    throw exception;
                }
            }
        });
    }

    public CompletableFuture<Void> synchronize(boolean flag) {
        CompletableFuture<Void> completablefuture = this.submitTask(() -> {
            return CompletableFuture.allOf((CompletableFuture[]) this.pendingWrites.values().stream().map((ioworker_a) -> {
                return ioworker_a.result;
            }).toArray((i) -> {
                return new CompletableFuture[i];
            }));
        }).thenCompose(Function.identity());

        return flag ? completablefuture.thenCompose((ovoid) -> {
            return this.submitThrowingTask(() -> {
                try {
                    this.storage.flush();
                    return null;
                } catch (Exception exception) {
                    IOWorker.LOGGER.warn("Failed to synchronize chunks", exception);
                    throw exception;
                }
            });
        }) : completablefuture.thenCompose((ovoid) -> {
            return this.submitTask(() -> {
                return null;
            });
        });
    }

    @Override
    public CompletableFuture<Void> scanChunk(ChunkCoordIntPair chunkcoordintpair, StreamTagVisitor streamtagvisitor) {
        return this.submitThrowingTask(() -> {
            try {
                IOWorker.a ioworker_a = (IOWorker.a) this.pendingWrites.get(chunkcoordintpair);

                if (ioworker_a != null) {
                    if (ioworker_a.data != null) {
                        ioworker_a.data.acceptAsRoot(streamtagvisitor);
                    }
                } else {
                    this.storage.scanChunk(chunkcoordintpair, streamtagvisitor);
                }

                return null;
            } catch (Exception exception) {
                IOWorker.LOGGER.warn("Failed to bulk scan chunk {}", chunkcoordintpair, exception);
                throw exception;
            }
        });
    }

    private <T> CompletableFuture<T> submitThrowingTask(IOWorker.c<T> ioworker_c) {
        return this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.FOREGROUND.ordinal(), (completablefuture) -> {
            if (!this.shutdownRequested.get()) {
                try {
                    completablefuture.complete(ioworker_c.get());
                } catch (Exception exception) {
                    completablefuture.completeExceptionally(exception);
                }
            }

            this.tellStorePending();
        });
    }

    private <T> CompletableFuture<T> submitTask(Supplier<T> supplier) {
        return this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.FOREGROUND.ordinal(), (completablefuture) -> {
            if (!this.shutdownRequested.get()) {
                completablefuture.complete(supplier.get());
            }

            this.tellStorePending();
        });
    }

    private void storePendingChunk() {
        Entry<ChunkCoordIntPair, IOWorker.a> entry = this.pendingWrites.pollFirstEntry();

        if (entry != null) {
            this.runStore((ChunkCoordIntPair) entry.getKey(), (IOWorker.a) entry.getValue());
            this.tellStorePending();
        }
    }

    private void tellStorePending() {
        this.consecutiveExecutor.schedule(new PairedQueue.c(IOWorker.Priority.BACKGROUND.ordinal(), this::storePendingChunk));
    }

    private void runStore(ChunkCoordIntPair chunkcoordintpair, IOWorker.a ioworker_a) {
        try {
            this.storage.write(chunkcoordintpair, ioworker_a.data);
            ioworker_a.result.complete((Object) null);
        } catch (Exception exception) {
            IOWorker.LOGGER.error("Failed to store chunk {}", chunkcoordintpair, exception);
            ioworker_a.result.completeExceptionally(exception);
        }

    }

    public void close() throws IOException {
        if (this.shutdownRequested.compareAndSet(false, true)) {
            this.waitForShutdown();
            this.consecutiveExecutor.close();

            try {
                this.storage.close();
            } catch (Exception exception) {
                IOWorker.LOGGER.error("Failed to close storage", exception);
            }

        }
    }

    private void waitForShutdown() {
        this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.SHUTDOWN.ordinal(), (completablefuture) -> {
            completablefuture.complete(Unit.INSTANCE);
        }).join();
    }

    public RegionStorageInfo storageInfo() {
        return this.storage.info();
    }

    private static enum Priority {

        FOREGROUND, BACKGROUND, SHUTDOWN;

        private Priority() {}
    }

    @FunctionalInterface
    private interface c<T> {

        @Nullable
        T get() throws Exception;
    }

    private static class a {

        @Nullable
        NBTTagCompound data;
        final CompletableFuture<Void> result = new CompletableFuture();

        public a(@Nullable NBTTagCompound nbttagcompound) {
            this.data = nbttagcompound;
        }

        @Nullable
        NBTTagCompound copyData() {
            NBTTagCompound nbttagcompound = this.data;

            return nbttagcompound == null ? null : nbttagcompound.copy();
        }
    }
}
