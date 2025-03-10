package net.minecraft;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public record TracingExecutor(ExecutorService service) implements Executor {

    public Executor forName(String s) {
        return (Executor) (SharedConstants.IS_RUNNING_IN_IDE ? (runnable) -> {
            this.service.execute(() -> {
                Thread thread = Thread.currentThread();
                String s1 = thread.getName();

                thread.setName(s);

                try {
                    Zone zone = TracyClient.beginZone(s, SharedConstants.IS_RUNNING_IN_IDE);

                    try {
                        runnable.run();
                    } catch (Throwable throwable) {
                        if (zone != null) {
                            try {
                                zone.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        }

                        throw throwable;
                    }

                    if (zone != null) {
                        zone.close();
                    }
                } finally {
                    thread.setName(s1);
                }

            });
        } : (TracyClient.isAvailable() ? (runnable) -> {
            this.service.execute(() -> {
                Zone zone = TracyClient.beginZone(s, SharedConstants.IS_RUNNING_IN_IDE);

                try {
                    runnable.run();
                } catch (Throwable throwable) {
                    if (zone != null) {
                        try {
                            zone.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (zone != null) {
                    zone.close();
                }

            });
        } : this.service));
    }

    public void execute(Runnable runnable) {
        this.service.execute(wrapUnnamed(runnable));
    }

    public void shutdownAndAwait(long i, TimeUnit timeunit) {
        this.service.shutdown();

        boolean flag;

        try {
            flag = this.service.awaitTermination(i, timeunit);
        } catch (InterruptedException interruptedexception) {
            flag = false;
        }

        if (!flag) {
            this.service.shutdownNow();
        }

    }

    private static Runnable wrapUnnamed(Runnable runnable) {
        return !TracyClient.isAvailable() ? runnable : () -> {
            Zone zone = TracyClient.beginZone("task", SharedConstants.IS_RUNNING_IN_IDE);

            try {
                runnable.run();
            } catch (Throwable throwable) {
                if (zone != null) {
                    try {
                        zone.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                }

                throw throwable;
            }

            if (zone != null) {
                zone.close();
            }

        };
    }
}
