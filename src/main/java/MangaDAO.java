import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MangaDAO {

    // 🔹 Salva manga nel database
    public static void saveManga(Manga manga) {
        String sql = """
            INSERT OR IGNORE INTO Manga(title, synopsis, chapters, image_url, rating, start_date, official_site)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, manga.title != null ? manga.title : "");
            ps.setString(2, manga.synopsis != null ? manga.synopsis : "");
            ps.setInt(3, manga.chapters != null ? manga.chapters : 0);
            ps.setString(4, manga.imageUrl != null ? manga.imageUrl : "");
            ps.setDouble(5, manga.rating != null ? manga.rating : 0.0);
            ps.setString(6, manga.startDate != null ? manga.startDate : "");
            ps.setString(7, manga.officialSite != null ? manga.officialSite : "");

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Recupera manga dal titolo
    public static Manga getMangaByTitle(String title) {
        String sql = "SELECT * FROM Manga WHERE title = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Manga manga = new Manga();
                manga.mangaId = rs.getInt("manga_id");
                manga.title = rs.getString("title");
                manga.synopsis = rs.getString("synopsis");
                manga.chapters = rs.getInt("chapters");
                manga.imageUrl = rs.getString("image_url");
                manga.rating = rs.getDouble("rating");
                manga.startDate = rs.getString("start_date");
                manga.officialSite = rs.getString("official_site");
                return manga;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 🔹 Recupera manga per ID (usato nella watchlist)
    public static Manga getMangaById(int mangaId) {
        String sql = "SELECT * FROM Manga WHERE manga_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mangaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Manga manga = new Manga();
                manga.mangaId = rs.getInt("manga_id");
                manga.title = rs.getString("title");
                manga.synopsis = rs.getString("synopsis");
                manga.chapters = rs.getInt("chapters");
                manga.imageUrl = rs.getString("image_url");
                manga.rating = rs.getDouble("rating");
                manga.startDate = rs.getString("start_date");
                manga.officialSite = rs.getString("official_site");
                return manga;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
