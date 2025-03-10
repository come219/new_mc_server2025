package net.minecraft.world.level.storage;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.PersistentBase;
import org.slf4j.Logger;

public class WorldPersistentData implements AutoCloseable {

    private static final Logger LOGGER = LogUtils.getLogger();
    public final Map<String, Optional<PersistentBase>> cache = new HashMap();
    private final DataFixer fixerUpper;
    private final HolderLookup.a registries;
    private final Path dataFolder;
    private CompletableFuture<?> pendingWriteFuture = CompletableFuture.completedFuture((Object) null);

    public WorldPersistentData(Path path, DataFixer datafixer, HolderLookup.a holderlookup_a) {
        this.fixerUpper = datafixer;
        this.dataFolder = path;
        this.registries = holderlookup_a;
    }

    private Path getDataFile(String s) {
        return this.dataFolder.resolve(s + ".dat");
    }

    public <T extends PersistentBase> T computeIfAbsent(PersistentBase.a<T> persistentbase_a, String s) {
        T t0 = this.get(persistentbase_a, s);

        if (t0 != null) {
            return t0;
        } else {
            T t1 = (PersistentBase) persistentbase_a.constructor().get();

            this.set(s, t1);
            return t1;
        }
    }

    @Nullable
    public <T extends PersistentBase> T get(PersistentBase.a<T> persistentbase_a, String s) {
        Optional<PersistentBase> optional = (Optional) this.cache.get(s);

        if (optional == null) {
            optional = Optional.ofNullable(this.readSavedData(persistentbase_a.deserializer(), persistentbase_a.type(), s));
            this.cache.put(s, optional);
        }

        return (PersistentBase) optional.orElse((Object) null);
    }

    @Nullable
    private <T extends PersistentBase> T readSavedData(BiFunction<NBTTagCompound, HolderLookup.a, T> bifunction, DataFixTypes datafixtypes, String s) {
        try {
            Path path = this.getDataFile(s);

            if (Files.exists(path, new LinkOption[0])) {
                NBTTagCompound nbttagcompound = this.readTagFromDisk(s, datafixtypes, SharedConstants.getCurrentVersion().getDataVersion().getVersion());

                return (PersistentBase) bifunction.apply(nbttagcompound.getCompound("data"), this.registries);
            }
        } catch (Exception exception) {
            WorldPersistentData.LOGGER.error("Error loading saved data: {}", s, exception);
        }

        return null;
    }

    public void set(String s, PersistentBase persistentbase) {
        this.cache.put(s, Optional.of(persistentbase));
        persistentbase.setDirty();
    }

    public NBTTagCompound readTagFromDisk(String s, DataFixTypes datafixtypes, int i) throws IOException {
        InputStream inputstream = Files.newInputStream(this.getDataFile(s));

        NBTTagCompound nbttagcompound;

        try {
            PushbackInputStream pushbackinputstream = new PushbackInputStream(new FastBufferedInputStream(inputstream), 2);

            try {
                NBTTagCompound nbttagcompound1;

                if (this.isGzip(pushbackinputstream)) {
                    nbttagcompound1 = NBTCompressedStreamTools.readCompressed((InputStream) pushbackinputstream, NBTReadLimiter.unlimitedHeap());
                } else {
                    DataInputStream datainputstream = new DataInputStream(pushbackinputstream);

                    try {
                        nbttagcompound1 = NBTCompressedStreamTools.read((DataInput) datainputstream);
                    } catch (Throwable throwable) {
                        try {
                            datainputstream.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }

                        throw throwable;
                    }

                    datainputstream.close();
                }

                int j = GameProfileSerializer.getDataVersion(nbttagcompound1, 1343);

                nbttagcompound = datafixtypes.update(this.fixerUpper, nbttagcompound1, j, i);
            } catch (Throwable throwable2) {
                try {
                    pushbackinputstream.close();
                } catch (Throwable throwable3) {
                    throwable2.addSuppressed(throwable3);
                }

                throw throwable2;
            }

            pushbackinputstream.close();
        } catch (Throwable throwable4) {
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (Throwable throwable5) {
                    throwable4.addSuppressed(throwable5);
                }
            }

            throw throwable4;
        }

        if (inputstream != null) {
            inputstream.close();
        }

        return nbttagcompound;
    }

    private boolean isGzip(PushbackInputStream pushbackinputstream) throws IOException {
        byte[] abyte = new byte[2];
        boolean flag = false;
        int i = pushbackinputstream.read(abyte, 0, 2);

        if (i == 2) {
            int j = (abyte[1] & 255) << 8 | abyte[0] & 255;

            if (j == 35615) {
                flag = true;
            }
        }

        if (i != 0) {
            pushbackinputstream.unread(abyte, 0, i);
        }

        return flag;
    }

    public CompletableFuture<?> scheduleSave() {
        Map<Path, NBTTagCompound> map = this.collectDirtyTagsToSave();

        if (map.isEmpty()) {
            return CompletableFuture.completedFuture((Object) null);
        } else {
            int i = SystemUtils.maxAllowedExecutorThreads();
            int j = map.size();

            if (j > i) {
                this.pendingWriteFuture = this.pendingWriteFuture.thenCompose((object) -> {
                    List<CompletableFuture<?>> list = new ArrayList(i);
                    int k = MathHelper.positiveCeilDiv(j, i);
                    Iterator iterator = Iterables.partition(map.entrySet(), k).iterator();

                    while (iterator.hasNext()) {
                        List<Entry<Path, NBTTagCompound>> list1 = (List) iterator.next();

                        list.add(CompletableFuture.runAsync(() -> {
                            Iterator iterator1 = list1.iterator();

                            while (iterator1.hasNext()) {
                                Entry<Path, NBTTagCompound> entry = (Entry) iterator1.next();

                                tryWrite((Path) entry.getKey(), (NBTTagCompound) entry.getValue());
                            }

                        }, SystemUtils.ioPool()));
                    }

                    return CompletableFuture.allOf((CompletableFuture[]) list.toArray((l) -> {
                        return new CompletableFuture[l];
                    }));
                });
            } else {
                this.pendingWriteFuture = this.pendingWriteFuture.thenCompose((object) -> {
                    return CompletableFuture.allOf((CompletableFuture[]) map.entrySet().stream().map((entry) -> {
                        return CompletableFuture.runAsync(() -> {
                            tryWrite((Path) entry.getKey(), (NBTTagCompound) entry.getValue());
                        }, SystemUtils.ioPool());
                    }).toArray((k) -> {
                        return new CompletableFuture[k];
                    }));
                });
            }

            return this.pendingWriteFuture;
        }
    }

    private Map<Path, NBTTagCompound> collectDirtyTagsToSave() {
        Map<Path, NBTTagCompound> map = new Object2ObjectArrayMap();

        this.cache.forEach((s, optional) -> {
            optional.filter(PersistentBase::isDirty).ifPresent((persistentbase) -> {
                map.put(this.getDataFile(s), persistentbase.save(this.registries));
            });
        });
        return map;
    }

    private static void tryWrite(Path path, NBTTagCompound nbttagcompound) {
        try {
            NBTCompressedStreamTools.writeCompressed(nbttagcompound, path);
        } catch (IOException ioexception) {
            WorldPersistentData.LOGGER.error("Could not save data to {}", path.getFileName(), ioexception);
        }

    }

    public void saveAndJoin() {
        this.scheduleSave().join();
    }

    public void close() {
        this.saveAndJoin();
    }
}
