package advisor.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Album extends SongList {
    private final String[] artists;

    public Album(String name, String url, String[] artists) {
        super(name, url);
        this.artists = artists;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append("\n");
        if (artists.length > 0) {
            builder.append("[");
            for (int i = 0; i < artists.length - 1; i++) {
                builder.append(artists[i]).append(", ");
            }
            builder.append(artists[artists.length - 1]).append("]\n");
        }
        builder.append(url).append("\n");
        return builder.toString();
    }
}
