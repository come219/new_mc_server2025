package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface TaskScheduler<R extends Runnable> extends AutoCloseable {

    String name();

    void schedule(R r0);

    default void close() {}

    R wrapRunnable(Runnable runnable);

    default <Source> CompletableFuture<Source> scheduleWithResult(Consumer<CompletableFuture<Source>> consumer) {
        CompletableFuture<Source> completablefuture = new CompletableFuture();

        this.schedule(this.wrapRunnable(() -> {
            consumer.accept(completablefuture);
        }));
        return completablefuture;
    }

    static TaskScheduler<Runnable> wrapExecutor(final String s, final Executor executor) {
        return new TaskScheduler<Runnable>() {
            @Override
            public String name() {
                return s;
            }

            @Override
            public void schedule(Runnable runnable) {
                executor.execute(runnable);
            }

            @Override
            public Runnable wrapRunnable(Runnable runnable) {
                return runnable;
            }

            public String toString() {
                return s;
            }
        };
    }
}
