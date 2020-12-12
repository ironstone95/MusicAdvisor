package advisor.models;

public class Playlist extends SongList {
    public Playlist(String name, String url) {
        super(name, url);
    }

    @Override
    public String toString() {
        return name + "\n" + url + "\n";
    }
}
