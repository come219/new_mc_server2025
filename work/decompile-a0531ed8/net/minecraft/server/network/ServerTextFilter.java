package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.UtilColor;
import net.minecraft.util.thread.ConsecutiveExecutor;
import org.slf4j.Logger;

public abstract class ServerTextFilter implements AutoCloseable {

    protected static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ThreadFactory THREAD_FACTORY = (runnable) -> {
        Thread thread = new Thread(runnable);

        thread.setName("Chat-Filter-Worker-" + ServerTextFilter.WORKER_COUNT.getAndIncrement());
        return thread;
    };
    private final URL chatEndpoint;
    private final ServerTextFilter.b chatEncoder;
    final ServerTextFilter.a chatIgnoreStrategy;
    final ExecutorService workerPool;

    protected static ExecutorService createWorkerPool(int i) {
        return Executors.newFixedThreadPool(i, ServerTextFilter.THREAD_FACTORY);
    }

    protected ServerTextFilter(URL url, ServerTextFilter.b servertextfilter_b, ServerTextFilter.a servertextfilter_a, ExecutorService executorservice) {
        this.chatIgnoreStrategy = servertextfilter_a;
        this.workerPool = executorservice;
        this.chatEndpoint = url;
        this.chatEncoder = servertextfilter_b;
    }

    protected static URL getEndpoint(URI uri, @Nullable JsonObject jsonobject, String s, String s1) throws MalformedURLException {
        String s2 = getEndpointFromConfig(jsonobject, s, s1);

        return uri.resolve("/" + s2).toURL();
    }

    protected static String getEndpointFromConfig(@Nullable JsonObject jsonobject, String s, String s1) {
        return jsonobject != null ? ChatDeserializer.getAsString(jsonobject, s, s1) : s1;
    }

    @Nullable
    public static ServerTextFilter createFromConfig(DedicatedServerProperties dedicatedserverproperties) {
        String s = dedicatedserverproperties.textFilteringConfig;

        if (UtilColor.isBlank(s)) {
            return null;
        } else {
            ServerTextFilter servertextfilter;

            switch (dedicatedserverproperties.textFilteringVersion) {
                case 0:
                    servertextfilter = LegacyTextFilter.createTextFilterFromConfig(s);
                    break;
                case 1:
                    servertextfilter = PlayerSafetyServiceTextFilter.createTextFilterFromConfig(s);
                    break;
                default:
                    ServerTextFilter.LOGGER.warn("Could not create text filter - unsupported text filtering version used");
                    servertextfilter = null;
            }

            return servertextfilter;
        }
    }

    protected CompletableFuture<FilteredText> requestMessageProcessing(GameProfile gameprofile, String s, ServerTextFilter.a servertextfilter_a, Executor executor) {
        return s.isEmpty() ? CompletableFuture.completedFuture(FilteredText.EMPTY) : CompletableFuture.supplyAsync(() -> {
            JsonObject jsonobject = this.chatEncoder.encode(gameprofile, s);

            try {
                JsonObject jsonobject1 = this.processRequestResponse(jsonobject, this.chatEndpoint);

                return this.filterText(s, servertextfilter_a, jsonobject1);
            } catch (Exception exception) {
                ServerTextFilter.LOGGER.warn("Failed to validate message '{}'", s, exception);
                return FilteredText.fullyFiltered(s);
            }
        }, executor);
    }

    protected abstract FilteredText filterText(String s, ServerTextFilter.a servertextfilter_a, JsonObject jsonobject);

    protected FilterMask parseMask(String s, JsonArray jsonarray, ServerTextFilter.a servertextfilter_a) {
        if (jsonarray.isEmpty()) {
            return FilterMask.PASS_THROUGH;
        } else if (servertextfilter_a.shouldIgnore(s, jsonarray.size())) {
            return FilterMask.FULLY_FILTERED;
        } else {
            FilterMask filtermask = new FilterMask(s.length());

            for (int i = 0; i < jsonarray.size(); ++i) {
                filtermask.setFiltered(jsonarray.get(i).getAsInt());
            }

            return filtermask;
        }
    }

    public void close() {
        this.workerPool.shutdownNow();
    }

    protected void drainStream(InputStream inputstream) throws IOException {
        byte[] abyte = new byte[1024];

        while (inputstream.read(abyte) != -1) {
            ;
        }

    }

    private JsonObject processRequestResponse(JsonObject jsonobject, URL url) throws IOException {
        HttpURLConnection httpurlconnection = this.makeRequest(jsonobject, url);
        InputStream inputstream = httpurlconnection.getInputStream();

        JsonObject jsonobject1;
        label91:
        {
            try {
                if (httpurlconnection.getResponseCode() != 204) {
                    try {
                        jsonobject1 = Streams.parse(new JsonReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))).getAsJsonObject();
                        break label91;
                    } finally {
                        this.drainStream(inputstream);
                    }
                }

                jsonobject1 = new JsonObject();
            } catch (Throwable throwable) {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                }

                throw throwable;
            }

            if (inputstream != null) {
                inputstream.close();
            }

            return jsonobject1;
        }

        if (inputstream != null) {
            inputstream.close();
        }

        return jsonobject1;
    }

    protected HttpURLConnection makeRequest(JsonObject jsonobject, URL url) throws IOException {
        HttpURLConnection httpurlconnection = this.getURLConnection(url);

        this.setAuthorizationProperty(httpurlconnection);
        OutputStreamWriter outputstreamwriter = new OutputStreamWriter(httpurlconnection.getOutputStream(), StandardCharsets.UTF_8);

        try {
            JsonWriter jsonwriter = new JsonWriter(outputstreamwriter);

            try {
                Streams.write(jsonobject, jsonwriter);
            } catch (Throwable throwable) {
                try {
                    jsonwriter.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            jsonwriter.close();
        } catch (Throwable throwable2) {
            try {
                outputstreamwriter.close();
            } catch (Throwable throwable3) {
                throwable2.addSuppressed(throwable3);
            }

            throw throwable2;
        }

        outputstreamwriter.close();
        int i = httpurlconnection.getResponseCode();

        if (i >= 200 && i < 300) {
            return httpurlconnection;
        } else {
            throw new ServerTextFilter.d("" + i + " " + httpurlconnection.getResponseMessage());
        }
    }

    protected abstract void setAuthorizationProperty(HttpURLConnection httpurlconnection);

    protected int connectionReadTimeout() {
        return 2000;
    }

    protected HttpURLConnection getURLConnection(URL url) throws IOException {
        HttpURLConnection httpurlconnection = (HttpURLConnection) url.openConnection();

        httpurlconnection.setConnectTimeout(15000);
        httpurlconnection.setReadTimeout(this.connectionReadTimeout());
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoOutput(true);
        httpurlconnection.setDoInput(true);
        httpurlconnection.setRequestMethod("POST");
        httpurlconnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        httpurlconnection.setRequestProperty("Accept", "application/json");
        httpurlconnection.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().getName());
        return httpurlconnection;
    }

    public ITextFilter createContext(GameProfile gameprofile) {
        return new ServerTextFilter.c(gameprofile);
    }

    @FunctionalInterface
    public interface a {

        ServerTextFilter.a NEVER_IGNORE = (s, i) -> {
            return false;
        };
        ServerTextFilter.a IGNORE_FULLY_FILTERED = (s, i) -> {
            return s.length() == i;
        };

        static ServerTextFilter.a ignoreOverThreshold(int i) {
            return (s, j) -> {
                return j >= i;
            };
        }

        static ServerTextFilter.a select(int i) {
            ServerTextFilter.a servertextfilter_a;

            switch (i) {
                case -1:
                    servertextfilter_a = ServerTextFilter.a.NEVER_IGNORE;
                    break;
                case 0:
                    servertextfilter_a = ServerTextFilter.a.IGNORE_FULLY_FILTERED;
                    break;
                default:
                    servertextfilter_a = ignoreOverThreshold(i);
            }

            return servertextfilter_a;
        }

        boolean shouldIgnore(String s, int i);
    }

    @FunctionalInterface
    protected interface b {

        JsonObject encode(GameProfile gameprofile, String s);
    }

    protected static class d extends RuntimeException {

        protected d(String s) {
            super(s);
        }
    }

    protected class c implements ITextFilter {

        protected final GameProfile profile;
        protected final Executor streamExecutor;

        protected c(final GameProfile gameprofile) {
            this.profile = gameprofile;
            ConsecutiveExecutor consecutiveexecutor = new ConsecutiveExecutor(ServerTextFilter.this.workerPool, "chat stream for " + gameprofile.getName());

            Objects.requireNonNull(consecutiveexecutor);
            this.streamExecutor = consecutiveexecutor::schedule;
        }

        @Override
        public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
            List<CompletableFuture<FilteredText>> list1 = (List) list.stream().map((s) -> {
                return ServerTextFilter.this.requestMessageProcessing(this.profile, s, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor);
            }).collect(ImmutableList.toImmutableList());

            return SystemUtils.sequenceFailFast(list1).exceptionally((throwable) -> {
                return ImmutableList.of();
            });
        }

        @Override
        public CompletableFuture<FilteredText> processStreamMessage(String s) {
            return ServerTextFilter.this.requestMessageProcessing(this.profile, s, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor);
        }
    }
}
