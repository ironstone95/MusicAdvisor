package advisor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Authorization {
    // Configuration fields
    private static final String DEFAULT_ACCESS_URI = "https://accounts.spotify.com";
    private static final String DEFAULT_RESOURCE_URI = "https://api.spotify.com";
    private static Authorization INSTANCE = null;

    private final static String REDIRECT_URI = "http://localhost:8080";
    private static final String CLIENT_ID = "MUST_BE_PROVIDED";
    private static final String CLIENT_SECRET = "MUST_BE_PROVIDED";

    private static String accessURI = null;
    private static String resourceURI = null;


    //Authorization fields
    private String authorizationCode = null;

    //Token Fields
    private AuthTokens tokens;


    public static Authorization getInstance() {
        return getInstance(null, null);
    }

    public static Authorization getInstance(String argsAccess, String argsResource) {
        if (INSTANCE == null) {
            INSTANCE = new Authorization();
            if (argsAccess == null) {
                accessURI = DEFAULT_ACCESS_URI;
            } else if (accessURI == null) {
                accessURI = argsAccess;
            }

            if (argsResource == null) {
                resourceURI = DEFAULT_RESOURCE_URI;
            } else if (resourceURI == null) {
                resourceURI = argsResource;
            }
        }
        return INSTANCE;
    }

    public void showAuthorizationLink() {
        String link = String.format("%s/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                accessURI, CLIENT_ID, REDIRECT_URI);
        System.out.println("use this link to request the access code:");
        System.out.println(link);
    }

    public void authorizeViaHttpServer() {
        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            server.start();
            server.createContext("/", exchange -> {
                String responseMessage;
                int responseCode;
                String query = exchange.getRequestURI().getQuery();
                if (query != null) {
                    String code = query.contains("code") ? query.substring(5) : null;

                    if (code != null) {
                        System.out.println("code received");
                        this.authorizationCode = code;
                        responseCode = 200;
                        responseMessage = "Got the code. Return back to your program.";
                    } else {
                        responseCode = 400;
                        responseMessage = "Authorization code not found. Try again.";
                    }
                } else {
                    responseCode = 400;
                    responseMessage = "Authorization code not found. Try again.";
                }
                exchange.sendResponseHeaders(responseCode, responseMessage.length());
                exchange.getResponseBody().write(responseMessage.getBytes());
                exchange.getResponseBody().close();
            });
            System.out.println("waiting for code...");
            Thread.sleep(5000);
            server.stop(1);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setAccessTokens() {
        System.out.println("making http request for access_token...");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(accessURI + "/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.
                        ofString(String.format("grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s",
                                authorizationCode, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response != null) {
                System.out.println("response:" + response.body());
                Gson gson = new Gson();
                Type responseMapType = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> responseMap = gson.fromJson(response.body(), responseMapType);
                this.tokens = new AuthTokens(responseMap.get("access_token"),
                        responseMap.get("token_type"), responseMap.get("scope"), responseMap.get("expires_in"),
                        responseMap.get("refresh_token"), System.currentTimeMillis() / 1000);
                if (tokens.allFieldsSet()) {
                    if (areTokensValid()) {
                        System.out.println("---SUCCESS---");
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public boolean isAuthorized() {
        return areTokensValid();
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public String getAccessToken() {
        return tokens.accessToken;
    }

    public void writeToFile() {
        if (!Objects.isNull(tokens)) {
            try (FileWriter fr = new FileWriter("./tokens.txt")) {
                Gson gson = new Gson();
                String json = gson.toJson(tokens);
                fr.write(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Authorization() {
        File file = new File("./tokens.txt");
        String jsonString = null;
        if (file.exists()) {
            try (FileReader fr = new FileReader(file)) {
                try (BufferedReader br = new BufferedReader(fr)) {
                    StringBuilder builder = new StringBuilder();
                    br.lines().forEach(builder::append);
                    jsonString = builder.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (jsonString != null) {
            JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
            long lastUpdate = obj.get("lastUpdate").getAsLong();
            long expiresIn = (long) obj.get("expiresIn").getAsDouble();
            if (System.currentTimeMillis() / 1000 - lastUpdate < expiresIn) {
                Gson gson = new Gson();
                try (FileReader fr = new FileReader("./tokens.txt")) {
                    this.tokens = gson.fromJson(fr, AuthTokens.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean areTokensValid() {
        if (Objects.isNull(tokens)) {
            return false;
        }

        return tokens.expiresIn > ((double) System.currentTimeMillis() / 1000 - tokens.lastUpdate);
    }


    private static class AuthTokens {
        String accessToken;
        String tokenType;
        String scope;
        double expiresIn;
        String refreshToken;
        long lastUpdate;

        private AuthTokens(Object accessToken, Object tokenType, Object scope, Object expiresIn, Object refreshToken, long lastUpdate) {
            this.accessToken = (String) accessToken;
            this.tokenType = (String) tokenType;
            this.scope = (String) scope;
            this.expiresIn = (double) expiresIn;
            this.refreshToken = (String) refreshToken;
            this.lastUpdate = lastUpdate;
        }

        private boolean allFieldsSet() {
            return !Objects.isNull(accessToken)
                    && !Objects.isNull(tokenType)
                    && !Objects.isNull(scope)
                    && !Objects.isNull(refreshToken);
        }

        @Override
        public String toString() {
            return "AuthTokens{" +
                    "accessToken='" + accessToken + '\'' +
                    ", tokenType='" + tokenType + '\'' +
                    ", scope='" + scope + '\'' +
                    ", expiresIn=" + expiresIn +
                    ", refreshToken='" + refreshToken + '\'' +
                    ", lastUpdate=" + lastUpdate +
                    '}';
        }
    }
}
