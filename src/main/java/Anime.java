import java.util.List;

public class Anime {
    public String title;
    public Integer animeId;
    public String synopsis;
    public Integer episodes;
    public String imageUrl;
    //public List<String> genres;
    public Double rating;
    public String startDate;
    public String trailerUrl;
    public String officialSite;
    //public String language;

    // Costruttore vuoto già esistente
    public Anime() {}

    // 🔹 Nuovo costruttore completo
    public Anime(String title, String synopsis, Integer episodes, String imageUrl,
                 Double rating, String startDate, String trailerUrl, String officialSite) {
        this.title = title;
        this.synopsis = synopsis;
        this.episodes = episodes;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.startDate = startDate;
        this.trailerUrl = trailerUrl;
        this.officialSite = officialSite;
    }

    @Override
    public String toString() {
        return "Anime{" +
                "animeId=" + animeId +
                ", title='" + title + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", episodes=" + episodes +
                ", rating=" + rating +
                ", startDate='" + startDate + '\'' +
                ", trailerUrl='" + trailerUrl + '\'' +
                ", officialSite='" + officialSite + '\'' +
                '}';
    }
}
