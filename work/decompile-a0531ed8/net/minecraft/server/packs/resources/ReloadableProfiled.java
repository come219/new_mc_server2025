package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.SystemUtils;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import org.slf4j.Logger;

public class ReloadableProfiled extends Reloadable<ReloadableProfiled.a> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Stopwatch total = Stopwatch.createUnstarted();

    public ReloadableProfiled(IResourceManager iresourcemanager, List<IReloadListener> list, Executor executor, Executor executor1, CompletableFuture<Unit> completablefuture) {
        super(executor, executor1, iresourcemanager, list, (ireloadlistener_a, iresourcemanager1, ireloadlistener, executor2, executor3) -> {
            AtomicLong atomiclong = new AtomicLong();
            AtomicLong atomiclong1 = new AtomicLong();
            CompletableFuture<Void> completablefuture1 = ireloadlistener.reload(ireloadlistener_a, iresourcemanager1, profiledExecutor(executor2, atomiclong, ireloadlistener.getName()), profiledExecutor(executor3, atomiclong1, ireloadlistener.getName()));

            return completablefuture1.thenApplyAsync((ovoid) -> {
                ReloadableProfiled.LOGGER.debug("Finished reloading {}", ireloadlistener.getName());
                return new ReloadableProfiled.a(ireloadlistener.getName(), atomiclong, atomiclong1);
            }, executor1);
        }, completablefuture);
        this.total.start();
        this.allDone = this.allDone.thenApplyAsync(this::finish, executor1);
    }

    private static Executor profiledExecutor(Executor executor, AtomicLong atomiclong, String s) {
        return (runnable) -> {
            executor.execute(() -> {
                GameProfilerFiller gameprofilerfiller = Profiler.get();

                gameprofilerfiller.push(s);
                long i = SystemUtils.getNanos();

                runnable.run();
                atomiclong.addAndGet(SystemUtils.getNanos() - i);
                gameprofilerfiller.pop();
            });
        };
    }

    private List<ReloadableProfiled.a> finish(List<ReloadableProfiled.a> list) {
        this.total.stop();
        long i = 0L;

        ReloadableProfiled.LOGGER.info("Resource reload finished after {} ms", this.total.elapsed(TimeUnit.MILLISECONDS));

        long j;

        for (Iterator iterator = list.iterator(); iterator.hasNext(); i += j) {
            ReloadableProfiled.a reloadableprofiled_a = (ReloadableProfiled.a) iterator.next();
            long k = TimeUnit.NANOSECONDS.toMillis(reloadableprofiled_a.preparationNanos.get());

            j = TimeUnit.NANOSECONDS.toMillis(reloadableprofiled_a.reloadNanos.get());
            long l = k + j;
            String s = reloadableprofiled_a.name;

            ReloadableProfiled.LOGGER.info("{} took approximately {} ms ({} ms preparing, {} ms applying)", new Object[]{s, l, k, j});
        }

        ReloadableProfiled.LOGGER.info("Total blocking time: {} ms", i);
        return list;
    }

    public static record a(String name, AtomicLong preparationNanos, AtomicLong reloadNanos) {

    }
}
