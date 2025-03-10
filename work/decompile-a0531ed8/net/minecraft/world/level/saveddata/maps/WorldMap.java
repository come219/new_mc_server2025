package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityItemFrame;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.saveddata.PersistentBase;
import org.slf4j.Logger;

public class WorldMap extends PersistentBase {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAP_SIZE = 128;
    private static final int HALF_MAP_SIZE = 64;
    public static final int MAX_SCALE = 4;
    public static final int TRACKED_DECORATION_LIMIT = 256;
    private static final String FRAME_PREFIX = "frame-";
    public int centerX;
    public int centerZ;
    public ResourceKey<World> dimension;
    public boolean trackingPosition;
    public boolean unlimitedTracking;
    public byte scale;
    public byte[] colors = new byte[16384];
    public boolean locked;
    public final List<WorldMap.WorldMapHumanTracker> carriedBy = Lists.newArrayList();
    public final Map<EntityHuman, WorldMap.WorldMapHumanTracker> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapIconBanner> bannerMarkers = Maps.newHashMap();
    public final Map<String, MapIcon> decorations = Maps.newLinkedHashMap();
    private final Map<String, WorldMapFrame> frameMarkers = Maps.newHashMap();
    private int trackedDecorationCount;

    public static PersistentBase.a<WorldMap> factory() {
        return new PersistentBase.a<>(() -> {
            throw new IllegalStateException("Should never create an empty map saved data");
        }, WorldMap::load, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    private WorldMap(int i, int j, byte b0, boolean flag, boolean flag1, boolean flag2, ResourceKey<World> resourcekey) {
        this.scale = b0;
        this.centerX = i;
        this.centerZ = j;
        this.dimension = resourcekey;
        this.trackingPosition = flag;
        this.unlimitedTracking = flag1;
        this.locked = flag2;
    }

    public static WorldMap createFresh(double d0, double d1, byte b0, boolean flag, boolean flag1, ResourceKey<World> resourcekey) {
        int i = 128 * (1 << b0);
        int j = MathHelper.floor((d0 + 64.0D) / (double) i);
        int k = MathHelper.floor((d1 + 64.0D) / (double) i);
        int l = j * i + i / 2 - 64;
        int i1 = k * i + i / 2 - 64;

        return new WorldMap(l, i1, b0, flag, flag1, false, resourcekey);
    }

    public static WorldMap createForClient(byte b0, boolean flag, ResourceKey<World> resourcekey) {
        return new WorldMap(0, 0, b0, false, false, flag, resourcekey);
    }

    public static WorldMap load(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        DataResult dataresult = DimensionManager.parseLegacy(new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound.get("dimension")));
        Logger logger = WorldMap.LOGGER;

        Objects.requireNonNull(logger);
        ResourceKey<World> resourcekey = (ResourceKey) dataresult.resultOrPartial(logger::error).orElseThrow(() -> {
            return new IllegalArgumentException("Invalid map dimension: " + String.valueOf(nbttagcompound.get("dimension")));
        });
        int i = nbttagcompound.getInt("xCenter");
        int j = nbttagcompound.getInt("zCenter");
        byte b0 = (byte) MathHelper.clamp(nbttagcompound.getByte("scale"), 0, 4);
        boolean flag = !nbttagcompound.contains("trackingPosition", 1) || nbttagcompound.getBoolean("trackingPosition");
        boolean flag1 = nbttagcompound.getBoolean("unlimitedTracking");
        boolean flag2 = nbttagcompound.getBoolean("locked");
        WorldMap worldmap = new WorldMap(i, j, b0, flag, flag1, flag2, resourcekey);
        byte[] abyte = nbttagcompound.getByteArray("colors");

        if (abyte.length == 16384) {
            worldmap.colors = abyte;
        }

        RegistryOps<NBTBase> registryops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);
        List<MapIconBanner> list = (List) MapIconBanner.LIST_CODEC.parse(registryops, nbttagcompound.get("banners")).resultOrPartial((s) -> {
            WorldMap.LOGGER.warn("Failed to parse map banner: '{}'", s);
        }).orElse(List.of());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            MapIconBanner mapiconbanner = (MapIconBanner) iterator.next();

            worldmap.bannerMarkers.put(mapiconbanner.getId(), mapiconbanner);
            worldmap.addDecoration(mapiconbanner.getDecoration(), (GeneratorAccess) null, mapiconbanner.getId(), (double) mapiconbanner.pos().getX(), (double) mapiconbanner.pos().getZ(), 180.0D, (IChatBaseComponent) mapiconbanner.name().orElse((Object) null));
        }

        NBTTagList nbttaglist = nbttagcompound.getList("frames", 10);

        for (int k = 0; k < nbttaglist.size(); ++k) {
            WorldMapFrame worldmapframe = WorldMapFrame.load(nbttaglist.getCompound(k));

            if (worldmapframe != null) {
                worldmap.frameMarkers.put(worldmapframe.getId(), worldmapframe);
                worldmap.addDecoration(MapDecorationTypes.FRAME, (GeneratorAccess) null, getFrameKey(worldmapframe.getEntityId()), (double) worldmapframe.getPos().getX(), (double) worldmapframe.getPos().getZ(), (double) worldmapframe.getRotation(), (IChatBaseComponent) null);
            }
        }

        return worldmap;
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        DataResult dataresult = MinecraftKey.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.dimension.location());
        Logger logger = WorldMap.LOGGER;

        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
            nbttagcompound.put("dimension", nbtbase);
        });
        nbttagcompound.putInt("xCenter", this.centerX);
        nbttagcompound.putInt("zCenter", this.centerZ);
        nbttagcompound.putByte("scale", this.scale);
        nbttagcompound.putByteArray("colors", this.colors);
        nbttagcompound.putBoolean("trackingPosition", this.trackingPosition);
        nbttagcompound.putBoolean("unlimitedTracking", this.unlimitedTracking);
        nbttagcompound.putBoolean("locked", this.locked);
        RegistryOps<NBTBase> registryops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);

        nbttagcompound.put("banners", (NBTBase) MapIconBanner.LIST_CODEC.encodeStart(registryops, List.copyOf(this.bannerMarkers.values())).getOrThrow());
        NBTTagList nbttaglist = new NBTTagList();
        Iterator iterator = this.frameMarkers.values().iterator();

        while (iterator.hasNext()) {
            WorldMapFrame worldmapframe = (WorldMapFrame) iterator.next();

            nbttaglist.add(worldmapframe.save());
        }

        nbttagcompound.put("frames", nbttaglist);
        return nbttagcompound;
    }

    public WorldMap locked() {
        WorldMap worldmap = new WorldMap(this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension);

        worldmap.bannerMarkers.putAll(this.bannerMarkers);
        worldmap.decorations.putAll(this.decorations);
        worldmap.trackedDecorationCount = this.trackedDecorationCount;
        System.arraycopy(this.colors, 0, worldmap.colors, 0, this.colors.length);
        return worldmap;
    }

    public WorldMap scaled() {
        return createFresh((double) this.centerX, (double) this.centerZ, (byte) MathHelper.clamp(this.scale + 1, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension);
    }

    private static Predicate<ItemStack> mapMatcher(ItemStack itemstack) {
        MapId mapid = (MapId) itemstack.get(DataComponents.MAP_ID);

        return (itemstack1) -> {
            return itemstack1 == itemstack ? true : itemstack1.is(itemstack.getItem()) && Objects.equals(mapid, itemstack1.get(DataComponents.MAP_ID));
        };
    }

    public void tickCarriedBy(EntityHuman entityhuman, ItemStack itemstack) {
        if (!this.carriedByPlayers.containsKey(entityhuman)) {
            WorldMap.WorldMapHumanTracker worldmap_worldmaphumantracker = new WorldMap.WorldMapHumanTracker(entityhuman);

            this.carriedByPlayers.put(entityhuman, worldmap_worldmaphumantracker);
            this.carriedBy.add(worldmap_worldmaphumantracker);
        }

        Predicate<ItemStack> predicate = mapMatcher(itemstack);

        if (!entityhuman.getInventory().contains(predicate)) {
            this.removeDecoration(entityhuman.getName().getString());
        }

        for (int i = 0; i < this.carriedBy.size(); ++i) {
            WorldMap.WorldMapHumanTracker worldmap_worldmaphumantracker1 = (WorldMap.WorldMapHumanTracker) this.carriedBy.get(i);
            EntityHuman entityhuman1 = worldmap_worldmaphumantracker1.player;
            String s = entityhuman1.getName().getString();

            if (!entityhuman1.isRemoved() && (entityhuman1.getInventory().contains(predicate) || itemstack.isFramed())) {
                if (!itemstack.isFramed() && entityhuman1.level().dimension() == this.dimension && this.trackingPosition) {
                    this.addDecoration(MapDecorationTypes.PLAYER, entityhuman1.level(), s, entityhuman1.getX(), entityhuman1.getZ(), (double) entityhuman1.getYRot(), (IChatBaseComponent) null);
                }
            } else {
                this.carriedByPlayers.remove(entityhuman1);
                this.carriedBy.remove(worldmap_worldmaphumantracker1);
                this.removeDecoration(s);
            }

            if (!entityhuman1.equals(entityhuman) && hasMapInvisibilityItemEquipped(entityhuman1)) {
                this.removeDecoration(s);
            }
        }

        if (itemstack.isFramed() && this.trackingPosition) {
            EntityItemFrame entityitemframe = itemstack.getFrame();
            BlockPosition blockposition = entityitemframe.getPos();
            WorldMapFrame worldmapframe = (WorldMapFrame) this.frameMarkers.get(WorldMapFrame.frameId(blockposition));

            if (worldmapframe != null && entityitemframe.getId() != worldmapframe.getEntityId() && this.frameMarkers.containsKey(worldmapframe.getId())) {
                this.removeDecoration(getFrameKey(worldmapframe.getEntityId()));
            }

            WorldMapFrame worldmapframe1 = new WorldMapFrame(blockposition, entityitemframe.getDirection().get2DDataValue() * 90, entityitemframe.getId());

            this.addDecoration(MapDecorationTypes.FRAME, entityhuman.level(), getFrameKey(entityitemframe.getId()), (double) blockposition.getX(), (double) blockposition.getZ(), (double) (entityitemframe.getDirection().get2DDataValue() * 90), (IChatBaseComponent) null);
            this.frameMarkers.put(worldmapframe1.getId(), worldmapframe1);
        }

        MapDecorations mapdecorations = (MapDecorations) itemstack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);

        if (!this.decorations.keySet().containsAll(mapdecorations.decorations().keySet())) {
            mapdecorations.decorations().forEach((s1, mapdecorations_a) -> {
                if (!this.decorations.containsKey(s1)) {
                    this.addDecoration(mapdecorations_a.type(), entityhuman.level(), s1, mapdecorations_a.x(), mapdecorations_a.z(), (double) mapdecorations_a.rotation(), (IChatBaseComponent) null);
                }

            });
        }

    }

    private static boolean hasMapInvisibilityItemEquipped(EntityHuman entityhuman) {
        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
        int i = aenumitemslot.length;

        for (int j = 0; j < i; ++j) {
            EnumItemSlot enumitemslot = aenumitemslot[j];

            if (enumitemslot != EnumItemSlot.MAINHAND && enumitemslot != EnumItemSlot.OFFHAND && entityhuman.getItemBySlot(enumitemslot).is(TagsItem.MAP_INVISIBILITY_EQUIPMENT)) {
                return true;
            }
        }

        return false;
    }

    private void removeDecoration(String s) {
        MapIcon mapicon = (MapIcon) this.decorations.remove(s);

        if (mapicon != null && ((MapDecorationType) mapicon.type().value()).trackCount()) {
            --this.trackedDecorationCount;
        }

        this.setDecorationsDirty();
    }

    public static void addTargetDecoration(ItemStack itemstack, BlockPosition blockposition, String s, Holder<MapDecorationType> holder) {
        MapDecorations.a mapdecorations_a = new MapDecorations.a(holder, (double) blockposition.getX(), (double) blockposition.getZ(), 180.0F);

        itemstack.update(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY, (mapdecorations) -> {
            return mapdecorations.withDecoration(s, mapdecorations_a);
        });
        if (((MapDecorationType) holder.value()).hasMapColor()) {
            itemstack.set(DataComponents.MAP_COLOR, new MapItemColor(((MapDecorationType) holder.value()).mapColor()));
        }

    }

    private void addDecoration(Holder<MapDecorationType> holder, @Nullable GeneratorAccess generatoraccess, String s, double d0, double d1, double d2, @Nullable IChatBaseComponent ichatbasecomponent) {
        int i = 1 << this.scale;
        float f = (float) (d0 - (double) this.centerX) / (float) i;
        float f1 = (float) (d1 - (double) this.centerZ) / (float) i;
        WorldMap.b worldmap_b = this.calculateDecorationLocationAndType(holder, generatoraccess, d2, f, f1);

        if (worldmap_b == null) {
            this.removeDecoration(s);
        } else {
            MapIcon mapicon = new MapIcon(worldmap_b.type(), worldmap_b.x(), worldmap_b.y(), worldmap_b.rot(), Optional.ofNullable(ichatbasecomponent));
            MapIcon mapicon1 = (MapIcon) this.decorations.put(s, mapicon);

            if (!mapicon.equals(mapicon1)) {
                if (mapicon1 != null && ((MapDecorationType) mapicon1.type().value()).trackCount()) {
                    --this.trackedDecorationCount;
                }

                if (((MapDecorationType) worldmap_b.type().value()).trackCount()) {
                    ++this.trackedDecorationCount;
                }

                this.setDecorationsDirty();
            }

        }
    }

    @Nullable
    private WorldMap.b calculateDecorationLocationAndType(Holder<MapDecorationType> holder, @Nullable GeneratorAccess generatoraccess, double d0, float f, float f1) {
        byte b0 = clampMapCoordinate(f);
        byte b1 = clampMapCoordinate(f1);

        if (holder.is(MapDecorationTypes.PLAYER)) {
            Pair<Holder<MapDecorationType>, Byte> pair = this.playerDecorationTypeAndRotation(holder, generatoraccess, d0, f, f1);

            return pair == null ? null : new WorldMap.b((Holder) pair.getFirst(), b0, b1, (Byte) pair.getSecond());
        } else {
            return !isInsideMap(f, f1) && !this.unlimitedTracking ? null : new WorldMap.b(holder, b0, b1, this.calculateRotation(generatoraccess, d0));
        }
    }

    @Nullable
    private Pair<Holder<MapDecorationType>, Byte> playerDecorationTypeAndRotation(Holder<MapDecorationType> holder, @Nullable GeneratorAccess generatoraccess, double d0, float f, float f1) {
        if (isInsideMap(f, f1)) {
            return Pair.of(holder, this.calculateRotation(generatoraccess, d0));
        } else {
            Holder<MapDecorationType> holder1 = this.decorationTypeForPlayerOutsideMap(f, f1);

            return holder1 == null ? null : Pair.of(holder1, (byte) 0);
        }
    }

    private byte calculateRotation(@Nullable GeneratorAccess generatoraccess, double d0) {
        if (this.dimension == World.NETHER && generatoraccess != null) {
            int i = (int) (generatoraccess.getLevelData().getDayTime() / 10L);

            return (byte) (i * i * 34187121 + i * 121 >> 15 & 15);
        } else {
            double d1 = d0 < 0.0D ? d0 - 8.0D : d0 + 8.0D;

            return (byte) ((int) (d1 * 16.0D / 360.0D));
        }
    }

    private static boolean isInsideMap(float f, float f1) {
        boolean flag = true;

        return f >= -63.0F && f1 >= -63.0F && f <= 63.0F && f1 <= 63.0F;
    }

    @Nullable
    private Holder<MapDecorationType> decorationTypeForPlayerOutsideMap(float f, float f1) {
        boolean flag = true;
        boolean flag1 = Math.abs(f) < 320.0F && Math.abs(f1) < 320.0F;

        return flag1 ? MapDecorationTypes.PLAYER_OFF_MAP : (this.unlimitedTracking ? MapDecorationTypes.PLAYER_OFF_LIMITS : null);
    }

    private static byte clampMapCoordinate(float f) {
        boolean flag = true;

        return f <= -63.0F ? Byte.MIN_VALUE : (f >= 63.0F ? 127 : (byte) ((int) ((double) (f * 2.0F) + 0.5D)));
    }

    @Nullable
    public Packet<?> getUpdatePacket(MapId mapid, EntityHuman entityhuman) {
        WorldMap.WorldMapHumanTracker worldmap_worldmaphumantracker = (WorldMap.WorldMapHumanTracker) this.carriedByPlayers.get(entityhuman);

        return worldmap_worldmaphumantracker == null ? null : worldmap_worldmaphumantracker.nextUpdatePacket(mapid);
    }

    public void setColorsDirty(int i, int j) {
        this.setDirty();
        Iterator iterator = this.carriedBy.iterator();

        while (iterator.hasNext()) {
            WorldMap.WorldMapHumanTracker worldmap_worldmaphumantracker = (WorldMap.WorldMapHumanTracker) iterator.next();

            worldmap_worldmaphumantracker.markColorsDirty(i, j);
        }

    }

    public void setDecorationsDirty() {
        this.setDirty();
        this.carriedBy.forEach(WorldMap.WorldMapHumanTracker::markDecorationsDirty);
    }

    public WorldMap.WorldMapHumanTracker getHoldingPlayer(EntityHuman entityhuman) {
        WorldMap.WorldMapHumanTracker worldmap_worldmaphumantracker = (WorldMap.WorldMapHumanTracker) this.carriedByPlayers.get(entityhuman);

        if (worldmap_worldmaphumantracker == null) {
            worldmap_worldmaphumantracker = new WorldMap.WorldMapHumanTracker(entityhuman);
            this.carriedByPlayers.put(entityhuman, worldmap_worldmaphumantracker);
            this.carriedBy.add(worldmap_worldmaphumantracker);
        }

        return worldmap_worldmaphumantracker;
    }

    public boolean toggleBanner(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        double d0 = (double) blockposition.getX() + 0.5D;
        double d1 = (double) blockposition.getZ() + 0.5D;
        int i = 1 << this.scale;
        double d2 = (d0 - (double) this.centerX) / (double) i;
        double d3 = (d1 - (double) this.centerZ) / (double) i;
        boolean flag = true;

        if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
            MapIconBanner mapiconbanner = MapIconBanner.fromWorld(generatoraccess, blockposition);

            if (mapiconbanner == null) {
                return false;
            }

            if (this.bannerMarkers.remove(mapiconbanner.getId(), mapiconbanner)) {
                this.removeDecoration(mapiconbanner.getId());
                return true;
            }

            if (!this.isTrackedCountOverLimit(256)) {
                this.bannerMarkers.put(mapiconbanner.getId(), mapiconbanner);
                this.addDecoration(mapiconbanner.getDecoration(), generatoraccess, mapiconbanner.getId(), d0, d1, 180.0D, (IChatBaseComponent) mapiconbanner.name().orElse((Object) null));
                return true;
            }
        }

        return false;
    }

    public void checkBanners(IBlockAccess iblockaccess, int i, int j) {
        Iterator<MapIconBanner> iterator = this.bannerMarkers.values().iterator();

        while (iterator.hasNext()) {
            MapIconBanner mapiconbanner = (MapIconBanner) iterator.next();

            if (mapiconbanner.pos().getX() == i && mapiconbanner.pos().getZ() == j) {
                MapIconBanner mapiconbanner1 = MapIconBanner.fromWorld(iblockaccess, mapiconbanner.pos());

                if (!mapiconbanner.equals(mapiconbanner1)) {
                    iterator.remove();
                    this.removeDecoration(mapiconbanner.getId());
                }
            }
        }

    }

    public Collection<MapIconBanner> getBanners() {
        return this.bannerMarkers.values();
    }

    public void removedFromFrame(BlockPosition blockposition, int i) {
        this.removeDecoration(getFrameKey(i));
        this.frameMarkers.remove(WorldMapFrame.frameId(blockposition));
        this.setDirty();
    }

    public boolean updateColor(int i, int j, byte b0) {
        byte b1 = this.colors[i + j * 128];

        if (b1 != b0) {
            this.setColor(i, j, b0);
            return true;
        } else {
            return false;
        }
    }

    public void setColor(int i, int j, byte b0) {
        this.colors[i + j * 128] = b0;
        this.setColorsDirty(i, j);
    }

    public boolean isExplorationMap() {
        Iterator iterator = this.decorations.values().iterator();

        MapIcon mapicon;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            mapicon = (MapIcon) iterator.next();
        } while (!((MapDecorationType) mapicon.type().value()).explorationMapElement());

        return true;
    }

    public void addClientSideDecorations(List<MapIcon> list) {
        this.decorations.clear();
        this.trackedDecorationCount = 0;

        for (int i = 0; i < list.size(); ++i) {
            MapIcon mapicon = (MapIcon) list.get(i);

            this.decorations.put("icon-" + i, mapicon);
            if (((MapDecorationType) mapicon.type().value()).trackCount()) {
                ++this.trackedDecorationCount;
            }
        }

    }

    public Iterable<MapIcon> getDecorations() {
        return this.decorations.values();
    }

    public boolean isTrackedCountOverLimit(int i) {
        return this.trackedDecorationCount >= i;
    }

    private static String getFrameKey(int i) {
        return "frame-" + i;
    }

    public class WorldMapHumanTracker {

        public final EntityHuman player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private boolean dirtyDecorations = true;
        private int tick;
        public int step;

        WorldMapHumanTracker(final EntityHuman entityhuman) {
            this.player = entityhuman;
        }

        private WorldMap.c createPatch() {
            int i = this.minDirtyX;
            int j = this.minDirtyY;
            int k = this.maxDirtyX + 1 - this.minDirtyX;
            int l = this.maxDirtyY + 1 - this.minDirtyY;
            byte[] abyte = new byte[k * l];

            for (int i1 = 0; i1 < k; ++i1) {
                for (int j1 = 0; j1 < l; ++j1) {
                    abyte[i1 + j1 * k] = WorldMap.this.colors[i + i1 + (j + j1) * 128];
                }
            }

            return new WorldMap.c(i, j, k, l, abyte);
        }

        @Nullable
        Packet<?> nextUpdatePacket(MapId mapid) {
            WorldMap.c worldmap_c;

            if (this.dirtyData) {
                this.dirtyData = false;
                worldmap_c = this.createPatch();
            } else {
                worldmap_c = null;
            }

            Collection collection;

            if (this.dirtyDecorations && this.tick++ % 5 == 0) {
                this.dirtyDecorations = false;
                collection = WorldMap.this.decorations.values();
            } else {
                collection = null;
            }

            return collection == null && worldmap_c == null ? null : new PacketPlayOutMap(mapid, WorldMap.this.scale, WorldMap.this.locked, collection, worldmap_c);
        }

        void markColorsDirty(int i, int j) {
            if (this.dirtyData) {
                this.minDirtyX = Math.min(this.minDirtyX, i);
                this.minDirtyY = Math.min(this.minDirtyY, j);
                this.maxDirtyX = Math.max(this.maxDirtyX, i);
                this.maxDirtyY = Math.max(this.maxDirtyY, j);
            } else {
                this.dirtyData = true;
                this.minDirtyX = i;
                this.minDirtyY = j;
                this.maxDirtyX = i;
                this.maxDirtyY = j;
            }

        }

        private void markDecorationsDirty() {
            this.dirtyDecorations = true;
        }
    }

    private static record b(Holder<MapDecorationType> type, byte x, byte y, byte rot) {

    }

    public static record c(int startX, int startY, int width, int height, byte[] mapColors) {

        public static final StreamCodec<ByteBuf, Optional<WorldMap.c>> STREAM_CODEC = StreamCodec.of(WorldMap.c::write, WorldMap.c::read);

        private static void write(ByteBuf bytebuf, Optional<WorldMap.c> optional) {
            if (optional.isPresent()) {
                WorldMap.c worldmap_c = (WorldMap.c) optional.get();

                bytebuf.writeByte(worldmap_c.width);
                bytebuf.writeByte(worldmap_c.height);
                bytebuf.writeByte(worldmap_c.startX);
                bytebuf.writeByte(worldmap_c.startY);
                PacketDataSerializer.writeByteArray(bytebuf, worldmap_c.mapColors);
            } else {
                bytebuf.writeByte(0);
            }

        }

        private static Optional<WorldMap.c> read(ByteBuf bytebuf) {
            short short0 = bytebuf.readUnsignedByte();

            if (short0 > 0) {
                short short1 = bytebuf.readUnsignedByte();
                short short2 = bytebuf.readUnsignedByte();
                short short3 = bytebuf.readUnsignedByte();
                byte[] abyte = PacketDataSerializer.readByteArray(bytebuf);

                return Optional.of(new WorldMap.c(short2, short3, short0, short1, abyte));
            } else {
                return Optional.empty();
            }
        }

        public void applyToMap(WorldMap worldmap) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    worldmap.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
                }
            }

        }
    }
}
