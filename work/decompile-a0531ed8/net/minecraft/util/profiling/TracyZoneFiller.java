package net.minecraft.util.profiling;

import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.slf4j.Logger;

public class TracyZoneFiller implements GameProfilerFiller {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(Option.RETAIN_CLASS_REFERENCE), 5);
    private final List<com.mojang.jtracy.Zone> activeZones = new ArrayList();
    private final Map<String, TracyZoneFiller.a> plots = new HashMap();
    private final String name = Thread.currentThread().getName();

    public TracyZoneFiller() {}

    @Override
    public void startTick() {}

    @Override
    public void endTick() {
        Iterator iterator = this.plots.values().iterator();

        while (iterator.hasNext()) {
            TracyZoneFiller.a tracyzonefiller_a = (TracyZoneFiller.a) iterator.next();

            tracyzonefiller_a.set(0);
        }

    }

    @Override
    public void push(String s) {
        String s1 = "";
        String s2 = "";
        int i = 0;

        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Optional<StackFrame> optional = (Optional) TracyZoneFiller.STACK_WALKER.walk((stream) -> {
                return stream.filter((stackframe) -> {
                    return stackframe.getDeclaringClass() != TracyZoneFiller.class && stackframe.getDeclaringClass() != GameProfilerFiller.a.class;
                }).findFirst();
            });

            if (optional.isPresent()) {
                StackFrame stackframe = (StackFrame) optional.get();

                s1 = stackframe.getMethodName();
                s2 = stackframe.getFileName();
                i = stackframe.getLineNumber();
            }
        }

        com.mojang.jtracy.Zone com_mojang_jtracy_zone = TracyClient.beginZone(s, s1, s2, i);

        this.activeZones.add(com_mojang_jtracy_zone);
    }

    @Override
    public void push(Supplier<String> supplier) {
        this.push((String) supplier.get());
    }

    @Override
    public void pop() {
        if (this.activeZones.isEmpty()) {
            TracyZoneFiller.LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
        } else {
            com.mojang.jtracy.Zone com_mojang_jtracy_zone = (com.mojang.jtracy.Zone) this.activeZones.removeLast();

            com_mojang_jtracy_zone.close();
        }
    }

    @Override
    public void popPush(String s) {
        this.pop();
        this.push(s);
    }

    @Override
    public void popPush(Supplier<String> supplier) {
        this.pop();
        this.push((String) supplier.get());
    }

    @Override
    public void markForCharting(MetricCategory metriccategory) {}

    @Override
    public void incrementCounter(String s, int i) {
        ((TracyZoneFiller.a) this.plots.computeIfAbsent(s, (s1) -> {
            return new TracyZoneFiller.a(this.name + " " + s);
        })).add(i);
    }

    @Override
    public void incrementCounter(Supplier<String> supplier, int i) {
        this.incrementCounter((String) supplier.get(), i);
    }

    private com.mojang.jtracy.Zone activeZone() {
        return (com.mojang.jtracy.Zone) this.activeZones.getLast();
    }

    @Override
    public void addZoneText(String s) {
        this.activeZone().addText(s);
    }

    @Override
    public void addZoneValue(long i) {
        this.activeZone().addValue(i);
    }

    @Override
    public void setZoneColor(int i) {
        this.activeZone().setColor(i);
    }

    private static final class a {

        private final Plot plot;
        private int value;

        a(String s) {
            this.plot = TracyClient.createPlot(s);
            this.value = 0;
        }

        void set(int i) {
            this.value = i;
            this.plot.setValue((double) i);
        }

        void add(int i) {
            this.set(this.value + i);
        }
    }
}
