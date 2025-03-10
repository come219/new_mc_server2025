package net.minecraft.world.ticks;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPosition;

public class ProtoChunkTickList<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {

    private final List<TickListChunk<T>> ticks = Lists.newArrayList();
    private final Set<TickListChunk<?>> ticksPerPosition;

    public ProtoChunkTickList() {
        this.ticksPerPosition = new ObjectOpenCustomHashSet(TickListChunk.UNIQUE_TICK_HASH);
    }

    @Override
    public void schedule(NextTickListEntry<T> nextticklistentry) {
        TickListChunk<T> ticklistchunk = new TickListChunk<>(nextticklistentry.type(), nextticklistentry.pos(), 0, nextticklistentry.priority());

        this.schedule(ticklistchunk);
    }

    private void schedule(TickListChunk<T> ticklistchunk) {
        if (this.ticksPerPosition.add(ticklistchunk)) {
            this.ticks.add(ticklistchunk);
        }

    }

    @Override
    public boolean hasScheduledTick(BlockPosition blockposition, T t0) {
        return this.ticksPerPosition.contains(TickListChunk.probe(t0, blockposition));
    }

    @Override
    public int count() {
        return this.ticks.size();
    }

    @Override
    public List<TickListChunk<T>> pack(long i) {
        return this.ticks;
    }

    public List<TickListChunk<T>> scheduledTicks() {
        return List.copyOf(this.ticks);
    }

    public static <T> ProtoChunkTickList<T> load(List<TickListChunk<T>> list) {
        ProtoChunkTickList<T> protochunkticklist = new ProtoChunkTickList<>();

        Objects.requireNonNull(protochunkticklist);
        list.forEach(protochunkticklist::schedule);
        return protochunkticklist;
    }
}
