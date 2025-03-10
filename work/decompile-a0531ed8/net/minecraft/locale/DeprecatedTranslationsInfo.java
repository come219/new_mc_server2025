package net.minecraft.locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public record DeprecatedTranslationsInfo(List<String> removed, Map<String, String> renamed) {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeprecatedTranslationsInfo EMPTY = new DeprecatedTranslationsInfo(List.of(), Map.of());
    public static final Codec<DeprecatedTranslationsInfo> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.STRING.listOf().fieldOf("removed").forGetter(DeprecatedTranslationsInfo::removed), Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("renamed").forGetter(DeprecatedTranslationsInfo::renamed)).apply(instance, DeprecatedTranslationsInfo::new);
    });

    public static DeprecatedTranslationsInfo loadFromJson(InputStream inputstream) {
        JsonElement jsonelement = JsonParser.parseReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));

        return (DeprecatedTranslationsInfo) DeprecatedTranslationsInfo.CODEC.parse(JsonOps.INSTANCE, jsonelement).getOrThrow((s) -> {
            return new IllegalStateException("Failed to parse deprecated language data: " + s);
        });
    }

    public static DeprecatedTranslationsInfo loadFromResource(String s) {
        try {
            InputStream inputstream = LocaleLanguage.class.getResourceAsStream(s);

            label44:
            {
                DeprecatedTranslationsInfo deprecatedtranslationsinfo;

                try {
                    if (inputstream == null) {
                        break label44;
                    }

                    deprecatedtranslationsinfo = loadFromJson(inputstream);
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

                return deprecatedtranslationsinfo;
            }

            if (inputstream != null) {
                inputstream.close();
            }
        } catch (Exception exception) {
            DeprecatedTranslationsInfo.LOGGER.error("Failed to read {}", s, exception);
        }

        return DeprecatedTranslationsInfo.EMPTY;
    }

    public static DeprecatedTranslationsInfo loadFromDefaultResource() {
        return loadFromResource("/assets/minecraft/lang/deprecated.json");
    }

    public void applyToMap(Map<String, String> map) {
        Iterator iterator = this.removed.iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            map.remove(s);
        }

        this.renamed.forEach((s1, s2) -> {
            String s3 = (String) map.remove(s1);

            if (s3 == null) {
                DeprecatedTranslationsInfo.LOGGER.warn("Missing translation key for rename: {}", s1);
                map.remove(s2);
            } else {
                map.put(s2, s3);
            }

        });
    }
}
