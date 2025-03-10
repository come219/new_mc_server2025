package net.minecraft.stats;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {

    public static final StreamCodec<PacketDataSerializer, RecipeBookSettings> STREAM_CODEC = StreamCodec.ofMember(RecipeBookSettings::write, RecipeBookSettings::read);
    private static final Map<RecipeBookType, Pair<String, String>> TAG_FIELDS = ImmutableMap.of(RecipeBookType.CRAFTING, Pair.of("isGuiOpen", "isFilteringCraftable"), RecipeBookType.FURNACE, Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"), RecipeBookType.BLAST_FURNACE, Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"), RecipeBookType.SMOKER, Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable"));
    private final Map<RecipeBookType, RecipeBookSettings.a> states;

    private RecipeBookSettings(Map<RecipeBookType, RecipeBookSettings.a> map) {
        this.states = map;
    }

    public RecipeBookSettings() {
        this(new EnumMap(RecipeBookType.class));
    }

    private RecipeBookSettings.a getSettings(RecipeBookType recipebooktype) {
        return (RecipeBookSettings.a) this.states.getOrDefault(recipebooktype, RecipeBookSettings.a.DEFAULT);
    }

    private void updateSettings(RecipeBookType recipebooktype, UnaryOperator<RecipeBookSettings.a> unaryoperator) {
        this.states.compute(recipebooktype, (recipebooktype1, recipebooksettings_a) -> {
            if (recipebooksettings_a == null) {
                recipebooksettings_a = RecipeBookSettings.a.DEFAULT;
            }

            recipebooksettings_a = (RecipeBookSettings.a) unaryoperator.apply(recipebooksettings_a);
            if (recipebooksettings_a.equals(RecipeBookSettings.a.DEFAULT)) {
                recipebooksettings_a = null;
            }

            return recipebooksettings_a;
        });
    }

    public boolean isOpen(RecipeBookType recipebooktype) {
        return this.getSettings(recipebooktype).open;
    }

    public void setOpen(RecipeBookType recipebooktype, boolean flag) {
        this.updateSettings(recipebooktype, (recipebooksettings_a) -> {
            return recipebooksettings_a.setOpen(flag);
        });
    }

    public boolean isFiltering(RecipeBookType recipebooktype) {
        return this.getSettings(recipebooktype).filtering;
    }

    public void setFiltering(RecipeBookType recipebooktype, boolean flag) {
        this.updateSettings(recipebooktype, (recipebooksettings_a) -> {
            return recipebooksettings_a.setFiltering(flag);
        });
    }

    private static RecipeBookSettings read(PacketDataSerializer packetdataserializer) {
        Map<RecipeBookType, RecipeBookSettings.a> map = new EnumMap(RecipeBookType.class);
        RecipeBookType[] arecipebooktype = RecipeBookType.values();
        int i = arecipebooktype.length;

        for (int j = 0; j < i; ++j) {
            RecipeBookType recipebooktype = arecipebooktype[j];
            boolean flag = packetdataserializer.readBoolean();
            boolean flag1 = packetdataserializer.readBoolean();

            if (flag || flag1) {
                map.put(recipebooktype, new RecipeBookSettings.a(flag, flag1));
            }
        }

        return new RecipeBookSettings(map);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        RecipeBookType[] arecipebooktype = RecipeBookType.values();
        int i = arecipebooktype.length;

        for (int j = 0; j < i; ++j) {
            RecipeBookType recipebooktype = arecipebooktype[j];
            RecipeBookSettings.a recipebooksettings_a = (RecipeBookSettings.a) this.states.getOrDefault(recipebooktype, RecipeBookSettings.a.DEFAULT);

            packetdataserializer.writeBoolean(recipebooksettings_a.open);
            packetdataserializer.writeBoolean(recipebooksettings_a.filtering);
        }

    }

    public static RecipeBookSettings read(NBTTagCompound nbttagcompound) {
        Map<RecipeBookType, RecipeBookSettings.a> map = new EnumMap(RecipeBookType.class);

        RecipeBookSettings.TAG_FIELDS.forEach((recipebooktype, pair) -> {
            boolean flag = nbttagcompound.getBoolean((String) pair.getFirst());
            boolean flag1 = nbttagcompound.getBoolean((String) pair.getSecond());

            if (flag || flag1) {
                map.put(recipebooktype, new RecipeBookSettings.a(flag, flag1));
            }

        });
        return new RecipeBookSettings(map);
    }

    public void write(NBTTagCompound nbttagcompound) {
        RecipeBookSettings.TAG_FIELDS.forEach((recipebooktype, pair) -> {
            RecipeBookSettings.a recipebooksettings_a = (RecipeBookSettings.a) this.states.getOrDefault(recipebooktype, RecipeBookSettings.a.DEFAULT);

            nbttagcompound.putBoolean((String) pair.getFirst(), recipebooksettings_a.open);
            nbttagcompound.putBoolean((String) pair.getSecond(), recipebooksettings_a.filtering);
        });
    }

    public RecipeBookSettings copy() {
        return new RecipeBookSettings(new EnumMap(this.states));
    }

    public void replaceFrom(RecipeBookSettings recipebooksettings) {
        this.states.clear();
        this.states.putAll(recipebooksettings.states);
    }

    public boolean equals(Object object) {
        return this == object || object instanceof RecipeBookSettings && this.states.equals(((RecipeBookSettings) object).states);
    }

    public int hashCode() {
        return this.states.hashCode();
    }

    private static record a(boolean open, boolean filtering) {

        public static final RecipeBookSettings.a DEFAULT = new RecipeBookSettings.a(false, false);

        public String toString() {
            return "[open=" + this.open + ", filtering=" + this.filtering + "]";
        }

        public RecipeBookSettings.a setOpen(boolean flag) {
            return new RecipeBookSettings.a(flag, this.filtering);
        }

        public RecipeBookSettings.a setFiltering(boolean flag) {
            return new RecipeBookSettings.a(this.open, flag);
        }
    }
}
