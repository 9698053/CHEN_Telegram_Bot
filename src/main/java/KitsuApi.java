import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class KitsuApi {

    private static final String BASE_URL_ANIME = "https://kitsu.io/api/edge/anime?filter[text]=";
    private static final String BASE_URL_MANGA = "https://kitsu.io/api/edge/manga?filter[text]=";

    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper mapper = new ObjectMapper();

    // ------------------- Anime -------------------
    public Anime searchAnime(String name) {
        String url = BASE_URL_ANIME + name.replace(" ", "%20");
        try {
            Request request = new Request.Builder().url(url).get().build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) return null;

            JsonNode root = mapper.readTree(response.body().string());
            JsonNode data = root.get("data");
            if (data.size() == 0) return null;

            JsonNode attr = data.get(0).get("attributes");

            Anime anime = new Anime();
            anime.title = attr.get("canonicalTitle").asText();
            anime.synopsis = attr.get("synopsis").asText();
            anime.episodes = attr.has("episodeCount") && !attr.get("episodeCount").isNull()
                    ? attr.get("episodeCount").asInt() : 0;
            anime.imageUrl = attr.has("posterImage") && attr.get("posterImage").has("small")
                    ? attr.get("posterImage").get("small").asText() : "";
            //anime.genres = new ArrayList<>();
            anime.rating = attr.has("averageRating") && !attr.get("averageRating").isNull()
                    ? attr.get("averageRating").asDouble() : 0.0;
            anime.startDate = attr.has("startDate") && !attr.get("startDate").isNull()
                    ? attr.get("startDate").asText() : "";
            anime.trailerUrl = attr.has("youtubeVideoId") && !attr.get("youtubeVideoId").isNull()
                    ? "https://www.youtube.com/watch?v=" + attr.get("youtubeVideoId").asText() : "";
            anime.officialSite = attr.has("siteUrl") && !attr.get("siteUrl").isNull()
                    ? attr.get("siteUrl").asText() : "";
            //anime.language = "ja"; // Default giapponese

            return anime;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------- Manga -------------------
    public Manga searchManga(String name) {
        String url = BASE_URL_MANGA + name.replace(" ", "%20");
        try {
            Request request = new Request.Builder().url(url).get().build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) return null;

            JsonNode root = mapper.readTree(response.body().string());
            JsonNode data = root.get("data");
            if (data.size() == 0) return null;

            JsonNode attr = data.get(0).get("attributes");

            Manga manga = new Manga();
            manga.title = attr.get("canonicalTitle").asText();
            manga.synopsis = attr.get("synopsis").asText();
            manga.chapters = attr.has("chapterCount") && !attr.get("chapterCount").isNull()
                    ? attr.get("chapterCount").asInt() : 0;
            manga.imageUrl = attr.has("posterImage") && attr.get("posterImage").has("small")
                    ? attr.get("posterImage").get("small").asText() : "";
            //manga.genres = new ArrayList<>();
            manga.rating = attr.has("averageRating") && !attr.get("averageRating").isNull()
                    ? attr.get("averageRating").asDouble() : 0.0;
            manga.startDate = attr.has("startDate") && !attr.get("startDate").isNull()
                    ? attr.get("startDate").asText() : "";
            manga.officialSite = attr.has("siteUrl") && !attr.get("siteUrl").isNull()
                    ? attr.get("siteUrl").asText() : "";
            //manga.language = "ja";

            return manga;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
