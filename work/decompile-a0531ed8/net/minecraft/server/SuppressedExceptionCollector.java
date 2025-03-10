package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.Queue;
import net.minecraft.util.ArrayListDeque;

public class SuppressedExceptionCollector {

    private static final int LATEST_ENTRY_COUNT = 8;
    private final Queue<SuppressedExceptionCollector.a> latestEntries = new ArrayListDeque<>();
    private final Object2IntLinkedOpenHashMap<SuppressedExceptionCollector.b> entryCounts = new Object2IntLinkedOpenHashMap();

    public SuppressedExceptionCollector() {}

    private static long currentTimeMs() {
        return System.currentTimeMillis();
    }

    public synchronized void addEntry(String s, Throwable throwable) {
        long i = currentTimeMs();
        String s1 = throwable.getMessage();

        this.latestEntries.add(new SuppressedExceptionCollector.a(i, s, throwable.getClass(), s1));

        while (this.latestEntries.size() > 8) {
            this.latestEntries.remove();
        }

        SuppressedExceptionCollector.b suppressedexceptioncollector_b = new SuppressedExceptionCollector.b(s, throwable.getClass());
        int j = this.entryCounts.getInt(suppressedexceptioncollector_b);

        this.entryCounts.putAndMoveToFirst(suppressedexceptioncollector_b, j + 1);
    }

    public synchronized String dump() {
        long i = currentTimeMs();
        StringBuilder stringbuilder = new StringBuilder();

        if (!this.latestEntries.isEmpty()) {
            stringbuilder.append("\n\t\tLatest entries:\n");
            Iterator iterator = this.latestEntries.iterator();

            while (iterator.hasNext()) {
                SuppressedExceptionCollector.a suppressedexceptioncollector_a = (SuppressedExceptionCollector.a) iterator.next();

                stringbuilder.append("\t\t\t").append(suppressedexceptioncollector_a.location).append(":").append(suppressedexceptioncollector_a.cls).append(": ").append(suppressedexceptioncollector_a.message).append(" (").append(i - suppressedexceptioncollector_a.timestampMs).append("ms ago)").append("\n");
            }
        }

        if (!this.entryCounts.isEmpty()) {
            if (stringbuilder.isEmpty()) {
                stringbuilder.append("\n");
            }

            stringbuilder.append("\t\tEntry counts:\n");
            ObjectIterator objectiterator = Object2IntMaps.fastIterable(this.entryCounts).iterator();

            while (objectiterator.hasNext()) {
                Entry<SuppressedExceptionCollector.b> entry = (Entry) objectiterator.next();

                stringbuilder.append("\t\t\t").append(((SuppressedExceptionCollector.b) entry.getKey()).location).append(":").append(((SuppressedExceptionCollector.b) entry.getKey()).cls).append(" x ").append(entry.getIntValue()).append("\n");
            }
        }

        return stringbuilder.isEmpty() ? "~~NONE~~" : stringbuilder.toString();
    }

    private static record a(long timestampMs, String location, Class<? extends Throwable> cls, String message) {

    }

    private static record b(String location, Class<? extends Throwable> cls) {

    }
}
