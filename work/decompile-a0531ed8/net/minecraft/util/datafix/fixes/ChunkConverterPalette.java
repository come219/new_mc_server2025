package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.RegistryID;
import net.minecraft.util.datafix.DataBitsPacked;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import org.slf4j.Logger;

public class ChunkConverterPalette extends DataFix {

    private static final int NORTH_WEST_MASK = 128;
    private static final int WEST_MASK = 64;
    private static final int SOUTH_WEST_MASK = 32;
    private static final int SOUTH_MASK = 16;
    private static final int SOUTH_EAST_MASK = 8;
    private static final int EAST_MASK = 4;
    private static final int NORTH_EAST_MASK = 2;
    private static final int NORTH_MASK = 1;
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIZE = 4096;

    public ChunkConverterPalette(Schema schema, boolean flag) {
        super(schema, flag);
    }

    public static String getName(Dynamic<?> dynamic) {
        return dynamic.get("Name").asString("");
    }

    public static String getProperty(Dynamic<?> dynamic, String s) {
        return dynamic.get("Properties").get(s).asString("");
    }

    public static int idFor(RegistryID<Dynamic<?>> registryid, Dynamic<?> dynamic) {
        int i = registryid.getId(dynamic);

        if (i == -1) {
            i = registryid.add(dynamic);
        }

        return i;
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        Optional<? extends Dynamic<?>> optional = dynamic.get("Level").result();

        return optional.isPresent() && ((Dynamic) optional.get()).get("Sections").asStreamOpt().result().isPresent() ? dynamic.set("Level", (new ChunkConverterPalette.e((Dynamic) optional.get())).write()) : dynamic;
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.CHUNK);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.CHUNK);

        return this.writeFixAndRead("ChunkPalettedStorageFix", type, type1, this::fix);
    }

    public static int getSideMask(boolean flag, boolean flag1, boolean flag2, boolean flag3) {
        int i = 0;

        if (flag2) {
            if (flag1) {
                i |= 2;
            } else if (flag) {
                i |= 128;
            } else {
                i |= 1;
            }
        } else if (flag3) {
            if (flag) {
                i |= 32;
            } else if (flag1) {
                i |= 8;
            } else {
                i |= 16;
            }
        } else if (flag1) {
            i |= 4;
        } else if (flag) {
            i |= 64;
        }

        return i;
    }

    private static final class e {

        private int sides;
        private final ChunkConverterPalette.d[] sections = new ChunkConverterPalette.d[16];
        private final Dynamic<?> level;
        private final int x;
        private final int z;
        private final Int2ObjectMap<Dynamic<?>> blockEntities = new Int2ObjectLinkedOpenHashMap(16);

        public e(Dynamic<?> dynamic) {
            this.level = dynamic;
            this.x = dynamic.get("xPos").asInt(0) << 4;
            this.z = dynamic.get("zPos").asInt(0) << 4;
            dynamic.get("TileEntities").asStreamOpt().ifSuccess((stream) -> {
                stream.forEach((dynamic1) -> {
                    int i = dynamic1.get("x").asInt(0) - this.x & 15;
                    int j = dynamic1.get("y").asInt(0);
                    int k = dynamic1.get("z").asInt(0) - this.z & 15;
                    int l = j << 8 | k << 4 | i;

                    if (this.blockEntities.put(l, dynamic1) != null) {
                        ChunkConverterPalette.LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", new Object[]{this.x, this.z, i, j, k});
                    }

                });
            });
            boolean flag = dynamic.get("convertedFromAlphaFormat").asBoolean(false);

            dynamic.get("Sections").asStreamOpt().ifSuccess((stream) -> {
                stream.forEach((dynamic1) -> {
                    ChunkConverterPalette.d chunkconverterpalette_d = new ChunkConverterPalette.d(dynamic1);

                    this.sides = chunkconverterpalette_d.upgrade(this.sides);
                    this.sections[chunkconverterpalette_d.y] = chunkconverterpalette_d;
                });
            });
            ChunkConverterPalette.d[] achunkconverterpalette_d = this.sections;
            int i = achunkconverterpalette_d.length;

            for (int j = 0; j < i; ++j) {
                ChunkConverterPalette.d chunkconverterpalette_d = achunkconverterpalette_d[j];

                if (chunkconverterpalette_d != null) {
                    ObjectIterator objectiterator = chunkconverterpalette_d.toFix.int2ObjectEntrySet().iterator();

                    label244:
                    while (objectiterator.hasNext()) {
                        Entry<IntList> entry = (Entry) objectiterator.next();
                        int k = chunkconverterpalette_d.y << 12;
                        IntListIterator intlistiterator;
                        int l;
                        Dynamic dynamic1;
                        String s;
                        String s1;
                        Dynamic dynamic2;
                        int i1;
                        String s2;

                        switch (entry.getIntKey()) {
                            case 2:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (intlistiterator.hasNext()) {
                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlock(l);
                                    if ("minecraft:grass_block".equals(ChunkConverterPalette.getName(dynamic1))) {
                                        s = ChunkConverterPalette.getName(this.getBlock(relative(l, ChunkConverterPalette.Direction.UP)));
                                        if ("minecraft:snow".equals(s) || "minecraft:snow_layer".equals(s)) {
                                            this.setBlock(l, ChunkConverterPalette.c.SNOWY_GRASS);
                                        }
                                    }
                                }
                                break;
                            case 3:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlock(l);
                                    if ("minecraft:podzol".equals(ChunkConverterPalette.getName(dynamic1))) {
                                        s = ChunkConverterPalette.getName(this.getBlock(relative(l, ChunkConverterPalette.Direction.UP)));
                                        if ("minecraft:snow".equals(s) || "minecraft:snow_layer".equals(s)) {
                                            this.setBlock(l, ChunkConverterPalette.c.SNOWY_PODZOL);
                                        }
                                    }
                                }
                            case 25:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.removeBlockEntity(l);
                                    if (dynamic1 != null) {
                                        s1 = Boolean.toString(dynamic1.get("powered").asBoolean(false));
                                        s = s1 + (byte) Math.min(Math.max(dynamic1.get("note").asInt(0), 0), 24);
                                        this.setBlock(l, (Dynamic) ChunkConverterPalette.c.NOTE_BLOCK_MAP.getOrDefault(s, (Dynamic) ChunkConverterPalette.c.NOTE_BLOCK_MAP.get("false0")));
                                    }
                                }
                            case 26:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlockEntity(l);
                                    dynamic2 = this.getBlock(l);
                                    if (dynamic1 != null) {
                                        i1 = dynamic1.get("color").asInt(0);
                                        if (i1 != 14 && i1 >= 0 && i1 < 16) {
                                            s1 = ChunkConverterPalette.getProperty(dynamic2, "facing");
                                            s2 = s1 + ChunkConverterPalette.getProperty(dynamic2, "occupied") + ChunkConverterPalette.getProperty(dynamic2, "part") + i1;
                                            if (ChunkConverterPalette.c.BED_BLOCK_MAP.containsKey(s2)) {
                                                this.setBlock(l, (Dynamic) ChunkConverterPalette.c.BED_BLOCK_MAP.get(s2));
                                            }
                                        }
                                    }
                                }
                            case 64:
                            case 71:
                            case 193:
                            case 194:
                            case 195:
                            case 196:
                            case 197:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlock(l);
                                    if (ChunkConverterPalette.getName(dynamic1).endsWith("_door")) {
                                        dynamic2 = this.getBlock(l);
                                        if ("lower".equals(ChunkConverterPalette.getProperty(dynamic2, "half"))) {
                                            i1 = relative(l, ChunkConverterPalette.Direction.UP);
                                            Dynamic<?> dynamic3 = this.getBlock(i1);
                                            String s3 = ChunkConverterPalette.getName(dynamic2);

                                            if (s3.equals(ChunkConverterPalette.getName(dynamic3))) {
                                                String s4 = ChunkConverterPalette.getProperty(dynamic2, "facing");
                                                String s5 = ChunkConverterPalette.getProperty(dynamic2, "open");
                                                String s6 = flag ? "left" : ChunkConverterPalette.getProperty(dynamic3, "hinge");
                                                String s7 = flag ? "false" : ChunkConverterPalette.getProperty(dynamic3, "powered");

                                                this.setBlock(l, (Dynamic) ChunkConverterPalette.c.DOOR_MAP.get(s3 + s4 + "lower" + s6 + s5 + s7));
                                                this.setBlock(i1, (Dynamic) ChunkConverterPalette.c.DOOR_MAP.get(s3 + s4 + "upper" + s6 + s5 + s7));
                                            }
                                        }
                                    }
                                }
                            case 86:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlock(l);
                                    if ("minecraft:carved_pumpkin".equals(ChunkConverterPalette.getName(dynamic1))) {
                                        s = ChunkConverterPalette.getName(this.getBlock(relative(l, ChunkConverterPalette.Direction.DOWN)));
                                        if ("minecraft:grass_block".equals(s) || "minecraft:dirt".equals(s)) {
                                            this.setBlock(l, ChunkConverterPalette.c.PUMPKIN);
                                        }
                                    }
                                }
                            case 110:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlock(l);
                                    if ("minecraft:mycelium".equals(ChunkConverterPalette.getName(dynamic1))) {
                                        s = ChunkConverterPalette.getName(this.getBlock(relative(l, ChunkConverterPalette.Direction.UP)));
                                        if ("minecraft:snow".equals(s) || "minecraft:snow_layer".equals(s)) {
                                            this.setBlock(l, ChunkConverterPalette.c.SNOWY_MYCELIUM);
                                        }
                                    }
                                }
                            case 140:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.removeBlockEntity(l);
                                    if (dynamic1 != null) {
                                        s1 = dynamic1.get("Item").asString("");
                                        s = s1 + dynamic1.get("Data").asInt(0);
                                        this.setBlock(l, (Dynamic) ChunkConverterPalette.c.FLOWER_POT_MAP.getOrDefault(s, (Dynamic) ChunkConverterPalette.c.FLOWER_POT_MAP.get("minecraft:air0")));
                                    }
                                }
                            case 144:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlockEntity(l);
                                    if (dynamic1 != null) {
                                        s = String.valueOf(dynamic1.get("SkullType").asInt(0));
                                        s8 = ChunkConverterPalette.getProperty(this.getBlock(l), "facing");
                                        if (!"up".equals(s8) && !"down".equals(s8)) {
                                            s2 = s + s8;
                                        } else {
                                            s2 = s + String.valueOf(dynamic1.get("Rot").asInt(0));
                                        }

                                        dynamic1.remove("SkullType");
                                        dynamic1.remove("facing");
                                        dynamic1.remove("Rot");
                                        this.setBlock(l, (Dynamic) ChunkConverterPalette.c.SKULL_MAP.getOrDefault(s2, (Dynamic) ChunkConverterPalette.c.SKULL_MAP.get("0north")));
                                    }
                                }
                            case 175:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (true) {
                                    if (!intlistiterator.hasNext()) {
                                        continue label244;
                                    }

                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlock(l);
                                    if ("upper".equals(ChunkConverterPalette.getProperty(dynamic1, "half"))) {
                                        dynamic2 = this.getBlock(relative(l, ChunkConverterPalette.Direction.DOWN));
                                        switch (ChunkConverterPalette.getName(dynamic2)) {
                                            case "minecraft:sunflower":
                                                this.setBlock(l, ChunkConverterPalette.c.UPPER_SUNFLOWER);
                                                break;
                                            case "minecraft:lilac":
                                                this.setBlock(l, ChunkConverterPalette.c.UPPER_LILAC);
                                                break;
                                            case "minecraft:tall_grass":
                                                this.setBlock(l, ChunkConverterPalette.c.UPPER_TALL_GRASS);
                                                break;
                                            case "minecraft:large_fern":
                                                this.setBlock(l, ChunkConverterPalette.c.UPPER_LARGE_FERN);
                                                break;
                                            case "minecraft:rose_bush":
                                                this.setBlock(l, ChunkConverterPalette.c.UPPER_ROSE_BUSH);
                                                break;
                                            case "minecraft:peony":
                                                this.setBlock(l, ChunkConverterPalette.c.UPPER_PEONY);
                                        }
                                    }
                                }
                            case 176:
                            case 177:
                                intlistiterator = ((IntList) entry.getValue()).iterator();

                                while (intlistiterator.hasNext()) {
                                    l = (Integer) intlistiterator.next();
                                    l |= k;
                                    dynamic1 = this.getBlockEntity(l);
                                    dynamic2 = this.getBlock(l);
                                    if (dynamic1 != null) {
                                        i1 = dynamic1.get("Base").asInt(0);
                                        if (i1 != 15 && i1 >= 0 && i1 < 16) {
                                            s1 = ChunkConverterPalette.getProperty(dynamic2, entry.getIntKey() == 176 ? "rotation" : "facing");
                                            s2 = s1 + "_" + i1;
                                            if (ChunkConverterPalette.c.BANNER_BLOCK_MAP.containsKey(s2)) {
                                                this.setBlock(l, (Dynamic) ChunkConverterPalette.c.BANNER_BLOCK_MAP.get(s2));
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }

        }

        @Nullable
        private Dynamic<?> getBlockEntity(int i) {
            return (Dynamic) this.blockEntities.get(i);
        }

        @Nullable
        private Dynamic<?> removeBlockEntity(int i) {
            return (Dynamic) this.blockEntities.remove(i);
        }

        public static int relative(int i, ChunkConverterPalette.Direction chunkconverterpalette_direction) {
            int j;
            int k;

            switch (chunkconverterpalette_direction.getAxis().ordinal()) {
                case 0:
                    j = (i & 15) + chunkconverterpalette_direction.getAxisDirection().getStep();
                    k = j >= 0 && j <= 15 ? i & -16 | j : -1;
                    break;
                case 1:
                    j = (i >> 8) + chunkconverterpalette_direction.getAxisDirection().getStep();
                    k = j >= 0 && j <= 255 ? i & 255 | j << 8 : -1;
                    break;
                case 2:
                    j = (i >> 4 & 15) + chunkconverterpalette_direction.getAxisDirection().getStep();
                    k = j >= 0 && j <= 15 ? i & -241 | j << 4 : -1;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return k;
        }

        private void setBlock(int i, Dynamic<?> dynamic) {
            if (i >= 0 && i <= 65535) {
                ChunkConverterPalette.d chunkconverterpalette_d = this.getSection(i);

                if (chunkconverterpalette_d != null) {
                    chunkconverterpalette_d.setBlock(i & 4095, dynamic);
                }
            }
        }

        @Nullable
        private ChunkConverterPalette.d getSection(int i) {
            int j = i >> 12;

            return j < this.sections.length ? this.sections[j] : null;
        }

        public Dynamic<?> getBlock(int i) {
            if (i >= 0 && i <= 65535) {
                ChunkConverterPalette.d chunkconverterpalette_d = this.getSection(i);

                return chunkconverterpalette_d == null ? ChunkConverterPalette.c.AIR : chunkconverterpalette_d.getBlock(i & 4095);
            } else {
                return ChunkConverterPalette.c.AIR;
            }
        }

        public Dynamic<?> write() {
            Dynamic<?> dynamic = this.level;

            if (this.blockEntities.isEmpty()) {
                dynamic = dynamic.remove("TileEntities");
            } else {
                dynamic = dynamic.set("TileEntities", dynamic.createList(this.blockEntities.values().stream()));
            }

            Dynamic<?> dynamic1 = dynamic.emptyMap();
            List<Dynamic<?>> list = Lists.newArrayList();
            ChunkConverterPalette.d[] achunkconverterpalette_d = this.sections;
            int i = achunkconverterpalette_d.length;

            for (int j = 0; j < i; ++j) {
                ChunkConverterPalette.d chunkconverterpalette_d = achunkconverterpalette_d[j];

                if (chunkconverterpalette_d != null) {
                    list.add(chunkconverterpalette_d.write());
                    dynamic1 = dynamic1.set(String.valueOf(chunkconverterpalette_d.y), dynamic1.createIntList(Arrays.stream(chunkconverterpalette_d.update.toIntArray())));
                }
            }

            Dynamic<?> dynamic2 = dynamic.emptyMap();

            dynamic2 = dynamic2.set("Sides", dynamic2.createByte((byte) this.sides));
            dynamic2 = dynamic2.set("Indices", dynamic1);
            return dynamic.set("UpgradeData", dynamic2).set("Sections", dynamic2.createList(list.stream()));
        }
    }

    public static enum Direction {

        DOWN(ChunkConverterPalette.Direction.AxisDirection.NEGATIVE, ChunkConverterPalette.Direction.Axis.Y), UP(ChunkConverterPalette.Direction.AxisDirection.POSITIVE, ChunkConverterPalette.Direction.Axis.Y), NORTH(ChunkConverterPalette.Direction.AxisDirection.NEGATIVE, ChunkConverterPalette.Direction.Axis.Z), SOUTH(ChunkConverterPalette.Direction.AxisDirection.POSITIVE, ChunkConverterPalette.Direction.Axis.Z), WEST(ChunkConverterPalette.Direction.AxisDirection.NEGATIVE, ChunkConverterPalette.Direction.Axis.X), EAST(ChunkConverterPalette.Direction.AxisDirection.POSITIVE, ChunkConverterPalette.Direction.Axis.X);

        private final ChunkConverterPalette.Direction.Axis axis;
        private final ChunkConverterPalette.Direction.AxisDirection axisDirection;

        private Direction(final ChunkConverterPalette.Direction.AxisDirection chunkconverterpalette_direction_axisdirection, final ChunkConverterPalette.Direction.Axis chunkconverterpalette_direction_axis) {
            this.axis = chunkconverterpalette_direction_axis;
            this.axisDirection = chunkconverterpalette_direction_axisdirection;
        }

        public ChunkConverterPalette.Direction.AxisDirection getAxisDirection() {
            return this.axisDirection;
        }

        public ChunkConverterPalette.Direction.Axis getAxis() {
            return this.axis;
        }

        public static enum Axis {

            X, Y, Z;

            private Axis() {}
        }

        public static enum AxisDirection {

            POSITIVE(1), NEGATIVE(-1);

            private final int step;

            private AxisDirection(final int i) {
                this.step = i;
            }

            public int getStep() {
                return this.step;
            }
        }
    }

    private static class a {

        private static final int SIZE = 2048;
        private static final int NIBBLE_SIZE = 4;
        private final byte[] data;

        public a() {
            this.data = new byte[2048];
        }

        public a(byte[] abyte) {
            this.data = abyte;
            if (abyte.length != 2048) {
                throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + abyte.length);
            }
        }

        public int get(int i, int j, int k) {
            int l = this.getPosition(j << 8 | k << 4 | i);

            return this.isFirst(j << 8 | k << 4 | i) ? this.data[l] & 15 : this.data[l] >> 4 & 15;
        }

        private boolean isFirst(int i) {
            return (i & 1) == 0;
        }

        private int getPosition(int i) {
            return i >> 1;
        }
    }

    private static class d {

        private final RegistryID<Dynamic<?>> palette = RegistryID.create(32);
        private final List<Dynamic<?>> listTag = Lists.newArrayList();
        private final Dynamic<?> section;
        private final boolean hasData;
        final Int2ObjectMap<IntList> toFix = new Int2ObjectLinkedOpenHashMap();
        final IntList update = new IntArrayList();
        public final int y;
        private final Set<Dynamic<?>> seen = Sets.newIdentityHashSet();
        private final int[] buffer = new int[4096];

        public d(Dynamic<?> dynamic) {
            this.section = dynamic;
            this.y = dynamic.get("Y").asInt(0);
            this.hasData = dynamic.get("Blocks").result().isPresent();
        }

        public Dynamic<?> getBlock(int i) {
            if (i >= 0 && i <= 4095) {
                Dynamic<?> dynamic = (Dynamic) this.palette.byId(this.buffer[i]);

                return dynamic == null ? ChunkConverterPalette.c.AIR : dynamic;
            } else {
                return ChunkConverterPalette.c.AIR;
            }
        }

        public void setBlock(int i, Dynamic<?> dynamic) {
            if (this.seen.add(dynamic)) {
                this.listTag.add("%%FILTER_ME%%".equals(ChunkConverterPalette.getName(dynamic)) ? ChunkConverterPalette.c.AIR : dynamic);
            }

            this.buffer[i] = ChunkConverterPalette.idFor(this.palette, dynamic);
        }

        public int upgrade(int i) {
            if (!this.hasData) {
                return i;
            } else {
                ByteBuffer bytebuffer = (ByteBuffer) this.section.get("Blocks").asByteBufferOpt().result().get();
                ChunkConverterPalette.a chunkconverterpalette_a = (ChunkConverterPalette.a) this.section.get("Data").asByteBufferOpt().map((bytebuffer1) -> {
                    return new ChunkConverterPalette.a(DataFixUtils.toArray(bytebuffer1));
                }).result().orElseGet(ChunkConverterPalette.a::new);
                ChunkConverterPalette.a chunkconverterpalette_a1 = (ChunkConverterPalette.a) this.section.get("Add").asByteBufferOpt().map((bytebuffer1) -> {
                    return new ChunkConverterPalette.a(DataFixUtils.toArray(bytebuffer1));
                }).result().orElseGet(ChunkConverterPalette.a::new);

                this.seen.add(ChunkConverterPalette.c.AIR);
                ChunkConverterPalette.idFor(this.palette, ChunkConverterPalette.c.AIR);
                this.listTag.add(ChunkConverterPalette.c.AIR);

                for (int j = 0; j < 4096; ++j) {
                    int k = j & 15;
                    int l = j >> 8 & 15;
                    int i1 = j >> 4 & 15;
                    int j1 = chunkconverterpalette_a1.get(k, l, i1) << 12 | (bytebuffer.get(j) & 255) << 4 | chunkconverterpalette_a.get(k, l, i1);

                    if (ChunkConverterPalette.c.FIX.get(j1 >> 4)) {
                        this.addFix(j1 >> 4, j);
                    }

                    if (ChunkConverterPalette.c.VIRTUAL.get(j1 >> 4)) {
                        int k1 = ChunkConverterPalette.getSideMask(k == 0, k == 15, i1 == 0, i1 == 15);

                        if (k1 == 0) {
                            this.update.add(j);
                        } else {
                            i |= k1;
                        }
                    }

                    this.setBlock(j, DataConverterFlattenData.getTag(j1));
                }

                return i;
            }
        }

        private void addFix(int i, int j) {
            Object object = (IntList) this.toFix.get(i);

            if (object == null) {
                object = new IntArrayList();
                this.toFix.put(i, object);
            }

            ((IntList) object).add(j);
        }

        public Dynamic<?> write() {
            Dynamic<?> dynamic = this.section;

            if (!this.hasData) {
                return dynamic;
            } else {
                dynamic = dynamic.set("Palette", dynamic.createList(this.listTag.stream()));
                int i = Math.max(4, DataFixUtils.ceillog2(this.seen.size()));
                DataBitsPacked databitspacked = new DataBitsPacked(i, 4096);

                for (int j = 0; j < this.buffer.length; ++j) {
                    databitspacked.set(j, this.buffer[j]);
                }

                dynamic = dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(databitspacked.getRaw())));
                dynamic = dynamic.remove("Blocks");
                dynamic = dynamic.remove("Data");
                dynamic = dynamic.remove("Add");
                return dynamic;
            }
        }
    }

    private static class c {

        static final BitSet VIRTUAL = new BitSet(256);
        static final BitSet FIX = new BitSet(256);
        static final Dynamic<?> PUMPKIN = ExtraDataFixUtils.blockState("minecraft:pumpkin");
        static final Dynamic<?> SNOWY_PODZOL = ExtraDataFixUtils.blockState("minecraft:podzol", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_GRASS = ExtraDataFixUtils.blockState("minecraft:grass_block", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_MYCELIUM = ExtraDataFixUtils.blockState("minecraft:mycelium", Map.of("snowy", "true"));
        static final Dynamic<?> UPPER_SUNFLOWER = ExtraDataFixUtils.blockState("minecraft:sunflower", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_LILAC = ExtraDataFixUtils.blockState("minecraft:lilac", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_TALL_GRASS = ExtraDataFixUtils.blockState("minecraft:tall_grass", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_LARGE_FERN = ExtraDataFixUtils.blockState("minecraft:large_fern", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_ROSE_BUSH = ExtraDataFixUtils.blockState("minecraft:rose_bush", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_PEONY = ExtraDataFixUtils.blockState("minecraft:peony", Map.of("half", "upper"));
        static final Map<String, Dynamic<?>> FLOWER_POT_MAP = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
            hashmap.put("minecraft:air0", ExtraDataFixUtils.blockState("minecraft:flower_pot"));
            hashmap.put("minecraft:red_flower0", ExtraDataFixUtils.blockState("minecraft:potted_poppy"));
            hashmap.put("minecraft:red_flower1", ExtraDataFixUtils.blockState("minecraft:potted_blue_orchid"));
            hashmap.put("minecraft:red_flower2", ExtraDataFixUtils.blockState("minecraft:potted_allium"));
            hashmap.put("minecraft:red_flower3", ExtraDataFixUtils.blockState("minecraft:potted_azure_bluet"));
            hashmap.put("minecraft:red_flower4", ExtraDataFixUtils.blockState("minecraft:potted_red_tulip"));
            hashmap.put("minecraft:red_flower5", ExtraDataFixUtils.blockState("minecraft:potted_orange_tulip"));
            hashmap.put("minecraft:red_flower6", ExtraDataFixUtils.blockState("minecraft:potted_white_tulip"));
            hashmap.put("minecraft:red_flower7", ExtraDataFixUtils.blockState("minecraft:potted_pink_tulip"));
            hashmap.put("minecraft:red_flower8", ExtraDataFixUtils.blockState("minecraft:potted_oxeye_daisy"));
            hashmap.put("minecraft:yellow_flower0", ExtraDataFixUtils.blockState("minecraft:potted_dandelion"));
            hashmap.put("minecraft:sapling0", ExtraDataFixUtils.blockState("minecraft:potted_oak_sapling"));
            hashmap.put("minecraft:sapling1", ExtraDataFixUtils.blockState("minecraft:potted_spruce_sapling"));
            hashmap.put("minecraft:sapling2", ExtraDataFixUtils.blockState("minecraft:potted_birch_sapling"));
            hashmap.put("minecraft:sapling3", ExtraDataFixUtils.blockState("minecraft:potted_jungle_sapling"));
            hashmap.put("minecraft:sapling4", ExtraDataFixUtils.blockState("minecraft:potted_acacia_sapling"));
            hashmap.put("minecraft:sapling5", ExtraDataFixUtils.blockState("minecraft:potted_dark_oak_sapling"));
            hashmap.put("minecraft:red_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_red_mushroom"));
            hashmap.put("minecraft:brown_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_brown_mushroom"));
            hashmap.put("minecraft:deadbush0", ExtraDataFixUtils.blockState("minecraft:potted_dead_bush"));
            hashmap.put("minecraft:tallgrass2", ExtraDataFixUtils.blockState("minecraft:potted_fern"));
            hashmap.put("minecraft:cactus0", ExtraDataFixUtils.blockState("minecraft:potted_cactus"));
        });
        static final Map<String, Dynamic<?>> SKULL_MAP = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
            mapSkull(hashmap, 0, "skeleton", "skull");
            mapSkull(hashmap, 1, "wither_skeleton", "skull");
            mapSkull(hashmap, 2, "zombie", "head");
            mapSkull(hashmap, 3, "player", "head");
            mapSkull(hashmap, 4, "creeper", "head");
            mapSkull(hashmap, 5, "dragon", "head");
        });
        static final Map<String, Dynamic<?>> DOOR_MAP = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
            mapDoor(hashmap, "oak_door");
            mapDoor(hashmap, "iron_door");
            mapDoor(hashmap, "spruce_door");
            mapDoor(hashmap, "birch_door");
            mapDoor(hashmap, "jungle_door");
            mapDoor(hashmap, "acacia_door");
            mapDoor(hashmap, "dark_oak_door");
        });
        static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
            for (int i = 0; i < 26; ++i) {
                hashmap.put("true" + i, ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "true", "note", String.valueOf(i))));
                hashmap.put("false" + i, ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "false", "note", String.valueOf(i))));
            }

        });
        private static final Int2ObjectMap<String> DYE_COLOR_MAP = (Int2ObjectMap) DataFixUtils.make(new Int2ObjectOpenHashMap(), (int2objectopenhashmap) -> {
            int2objectopenhashmap.put(0, "white");
            int2objectopenhashmap.put(1, "orange");
            int2objectopenhashmap.put(2, "magenta");
            int2objectopenhashmap.put(3, "light_blue");
            int2objectopenhashmap.put(4, "yellow");
            int2objectopenhashmap.put(5, "lime");
            int2objectopenhashmap.put(6, "pink");
            int2objectopenhashmap.put(7, "gray");
            int2objectopenhashmap.put(8, "light_gray");
            int2objectopenhashmap.put(9, "cyan");
            int2objectopenhashmap.put(10, "purple");
            int2objectopenhashmap.put(11, "blue");
            int2objectopenhashmap.put(12, "brown");
            int2objectopenhashmap.put(13, "green");
            int2objectopenhashmap.put(14, "red");
            int2objectopenhashmap.put(15, "black");
        });
        static final Map<String, Dynamic<?>> BED_BLOCK_MAP = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
            ObjectIterator objectiterator = ChunkConverterPalette.c.DYE_COLOR_MAP.int2ObjectEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<String> entry = (Entry) objectiterator.next();

                if (!Objects.equals(entry.getValue(), "red")) {
                    addBeds(hashmap, entry.getIntKey(), (String) entry.getValue());
                }
            }

        });
        static final Map<String, Dynamic<?>> BANNER_BLOCK_MAP = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
            ObjectIterator objectiterator = ChunkConverterPalette.c.DYE_COLOR_MAP.int2ObjectEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<String> entry = (Entry) objectiterator.next();

                if (!Objects.equals(entry.getValue(), "white")) {
                    addBanners(hashmap, 15 - entry.getIntKey(), (String) entry.getValue());
                }
            }

        });
        static final Dynamic<?> AIR;

        private c() {}

        private static void mapSkull(Map<String, Dynamic<?>> map, int i, String s, String s1) {
            map.put("" + i + "north", ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_" + s1, Map.of("facing", "north")));
            map.put("" + i + "east", ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_" + s1, Map.of("facing", "east")));
            map.put("" + i + "south", ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_" + s1, Map.of("facing", "south")));
            map.put("" + i + "west", ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_" + s1, Map.of("facing", "west")));

            for (int j = 0; j < 16; ++j) {
                map.put("" + i + j, ExtraDataFixUtils.blockState("minecraft:" + s + "_" + s1, Map.of("rotation", String.valueOf(j))));
            }

        }

        private static void mapDoor(Map<String, Dynamic<?>> map, String s) {
            String s1 = "minecraft:" + s;

            map.put("minecraft:" + s + "eastlowerleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "eastlowerleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "eastlowerlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "eastlowerlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "eastlowerrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "eastlowerrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "eastlowerrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "eastlowerrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "eastupperleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "eastupperleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "eastupperlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "eastupperlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "eastupperrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "eastupperrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "eastupperrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "eastupperrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "northlowerleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "northlowerleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "northlowerlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "northlowerlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "northlowerrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "northlowerrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "northlowerrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "northlowerrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "northupperleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "northupperleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "northupperlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "northupperlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "northupperrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "northupperrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "northupperrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "northupperrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "southlowerleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "southlowerleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "southlowerlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "southlowerlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "southlowerrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "southlowerrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "southlowerrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "southlowerrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "southupperleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "southupperleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "southupperlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "southupperlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "southupperrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "southupperrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "southupperrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "southupperrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "westlowerleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "westlowerleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "westlowerlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "westlowerlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "westlowerrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "westlowerrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "westlowerrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "westlowerrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "westupperleftfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "westupperleftfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "westupperlefttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "westupperlefttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + s + "westupperrightfalsefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + s + "westupperrightfalsetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + s + "westupperrighttruefalse", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + s + "westupperrighttruetrue", ExtraDataFixUtils.blockState(s1, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
        }

        private static void addBeds(Map<String, Dynamic<?>> map, int i, String s) {
            map.put("southfalsefoot" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "south", "occupied", "false", "part", "foot")));
            map.put("westfalsefoot" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "west", "occupied", "false", "part", "foot")));
            map.put("northfalsefoot" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "north", "occupied", "false", "part", "foot")));
            map.put("eastfalsefoot" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "east", "occupied", "false", "part", "foot")));
            map.put("southfalsehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "south", "occupied", "false", "part", "head")));
            map.put("westfalsehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "west", "occupied", "false", "part", "head")));
            map.put("northfalsehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "north", "occupied", "false", "part", "head")));
            map.put("eastfalsehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "east", "occupied", "false", "part", "head")));
            map.put("southtruehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "south", "occupied", "true", "part", "head")));
            map.put("westtruehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "west", "occupied", "true", "part", "head")));
            map.put("northtruehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "north", "occupied", "true", "part", "head")));
            map.put("easttruehead" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_bed", Map.of("facing", "east", "occupied", "true", "part", "head")));
        }

        private static void addBanners(Map<String, Dynamic<?>> map, int i, String s) {
            for (int j = 0; j < 16; ++j) {
                map.put("" + j + "_" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_banner", Map.of("rotation", String.valueOf(j))));
            }

            map.put("north_" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_banner", Map.of("facing", "north")));
            map.put("south_" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_banner", Map.of("facing", "south")));
            map.put("west_" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_banner", Map.of("facing", "west")));
            map.put("east_" + i, ExtraDataFixUtils.blockState("minecraft:" + s + "_wall_banner", Map.of("facing", "east")));
        }

        static {
            ChunkConverterPalette.c.FIX.set(2);
            ChunkConverterPalette.c.FIX.set(3);
            ChunkConverterPalette.c.FIX.set(110);
            ChunkConverterPalette.c.FIX.set(140);
            ChunkConverterPalette.c.FIX.set(144);
            ChunkConverterPalette.c.FIX.set(25);
            ChunkConverterPalette.c.FIX.set(86);
            ChunkConverterPalette.c.FIX.set(26);
            ChunkConverterPalette.c.FIX.set(176);
            ChunkConverterPalette.c.FIX.set(177);
            ChunkConverterPalette.c.FIX.set(175);
            ChunkConverterPalette.c.FIX.set(64);
            ChunkConverterPalette.c.FIX.set(71);
            ChunkConverterPalette.c.FIX.set(193);
            ChunkConverterPalette.c.FIX.set(194);
            ChunkConverterPalette.c.FIX.set(195);
            ChunkConverterPalette.c.FIX.set(196);
            ChunkConverterPalette.c.FIX.set(197);
            ChunkConverterPalette.c.VIRTUAL.set(54);
            ChunkConverterPalette.c.VIRTUAL.set(146);
            ChunkConverterPalette.c.VIRTUAL.set(25);
            ChunkConverterPalette.c.VIRTUAL.set(26);
            ChunkConverterPalette.c.VIRTUAL.set(51);
            ChunkConverterPalette.c.VIRTUAL.set(53);
            ChunkConverterPalette.c.VIRTUAL.set(67);
            ChunkConverterPalette.c.VIRTUAL.set(108);
            ChunkConverterPalette.c.VIRTUAL.set(109);
            ChunkConverterPalette.c.VIRTUAL.set(114);
            ChunkConverterPalette.c.VIRTUAL.set(128);
            ChunkConverterPalette.c.VIRTUAL.set(134);
            ChunkConverterPalette.c.VIRTUAL.set(135);
            ChunkConverterPalette.c.VIRTUAL.set(136);
            ChunkConverterPalette.c.VIRTUAL.set(156);
            ChunkConverterPalette.c.VIRTUAL.set(163);
            ChunkConverterPalette.c.VIRTUAL.set(164);
            ChunkConverterPalette.c.VIRTUAL.set(180);
            ChunkConverterPalette.c.VIRTUAL.set(203);
            ChunkConverterPalette.c.VIRTUAL.set(55);
            ChunkConverterPalette.c.VIRTUAL.set(85);
            ChunkConverterPalette.c.VIRTUAL.set(113);
            ChunkConverterPalette.c.VIRTUAL.set(188);
            ChunkConverterPalette.c.VIRTUAL.set(189);
            ChunkConverterPalette.c.VIRTUAL.set(190);
            ChunkConverterPalette.c.VIRTUAL.set(191);
            ChunkConverterPalette.c.VIRTUAL.set(192);
            ChunkConverterPalette.c.VIRTUAL.set(93);
            ChunkConverterPalette.c.VIRTUAL.set(94);
            ChunkConverterPalette.c.VIRTUAL.set(101);
            ChunkConverterPalette.c.VIRTUAL.set(102);
            ChunkConverterPalette.c.VIRTUAL.set(160);
            ChunkConverterPalette.c.VIRTUAL.set(106);
            ChunkConverterPalette.c.VIRTUAL.set(107);
            ChunkConverterPalette.c.VIRTUAL.set(183);
            ChunkConverterPalette.c.VIRTUAL.set(184);
            ChunkConverterPalette.c.VIRTUAL.set(185);
            ChunkConverterPalette.c.VIRTUAL.set(186);
            ChunkConverterPalette.c.VIRTUAL.set(187);
            ChunkConverterPalette.c.VIRTUAL.set(132);
            ChunkConverterPalette.c.VIRTUAL.set(139);
            ChunkConverterPalette.c.VIRTUAL.set(199);
            AIR = ExtraDataFixUtils.blockState("minecraft:air");
        }
    }
}
