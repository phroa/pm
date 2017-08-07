package net.phroa.pm.model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Ore {

    String BASE_URL = "https://ore.spongepowered.org/api/";

    /**
     * String categoryList,
     * int sort
     * String q
     * int limit
     * int offset
     */
    @GET("projects")
    Call<List<Project>> listProjects(@QueryMap Map<String, String> options);

    @GET("projects/{pluginId}")
    Call<Project> getProject(@Path("pluginId") String id);

    @GET("projects/{pluginId}/versions")
    Call<List<ProjectVersion>> listProjectVersions(@Path("pluginId") String id, @QueryMap Map<String, String> options);

    @GET("projects/{pluginId}/versions/{version}")
    Call<ProjectVersion> getProjectVersion(@Path("pluginId") String id, @Path("version") String version);

    @GET("projects/{pluginId}/versions/{version}/download")
    Call<byte[]> download(@Path("pluginId") String id, @Path("version") String version);

    enum Category {
        AdministratorTools(0, "admin", "Administrator Tools"),
        ChatTools(1, "chat", "Chat Tools"),
        DeveloperTools(2, "dev", "Developer Tools"),
        Economy(3, "economy", "Economy"),
        Gameplay(4, "gameplay", "Gameplay"),
        Games(5, "games", "Games"),
        Protection(6, "protection", "Protection"),
        RolePlaying(7, "role", "Role Playing"),
        WorldManagement(8, "world", "World Management"),
        Miscellaneous(9, "misc", "Miscellaneous"),
        Undefined(10, "undefined", "Undefined");

        public static final Map<String, Category> ALL;

        static {
            Map<String, Category> map = new HashMap<String, Category>() {
                @Override
                public Category get(Object o) {
                    return super.get(((String) o).toLowerCase());
                }

                @Override
                public Category put(String s, Category category) {
                    return super.put(s.toLowerCase(), category);
                }
            };

            for (Category category : values()) {
                map.put(category.longName, category);
                map.put(category.longName.split(" ")[0], category);
                map.put(category.shortName, category);
            }

            ALL = Collections.unmodifiableMap(map);
        }

        public final int value;
        public final String shortName;
        public final String longName;

        Category(int value, String shortName, String longName) {
            this.value = value;
            this.shortName = shortName;
            this.longName = longName;
        }
    }

    enum Sorting {
        MostStars(0, "stars", "Most Stars"),
        MostDownloads(1, "downloads", "Most Downloads"),
        MostViews(2, "views", "Most Views"),
        Newest(3, "new", "Newest"),
        RecentlyUpdated(4, "recent", "Recently Updated");

        public static final Map<String, Sorting> ALL;

        static {
            Map<String, Sorting> map = new HashMap<String, Sorting>() {
                @Override
                public Sorting get(Object o) {
                    return super.get(((String) o).toLowerCase());
                }

                @Override
                public Sorting put(String s, Sorting category) {
                    return super.put(s.toLowerCase(), category);
                }
            };

            for (Sorting sorting : values()) {
                String[] split = sorting.longName.split(" ");

                map.put(sorting.longName, sorting);
                map.put(split[split.length - 1], sorting);
                map.put(sorting.shortName, sorting);
            }

            ALL = Collections.unmodifiableMap(map);
        }

        public final int value;
        public final String shortName;
        public final String longName;

        Sorting(int value, String shortName, String longName) {
            this.value = value;
            this.shortName = shortName;
            this.longName = longName;
        }
    }
}
