package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface IReloadListener {

    CompletableFuture<Void> reload(IReloadListener.a ireloadlistener_a, IResourceManager iresourcemanager, Executor executor, Executor executor1);

    default String getName() {
        return this.getClass().getSimpleName();
    }

    public interface a {

        <T> CompletableFuture<T> wait(T t0);
    }
}
