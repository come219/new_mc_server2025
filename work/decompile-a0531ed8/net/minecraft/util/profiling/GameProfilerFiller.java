package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface GameProfilerFiller {

    String ROOT = "root";

    void startTick();

    void endTick();

    void push(String s);

    void push(Supplier<String> supplier);

    void pop();

    void popPush(String s);

    void popPush(Supplier<String> supplier);

    default void addZoneText(String s) {}

    default void addZoneValue(long i) {}

    default void setZoneColor(int i) {}

    default Zone zone(String s) {
        this.push(s);
        return new Zone(this);
    }

    default Zone zone(Supplier<String> supplier) {
        this.push(supplier);
        return new Zone(this);
    }

    void markForCharting(MetricCategory metriccategory);

    default void incrementCounter(String s) {
        this.incrementCounter(s, 1);
    }

    void incrementCounter(String s, int i);

    default void incrementCounter(Supplier<String> supplier) {
        this.incrementCounter(supplier, 1);
    }

    void incrementCounter(Supplier<String> supplier, int i);

    static GameProfilerFiller combine(GameProfilerFiller gameprofilerfiller, GameProfilerFiller gameprofilerfiller1) {
        return (GameProfilerFiller) (gameprofilerfiller == GameProfilerDisabled.INSTANCE ? gameprofilerfiller1 : (gameprofilerfiller1 == GameProfilerDisabled.INSTANCE ? gameprofilerfiller : new GameProfilerFiller.a(gameprofilerfiller, gameprofilerfiller1)));
    }

    public static class a implements GameProfilerFiller {

        private final GameProfilerFiller first;
        private final GameProfilerFiller second;

        public a(GameProfilerFiller gameprofilerfiller, GameProfilerFiller gameprofilerfiller1) {
            this.first = gameprofilerfiller;
            this.second = gameprofilerfiller1;
        }

        @Override
        public void startTick() {
            this.first.startTick();
            this.second.startTick();
        }

        @Override
        public void endTick() {
            this.first.endTick();
            this.second.endTick();
        }

        @Override
        public void push(String s) {
            this.first.push(s);
            this.second.push(s);
        }

        @Override
        public void push(Supplier<String> supplier) {
            this.first.push(supplier);
            this.second.push(supplier);
        }

        @Override
        public void markForCharting(MetricCategory metriccategory) {
            this.first.markForCharting(metriccategory);
            this.second.markForCharting(metriccategory);
        }

        @Override
        public void pop() {
            this.first.pop();
            this.second.pop();
        }

        @Override
        public void popPush(String s) {
            this.first.popPush(s);
            this.second.popPush(s);
        }

        @Override
        public void popPush(Supplier<String> supplier) {
            this.first.popPush(supplier);
            this.second.popPush(supplier);
        }

        @Override
        public void incrementCounter(String s, int i) {
            this.first.incrementCounter(s, i);
            this.second.incrementCounter(s, i);
        }

        @Override
        public void incrementCounter(Supplier<String> supplier, int i) {
            this.first.incrementCounter(supplier, i);
            this.second.incrementCounter(supplier, i);
        }

        @Override
        public void addZoneText(String s) {
            this.first.addZoneText(s);
            this.second.addZoneText(s);
        }

        @Override
        public void addZoneValue(long i) {
            this.first.addZoneValue(i);
            this.second.addZoneValue(i);
        }

        @Override
        public void setZoneColor(int i) {
            this.first.setZoneColor(i);
            this.second.setZoneColor(i);
        }
    }
}
