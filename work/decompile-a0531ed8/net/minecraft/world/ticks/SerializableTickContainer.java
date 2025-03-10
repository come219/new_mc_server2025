package net.minecraft.world.ticks;

import java.util.List;

public interface SerializableTickContainer<T> {

    List<TickListChunk<T>> pack(long i);
}
