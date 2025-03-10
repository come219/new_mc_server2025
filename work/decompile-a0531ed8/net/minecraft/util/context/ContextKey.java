package net.minecraft.util.context;

import net.minecraft.resources.MinecraftKey;

public class ContextKey<T> {

    private final MinecraftKey name;

    public ContextKey(MinecraftKey minecraftkey) {
        this.name = minecraftkey;
    }

    public static <T> ContextKey<T> vanilla(String s) {
        return new ContextKey<>(MinecraftKey.withDefaultNamespace(s));
    }

    public MinecraftKey name() {
        return this.name;
    }

    public String toString() {
        return "<parameter " + String.valueOf(this.name) + ">";
    }
}
