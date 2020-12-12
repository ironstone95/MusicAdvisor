package advisor.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

abstract public class SongList {
    protected final String name;
    protected final String url;
    protected final Set<Song> songs;


    public SongList(String name, String url) {
        this.name = name;
        this.url = url;
        this.songs = new LinkedHashSet<>();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public List<Song> getSongs() {
        return new ArrayList<>(songs);
    }

    abstract public String toString();
}
