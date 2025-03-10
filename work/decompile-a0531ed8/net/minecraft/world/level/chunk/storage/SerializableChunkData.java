package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NbtException;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.village.poi.VillagePlace;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkConverter;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.NibbleArray;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunkExtension;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTickList;
import net.minecraft.world.ticks.TickListChunk;
import org.slf4j.Logger;

public record SerializableChunkData(IRegistry<BiomeBase> biomeRegistry, ChunkCoordIntPair chunkPos, int minSectionY, long lastUpdateTime, long inhabitedTime, ChunkStatus chunkStatus, @Nullable BlendingData.d blendingData, @Nullable BelowZeroRetrogen belowZeroRetrogen, ChunkConverter upgradeData, @Nullable long[] carvingMask, Map<HeightMap.Type, long[]> heightmaps, IChunkAccess.a packedTicks, ShortList[] postProcessingSections, boolean lightCorrect, List<SerializableChunkData.b> sectionData, List<NBTTagCompound> entities, List<NBTTagCompound> blockEntities, NBTTagCompound structureData) {

    public static final Codec<DataPaletteBlock<IBlockData>> BLOCK_STATE_CODEC = DataPaletteBlock.codecRW(Block.BLOCK_STATE_REGISTRY, IBlockData.CODEC, DataPaletteBlock.d.SECTION_STATES, Blocks.AIR.defaultBlockState());
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    @Nullable
    public static SerializableChunkData parse(LevelHeightAccessor levelheightaccessor, IRegistryCustom iregistrycustom, NBTTagCompound nbttagcompound) {
        if (!nbttagcompound.contains("Status", 8)) {
            return null;
        } else {
            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(nbttagcompound.getInt("xPos"), nbttagcompound.getInt("zPos"));
            long i = nbttagcompound.getLong("LastUpdate");
            long j = nbttagcompound.getLong("InhabitedTime");
            ChunkStatus chunkstatus = ChunkStatus.byName(nbttagcompound.getString("Status"));
            ChunkConverter chunkconverter = nbttagcompound.contains("UpgradeData", 10) ? new ChunkConverter(nbttagcompound.getCompound("UpgradeData"), levelheightaccessor) : ChunkConverter.EMPTY;
            boolean flag = nbttagcompound.getBoolean("isLightOn");
            DataResult dataresult;
            Logger logger;
            BlendingData.d blendingdata_d;

            if (nbttagcompound.contains("blending_data", 10)) {
                dataresult = BlendingData.d.CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound.getCompound("blending_data"));
                logger = SerializableChunkData.LOGGER;
                Objects.requireNonNull(logger);
                blendingdata_d = (BlendingData.d) dataresult.resultOrPartial(logger::error).orElse((Object) null);
            } else {
                blendingdata_d = null;
            }

            BelowZeroRetrogen belowzeroretrogen;

            if (nbttagcompound.contains("below_zero_retrogen", 10)) {
                dataresult = BelowZeroRetrogen.CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound.getCompound("below_zero_retrogen"));
                logger = SerializableChunkData.LOGGER;
                Objects.requireNonNull(logger);
                belowzeroretrogen = (BelowZeroRetrogen) dataresult.resultOrPartial(logger::error).orElse((Object) null);
            } else {
                belowzeroretrogen = null;
            }

            long[] along;

            if (nbttagcompound.contains("carving_mask", 12)) {
                along = nbttagcompound.getLongArray("carving_mask");
            } else {
                along = null;
            }

            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Heightmaps");
            Map<HeightMap.Type, long[]> map = new EnumMap(HeightMap.Type.class);
            Iterator iterator = chunkstatus.heightmapsAfter().iterator();

            while (iterator.hasNext()) {
                HeightMap.Type heightmap_type = (HeightMap.Type) iterator.next();
                String s = heightmap_type.getSerializationKey();

                if (nbttagcompound1.contains(s, 12)) {
                    map.put(heightmap_type, nbttagcompound1.getLongArray(s));
                }
            }

            List<TickListChunk<Block>> list = TickListChunk.loadTickList(nbttagcompound.getList("block_ticks", 10), (s1) -> {
                return BuiltInRegistries.BLOCK.getOptional(MinecraftKey.tryParse(s1));
            }, chunkcoordintpair);
            List<TickListChunk<FluidType>> list1 = TickListChunk.loadTickList(nbttagcompound.getList("fluid_ticks", 10), (s1) -> {
                return BuiltInRegistries.FLUID.getOptional(MinecraftKey.tryParse(s1));
            }, chunkcoordintpair);
            IChunkAccess.a ichunkaccess_a = new IChunkAccess.a(list, list1);
            NBTTagList nbttaglist = nbttagcompound.getList("PostProcessing", 9);
            ShortList[] ashortlist = new ShortList[nbttaglist.size()];

            for (int k = 0; k < nbttaglist.size(); ++k) {
                NBTTagList nbttaglist1 = nbttaglist.getList(k);
                ShortArrayList shortarraylist = new ShortArrayList(nbttaglist1.size());

                for (int l = 0; l < nbttaglist1.size(); ++l) {
                    shortarraylist.add(nbttaglist1.getShort(l));
                }

                ashortlist[k] = shortarraylist;
            }

            List<NBTTagCompound> list2 = Lists.transform(nbttagcompound.getList("entities", 10), (nbtbase) -> {
                return (NBTTagCompound) nbtbase;
            });
            List<NBTTagCompound> list3 = Lists.transform(nbttagcompound.getList("block_entities", 10), (nbtbase) -> {
                return (NBTTagCompound) nbtbase;
            });
            NBTTagCompound nbttagcompound2 = nbttagcompound.getCompound("structures");
            NBTTagList nbttaglist2 = nbttagcompound.getList("sections", 10);
            List<SerializableChunkData.b> list4 = new ArrayList(nbttaglist2.size());
            IRegistry<BiomeBase> iregistry = iregistrycustom.lookupOrThrow(Registries.BIOME);
            Codec<PalettedContainerRO<Holder<BiomeBase>>> codec = makeBiomeCodec(iregistry);

            for (int i1 = 0; i1 < nbttaglist2.size(); ++i1) {
                NBTTagCompound nbttagcompound3 = nbttaglist2.getCompound(i1);
                byte b0 = nbttagcompound3.getByte("Y");
                ChunkSection chunksection;

                if (b0 >= levelheightaccessor.getMinSectionY() && b0 <= levelheightaccessor.getMaxSectionY()) {
                    DataPaletteBlock datapaletteblock;

                    if (nbttagcompound3.contains("block_states", 10)) {
                        datapaletteblock = (DataPaletteBlock) SerializableChunkData.BLOCK_STATE_CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound3.getCompound("block_states")).promotePartial((s1) -> {
                            logErrors(chunkcoordintpair, b0, s1);
                        }).getOrThrow(SerializableChunkData.a::new);
                    } else {
                        datapaletteblock = new DataPaletteBlock<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), DataPaletteBlock.d.SECTION_STATES);
                    }

                    Object object;

                    if (nbttagcompound3.contains("biomes", 10)) {
                        object = (PalettedContainerRO) codec.parse(DynamicOpsNBT.INSTANCE, nbttagcompound3.getCompound("biomes")).promotePartial((s1) -> {
                            logErrors(chunkcoordintpair, b0, s1);
                        }).getOrThrow(SerializableChunkData.a::new);
                    } else {
                        object = new DataPaletteBlock<>(iregistry.asHolderIdMap(), iregistry.getOrThrow(Biomes.PLAINS), DataPaletteBlock.d.SECTION_BIOMES);
                    }

                    chunksection = new ChunkSection(datapaletteblock, (PalettedContainerRO) object);
                } else {
                    chunksection = null;
                }

                NibbleArray nibblearray = nbttagcompound3.contains("BlockLight", 7) ? new NibbleArray(nbttagcompound3.getByteArray("BlockLight")) : null;
                NibbleArray nibblearray1 = nbttagcompound3.contains("SkyLight", 7) ? new NibbleArray(nbttagcompound3.getByteArray("SkyLight")) : null;

                list4.add(new SerializableChunkData.b(b0, chunksection, nibblearray, nibblearray1));
            }

            return new SerializableChunkData(iregistry, chunkcoordintpair, levelheightaccessor.getMinSectionY(), i, j, chunkstatus, blendingdata_d, belowzeroretrogen, chunkconverter, along, map, ichunkaccess_a, ashortlist, flag, list4, list2, list3, nbttagcompound2);
        }
    }

    public ProtoChunk read(WorldServer worldserver, VillagePlace villageplace, RegionStorageInfo regionstorageinfo, ChunkCoordIntPair chunkcoordintpair) {
        if (!Objects.equals(chunkcoordintpair, this.chunkPos)) {
            SerializableChunkData.LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{chunkcoordintpair, chunkcoordintpair, this.chunkPos});
            worldserver.getServer().reportMisplacedChunk(this.chunkPos, chunkcoordintpair, regionstorageinfo);
        }

        int i = worldserver.getSectionsCount();
        ChunkSection[] achunksection = new ChunkSection[i];
        boolean flag = worldserver.dimensionType().hasSkyLight();
        ChunkProviderServer chunkproviderserver = worldserver.getChunkSource();
        LevelLightEngine levellightengine = chunkproviderserver.getLightEngine();
        IRegistry<BiomeBase> iregistry = worldserver.registryAccess().lookupOrThrow(Registries.BIOME);
        boolean flag1 = false;
        Iterator iterator = this.sectionData.iterator();

        while (iterator.hasNext()) {
            SerializableChunkData.b serializablechunkdata_b = (SerializableChunkData.b) iterator.next();
            SectionPosition sectionposition = SectionPosition.of(chunkcoordintpair, serializablechunkdata_b.y);

            if (serializablechunkdata_b.chunkSection != null) {
                achunksection[worldserver.getSectionIndexFromSectionY(serializablechunkdata_b.y)] = serializablechunkdata_b.chunkSection;
                villageplace.checkConsistencyWithBlocks(sectionposition, serializablechunkdata_b.chunkSection);
            }

            boolean flag2 = serializablechunkdata_b.blockLight != null;
            boolean flag3 = flag && serializablechunkdata_b.skyLight != null;

            if (flag2 || flag3) {
                if (!flag1) {
                    levellightengine.retainData(chunkcoordintpair, true);
                    flag1 = true;
                }

                if (flag2) {
                    levellightengine.queueSectionData(EnumSkyBlock.BLOCK, sectionposition, serializablechunkdata_b.blockLight);
                }

                if (flag3) {
                    levellightengine.queueSectionData(EnumSkyBlock.SKY, sectionposition, serializablechunkdata_b.skyLight);
                }
            }
        }

        ChunkType chunktype = this.chunkStatus.getChunkType();
        Object object;

        if (chunktype == ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> levelchunkticks = new LevelChunkTicks<>(this.packedTicks.blocks());
            LevelChunkTicks<FluidType> levelchunkticks1 = new LevelChunkTicks<>(this.packedTicks.fluids());

            object = new Chunk(worldserver.getLevel(), chunkcoordintpair, this.upgradeData, levelchunkticks, levelchunkticks1, this.inhabitedTime, achunksection, postLoadChunk(worldserver, this.entities, this.blockEntities), BlendingData.unpack(this.blendingData));
        } else {
            ProtoChunkTickList<Block> protochunkticklist = ProtoChunkTickList.load(this.packedTicks.blocks());
            ProtoChunkTickList<FluidType> protochunkticklist1 = ProtoChunkTickList.load(this.packedTicks.fluids());
            ProtoChunk protochunk = new ProtoChunk(chunkcoordintpair, this.upgradeData, achunksection, protochunkticklist, protochunkticklist1, worldserver, iregistry, BlendingData.unpack(this.blendingData));

            object = protochunk;
            protochunk.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protochunk.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }

            protochunk.setPersistedStatus(this.chunkStatus);
            if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                protochunk.setLightEngine(levellightengine);
            }
        }

        ((IChunkAccess) object).setLightCorrect(this.lightCorrect);
        EnumSet<HeightMap.Type> enumset = EnumSet.noneOf(HeightMap.Type.class);
        Iterator iterator1 = ((IChunkAccess) object).getPersistedStatus().heightmapsAfter().iterator();

        while (iterator1.hasNext()) {
            HeightMap.Type heightmap_type = (HeightMap.Type) iterator1.next();
            long[] along = (long[]) this.heightmaps.get(heightmap_type);

            if (along != null) {
                ((IChunkAccess) object).setHeightmap(heightmap_type, along);
            } else {
                enumset.add(heightmap_type);
            }
        }

        HeightMap.primeHeightmaps((IChunkAccess) object, enumset);
        ((IChunkAccess) object).setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(worldserver), this.structureData, worldserver.getSeed()));
        ((IChunkAccess) object).setAllReferences(unpackStructureReferences(worldserver.registryAccess(), chunkcoordintpair, this.structureData));

        for (int j = 0; j < this.postProcessingSections.length; ++j) {
            ((IChunkAccess) object).addPackedPostProcess(this.postProcessingSections[j], j);
        }

        if (chunktype == ChunkType.LEVELCHUNK) {
            return new ProtoChunkExtension((Chunk) object, false);
        } else {
            ProtoChunk protochunk1 = (ProtoChunk) object;
            Iterator iterator2 = this.entities.iterator();

            NBTTagCompound nbttagcompound;

            while (iterator2.hasNext()) {
                nbttagcompound = (NBTTagCompound) iterator2.next();
                protochunk1.addEntity(nbttagcompound);
            }

            iterator2 = this.blockEntities.iterator();

            while (iterator2.hasNext()) {
                nbttagcompound = (NBTTagCompound) iterator2.next();
                protochunk1.setBlockEntityNbt(nbttagcompound);
            }

            if (this.carvingMask != null) {
                protochunk1.setCarvingMask(new CarvingMask(this.carvingMask, ((IChunkAccess) object).getMinY()));
            }

            return protochunk1;
        }
    }

    private static void logErrors(ChunkCoordIntPair chunkcoordintpair, int i, String s) {
        SerializableChunkData.LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[]{chunkcoordintpair.x, i, chunkcoordintpair.z, s});
    }

    private static Codec<PalettedContainerRO<Holder<BiomeBase>>> makeBiomeCodec(IRegistry<BiomeBase> iregistry) {
        return DataPaletteBlock.codecRO(iregistry.asHolderIdMap(), iregistry.holderByNameCodec(), DataPaletteBlock.d.SECTION_BIOMES, iregistry.getOrThrow(Biomes.PLAINS));
    }

    public static SerializableChunkData copyOf(WorldServer worldserver, IChunkAccess ichunkaccess) {
        if (!ichunkaccess.canBeSerialized()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + String.valueOf(ichunkaccess));
        } else {
            ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
            List<SerializableChunkData.b> list = new ArrayList();
            ChunkSection[] achunksection = ichunkaccess.getSections();
            LightEngineThreaded lightenginethreaded = worldserver.getChunkSource().getLightEngine();

            for (int i = lightenginethreaded.getMinLightSection(); i < lightenginethreaded.getMaxLightSection(); ++i) {
                int j = ichunkaccess.getSectionIndexFromSectionY(i);
                boolean flag = j >= 0 && j < achunksection.length;
                NibbleArray nibblearray = lightenginethreaded.getLayerListener(EnumSkyBlock.BLOCK).getDataLayerData(SectionPosition.of(chunkcoordintpair, i));
                NibbleArray nibblearray1 = lightenginethreaded.getLayerListener(EnumSkyBlock.SKY).getDataLayerData(SectionPosition.of(chunkcoordintpair, i));
                NibbleArray nibblearray2 = nibblearray != null && !nibblearray.isEmpty() ? nibblearray.copy() : null;
                NibbleArray nibblearray3 = nibblearray1 != null && !nibblearray1.isEmpty() ? nibblearray1.copy() : null;

                if (flag || nibblearray2 != null || nibblearray3 != null) {
                    ChunkSection chunksection = flag ? achunksection[j].copy() : null;

                    list.add(new SerializableChunkData.b(i, chunksection, nibblearray2, nibblearray3));
                }
            }

            List<NBTTagCompound> list1 = new ArrayList(ichunkaccess.getBlockEntitiesPos().size());
            Iterator iterator = ichunkaccess.getBlockEntitiesPos().iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition = (BlockPosition) iterator.next();
                NBTTagCompound nbttagcompound = ichunkaccess.getBlockEntityNbtForSaving(blockposition, worldserver.registryAccess());

                if (nbttagcompound != null) {
                    list1.add(nbttagcompound);
                }
            }

            List<NBTTagCompound> list2 = new ArrayList();
            long[] along = null;

            if (ichunkaccess.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                ProtoChunk protochunk = (ProtoChunk) ichunkaccess;

                list2.addAll(protochunk.getEntities());
                CarvingMask carvingmask = protochunk.getCarvingMask();

                if (carvingmask != null) {
                    along = carvingmask.toArray();
                }
            }

            Map<HeightMap.Type, long[]> map = new EnumMap(HeightMap.Type.class);
            Iterator iterator1 = ichunkaccess.getHeightmaps().iterator();

            while (iterator1.hasNext()) {
                Entry<HeightMap.Type, HeightMap> entry = (Entry) iterator1.next();

                if (ichunkaccess.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) {
                    long[] along1 = ((HeightMap) entry.getValue()).getRawData();

                    map.put((HeightMap.Type) entry.getKey(), (long[]) along1.clone());
                }
            }

            IChunkAccess.a ichunkaccess_a = ichunkaccess.getTicksForSerialization(worldserver.getGameTime());
            ShortList[] ashortlist = (ShortList[]) Arrays.stream(ichunkaccess.getPostProcessing()).map((shortlist) -> {
                return shortlist != null ? new ShortArrayList(shortlist) : null;
            }).toArray((k) -> {
                return new ShortList[k];
            });
            NBTTagCompound nbttagcompound1 = packStructureData(StructurePieceSerializationContext.fromLevel(worldserver), chunkcoordintpair, ichunkaccess.getAllStarts(), ichunkaccess.getAllReferences());

            return new SerializableChunkData(worldserver.registryAccess().lookupOrThrow(Registries.BIOME), chunkcoordintpair, ichunkaccess.getMinSectionY(), worldserver.getGameTime(), ichunkaccess.getInhabitedTime(), ichunkaccess.getPersistedStatus(), (BlendingData.d) Optionull.map(ichunkaccess.getBlendingData(), BlendingData::pack), ichunkaccess.getBelowZeroRetrogen(), ichunkaccess.getUpgradeData().copy(), along, map, ichunkaccess_a, ashortlist, ichunkaccess.isLightCorrect(), list, list2, list1, nbttagcompound1);
        }
    }

    public NBTTagCompound write() {
        NBTTagCompound nbttagcompound = GameProfileSerializer.addCurrentDataVersion(new NBTTagCompound());

        nbttagcompound.putInt("xPos", this.chunkPos.x);
        nbttagcompound.putInt("yPos", this.minSectionY);
        nbttagcompound.putInt("zPos", this.chunkPos.z);
        nbttagcompound.putLong("LastUpdate", this.lastUpdateTime);
        nbttagcompound.putLong("InhabitedTime", this.inhabitedTime);
        nbttagcompound.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        DataResult dataresult;
        Logger logger;

        if (this.blendingData != null) {
            dataresult = BlendingData.d.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.blendingData);
            logger = SerializableChunkData.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
                nbttagcompound.put("blending_data", nbtbase);
            });
        }

        if (this.belowZeroRetrogen != null) {
            dataresult = BelowZeroRetrogen.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.belowZeroRetrogen);
            logger = SerializableChunkData.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
                nbttagcompound.put("below_zero_retrogen", nbtbase);
            });
        }

        if (!this.upgradeData.isEmpty()) {
            nbttagcompound.put("UpgradeData", this.upgradeData.write());
        }

        NBTTagList nbttaglist = new NBTTagList();
        Codec<PalettedContainerRO<Holder<BiomeBase>>> codec = makeBiomeCodec(this.biomeRegistry);
        Iterator iterator = this.sectionData.iterator();

        while (iterator.hasNext()) {
            SerializableChunkData.b serializablechunkdata_b = (SerializableChunkData.b) iterator.next();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            ChunkSection chunksection = serializablechunkdata_b.chunkSection;

            if (chunksection != null) {
                nbttagcompound1.put("block_states", (NBTBase) SerializableChunkData.BLOCK_STATE_CODEC.encodeStart(DynamicOpsNBT.INSTANCE, chunksection.getStates()).getOrThrow());
                nbttagcompound1.put("biomes", (NBTBase) codec.encodeStart(DynamicOpsNBT.INSTANCE, chunksection.getBiomes()).getOrThrow());
            }

            if (serializablechunkdata_b.blockLight != null) {
                nbttagcompound1.putByteArray("BlockLight", serializablechunkdata_b.blockLight.getData());
            }

            if (serializablechunkdata_b.skyLight != null) {
                nbttagcompound1.putByteArray("SkyLight", serializablechunkdata_b.skyLight.getData());
            }

            if (!nbttagcompound1.isEmpty()) {
                nbttagcompound1.putByte("Y", (byte) serializablechunkdata_b.y);
                nbttaglist.add(nbttagcompound1);
            }
        }

        nbttagcompound.put("sections", nbttaglist);
        if (this.lightCorrect) {
            nbttagcompound.putBoolean("isLightOn", true);
        }

        NBTTagList nbttaglist1 = new NBTTagList();

        nbttaglist1.addAll(this.blockEntities);
        nbttagcompound.put("block_entities", nbttaglist1);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            NBTTagList nbttaglist2 = new NBTTagList();

            nbttaglist2.addAll(this.entities);
            nbttagcompound.put("entities", nbttaglist2);
            if (this.carvingMask != null) {
                nbttagcompound.putLongArray("carving_mask", this.carvingMask);
            }
        }

        saveTicks(nbttagcompound, this.packedTicks);
        nbttagcompound.put("PostProcessing", packOffsets(this.postProcessingSections));
        NBTTagCompound nbttagcompound2 = new NBTTagCompound();

        this.heightmaps.forEach((heightmap_type, along) -> {
            nbttagcompound2.put(heightmap_type.getSerializationKey(), new NBTTagLongArray(along));
        });
        nbttagcompound.put("Heightmaps", nbttagcompound2);
        nbttagcompound.put("structures", this.structureData);
        return nbttagcompound;
    }

    private static void saveTicks(NBTTagCompound nbttagcompound, IChunkAccess.a ichunkaccess_a) {
        NBTTagList nbttaglist = new NBTTagList();
        Iterator iterator = ichunkaccess_a.blocks().iterator();

        while (iterator.hasNext()) {
            TickListChunk<Block> ticklistchunk = (TickListChunk) iterator.next();

            nbttaglist.add(ticklistchunk.save((block) -> {
                return BuiltInRegistries.BLOCK.getKey(block).toString();
            }));
        }

        nbttagcompound.put("block_ticks", nbttaglist);
        NBTTagList nbttaglist1 = new NBTTagList();
        Iterator iterator1 = ichunkaccess_a.fluids().iterator();

        while (iterator1.hasNext()) {
            TickListChunk<FluidType> ticklistchunk1 = (TickListChunk) iterator1.next();

            nbttaglist1.add(ticklistchunk1.save((fluidtype) -> {
                return BuiltInRegistries.FLUID.getKey(fluidtype).toString();
            }));
        }

        nbttagcompound.put("fluid_ticks", nbttaglist1);
    }

    public static ChunkType getChunkTypeFromTag(@Nullable NBTTagCompound nbttagcompound) {
        return nbttagcompound != null ? ChunkStatus.byName(nbttagcompound.getString("Status")).getChunkType() : ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static Chunk.c postLoadChunk(WorldServer worldserver, List<NBTTagCompound> list, List<NBTTagCompound> list1) {
        return list.isEmpty() && list1.isEmpty() ? null : (chunk) -> {
            if (!list.isEmpty()) {
                worldserver.addLegacyChunkEntities(EntityTypes.loadEntitiesRecursive(list, worldserver, EntitySpawnReason.LOAD));
            }

            Iterator iterator = list1.iterator();

            while (iterator.hasNext()) {
                NBTTagCompound nbttagcompound = (NBTTagCompound) iterator.next();
                boolean flag = nbttagcompound.getBoolean("keepPacked");

                if (flag) {
                    chunk.setBlockEntityNbt(nbttagcompound);
                } else {
                    BlockPosition blockposition = TileEntity.getPosFromTag(nbttagcompound);
                    TileEntity tileentity = TileEntity.loadStatic(blockposition, chunk.getBlockState(blockposition), nbttagcompound, worldserver.registryAccess());

                    if (tileentity != null) {
                        chunk.setBlockEntity(tileentity);
                    }
                }
            }

        };
    }

    private static NBTTagCompound packStructureData(StructurePieceSerializationContext structurepieceserializationcontext, ChunkCoordIntPair chunkcoordintpair, Map<Structure, StructureStart> map, Map<Structure, LongSet> map1) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        IRegistry<Structure> iregistry = structurepieceserializationcontext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Structure, StructureStart> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = iregistry.getKey((Structure) entry.getKey());

            nbttagcompound1.put(minecraftkey.toString(), ((StructureStart) entry.getValue()).createTag(structurepieceserializationcontext, chunkcoordintpair));
        }

        nbttagcompound.put("starts", nbttagcompound1);
        NBTTagCompound nbttagcompound2 = new NBTTagCompound();
        Iterator iterator1 = map1.entrySet().iterator();

        while (iterator1.hasNext()) {
            Entry<Structure, LongSet> entry1 = (Entry) iterator1.next();

            if (!((LongSet) entry1.getValue()).isEmpty()) {
                MinecraftKey minecraftkey1 = iregistry.getKey((Structure) entry1.getKey());

                nbttagcompound2.put(minecraftkey1.toString(), new NBTTagLongArray((LongSet) entry1.getValue()));
            }
        }

        nbttagcompound.put("References", nbttagcompound2);
        return nbttagcompound;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound, long i) {
        Map<Structure, StructureStart> map = Maps.newHashMap();
        IRegistry<Structure> iregistry = structurepieceserializationcontext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("starts");
        Iterator iterator = nbttagcompound1.getAllKeys().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            MinecraftKey minecraftkey = MinecraftKey.tryParse(s);
            Structure structure = (Structure) iregistry.getValue(minecraftkey);

            if (structure == null) {
                SerializableChunkData.LOGGER.error("Unknown structure start: {}", minecraftkey);
            } else {
                StructureStart structurestart = StructureStart.loadStaticStart(structurepieceserializationcontext, nbttagcompound1.getCompound(s), i);

                if (structurestart != null) {
                    map.put(structure, structurestart);
                }
            }
        }

        return map;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(IRegistryCustom iregistrycustom, ChunkCoordIntPair chunkcoordintpair, NBTTagCompound nbttagcompound) {
        Map<Structure, LongSet> map = Maps.newHashMap();
        IRegistry<Structure> iregistry = iregistrycustom.lookupOrThrow(Registries.STRUCTURE);
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("References");
        Iterator iterator = nbttagcompound1.getAllKeys().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            MinecraftKey minecraftkey = MinecraftKey.tryParse(s);
            Structure structure = (Structure) iregistry.getValue(minecraftkey);

            if (structure == null) {
                SerializableChunkData.LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", minecraftkey, chunkcoordintpair);
            } else {
                long[] along = nbttagcompound1.getLongArray(s);

                if (along.length != 0) {
                    map.put(structure, new LongOpenHashSet(Arrays.stream(along).filter((i) -> {
                        ChunkCoordIntPair chunkcoordintpair1 = new ChunkCoordIntPair(i);

                        if (chunkcoordintpair1.getChessboardDistance(chunkcoordintpair) > 8) {
                            SerializableChunkData.LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{minecraftkey, chunkcoordintpair1, chunkcoordintpair});
                            return false;
                        } else {
                            return true;
                        }
                    }).toArray()));
                }
            }
        }

        return map;
    }

    private static NBTTagList packOffsets(ShortList[] ashortlist) {
        NBTTagList nbttaglist = new NBTTagList();
        ShortList[] ashortlist1 = ashortlist;
        int i = ashortlist.length;

        for (int j = 0; j < i; ++j) {
            ShortList shortlist = ashortlist1[j];
            NBTTagList nbttaglist1 = new NBTTagList();

            if (shortlist != null) {
                for (int k = 0; k < shortlist.size(); ++k) {
                    nbttaglist1.add(NBTTagShort.valueOf(shortlist.getShort(k)));
                }
            }

            nbttaglist.add(nbttaglist1);
        }

        return nbttaglist;
    }

    public static record b(int y, @Nullable ChunkSection chunkSection, @Nullable NibbleArray blockLight, @Nullable NibbleArray skyLight) {

    }

    public static class a extends NbtException {

        public a(String s) {
            super(s);
        }
    }
}
