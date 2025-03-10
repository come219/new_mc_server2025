package net.minecraft.resources;

@FunctionalInterface
public interface DependantName<T, V> {

    V get(ResourceKey<T> resourcekey);

    static <T, V> DependantName<T, V> fixed(V v0) {
        return (resourcekey) -> {
            return v0;
        };
    }
}
