package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.core.Holder;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.levelgen.structure.Structure;

@Name("minecraft.StructureGeneration")
@Label("Structure Generation")
@Category({"Minecraft", "World Generation"})
@StackTrace(false)
@Enabled(false)
@DontObfuscate
public class StructureGenerationEvent extends Event {

    public static final String EVENT_NAME = "minecraft.StructureGeneration";
    public static final EventType TYPE = EventType.getEventType(StructureGenerationEvent.class);
    @Name("chunkPosX")
    @Label("Chunk X Position")
    public final int chunkPosX;
    @Name("chunkPosZ")
    @Label("Chunk Z Position")
    public final int chunkPosZ;
    @Name("structure")
    @Label("Structure")
    public final String structure;
    @Name("level")
    @Label("Level")
    public final String level;
    @Name("success")
    @Label("Success")
    public boolean success;

    public StructureGenerationEvent(ChunkCoordIntPair chunkcoordintpair, Holder<Structure> holder, ResourceKey<World> resourcekey) {
        this.chunkPosX = chunkcoordintpair.x;
        this.chunkPosZ = chunkcoordintpair.z;
        this.structure = holder.getRegisteredName();
        this.level = resourcekey.location().toString();
    }

    public interface a {

        String a = "chunkPosX";
        String b = "chunkPosZ";
        String c = "structure";
        String d = "level";
        String e = "success";
    }
}
