package net.minecraft.util;

// CraftBukkit start
import java.util.concurrent.atomic.AtomicInteger;
// CraftBukkit end

public class TickThrottler {

    private final int incrementStep;
    private final int threshold;
    private final AtomicInteger count = new AtomicInteger(); // CraftBukkit - multithreaded field

    public TickThrottler(int i, int j) {
        this.incrementStep = i;
        this.threshold = j;
    }

    public void increment() {
        this.count.addAndGet(this.incrementStep); // CraftBukkit - use thread-safe field access instead
    }

    public void tick() {
        // CraftBukkit start
        for (int val; (val = this.count.get()) > 0 && !count.compareAndSet(val, val - 1); ) ;
        /* Use thread-safe field access instead
        if (this.count > 0) {
            --this.count;
        }
        */
        // CraftBukkit end

    }

    public boolean isUnderThreshold() {
        // CraftBukkit start - use thread-safe field access instead
        return this.count.get() < this.threshold;
    }

    public boolean isIncrementAndUnderThreshold() {
        return isIncrementAndUnderThreshold(this.incrementStep, this.threshold);
    }

    public boolean isIncrementAndUnderThreshold(int incrementStep, int threshold) {
        return this.count.addAndGet(incrementStep) < threshold;
        // CraftBukkit end
    }
}
