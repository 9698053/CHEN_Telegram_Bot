import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class KitsuApi {

    private static final String BASE_URL = "https://kitsu.io/api/edge/anime?filter[text]=";
    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper mapper = new ObjectMapper();

    public Anime searchAnime(String name) {
        String url = BASE_URL + name.replace(" ", "%20");

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;

            JsonNode root = mapper.readTree(response.body().string());
            JsonNode data = root.get("data");
            if (data.size() == 0) return null;

            JsonNode attributes = data.get(0).get("attributes");

            String title = attributes.get("canonicalTitle").asText();
            String synopsis = attributes.get("synopsis").asText();
            int episodes = attributes.has("episodeCount") && !attributes.get("episodeCount").isNull()
                    ? attributes.get("episodeCount").asInt()
                    : 0;
            String imageUrl = attributes.has("posterImage") && attributes.get("posterImage").has("small")
                    ? attributes.get("posterImage").get("small").asText()
                    : "";

            return new Anime(title, synopsis, episodes, imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
