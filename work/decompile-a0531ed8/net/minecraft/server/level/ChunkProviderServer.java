package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.FileUtils;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.WorldLoadListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.thread.IAsyncTaskHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.entity.ai.village.poi.VillagePlace;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.SpawnerCreature;
import net.minecraft.world.level.World;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.IChunkProvider;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.WorldPersistentData;
import org.slf4j.Logger;

public class ChunkProviderServer extends IChunkProvider {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final ChunkMapDistance distanceManager;
    private final WorldServer level;
    final Thread mainThread;
    final LightEngineThreaded lightEngine;
    private final ChunkProviderServer.b mainThreadProcessor;
    public final PlayerChunkMap chunkMap;
    private final WorldPersistentData dataStorage;
    private long lastInhabitedUpdate;
    public boolean spawnEnemies = true;
    public boolean spawnFriendlies = true;
    private static final int CACHE_SIZE = 4;
    private final long[] lastChunkPos = new long[4];
    private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
    private final IChunkAccess[] lastChunk = new IChunkAccess[4];
    private final List<Chunk> tickingChunks = new ArrayList();
    private final Set<PlayerChunk> chunkHoldersToBroadcast = new ReferenceOpenHashSet();
    @Nullable
    @VisibleForDebug
    private SpawnerCreature.d lastSpawnState;

    public ChunkProviderServer(WorldServer worldserver, Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer, StructureTemplateManager structuretemplatemanager, Executor executor, ChunkGenerator chunkgenerator, int i, int j, boolean flag, WorldLoadListener worldloadlistener, ChunkStatusUpdateListener chunkstatusupdatelistener, Supplier<WorldPersistentData> supplier) {
        this.level = worldserver;
        this.mainThreadProcessor = new ChunkProviderServer.b(worldserver);
        this.mainThread = Thread.currentThread();
        Path path = convertable_conversionsession.getDimensionPath(worldserver.dimension()).resolve("data");

        try {
            FileUtils.createDirectoriesSafe(path);
        } catch (IOException ioexception) {
            ChunkProviderServer.LOGGER.error("Failed to create dimension data storage directory", ioexception);
        }

        this.dataStorage = new WorldPersistentData(path, datafixer, worldserver.registryAccess());
        this.chunkMap = new PlayerChunkMap(worldserver, convertable_conversionsession, datafixer, structuretemplatemanager, executor, this.mainThreadProcessor, this, chunkgenerator, worldloadlistener, chunkstatusupdatelistener, supplier, i, flag);
        this.lightEngine = this.chunkMap.getLightEngine();
        this.distanceManager = this.chunkMap.getDistanceManager();
        this.distanceManager.updateSimulationDistance(j);
        this.clearCache();
    }

    @Override
    public LightEngineThreaded getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    private PlayerChunk getVisibleChunkIfPresent(long i) {
        return this.chunkMap.getVisibleChunkIfPresent(i);
    }

    public int getTickingGenerated() {
        return this.chunkMap.getTickingGenerated();
    }

    private void storeInCache(long i, @Nullable IChunkAccess ichunkaccess, ChunkStatus chunkstatus) {
        for (int j = 3; j > 0; --j) {
            this.lastChunkPos[j] = this.lastChunkPos[j - 1];
            this.lastChunkStatus[j] = this.lastChunkStatus[j - 1];
            this.lastChunk[j] = this.lastChunk[j - 1];
        }

        this.lastChunkPos[0] = i;
        this.lastChunkStatus[0] = chunkstatus;
        this.lastChunk[0] = ichunkaccess;
    }

    @Nullable
    @Override
    public IChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        if (Thread.currentThread() != this.mainThread) {
            return (IChunkAccess) CompletableFuture.supplyAsync(() -> {
                return this.getChunk(i, j, chunkstatus, flag);
            }, this.mainThreadProcessor).join();
        } else {
            GameProfilerFiller gameprofilerfiller = Profiler.get();

            gameprofilerfiller.incrementCounter("getChunk");
            long k = ChunkCoordIntPair.asLong(i, j);

            for (int l = 0; l < 4; ++l) {
                if (k == this.lastChunkPos[l] && chunkstatus == this.lastChunkStatus[l]) {
                    IChunkAccess ichunkaccess = this.lastChunk[l];

                    if (ichunkaccess != null || !flag) {
                        return ichunkaccess;
                    }
                }
            }

            gameprofilerfiller.incrementCounter("getChunkCacheMiss");
            CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = this.getChunkFutureMainThread(i, j, chunkstatus, flag);
            ChunkProviderServer.b chunkproviderserver_b = this.mainThreadProcessor;

            Objects.requireNonNull(completablefuture);
            chunkproviderserver_b.managedBlock(completablefuture::isDone);
            ChunkResult<IChunkAccess> chunkresult = (ChunkResult) completablefuture.join();
            IChunkAccess ichunkaccess1 = (IChunkAccess) chunkresult.orElse((Object) null);

            if (ichunkaccess1 == null && flag) {
                throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException("Chunk not there when requested: " + chunkresult.getError()));
            } else {
                this.storeInCache(k, ichunkaccess1, chunkstatus);
                return ichunkaccess1;
            }
        }
    }

    @Nullable
    @Override
    public Chunk getChunkNow(int i, int j) {
        if (Thread.currentThread() != this.mainThread) {
            return null;
        } else {
            Profiler.get().incrementCounter("getChunkNow");
            long k = ChunkCoordIntPair.asLong(i, j);

            IChunkAccess ichunkaccess;

            for (int l = 0; l < 4; ++l) {
                if (k == this.lastChunkPos[l] && this.lastChunkStatus[l] == ChunkStatus.FULL) {
                    ichunkaccess = this.lastChunk[l];
                    return ichunkaccess instanceof Chunk ? (Chunk) ichunkaccess : null;
                }
            }

            PlayerChunk playerchunk = this.getVisibleChunkIfPresent(k);

            if (playerchunk == null) {
                return null;
            } else {
                ichunkaccess = playerchunk.getChunkIfPresent(ChunkStatus.FULL);
                if (ichunkaccess != null) {
                    this.storeInCache(k, ichunkaccess, ChunkStatus.FULL);
                    if (ichunkaccess instanceof Chunk) {
                        return (Chunk) ichunkaccess;
                    }
                }

                return null;
            }
        }
    }

    private void clearCache() {
        Arrays.fill(this.lastChunkPos, ChunkCoordIntPair.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunkStatus, (Object) null);
        Arrays.fill(this.lastChunk, (Object) null);
    }

    public CompletableFuture<ChunkResult<IChunkAccess>> getChunkFuture(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        boolean flag1 = Thread.currentThread() == this.mainThread;
        CompletableFuture completablefuture;

        if (flag1) {
            completablefuture = this.getChunkFutureMainThread(i, j, chunkstatus, flag);
            ChunkProviderServer.b chunkproviderserver_b = this.mainThreadProcessor;

            Objects.requireNonNull(completablefuture);
            chunkproviderserver_b.managedBlock(completablefuture::isDone);
        } else {
            completablefuture = CompletableFuture.supplyAsync(() -> {
                return this.getChunkFutureMainThread(i, j, chunkstatus, flag);
            }, this.mainThreadProcessor).thenCompose((completablefuture1) -> {
                return completablefuture1;
            });
        }

        return completablefuture;
    }

    private CompletableFuture<ChunkResult<IChunkAccess>> getChunkFutureMainThread(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i, j);
        long k = chunkcoordintpair.toLong();
        int l = ChunkLevel.byStatus(chunkstatus);
        PlayerChunk playerchunk = this.getVisibleChunkIfPresent(k);

        if (flag) {
            this.distanceManager.addTicket(TicketType.UNKNOWN, chunkcoordintpair, l, chunkcoordintpair);
            if (this.chunkAbsent(playerchunk, l)) {
                GameProfilerFiller gameprofilerfiller = Profiler.get();

                gameprofilerfiller.push("chunkLoad");
                this.runDistanceManagerUpdates();
                playerchunk = this.getVisibleChunkIfPresent(k);
                gameprofilerfiller.pop();
                if (this.chunkAbsent(playerchunk, l)) {
                    throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }

        return this.chunkAbsent(playerchunk, l) ? GenerationChunkHolder.UNLOADED_CHUNK_FUTURE : playerchunk.scheduleChunkGenerationTask(chunkstatus, this.chunkMap);
    }

    private boolean chunkAbsent(@Nullable PlayerChunk playerchunk, int i) {
        return playerchunk == null || playerchunk.getTicketLevel() > i;
    }

    @Override
    public boolean hasChunk(int i, int j) {
        PlayerChunk playerchunk = this.getVisibleChunkIfPresent((new ChunkCoordIntPair(i, j)).toLong());
        int k = ChunkLevel.byStatus(ChunkStatus.FULL);

        return !this.chunkAbsent(playerchunk, k);
    }

    @Nullable
    @Override
    public LightChunk getChunkForLighting(int i, int j) {
        long k = ChunkCoordIntPair.asLong(i, j);
        PlayerChunk playerchunk = this.getVisibleChunkIfPresent(k);

        return playerchunk == null ? null : playerchunk.getChunkIfPresentUnchecked(ChunkStatus.INITIALIZE_LIGHT.getParent());
    }

    @Override
    public World getLevel() {
        return this.level;
    }

    public boolean pollTask() {
        return this.mainThreadProcessor.pollTask();
    }

    boolean runDistanceManagerUpdates() {
        boolean flag = this.distanceManager.runAllUpdates(this.chunkMap);
        boolean flag1 = this.chunkMap.promoteChunkMap();

        this.chunkMap.runGenerationTasks();
        if (!flag && !flag1) {
            return false;
        } else {
            this.clearCache();
            return true;
        }
    }

    public boolean isPositionTicking(long i) {
        if (!this.level.shouldTickBlocksAt(i)) {
            return false;
        } else {
            PlayerChunk playerchunk = this.getVisibleChunkIfPresent(i);

            return playerchunk == null ? false : ((ChunkResult) playerchunk.getTickingChunkFuture().getNow(PlayerChunk.UNLOADED_LEVEL_CHUNK)).isSuccess();
        }
    }

    public void save(boolean flag) {
        this.runDistanceManagerUpdates();
        this.chunkMap.saveAllChunks(flag);
    }

    @Override
    public void close() throws IOException {
        this.save(true);
        this.dataStorage.close();
        this.lightEngine.close();
        this.chunkMap.close();
    }

    @Override
    public void tick(BooleanSupplier booleansupplier, boolean flag) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("purge");
        if (this.level.tickRateManager().runsNormally() || !flag) {
            this.distanceManager.purgeStaleTickets();
        }

        this.runDistanceManagerUpdates();
        gameprofilerfiller.popPush("chunks");
        if (flag) {
            this.tickChunks();
            this.chunkMap.tick();
        }

        gameprofilerfiller.popPush("unload");
        this.chunkMap.tick(booleansupplier);
        gameprofilerfiller.pop();
        this.clearCache();
    }

    private void tickChunks() {
        long i = this.level.getGameTime();
        long j = i - this.lastInhabitedUpdate;

        this.lastInhabitedUpdate = i;
        if (!this.level.isDebug()) {
            GameProfilerFiller gameprofilerfiller = Profiler.get();

            gameprofilerfiller.push("pollingChunks");
            if (this.level.tickRateManager().runsNormally()) {
                List<Chunk> list = this.tickingChunks;

                try {
                    gameprofilerfiller.push("filteringTickingChunks");
                    this.collectTickingChunks(list);
                    gameprofilerfiller.popPush("shuffleChunks");
                    SystemUtils.shuffle(list, this.level.random);
                    this.tickChunks(gameprofilerfiller, j, list);
                    gameprofilerfiller.pop();
                } finally {
                    list.clear();
                }
            }

            this.broadcastChangedChunks(gameprofilerfiller);
            gameprofilerfiller.pop();
        }
    }

    private void broadcastChangedChunks(GameProfilerFiller gameprofilerfiller) {
        gameprofilerfiller.push("broadcast");
        Iterator iterator = this.chunkHoldersToBroadcast.iterator();

        while (iterator.hasNext()) {
            PlayerChunk playerchunk = (PlayerChunk) iterator.next();
            Chunk chunk = playerchunk.getTickingChunk();

            if (chunk != null) {
                playerchunk.broadcastChanges(chunk);
            }
        }

        this.chunkHoldersToBroadcast.clear();
        gameprofilerfiller.pop();
    }

    private void collectTickingChunks(List<Chunk> list) {
        this.chunkMap.forEachSpawnCandidateChunk((playerchunk) -> {
            Chunk chunk = playerchunk.getTickingChunk();

            if (chunk != null && this.level.isNaturalSpawningAllowed(playerchunk.getPos())) {
                list.add(chunk);
            }

        });
    }

    private void tickChunks(GameProfilerFiller gameprofilerfiller, long i, List<Chunk> list) {
        gameprofilerfiller.popPush("naturalSpawnCount");
        int j = this.distanceManager.getNaturalSpawnChunkCount();
        SpawnerCreature.d spawnercreature_d = SpawnerCreature.createState(j, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));

        this.lastSpawnState = spawnercreature_d;
        gameprofilerfiller.popPush("spawnAndTick");
        boolean flag = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        List list1;

        if (flag && (this.spawnEnemies || this.spawnFriendlies)) {
            boolean flag1 = this.level.getLevelData().getGameTime() % 400L == 0L;

            list1 = SpawnerCreature.getFilteredSpawningCategories(spawnercreature_d, this.spawnFriendlies, this.spawnEnemies, flag1);
        } else {
            list1 = List.of();
        }

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();
            ChunkCoordIntPair chunkcoordintpair = chunk.getPos();

            chunk.incrementInhabitedTime(i);
            if (!list1.isEmpty() && this.level.getWorldBorder().isWithinBounds(chunkcoordintpair)) {
                SpawnerCreature.spawnForChunk(this.level, chunk, spawnercreature_d, list1);
            }

            if (this.level.shouldTickBlocksAt(chunkcoordintpair.toLong())) {
                this.level.tickChunk(chunk, k);
            }
        }

        gameprofilerfiller.popPush("customSpawners");
        if (flag) {
            this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
        }

    }

    private void getFullChunk(long i, Consumer<Chunk> consumer) {
        PlayerChunk playerchunk = this.getVisibleChunkIfPresent(i);

        if (playerchunk != null) {
            ((ChunkResult) playerchunk.getFullChunkFuture().getNow(PlayerChunk.UNLOADED_LEVEL_CHUNK)).ifSuccess(consumer);
        }

    }

    @Override
    public String gatherStats() {
        return Integer.toString(this.getLoadedChunksCount());
    }

    @VisibleForTesting
    public int getPendingTasksCount() {
        return this.mainThreadProcessor.getPendingTasksCount();
    }

    public ChunkGenerator getGenerator() {
        return this.chunkMap.generator();
    }

    public ChunkGeneratorStructureState getGeneratorState() {
        return this.chunkMap.generatorState();
    }

    public RandomState randomState() {
        return this.chunkMap.randomState();
    }

    @Override
    public int getLoadedChunksCount() {
        return this.chunkMap.size();
    }

    public void blockChanged(BlockPosition blockposition) {
        int i = SectionPosition.blockToSectionCoord(blockposition.getX());
        int j = SectionPosition.blockToSectionCoord(blockposition.getZ());
        PlayerChunk playerchunk = this.getVisibleChunkIfPresent(ChunkCoordIntPair.asLong(i, j));

        if (playerchunk != null && playerchunk.blockChanged(blockposition)) {
            this.chunkHoldersToBroadcast.add(playerchunk);
        }

    }

    @Override
    public void onLightUpdate(EnumSkyBlock enumskyblock, SectionPosition sectionposition) {
        this.mainThreadProcessor.execute(() -> {
            PlayerChunk playerchunk = this.getVisibleChunkIfPresent(sectionposition.chunk().toLong());

            if (playerchunk != null && playerchunk.sectionLightChanged(enumskyblock, sectionposition.y())) {
                this.chunkHoldersToBroadcast.add(playerchunk);
            }

        });
    }

    public <T> void addRegionTicket(TicketType<T> tickettype, ChunkCoordIntPair chunkcoordintpair, int i, T t0) {
        this.distanceManager.addRegionTicket(tickettype, chunkcoordintpair, i, t0);
    }

    public <T> void removeRegionTicket(TicketType<T> tickettype, ChunkCoordIntPair chunkcoordintpair, int i, T t0) {
        this.distanceManager.removeRegionTicket(tickettype, chunkcoordintpair, i, t0);
    }

    @Override
    public void updateChunkForced(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.distanceManager.updateChunkForced(chunkcoordintpair, flag);
    }

    public void move(EntityPlayer entityplayer) {
        if (!entityplayer.isRemoved()) {
            this.chunkMap.move(entityplayer);
        }

    }

    public void removeEntity(Entity entity) {
        this.chunkMap.removeEntity(entity);
    }

    public void addEntity(Entity entity) {
        this.chunkMap.addEntity(entity);
    }

    public void broadcastAndSend(Entity entity, Packet<?> packet) {
        this.chunkMap.broadcastAndSend(entity, packet);
    }

    public void broadcast(Entity entity, Packet<?> packet) {
        this.chunkMap.broadcast(entity, packet);
    }

    public void setViewDistance(int i) {
        this.chunkMap.setServerViewDistance(i);
    }

    public void setSimulationDistance(int i) {
        this.distanceManager.updateSimulationDistance(i);
    }

    @Override
    public void setSpawnSettings(boolean flag) {
        this.spawnEnemies = flag;
        this.spawnFriendlies = this.spawnFriendlies;
    }

    public String getChunkDebugData(ChunkCoordIntPair chunkcoordintpair) {
        return this.chunkMap.getChunkDebugData(chunkcoordintpair);
    }

    public WorldPersistentData getDataStorage() {
        return this.dataStorage;
    }

    public VillagePlace getPoiManager() {
        return this.chunkMap.getPoiManager();
    }

    public ChunkScanAccess chunkScanner() {
        return this.chunkMap.chunkScanner();
    }

    @Nullable
    @VisibleForDebug
    public SpawnerCreature.d getLastSpawnState() {
        return this.lastSpawnState;
    }

    public void removeTicketsOnClosing() {
        this.distanceManager.removeTicketsOnClosing();
    }

    public void onChunkReadyToSend(PlayerChunk playerchunk) {
        if (playerchunk.hasChangesToBroadcast()) {
            this.chunkHoldersToBroadcast.add(playerchunk);
        }

    }

    private final class b extends IAsyncTaskHandler<Runnable> {

        b(final World world) {
            super("Chunk source main thread executor for " + String.valueOf(world.dimension().location()));
        }

        @Override
        public void managedBlock(BooleanSupplier booleansupplier) {
            super.managedBlock(() -> {
                return MinecraftServer.throwIfFatalException() && booleansupplier.getAsBoolean();
            });
        }

        @Override
        public Runnable wrapRunnable(Runnable runnable) {
            return runnable;
        }

        @Override
        protected boolean shouldRun(Runnable runnable) {
            return true;
        }

        @Override
        protected boolean scheduleExecutables() {
            return true;
        }

        @Override
        protected Thread getRunningThread() {
            return ChunkProviderServer.this.mainThread;
        }

        @Override
        protected void doRunTask(Runnable runnable) {
            Profiler.get().incrementCounter("runTask");
            super.doRunTask(runnable);
        }

        @Override
        protected boolean pollTask() {
            if (ChunkProviderServer.this.runDistanceManagerUpdates()) {
                return true;
            } else {
                ChunkProviderServer.this.lightEngine.tryScheduleUpdate();
                return super.pollTask();
            }
        }
    }

    private static record a(Chunk chunk, PlayerChunk holder) {

    }
}
