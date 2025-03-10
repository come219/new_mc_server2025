package net.minecraft.util.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class ConsecutiveExecutor extends AbstractConsecutiveExecutor<Runnable> {

    public ConsecutiveExecutor(Executor executor, String s) {
        super(new PairedQueue.b(new ConcurrentLinkedQueue()), executor, s);
    }

    @Override
    public Runnable wrapRunnable(Runnable runnable) {
        return runnable;
    }
}
