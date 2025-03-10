package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.world.level.ChunkCoordIntPair;

public record StructureGenStat(Duration duration, ChunkCoordIntPair chunkPos, String structureName, String level, boolean success) implements TimedStat {

    public static StructureGenStat from(RecordedEvent recordedevent) {
        return new StructureGenStat(recordedevent.getDuration(), new ChunkCoordIntPair(recordedevent.getInt("chunkPosX"), recordedevent.getInt("chunkPosX")), recordedevent.getString("structure"), recordedevent.getString("level"), recordedevent.getBoolean("success"));
    }
}
