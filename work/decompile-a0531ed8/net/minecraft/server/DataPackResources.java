package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.packs.resources.IReloadListener;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.Reloadable;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.CraftingManager;
import org.slf4j.Logger;

public class DataPackResources {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final ReloadableServerRegistries.a fullRegistryHolder;
    public CommandDispatcher commands;
    private final CraftingManager recipes;
    private final AdvancementDataWorld advancements;
    private final CustomFunctionManager functionLibrary;
    private final List<IRegistry.a<?>> postponedTags;

    private DataPackResources(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, HolderLookup.a holderlookup_a, FeatureFlagSet featureflagset, CommandDispatcher.ServerType commanddispatcher_servertype, List<IRegistry.a<?>> list, int i) {
        this.fullRegistryHolder = new ReloadableServerRegistries.a(layeredregistryaccess.compositeAccess());
        this.postponedTags = list;
        this.recipes = new CraftingManager(holderlookup_a);
        this.commands = new CommandDispatcher(commanddispatcher_servertype, CommandBuildContext.simple(holderlookup_a, featureflagset));
        this.advancements = new AdvancementDataWorld(holderlookup_a);
        this.functionLibrary = new CustomFunctionManager(i, this.commands.getDispatcher());
    }

    public CustomFunctionManager getFunctionLibrary() {
        return this.functionLibrary;
    }

    public ReloadableServerRegistries.a fullRegistries() {
        return this.fullRegistryHolder;
    }

    public CraftingManager getRecipeManager() {
        return this.recipes;
    }

    public CommandDispatcher getCommands() {
        return this.commands;
    }

    public AdvancementDataWorld getAdvancements() {
        return this.advancements;
    }

    public List<IReloadListener> listeners() {
        return List.of(this.recipes, this.functionLibrary, this.advancements);
    }

    public static CompletableFuture<DataPackResources> loadResources(IResourceManager iresourcemanager, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, List<IRegistry.a<?>> list, FeatureFlagSet featureflagset, CommandDispatcher.ServerType commanddispatcher_servertype, int i, Executor executor, Executor executor1) {
        return ReloadableServerRegistries.reload(layeredregistryaccess, list, iresourcemanager, executor).thenCompose((reloadableserverregistries_b) -> {
            DataPackResources datapackresources = new DataPackResources(reloadableserverregistries_b.layers(), reloadableserverregistries_b.lookupWithUpdatedTags(), featureflagset, commanddispatcher_servertype, list, i);

            return Reloadable.create(iresourcemanager, datapackresources.listeners(), executor, executor1, DataPackResources.DATA_RELOAD_INITIAL_TASK, DataPackResources.LOGGER.isDebugEnabled()).done().thenApply((object) -> {
                return datapackresources;
            });
        });
    }

    public void updateStaticRegistryTags() {
        this.postponedTags.forEach(IRegistry.a::apply);
    }
}
