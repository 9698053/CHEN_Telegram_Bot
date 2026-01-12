import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AnimeDAO {

    public static void saveAnime(Anime anime) {
        String sql = "INSERT OR IGNORE INTO Anime(title, synopsis, episodes, image_url, rating, start_date, trailer_url, official_site) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, anime.title);
            ps.setString(2, anime.synopsis);
            ps.setInt(3, anime.episodes != null ? anime.episodes : 0);
            ps.setString(4, anime.imageUrl != null ? anime.imageUrl : "");
            ps.setDouble(5, anime.rating != null ? anime.rating : 0.0);
            ps.setString(6, anime.startDate != null ? anime.startDate : "");
            ps.setString(7, anime.trailerUrl != null ? anime.trailerUrl : "");
            ps.setString(8, anime.officialSite != null ? anime.officialSite : "");

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 🔹 Metodo che manca
    public static Anime getAnimeByTitle(String title) {
        String sql = "SELECT * FROM Anime WHERE title = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Anime(
                        rs.getString("title"),
                        rs.getString("synopsis"),
                        rs.getInt("episodes"),
                        rs.getString("image_url"),
                        rs.getDouble("rating"),
                        rs.getString("start_date"),
                        rs.getString("trailer_url"),
                        rs.getString("official_site")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 🔹 Metodo per prendere anime per ID (usato nella watchlist)
    public static Anime getAnimeById(int animeId) {
        String sql = "SELECT * FROM Anime WHERE anime_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, animeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Anime(
                        rs.getString("title"),
                        rs.getString("synopsis"),
                        rs.getInt("episodes"),
                        rs.getString("image_url"),
                        rs.getDouble("rating"),
                        rs.getString("start_date"),
                        rs.getString("trailer_url"),
                        rs.getString("official_site")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
