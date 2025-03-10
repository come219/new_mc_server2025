package net.minecraft.util.profiling;

import com.mojang.jtracy.TracyClient;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class Profiler {

    private static final ThreadLocal<TracyZoneFiller> TRACY_FILLER = ThreadLocal.withInitial(TracyZoneFiller::new);
    private static final ThreadLocal<GameProfilerFiller> ACTIVE = new ThreadLocal();
    private static final AtomicInteger ACTIVE_COUNT = new AtomicInteger();

    private Profiler() {}

    public static Profiler.a use(GameProfilerFiller gameprofilerfiller) {
        startUsing(gameprofilerfiller);
        return Profiler::stopUsing;
    }

    private static void startUsing(GameProfilerFiller gameprofilerfiller) {
        if (Profiler.ACTIVE.get() != null) {
            throw new IllegalStateException("Profiler is already active");
        } else {
            GameProfilerFiller gameprofilerfiller1 = decorateFiller(gameprofilerfiller);

            Profiler.ACTIVE.set(gameprofilerfiller1);
            Profiler.ACTIVE_COUNT.incrementAndGet();
            gameprofilerfiller1.startTick();
        }
    }

    private static void stopUsing() {
        GameProfilerFiller gameprofilerfiller = (GameProfilerFiller) Profiler.ACTIVE.get();

        if (gameprofilerfiller == null) {
            throw new IllegalStateException("Profiler was not active");
        } else {
            Profiler.ACTIVE.remove();
            Profiler.ACTIVE_COUNT.decrementAndGet();
            gameprofilerfiller.endTick();
        }
    }

    private static GameProfilerFiller decorateFiller(GameProfilerFiller gameprofilerfiller) {
        return GameProfilerFiller.combine(getDefaultFiller(), gameprofilerfiller);
    }

    public static GameProfilerFiller get() {
        return Profiler.ACTIVE_COUNT.get() == 0 ? getDefaultFiller() : (GameProfilerFiller) Objects.requireNonNullElseGet((GameProfilerFiller) Profiler.ACTIVE.get(), Profiler::getDefaultFiller);
    }

    private static GameProfilerFiller getDefaultFiller() {
        return (GameProfilerFiller) (TracyClient.isAvailable() ? (GameProfilerFiller) Profiler.TRACY_FILLER.get() : GameProfilerDisabled.INSTANCE);
    }

    public interface a extends AutoCloseable {

        void close();
    }
}
