package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.util.ChatDeserializer;

public class LegacyTextFilter extends ServerTextFilter {

    private static final String ENDPOINT = "v1/chat";
    final URL joinEndpoint;
    final LegacyTextFilter.a joinEncoder;
    final URL leaveEndpoint;
    final LegacyTextFilter.a leaveEncoder;
    private final String authKey;

    private LegacyTextFilter(URL url, ServerTextFilter.b servertextfilter_b, URL url1, LegacyTextFilter.a legacytextfilter_a, URL url2, LegacyTextFilter.a legacytextfilter_a1, String s, ServerTextFilter.a servertextfilter_a, ExecutorService executorservice) {
        super(url, servertextfilter_b, servertextfilter_a, executorservice);
        this.joinEndpoint = url1;
        this.joinEncoder = legacytextfilter_a;
        this.leaveEndpoint = url2;
        this.leaveEncoder = legacytextfilter_a1;
        this.authKey = s;
    }

    @Nullable
    public static ServerTextFilter createTextFilterFromConfig(String s) {
        try {
            JsonObject jsonobject = ChatDeserializer.parse(s);
            URI uri = new URI(ChatDeserializer.getAsString(jsonobject, "apiServer"));
            String s1 = ChatDeserializer.getAsString(jsonobject, "apiKey");

            if (s1.isEmpty()) {
                throw new IllegalArgumentException("Missing API key");
            } else {
                int i = ChatDeserializer.getAsInt(jsonobject, "ruleId", 1);
                String s2 = ChatDeserializer.getAsString(jsonobject, "serverId", "");
                String s3 = ChatDeserializer.getAsString(jsonobject, "roomId", "Java:Chat");
                int j = ChatDeserializer.getAsInt(jsonobject, "hashesToDrop", -1);
                int k = ChatDeserializer.getAsInt(jsonobject, "maxConcurrentRequests", 7);
                JsonObject jsonobject1 = ChatDeserializer.getAsJsonObject(jsonobject, "endpoints", (JsonObject) null);
                String s4 = getEndpointFromConfig(jsonobject1, "chat", "v1/chat");
                boolean flag = s4.equals("v1/chat");
                URL url = uri.resolve("/" + s4).toURL();
                URL url1 = getEndpoint(uri, jsonobject1, "join", "v1/join");
                URL url2 = getEndpoint(uri, jsonobject1, "leave", "v1/leave");
                LegacyTextFilter.a legacytextfilter_a = (gameprofile) -> {
                    JsonObject jsonobject2 = new JsonObject();

                    jsonobject2.addProperty("server", s2);
                    jsonobject2.addProperty("room", s3);
                    jsonobject2.addProperty("user_id", gameprofile.getId().toString());
                    jsonobject2.addProperty("user_display_name", gameprofile.getName());
                    return jsonobject2;
                };
                ServerTextFilter.b servertextfilter_b;

                if (flag) {
                    servertextfilter_b = (gameprofile, s5) -> {
                        JsonObject jsonobject2 = new JsonObject();

                        jsonobject2.addProperty("rule", i);
                        jsonobject2.addProperty("server", s2);
                        jsonobject2.addProperty("room", s3);
                        jsonobject2.addProperty("player", gameprofile.getId().toString());
                        jsonobject2.addProperty("player_display_name", gameprofile.getName());
                        jsonobject2.addProperty("text", s5);
                        jsonobject2.addProperty("language", "*");
                        return jsonobject2;
                    };
                } else {
                    String s5 = String.valueOf(i);

                    servertextfilter_b = (gameprofile, s6) -> {
                        JsonObject jsonobject2 = new JsonObject();

                        jsonobject2.addProperty("rule_id", s5);
                        jsonobject2.addProperty("category", s2);
                        jsonobject2.addProperty("subcategory", s3);
                        jsonobject2.addProperty("user_id", gameprofile.getId().toString());
                        jsonobject2.addProperty("user_display_name", gameprofile.getName());
                        jsonobject2.addProperty("text", s6);
                        jsonobject2.addProperty("language", "*");
                        return jsonobject2;
                    };
                }

                ServerTextFilter.a servertextfilter_a = ServerTextFilter.a.select(j);
                ExecutorService executorservice = createWorkerPool(k);
                String s6 = Base64.getEncoder().encodeToString(s1.getBytes(StandardCharsets.US_ASCII));

                return new LegacyTextFilter(url, servertextfilter_b, url1, legacytextfilter_a, url2, legacytextfilter_a, s6, servertextfilter_a, executorservice);
            }
        } catch (Exception exception) {
            LegacyTextFilter.LOGGER.warn("Failed to parse chat filter config {}", s, exception);
            return null;
        }
    }

    @Override
    public ITextFilter createContext(GameProfile gameprofile) {
        return new ServerTextFilter.c(gameprofile) {
            @Override
            public void join() {
                LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.joinEndpoint, LegacyTextFilter.this.joinEncoder, this.streamExecutor);
            }

            @Override
            public void leave() {
                LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.leaveEndpoint, LegacyTextFilter.this.leaveEncoder, this.streamExecutor);
            }
        };
    }

    void processJoinOrLeave(GameProfile gameprofile, URL url, LegacyTextFilter.a legacytextfilter_a, Executor executor) {
        executor.execute(() -> {
            JsonObject jsonobject = legacytextfilter_a.encode(gameprofile);

            try {
                this.processRequest(jsonobject, url);
            } catch (Exception exception) {
                LegacyTextFilter.LOGGER.warn("Failed to send join/leave packet to {} for player {}", new Object[]{url, gameprofile, exception});
            }

        });
    }

    private void processRequest(JsonObject jsonobject, URL url) throws IOException {
        HttpURLConnection httpurlconnection = this.makeRequest(jsonobject, url);
        InputStream inputstream = httpurlconnection.getInputStream();

        try {
            this.drainStream(inputstream);
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

    }

    @Override
    protected void setAuthorizationProperty(HttpURLConnection httpurlconnection) {
        httpurlconnection.setRequestProperty("Authorization", "Basic " + this.authKey);
    }

    @Override
    protected FilteredText filterText(String s, ServerTextFilter.a servertextfilter_a, JsonObject jsonobject) {
        boolean flag = ChatDeserializer.getAsBoolean(jsonobject, "response", false);

        if (flag) {
            return FilteredText.passThrough(s);
        } else {
            String s1 = ChatDeserializer.getAsString(jsonobject, "hashed", (String) null);

            if (s1 == null) {
                return FilteredText.fullyFiltered(s);
            } else {
                JsonArray jsonarray = ChatDeserializer.getAsJsonArray(jsonobject, "hashes");
                FilterMask filtermask = this.parseMask(s, jsonarray, servertextfilter_a);

                return new FilteredText(s, filtermask);
            }
        }
    }

    @FunctionalInterface
    private interface a {

        JsonObject encode(GameProfile gameprofile);
    }
}
