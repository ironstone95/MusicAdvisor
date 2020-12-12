package advisor;

import java.util.List;
import java.util.Objects;

public class MusicAdvisorController {
    private final Authorization authorization;
    private final SpotifyRepository repository;
    private final String[] args;
    private final int limit;
    private List<?> fetchedList;

    public MusicAdvisorController(String[] args) {
        this.args = args;
        String accessPoint = getAccessPoint();
        String resourcePoint = getResourcePoint();
        authorization = Authorization.getInstance(accessPoint, resourcePoint);
        repository = new SpotifyRepository();
        limit = getLimit();
    }


    public void auth() {
        authorization.showAuthorizationLink();
        authorization.authorizeViaHttpServer();
        authorization.setAccessTokens();
    }

    // 0 -> Will print list, 1 -> Authorization Required, 2 -> No more pages
    public int getNewReleases(int page) {
        if (notAuthorized()) {
            return 1;
        }
        fetchedList = repository.getNewReleases();
        if (noMorePages(page)) {
            return 2;
        }
        return 0;
    }

    // 0 -> Will print list, 1 -> Authorization Required, 2 -> No more pages
    public int getFeaturedLists(int page) {
        if (notAuthorized()) {
            return 1;
        }
        fetchedList = repository.getFeaturedLists();
        if (noMorePages(page)) {
            return 2;
        }
        return 0;
    }

    // 0 -> Will print list, 1 -> Authorization Required, 2 -> No more pages
    public int getCategoryList(int page) {
        if (notAuthorized()) {
            return 1;
        }
        fetchedList = repository.getCategories();
        if (noMorePages(page)) {
            return 2;
        }
        return 0;
    }

    // 0 -> Will print list, 1 -> Authorization Required, 2 -> No more pages, 3 -> Category Id Not Correct
    public int getPlaylists(int page, String category) {
        if (notAuthorized()) {
            return 1;
        }
        fetchedList = repository.getPlaylists(category);
        if (Objects.isNull(fetchedList)) {
            return 3;
        }
        if (noMorePages(page)) {
            return 2;
        }
        return 0;
    }

    public boolean notAuthorized() {
        if (authorization == null) {
            return true;
        } else {
            return !authorization.isAuthorized();
        }
    }

    public void exit() {
        Authorization.getInstance().writeToFile();
    }

    public String getPagedList(int page) {
        int from = page * limit;
        int to = Math.min((page + 1) * limit, fetchedList.size());
        int currentPage = page + 1;
        int totalPage = (int) Math.ceil(((double) fetchedList.size()) / limit);

        if (noMorePages(page)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = from; i < to; i++) {
            builder.append(fetchedList.get(i)).append("\n");
        }
        builder.append("\n---PAGE ").append(currentPage).append(" OF ")
                .append(totalPage)
                .append("---");
        return builder.toString();
    }

    private int getLimit() {
        if (args != null) {
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("-page")) {
                    return Integer.parseInt(args[i + 1]);
                }
            }
        }
        return 5;
    }

    private String getAccessPoint() {
        if (args != null) {
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("-access")) {
                    return args[i + 1];
                }
            }
        }
        return null;
    }

    private String getResourcePoint() {
        if (args != null) {
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("-resource")) {
                    return args[i + 1];
                }
            }
        }
        return null;
    }

    private boolean noMorePages(int page) {
        int currentPage = page + 1;
        int totalPage = (int) Math.ceil(((double) fetchedList.size()) / limit);

        return currentPage > totalPage || page < 0;
    }
}
