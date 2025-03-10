package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.ConfidentialClientApplication.Builder;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCertificate;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import net.minecraft.util.ChatDeserializer;

public class PlayerSafetyServiceTextFilter extends ServerTextFilter {

    private final ConfidentialClientApplication client;
    private final ClientCredentialParameters clientParameters;
    private final Set<String> fullyFilteredEvents;
    private final int connectionReadTimeoutMs;

    private PlayerSafetyServiceTextFilter(URL url, ServerTextFilter.b servertextfilter_b, ServerTextFilter.a servertextfilter_a, ExecutorService executorservice, ConfidentialClientApplication confidentialclientapplication, ClientCredentialParameters clientcredentialparameters, Set<String> set, int i) {
        super(url, servertextfilter_b, servertextfilter_a, executorservice);
        this.client = confidentialclientapplication;
        this.clientParameters = clientcredentialparameters;
        this.fullyFilteredEvents = set;
        this.connectionReadTimeoutMs = i;
    }

    @Nullable
    public static ServerTextFilter createTextFilterFromConfig(String s) {
        JsonObject jsonobject = ChatDeserializer.parse(s);
        URI uri = URI.create(ChatDeserializer.getAsString(jsonobject, "apiServer"));
        String s1 = ChatDeserializer.getAsString(jsonobject, "apiPath");
        String s2 = ChatDeserializer.getAsString(jsonobject, "scope");
        String s3 = ChatDeserializer.getAsString(jsonobject, "serverId", "");
        String s4 = ChatDeserializer.getAsString(jsonobject, "applicationId");
        String s5 = ChatDeserializer.getAsString(jsonobject, "tenantId");
        String s6 = ChatDeserializer.getAsString(jsonobject, "roomId", "Java:Chat");
        String s7 = ChatDeserializer.getAsString(jsonobject, "certificatePath");
        String s8 = ChatDeserializer.getAsString(jsonobject, "certificatePassword", "");
        int i = ChatDeserializer.getAsInt(jsonobject, "hashesToDrop", -1);
        int j = ChatDeserializer.getAsInt(jsonobject, "maxConcurrentRequests", 7);
        JsonArray jsonarray = ChatDeserializer.getAsJsonArray(jsonobject, "fullyFilteredEvents");
        Set<String> set = new HashSet();

        jsonarray.forEach((jsonelement) -> {
            set.add(ChatDeserializer.convertToString(jsonelement, "filteredEvent"));
        });
        int k = ChatDeserializer.getAsInt(jsonobject, "connectionReadTimeoutMs", 2000);

        URL url;

        try {
            url = uri.resolve(s1).toURL();
        } catch (MalformedURLException malformedurlexception) {
            throw new RuntimeException(malformedurlexception);
        }

        ServerTextFilter.b servertextfilter_b = (gameprofile, s9) -> {
            JsonObject jsonobject1 = new JsonObject();

            jsonobject1.addProperty("userId", gameprofile.getId().toString());
            jsonobject1.addProperty("userDisplayName", gameprofile.getName());
            jsonobject1.addProperty("server", s3);
            jsonobject1.addProperty("room", s6);
            jsonobject1.addProperty("area", "JavaChatRealms");
            jsonobject1.addProperty("data", s9);
            jsonobject1.addProperty("language", "*");
            return jsonobject1;
        };
        ServerTextFilter.a servertextfilter_a = ServerTextFilter.a.select(i);
        ExecutorService executorservice = createWorkerPool(j);

        IClientCertificate iclientcertificate;

        try {
            InputStream inputstream = Files.newInputStream(Path.of(s7));

            try {
                iclientcertificate = ClientCredentialFactory.createFromCertificate(inputstream, s8);
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
        } catch (Exception exception) {
            PlayerSafetyServiceTextFilter.LOGGER.warn("Failed to open certificate file");
            return null;
        }

        ConfidentialClientApplication confidentialclientapplication;

        try {
            confidentialclientapplication = ((Builder) ((Builder) ConfidentialClientApplication.builder(s4, iclientcertificate).sendX5c(true).executorService(executorservice)).authority(String.format(Locale.ROOT, "https://login.microsoftonline.com/%s/", s5))).build();
        } catch (Exception exception1) {
            PlayerSafetyServiceTextFilter.LOGGER.warn("Failed to create confidential client application");
            return null;
        }

        ClientCredentialParameters clientcredentialparameters = ClientCredentialParameters.builder(Set.of(s2)).build();

        return new PlayerSafetyServiceTextFilter(url, servertextfilter_b, servertextfilter_a, executorservice, confidentialclientapplication, clientcredentialparameters, set, k);
    }

    private IAuthenticationResult aquireIAuthenticationResult() {
        return (IAuthenticationResult) this.client.acquireToken(this.clientParameters).join();
    }

    @Override
    protected void setAuthorizationProperty(HttpURLConnection httpurlconnection) {
        IAuthenticationResult iauthenticationresult = this.aquireIAuthenticationResult();

        httpurlconnection.setRequestProperty("Authorization", "Bearer " + iauthenticationresult.accessToken());
    }

    @Override
    protected FilteredText filterText(String s, ServerTextFilter.a servertextfilter_a, JsonObject jsonobject) {
        JsonObject jsonobject1 = ChatDeserializer.getAsJsonObject(jsonobject, "result", (JsonObject) null);

        if (jsonobject1 == null) {
            return FilteredText.fullyFiltered(s);
        } else {
            boolean flag = ChatDeserializer.getAsBoolean(jsonobject1, "filtered", true);

            if (!flag) {
                return FilteredText.passThrough(s);
            } else {
                JsonArray jsonarray = ChatDeserializer.getAsJsonArray(jsonobject1, "events", new JsonArray());
                Iterator iterator = jsonarray.iterator();

                String s1;

                do {
                    if (!iterator.hasNext()) {
                        JsonArray jsonarray1 = ChatDeserializer.getAsJsonArray(jsonobject1, "redactedTextIndex", new JsonArray());

                        return new FilteredText(s, this.parseMask(s, jsonarray1, servertextfilter_a));
                    }

                    JsonElement jsonelement = (JsonElement) iterator.next();
                    JsonObject jsonobject2 = jsonelement.getAsJsonObject();

                    s1 = ChatDeserializer.getAsString(jsonobject2, "id", "");
                } while (!this.fullyFilteredEvents.contains(s1));

                return FilteredText.fullyFiltered(s);
            }
        }
    }

    @Override
    protected int connectionReadTimeout() {
        return this.connectionReadTimeoutMs;
    }
}
