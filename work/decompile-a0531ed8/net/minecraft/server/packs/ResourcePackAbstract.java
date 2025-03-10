package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.ChatDeserializer;
import org.slf4j.Logger;

public abstract class ResourcePackAbstract implements IResourcePack {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;

    protected ResourcePackAbstract(PackLocationInfo packlocationinfo) {
        this.location = packlocationinfo;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> metadatasectiontype) throws IOException {
        IoSupplier<InputStream> iosupplier = this.getRootResource(new String[]{"pack.mcmeta"});

        if (iosupplier == null) {
            return null;
        } else {
            InputStream inputstream = (InputStream) iosupplier.get();

            Object object;

            try {
                object = getMetadataFromStream(metadatasectiontype, inputstream);
            } catch (Throwable throwable) {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                }

                throw throwable;
            }

            if (inputstream != null) {
                inputstream.close();
            }

            return object;
        }
    }

    @Nullable
    public static <T> T getMetadataFromStream(MetadataSectionType<T> metadatasectiontype, InputStream inputstream) {
        JsonObject jsonobject;

        try {
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));

            try {
                jsonobject = ChatDeserializer.parse((Reader) bufferedreader);
            } catch (Throwable throwable) {
                try {
                    bufferedreader.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            bufferedreader.close();
        } catch (Exception exception) {
            ResourcePackAbstract.LOGGER.error("Couldn't load {} metadata", metadatasectiontype.name(), exception);
            return null;
        }

        return !jsonobject.has(metadatasectiontype.name()) ? null : metadatasectiontype.codec().parse(JsonOps.INSTANCE, jsonobject.get(metadatasectiontype.name())).ifError((error) -> {
            ResourcePackAbstract.LOGGER.error("Couldn't load {} metadata: {}", metadatasectiontype.name(), error);
        }).result().orElse((Object) null);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }
}
