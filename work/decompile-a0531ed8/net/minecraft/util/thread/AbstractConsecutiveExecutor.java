package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.SystemUtils;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import org.slf4j.Logger;

public abstract class AbstractConsecutiveExecutor<T extends Runnable> implements ProfilerMeasured, TaskScheduler<T>, Runnable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final AtomicReference<AbstractConsecutiveExecutor.a> status;
    private final PairedQueue<T> queue;
    private final Executor executor;
    private final String name;

    public AbstractConsecutiveExecutor(PairedQueue<T> pairedqueue, Executor executor, String s) {
        this.status = new AtomicReference(AbstractConsecutiveExecutor.a.SLEEPING);
        this.executor = executor;
        this.queue = pairedqueue;
        this.name = s;
        MetricsRegistry.INSTANCE.add(this);
    }

    private boolean canBeScheduled() {
        return !this.isClosed() && !this.queue.isEmpty();
    }

    @Override
    public void close() {
        this.status.set(AbstractConsecutiveExecutor.a.CLOSED);
    }

    private boolean pollTask() {
        if (!this.isRunning()) {
            return false;
        } else {
            Runnable runnable = this.queue.pop();

            if (runnable == null) {
                return false;
            } else {
                SystemUtils.runNamed(runnable, this.name);
                return true;
            }
        }
    }

    public void run() {
        try {
            this.pollTask();
        } finally {
            this.setSleeping();
            this.registerForExecution();
        }

    }

    public void runAll() {
        while (true) {
            try {
                if (this.pollTask()) {
                    continue;
                }
            } finally {
                this.setSleeping();
                this.registerForExecution();
            }

            return;
        }
    }

    @Override
    public void schedule(T t0) {
        this.queue.push(t0);
        this.registerForExecution();
    }

    private void registerForExecution() {
        if (this.canBeScheduled() && this.setRunning()) {
            try {
                this.executor.execute(this);
            } catch (RejectedExecutionException rejectedexecutionexception) {
                try {
                    this.executor.execute(this);
                } catch (RejectedExecutionException rejectedexecutionexception1) {
                    AbstractConsecutiveExecutor.LOGGER.error("Could not schedule ConsecutiveExecutor", rejectedexecutionexception1);
                }
            }
        }

    }

    public int size() {
        return this.queue.size();
    }

    public boolean hasWork() {
        return this.isRunning() && !this.queue.isEmpty();
    }

    public String toString() {
        return this.name + " " + String.valueOf(this.status.get()) + " " + this.queue.isEmpty();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public List<MetricSampler> profiledMetrics() {
        return ImmutableList.of(MetricSampler.create(this.name + "-queue-size", MetricCategory.CONSECUTIVE_EXECUTORS, this::size));
    }

    private boolean setRunning() {
        return this.status.compareAndSet(AbstractConsecutiveExecutor.a.SLEEPING, AbstractConsecutiveExecutor.a.RUNNING);
    }

    private void setSleeping() {
        this.status.compareAndSet(AbstractConsecutiveExecutor.a.RUNNING, AbstractConsecutiveExecutor.a.SLEEPING);
    }

    private boolean isRunning() {
        return this.status.get() == AbstractConsecutiveExecutor.a.RUNNING;
    }

    private boolean isClosed() {
        return this.status.get() == AbstractConsecutiveExecutor.a.CLOSED;
    }

    private static enum a {

        SLEEPING, RUNNING, CLOSED;

        private a() {}
    }
}
