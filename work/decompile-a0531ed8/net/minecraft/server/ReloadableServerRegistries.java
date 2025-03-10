package net.minecraft.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceDataJson;
import net.minecraft.tags.TagDataPack;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import org.slf4j.Logger;

public class ReloadableServerRegistries {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

    public ReloadableServerRegistries() {}

    public static CompletableFuture<ReloadableServerRegistries.b> reload(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, List<IRegistry.a<?>> list, IResourceManager iresourcemanager, Executor executor) {
        List<HolderLookup.b<?>> list1 = TagDataPack.buildUpdatedLookups(layeredregistryaccess.getAccessForLoading(RegistryLayer.RELOADABLE), list);
        HolderLookup.a holderlookup_a = HolderLookup.a.create(list1.stream());
        RegistryOps<JsonElement> registryops = holderlookup_a.createSerializationContext(JsonOps.INSTANCE);
        List<CompletableFuture<IRegistryWritable<?>>> list2 = LootDataType.values().map((lootdatatype) -> {
            return scheduleRegistryLoad(lootdatatype, registryops, iresourcemanager, executor);
        }).toList();
        CompletableFuture<List<IRegistryWritable<?>>> completablefuture = SystemUtils.sequence(list2);

        return completablefuture.thenApplyAsync((list3) -> {
            return createAndValidateFullContext(layeredregistryaccess, holderlookup_a, list3);
        }, executor);
    }

    private static <T> CompletableFuture<IRegistryWritable<?>> scheduleRegistryLoad(LootDataType<T> lootdatatype, RegistryOps<JsonElement> registryops, IResourceManager iresourcemanager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            IRegistryWritable<T> iregistrywritable = new RegistryMaterials<>(lootdatatype.registryKey(), Lifecycle.experimental());
            Map<MinecraftKey, T> map = new HashMap();

            ResourceDataJson.scanDirectory(iresourcemanager, lootdatatype.registryKey(), registryops, lootdatatype.codec(), map);
            map.forEach((minecraftkey, object) -> {
                iregistrywritable.register(ResourceKey.create(lootdatatype.registryKey(), minecraftkey), object, ReloadableServerRegistries.DEFAULT_REGISTRATION_INFO);
            });
            TagDataPack.loadTagsForRegistry(iresourcemanager, iregistrywritable);
            return iregistrywritable;
        }, executor);
    }

    private static ReloadableServerRegistries.b createAndValidateFullContext(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, HolderLookup.a holderlookup_a, List<IRegistryWritable<?>> list) {
        LayeredRegistryAccess<RegistryLayer> layeredregistryaccess1 = createUpdatedRegistries(layeredregistryaccess, list);
        HolderLookup.a holderlookup_a1 = concatenateLookups(holderlookup_a, layeredregistryaccess1.getLayer(RegistryLayer.RELOADABLE));

        validateLootRegistries(holderlookup_a1);
        return new ReloadableServerRegistries.b(layeredregistryaccess1, holderlookup_a1);
    }

    private static HolderLookup.a concatenateLookups(HolderLookup.a holderlookup_a, HolderLookup.a holderlookup_a1) {
        return HolderLookup.a.create(Stream.concat(holderlookup_a.listRegistries(), holderlookup_a1.listRegistries()));
    }

    private static void validateLootRegistries(HolderLookup.a holderlookup_a) {
        ProblemReporter.a problemreporter_a = new ProblemReporter.a();
        LootCollector lootcollector = new LootCollector(problemreporter_a, LootContextParameterSets.ALL_PARAMS, holderlookup_a);

        LootDataType.values().forEach((lootdatatype) -> {
            validateRegistry(lootcollector, lootdatatype, holderlookup_a);
        });
        problemreporter_a.get().forEach((s, s1) -> {
            ReloadableServerRegistries.LOGGER.warn("Found loot table element validation problem in {}: {}", s, s1);
        });
    }

    private static LayeredRegistryAccess<RegistryLayer> createUpdatedRegistries(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, List<IRegistryWritable<?>> list) {
        return layeredregistryaccess.replaceFrom(RegistryLayer.RELOADABLE, (new IRegistryCustom.c(list)).freeze());
    }

    private static <T> void validateRegistry(LootCollector lootcollector, LootDataType<T> lootdatatype, HolderLookup.a holderlookup_a) {
        HolderLookup<T> holderlookup = holderlookup_a.lookupOrThrow(lootdatatype.registryKey());

        holderlookup.listElements().forEach((holder_c) -> {
            lootdatatype.runValidation(lootcollector, holder_c.key(), holder_c.value());
        });
    }

    public static record b(LayeredRegistryAccess<RegistryLayer> layers, HolderLookup.a lookupWithUpdatedTags) {

    }

    public static class a {

        private final HolderLookup.a registries;

        public a(HolderLookup.a holderlookup_a) {
            this.registries = holderlookup_a;
        }

        public HolderGetter.a lookup() {
            return this.registries;
        }

        public Collection<MinecraftKey> getKeys(ResourceKey<? extends IRegistry<?>> resourcekey) {
            return this.registries.lookupOrThrow(resourcekey).listElementIds().map(ResourceKey::location).toList();
        }

        public LootTable getLootTable(ResourceKey<LootTable> resourcekey) {
            return (LootTable) this.registries.lookup(Registries.LOOT_TABLE).flatMap((holderlookup_b) -> {
                return holderlookup_b.get(resourcekey);
            }).map(Holder::value).orElse(LootTable.EMPTY);
        }
    }
}
