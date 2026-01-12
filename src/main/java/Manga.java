import java.util.List;

public class Manga {
    public String title;
    public Integer mangaId;
    public String synopsis;
    public Integer chapters;   // invece di episodes
    public String imageUrl;
    //public List<String> genres;
    public Double rating;
    public String startDate;
    public String officialSite;
    //public String language;

    public Manga() {} // Costruttore vuoto

    @Override
    public String toString() {
        return "Manga{" +
                "mangaId=" + mangaId +
                "title='" + title + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", chapters=" + chapters +
                ", imageUrl='" + imageUrl + '\'' +
                //", genres=" + genres +
                ", rating=" + rating +
                ", startDate='" + startDate + '\'' +
                ", officialSite='" + officialSite + '\'' +
                //", language='" + language + '\'' +
                '}';
    }
}