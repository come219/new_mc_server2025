package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.thread.TaskScheduler;
import net.minecraft.world.level.ChunkCoordIntPair;

public class ThrottlingChunkTaskDispatcher extends ChunkTaskDispatcher {

    private final LongSet chunkPositionsInExecution = new LongOpenHashSet();
    private final int maxChunksInExecution;
    private final String executorSchedulerName;

    public ThrottlingChunkTaskDispatcher(TaskScheduler<Runnable> taskscheduler, Executor executor, int i) {
        super(taskscheduler, executor);
        this.maxChunksInExecution = i;
        this.executorSchedulerName = taskscheduler.name();
    }

    @Override
    protected void onRelease(long i) {
        this.chunkPositionsInExecution.remove(i);
    }

    @Nullable
    @Override
    protected ChunkTaskQueue.a popTasks() {
        return this.chunkPositionsInExecution.size() < this.maxChunksInExecution ? super.popTasks() : null;
    }

    @Override
    protected void scheduleForExecution(ChunkTaskQueue.a chunktaskqueue_a) {
        this.chunkPositionsInExecution.add(chunktaskqueue_a.chunkPos());
        super.scheduleForExecution(chunktaskqueue_a);
    }

    @VisibleForTesting
    public String getDebugStatus() {
        return this.executorSchedulerName + "=[" + (String) this.chunkPositionsInExecution.longStream().mapToObj((i) -> {
            return "" + i + ":" + String.valueOf(new ChunkCoordIntPair(i));
        }).collect(Collectors.joining(",")) + "], s=" + this.sleeping;
    }
}
