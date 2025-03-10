package net.minecraft.util.profiling;

import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Zone implements AutoCloseable {

    public static final Zone INACTIVE = new Zone((GameProfilerFiller) null);
    @Nullable
    private final GameProfilerFiller profiler;

    Zone(@Nullable GameProfilerFiller gameprofilerfiller) {
        this.profiler = gameprofilerfiller;
    }

    public Zone addText(String s) {
        if (this.profiler != null) {
            this.profiler.addZoneText(s);
        }

        return this;
    }

    public Zone addText(Supplier<String> supplier) {
        if (this.profiler != null) {
            this.profiler.addZoneText((String) supplier.get());
        }

        return this;
    }

    public Zone addValue(long i) {
        if (this.profiler != null) {
            this.profiler.addZoneValue(i);
        }

        return this;
    }

    public Zone setColor(int i) {
        if (this.profiler != null) {
            this.profiler.setZoneColor(i);
        }

        return this;
    }

    public void close() {
        if (this.profiler != null) {
            this.profiler.pop();
        }

    }
}
