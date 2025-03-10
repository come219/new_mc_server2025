package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.util.profiling.metrics.MetricsRegistry;

public class PriorityConsecutiveExecutor extends AbstractConsecutiveExecutor<PairedQueue.c> {

    public PriorityConsecutiveExecutor(int i, Executor executor, String s) {
        super(new PairedQueue.a(i), executor, s);
        MetricsRegistry.INSTANCE.add(this);
    }

    @Override
    public PairedQueue.c wrapRunnable(Runnable runnable) {
        return new PairedQueue.c(0, runnable);
    }

    public <Source> CompletableFuture<Source> scheduleWithResult(int i, Consumer<CompletableFuture<Source>> consumer) {
        CompletableFuture<Source> completablefuture = new CompletableFuture();

        this.schedule(new PairedQueue.c(i, () -> {
            consumer.accept(completablefuture);
        }));
        return completablefuture;
    }
}
