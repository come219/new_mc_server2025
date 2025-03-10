package net.minecraft.server.level;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkCoordIntPair;

public class ChunkTaskQueue {

    public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
    private final List<Long2ObjectLinkedOpenHashMap<List<Runnable>>> queuesPerPriority;
    private volatile int topPriorityQueueIndex;
    private final String name;

    public ChunkTaskQueue(String s) {
        this.queuesPerPriority = IntStream.range(0, ChunkTaskQueue.PRIORITY_LEVEL_COUNT).mapToObj((i) -> {
            return new Long2ObjectLinkedOpenHashMap();
        }).toList();
        this.topPriorityQueueIndex = ChunkTaskQueue.PRIORITY_LEVEL_COUNT;
        this.name = s;
    }

    protected void resortChunkTasks(int i, ChunkCoordIntPair chunkcoordintpair, int j) {
        if (i < ChunkTaskQueue.PRIORITY_LEVEL_COUNT) {
            Long2ObjectLinkedOpenHashMap<List<Runnable>> long2objectlinkedopenhashmap = (Long2ObjectLinkedOpenHashMap) this.queuesPerPriority.get(i);
            List<Runnable> list = (List) long2objectlinkedopenhashmap.remove(chunkcoordintpair.toLong());

            if (i == this.topPriorityQueueIndex) {
                while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap) this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty()) {
                    ++this.topPriorityQueueIndex;
                }
            }

            if (list != null && !list.isEmpty()) {
                ((List) ((Long2ObjectLinkedOpenHashMap) this.queuesPerPriority.get(j)).computeIfAbsent(chunkcoordintpair.toLong(), (k) -> {
                    return Lists.newArrayList();
                })).addAll(list);
                this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, j);
            }

        }
    }

    protected void submit(Runnable runnable, long i, int j) {
        ((List) ((Long2ObjectLinkedOpenHashMap) this.queuesPerPriority.get(j)).computeIfAbsent(i, (k) -> {
            return Lists.newArrayList();
        })).add(runnable);
        this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, j);
    }

    protected void release(long i, boolean flag) {
        Iterator iterator = this.queuesPerPriority.iterator();

        while (iterator.hasNext()) {
            Long2ObjectLinkedOpenHashMap<List<Runnable>> long2objectlinkedopenhashmap = (Long2ObjectLinkedOpenHashMap) iterator.next();
            List<Runnable> list = (List) long2objectlinkedopenhashmap.get(i);

            if (list != null) {
                if (flag) {
                    list.clear();
                }

                if (list.isEmpty()) {
                    long2objectlinkedopenhashmap.remove(i);
                }
            }
        }

        while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap) this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty()) {
            ++this.topPriorityQueueIndex;
        }

    }

    @Nullable
    public ChunkTaskQueue.a pop() {
        if (!this.hasWork()) {
            return null;
        } else {
            int i = this.topPriorityQueueIndex;
            Long2ObjectLinkedOpenHashMap<List<Runnable>> long2objectlinkedopenhashmap = (Long2ObjectLinkedOpenHashMap) this.queuesPerPriority.get(i);
            long j = long2objectlinkedopenhashmap.firstLongKey();

            List list;

            for (list = (List) long2objectlinkedopenhashmap.removeFirst(); this.hasWork() && ((Long2ObjectLinkedOpenHashMap) this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty(); ++this.topPriorityQueueIndex) {
                ;
            }

            return new ChunkTaskQueue.a(j, list);
        }
    }

    public boolean hasWork() {
        return this.topPriorityQueueIndex < ChunkTaskQueue.PRIORITY_LEVEL_COUNT;
    }

    public String toString() {
        return this.name + " " + this.topPriorityQueueIndex + "...";
    }

    public static record a(long chunkPos, List<Runnable> tasks) {

    }
}
