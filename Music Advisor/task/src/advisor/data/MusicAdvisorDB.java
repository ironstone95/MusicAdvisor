package advisor.data;

import advisor.models.Album;
import advisor.models.Category;
import advisor.models.Playlist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MusicAdvisorDB {
    private final List<Album> albums;
    private final List<Playlist> featuredLists;
    private final Map<Category, List<Playlist>> categoryPlaylistMap;

    public MusicAdvisorDB() {
        this.albums = new ArrayList<>();
        this.featuredLists = new ArrayList<>();
        this.categoryPlaylistMap = new LinkedHashMap<>();
        setDummyData();
    }

    private Album createAlbum(String name) {
        return new Album(name, "lolo", new String[]{"a"});
    }

    private void addFeaturedLists(String... playListNames) {
        for (String playlistName : playListNames) {
            featuredLists.add(new Playlist(playlistName, "lolo"));
        }
    }

    private void addPlaylistsToCategory(String categoryName, String... playListNames) {
        Category category = new Category("id", categoryName);
        categoryPlaylistMap.putIfAbsent(category, new ArrayList<>());
        List<Playlist> playlists = categoryPlaylistMap.get(category);
        for (String playListName : playListNames) {
            playlists.add(new Playlist(playListName, "lolo"));
        }
    }

    private void setDummyData() {
        //Add albums
        albums.add(createAlbum("Mountains"));
        albums.add(createAlbum("Runaway"));
        albums.add(createAlbum("The Greatest Show"));
        albums.add(createAlbum("All Out Life"));

        //Add Featured
        addFeaturedLists("Mellow Morning", "Wake Up and Smell the Coffee", "Monday Motivation",
                "Songs to Sing in the Shower");

        addPlaylistsToCategory("Top Lists", "World Top 50", "USA Top 50",
                "Canada Top 50", "Turkey Top 50", "Greece Top 50", "Germany Top 50");

        addPlaylistsToCategory("Mood", "Walk Like A Badass",
                "Rage Beats", "Arab Mood Booster", "Sunday Stroll");
    }

    public List<Album> getAlbums() {
        return this.albums;
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categoryPlaylistMap.keySet());
    }

    public List<Playlist> getFeaturedLists() {
        return this.featuredLists;
    }

    public List<Playlist> getListsByCategory(String category) {
        return categoryPlaylistMap.get(new Category("id", category));
    }
}
