package net.minecraft.server.packs.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.GameProfilerFiller;
import org.slf4j.Logger;

public abstract class ResourceDataJson<T> extends ResourceDataAbstract<Map<MinecraftKey, T>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final DynamicOps<JsonElement> ops;
    private final Codec<T> codec;
    private final FileToIdConverter lister;

    protected ResourceDataJson(HolderLookup.a holderlookup_a, Codec<T> codec, ResourceKey<? extends IRegistry<T>> resourcekey) {
        this((DynamicOps) holderlookup_a.createSerializationContext(JsonOps.INSTANCE), codec, FileToIdConverter.registry(resourcekey));
    }

    protected ResourceDataJson(Codec<T> codec, FileToIdConverter filetoidconverter) {
        this((DynamicOps) JsonOps.INSTANCE, codec, filetoidconverter);
    }

    private ResourceDataJson(DynamicOps<JsonElement> dynamicops, Codec<T> codec, FileToIdConverter filetoidconverter) {
        this.ops = dynamicops;
        this.codec = codec;
        this.lister = filetoidconverter;
    }

    @Override
    protected Map<MinecraftKey, T> prepare(IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        Map<MinecraftKey, T> map = new HashMap();

        scanDirectory(iresourcemanager, this.lister, this.ops, this.codec, map);
        return map;
    }

    public static <T> void scanDirectory(IResourceManager iresourcemanager, ResourceKey<? extends IRegistry<T>> resourcekey, DynamicOps<JsonElement> dynamicops, Codec<T> codec, Map<MinecraftKey, T> map) {
        scanDirectory(iresourcemanager, FileToIdConverter.registry(resourcekey), dynamicops, codec, map);
    }

    public static <T> void scanDirectory(IResourceManager iresourcemanager, FileToIdConverter filetoidconverter, DynamicOps<JsonElement> dynamicops, Codec<T> codec, Map<MinecraftKey, T> map) {
        Iterator iterator = filetoidconverter.listMatchingResources(iresourcemanager).entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, IResource> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();
            MinecraftKey minecraftkey1 = filetoidconverter.fileToId(minecraftkey);

            try {
                BufferedReader bufferedreader = ((IResource) entry.getValue()).openAsReader();

                try {
                    codec.parse(dynamicops, JsonParser.parseReader(bufferedreader)).ifSuccess((object) -> {
                        if (map.putIfAbsent(minecraftkey1, object) != null) {
                            throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf(minecraftkey1));
                        }
                    }).ifError((error) -> {
                        ResourceDataJson.LOGGER.error("Couldn't parse data file '{}' from '{}': {}", new Object[]{minecraftkey1, minecraftkey, error});
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
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                ResourceDataJson.LOGGER.error("Couldn't parse data file '{}' from '{}'", new Object[]{minecraftkey1, minecraftkey, jsonparseexception});
            }
        }

    }
}
