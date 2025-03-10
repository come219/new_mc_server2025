package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.IResourcePack;
import net.minecraft.server.packs.repository.ResourcePackRepository;
import net.minecraft.server.packs.resources.IReloadableResourceManager;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagDataPack;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    public WorldLoader() {}

    public static <D, R> CompletableFuture<R> load(WorldLoader.c worldloader_c, WorldLoader.f<D> worldloader_f, WorldLoader.e<D, R> worldloader_e, Executor executor, Executor executor1) {
        try {
            Pair<WorldDataConfiguration, IReloadableResourceManager> pair = worldloader_c.packConfig.createResourceManager();
            IReloadableResourceManager ireloadableresourcemanager = (IReloadableResourceManager) pair.getSecond();
            LayeredRegistryAccess<RegistryLayer> layeredregistryaccess = RegistryLayer.createRegistryAccess();
            List<IRegistry.a<?>> list = TagDataPack.loadTagsForExistingRegistries(ireloadableresourcemanager, layeredregistryaccess.getLayer(RegistryLayer.STATIC));
            IRegistryCustom.Dimension iregistrycustom_dimension = layeredregistryaccess.getAccessForLoading(RegistryLayer.WORLDGEN);
            List<HolderLookup.b<?>> list1 = TagDataPack.buildUpdatedLookups(iregistrycustom_dimension, list);
            IRegistryCustom.Dimension iregistrycustom_dimension1 = RegistryDataLoader.load((IResourceManager) ireloadableresourcemanager, list1, RegistryDataLoader.WORLDGEN_REGISTRIES);
            List<HolderLookup.b<?>> list2 = Stream.concat(list1.stream(), iregistrycustom_dimension1.listRegistries()).toList();
            IRegistryCustom.Dimension iregistrycustom_dimension2 = RegistryDataLoader.load((IResourceManager) ireloadableresourcemanager, list2, RegistryDataLoader.DIMENSION_REGISTRIES);
            WorldDataConfiguration worlddataconfiguration = (WorldDataConfiguration) pair.getFirst();
            HolderLookup.a holderlookup_a = HolderLookup.a.create(list2.stream());
            WorldLoader.b<D> worldloader_b = worldloader_f.get(new WorldLoader.a(ireloadableresourcemanager, worlddataconfiguration, holderlookup_a, iregistrycustom_dimension2));
            LayeredRegistryAccess<RegistryLayer> layeredregistryaccess1 = layeredregistryaccess.replaceFrom(RegistryLayer.WORLDGEN, iregistrycustom_dimension1, worldloader_b.finalDimensions);

            return DataPackResources.loadResources(ireloadableresourcemanager, layeredregistryaccess1, list, worlddataconfiguration.enabledFeatures(), worldloader_c.commandSelection(), worldloader_c.functionCompilationLevel(), executor, executor1).whenComplete((datapackresources, throwable) -> {
                if (throwable != null) {
                    ireloadableresourcemanager.close();
                }

            }).thenApplyAsync((datapackresources) -> {
                datapackresources.updateStaticRegistryTags();
                return worldloader_e.create(ireloadableresourcemanager, datapackresources, layeredregistryaccess1, worldloader_b.cookie);
            }, executor1);
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    public static record c(WorldLoader.d packConfig, CommandDispatcher.ServerType commandSelection, int functionCompilationLevel) {

    }

    public static record d(ResourcePackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {

        public Pair<WorldDataConfiguration, IReloadableResourceManager> createResourceManager() {
            WorldDataConfiguration worlddataconfiguration = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataConfig, this.initMode, this.safeMode);
            List<IResourcePack> list = this.packRepository.openAllSelected();
            ResourceManager resourcemanager = new ResourceManager(EnumResourcePackType.SERVER_DATA, list);

            return Pair.of(worlddataconfiguration, resourcemanager);
        }
    }

    public static record a(IResourceManager resources, WorldDataConfiguration dataConfiguration, HolderLookup.a datapackWorldgen, IRegistryCustom.Dimension datapackDimensions) {

    }

    @FunctionalInterface
    public interface f<D> {

        WorldLoader.b<D> get(WorldLoader.a worldloader_a);
    }

    public static record b<D>(D cookie, IRegistryCustom.Dimension finalDimensions) {

    }

    @FunctionalInterface
    public interface e<D, R> {

        R create(IReloadableResourceManager ireloadableresourcemanager, DataPackResources datapackresources, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, D d0);
    }
}
