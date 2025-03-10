package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public class BuiltInMetadata {

    private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
    private final Map<MetadataSectionType<?>, ?> values;

    private BuiltInMetadata(Map<MetadataSectionType<?>, ?> map) {
        this.values = map;
    }

    public <T> T get(MetadataSectionType<T> metadatasectiontype) {
        return this.values.get(metadatasectiontype);
    }

    public static BuiltInMetadata of() {
        return BuiltInMetadata.EMPTY;
    }

    public static <T> BuiltInMetadata of(MetadataSectionType<T> metadatasectiontype, T t0) {
        return new BuiltInMetadata(Map.of(metadatasectiontype, t0));
    }

    public static <T1, T2> BuiltInMetadata of(MetadataSectionType<T1> metadatasectiontype, T1 t1, MetadataSectionType<T2> metadatasectiontype1, T2 t2) {
        return new BuiltInMetadata(Map.of(metadatasectiontype, t1, metadatasectiontype1, t2));
    }
}
