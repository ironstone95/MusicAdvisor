package advisor;

import advisor.models.Album;
import advisor.models.Category;
import advisor.models.Playlist;
import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class SpotifyRepository {
    private final static String ALL_CATEGORIES_URI = "/v1/browse/categories";
    private final static String FEATURED_URI = "/v1/browse/featured-playlists";
    private final static String NEW_RELEASES_URI = "/v1/browse/new-releases";
    private final String resourceUri;

    //Cached Data
    private Map<String, Category> categoryMap;
    private Map<String, List<Playlist>> playListMap;
    private List<Playlist> featuredList;
    private List<Album> newReleases;

    public SpotifyRepository() {
        resourceUri = Authorization.getInstance().getResourceURI();
    }

    /**
     * To get all categories, use https://api.spotify.com/v1/browse/categories To get a playlist, use
     * https://api.spotify.com/v1/browse/categories/{category_id}/playlists To get new releases, use
     * https://api.spotify.com/v1/browse/new-releases To get featured playlists, use
     * https://api.spotify.com/v1/browse/featured-playlists
     */

    public List<String> getCategories() {
        if (Objects.isNull(categoryMap)) {
            categoryMap = new LinkedHashMap<>();
            if (Authorization.getInstance().isAuthorized()) {
                HttpClient client = HttpClient.newBuilder().build();
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(resourceUri + ALL_CATEGORIES_URI))
                        .header("Authorization", "Bearer " + Authorization.getInstance().getAccessToken())
                        .build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonArray jo = JsonParser.parseString(response.body())
                            .getAsJsonObject().get("categories")
                            .getAsJsonObject().getAsJsonArray("items");
                    for (JsonElement c : jo) {
                        JsonObject obj = c.getAsJsonObject();
                        String id = obj.get("id").getAsString();
                        String name = obj.get("name").getAsString();
                        categoryMap.put(name, new Category(id, name));
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<>(categoryMap.keySet());
    }

    public List<Playlist> getFeaturedLists() {
        if (Objects.isNull(featuredList)) {
            featuredList = new ArrayList<>();
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resourceUri + FEATURED_URI))
                    .GET()
                    .header("Authorization", "Bearer " + Authorization.getInstance().getAccessToken())
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonArray jArr = JsonParser.parseString(response.body()).getAsJsonObject()
                        .get("playlists").getAsJsonObject()
                        .getAsJsonArray("items");
                for (JsonElement elem : jArr) {
                    JsonObject obj = elem.getAsJsonObject();
                    String url = obj.get("external_urls").getAsJsonObject().get("spotify").getAsString();
                    String name = obj.get("name").getAsString();
                    featuredList.add(new Playlist(name, url));
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return featuredList;
    }

    public List<Playlist> getPlaylists(String categoryName) {
        if (Objects.isNull(categoryMap)) {
            getCategories();
        }
        if (!categoryMap.containsKey(categoryName)) {
            return null;
        }
        if (Objects.isNull(playListMap)) {
            playListMap = new HashMap<>();
        }
        if (playListMap.containsKey(categoryName)) {
            return playListMap.get(categoryName);
        } else {
            Category category = categoryMap.get(categoryName);
            List<Playlist> list = new ArrayList<>();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(String.format("%s/v1/browse/categories/%s/playlists", resourceUri, category.getId())))
                    .header("Authorization", "Bearer " + Authorization.getInstance().getAccessToken())
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response != null) {
                    if (response.body() != null) {
                        JsonElement jsonElement = JsonParser.parseString(response.body());
                        if (jsonElement != null && !jsonElement.isJsonNull()) {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            JsonElement jsonError = jsonObject.get("error");
                            if (jsonError != null && !jsonError.isJsonNull()) {
                                System.out.println(response.body());
                                return null;
                            }
                            JsonElement jsonElement1 = jsonObject.get("playlists");
                            if (jsonElement1 != null && !jsonElement1.isJsonNull()) {
                                JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
                                JsonArray jArr = jsonObject1.getAsJsonArray("items");
                                if (!jArr.isJsonNull()) {
                                    for (JsonElement elem : jArr) {
                                        JsonObject obj = elem.getAsJsonObject();
                                        String listName = obj.get("name").getAsString();
                                        String url = obj.get("external_urls").getAsJsonObject().get("spotify").getAsString();
                                        list.add(new Playlist(listName, url));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }
            playListMap.put(categoryName, list);
            return list;
        }
    }

    public List<Album> getNewReleases() {
        if (newReleases == null) {
            newReleases = new ArrayList<>();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(resourceUri + NEW_RELEASES_URI))
                    .header("Authorization", "Bearer " + Authorization.getInstance().getAccessToken())
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonArray jArr = JsonParser.parseString(response.body())
                        .getAsJsonObject().get("albums").getAsJsonObject()
                        .getAsJsonArray("items");

                for (JsonElement elem : jArr) {
                    JsonObject obj = elem.getAsJsonObject();
                    String albumName = obj.get("name").getAsString();
                    String url = obj.get("external_urls").getAsJsonObject().get("spotify").getAsString();
                    JsonArray artistsJsonArr = obj.getAsJsonArray("artists");
                    List<String> artistList = new ArrayList<>();
                    for (JsonElement elem1 : artistsJsonArr) {
                        JsonObject obj1 = elem1.getAsJsonObject();
                        String artistName = obj1.get("name").getAsString();
                        artistList.add(artistName);
                    }
                    String[] artistsArr = new String[artistList.size()];
                    artistList.toArray(artistsArr);
                    Album album = new Album(albumName, url, artistsArr);
                    newReleases.add(album);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }

        return newReleases;
    }
}
