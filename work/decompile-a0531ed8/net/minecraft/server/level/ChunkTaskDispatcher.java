package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.PairedQueue;
import net.minecraft.util.thread.PriorityConsecutiveExecutor;
import net.minecraft.util.thread.TaskScheduler;
import net.minecraft.world.level.ChunkCoordIntPair;
import org.slf4j.Logger;

public class ChunkTaskDispatcher implements PlayerChunk.a, AutoCloseable {

    public static final int DISPATCHER_PRIORITY_COUNT = 4;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ChunkTaskQueue queue;
    private final TaskScheduler<Runnable> executor;
    private final PriorityConsecutiveExecutor dispatcher;
    protected boolean sleeping;

    public ChunkTaskDispatcher(TaskScheduler<Runnable> taskscheduler, Executor executor) {
        this.queue = new ChunkTaskQueue(taskscheduler.name() + "_queue");
        this.executor = taskscheduler;
        this.dispatcher = new PriorityConsecutiveExecutor(4, executor, "dispatcher");
        this.sleeping = true;
    }

    public boolean hasWork() {
        return this.dispatcher.hasWork() || this.queue.hasWork();
    }

    @Override
    public void onLevelChange(ChunkCoordIntPair chunkcoordintpair, IntSupplier intsupplier, int i, IntConsumer intconsumer) {
        this.dispatcher.schedule(new PairedQueue.c(0, () -> {
            int j = intsupplier.getAsInt();

            this.queue.resortChunkTasks(j, chunkcoordintpair, i);
            intconsumer.accept(i);
        }));
    }

    public void release(long i, Runnable runnable, boolean flag) {
        this.dispatcher.schedule(new PairedQueue.c(1, () -> {
            this.queue.release(i, flag);
            this.onRelease(i);
            if (this.sleeping) {
                this.sleeping = false;
                this.pollTask();
            }

            runnable.run();
        }));
    }

    public void submit(Runnable runnable, long i, IntSupplier intsupplier) {
        this.dispatcher.schedule(new PairedQueue.c(2, () -> {
            int j = intsupplier.getAsInt();

            this.queue.submit(runnable, i, j);
            if (this.sleeping) {
                this.sleeping = false;
                this.pollTask();
            }

        }));
    }

    protected void pollTask() {
        this.dispatcher.schedule(new PairedQueue.c(3, () -> {
            ChunkTaskQueue.a chunktaskqueue_a = this.popTasks();

            if (chunktaskqueue_a == null) {
                this.sleeping = true;
            } else {
                this.scheduleForExecution(chunktaskqueue_a);
            }

        }));
    }

    protected void scheduleForExecution(ChunkTaskQueue.a chunktaskqueue_a) {
        CompletableFuture.allOf((CompletableFuture[]) chunktaskqueue_a.tasks().stream().map((runnable) -> {
            return this.executor.scheduleWithResult((completablefuture) -> {
                runnable.run();
                completablefuture.complete(Unit.INSTANCE);
            });
        }).toArray((i) -> {
            return new CompletableFuture[i];
        })).thenAccept((ovoid) -> {
            this.pollTask();
        });
    }

    protected void onRelease(long i) {}

    @Nullable
    protected ChunkTaskQueue.a popTasks() {
        return this.queue.pop();
    }

    public void close() {
        this.executor.close();
    }
}
