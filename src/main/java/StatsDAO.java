import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatsDAO {

    // Crea una riga stats se non esiste
    public static void createStatsIfNotExist(long userId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR IGNORE INTO Stats(user_id) VALUES(?)")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Aggiorna stats quando un anime viene completato
    public static void incrementAnimeWatched(long userId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Stats SET total_anime = total_anime + 1 WHERE user_id=?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Aggiunge episodi visti
    public static void addEpisodesWatched(long userId, int episodes) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Stats SET episodes_watched = episodes_watched + ? WHERE user_id=?")) {
            ps.setInt(1, episodes);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Aggiorna stats manga
    public static void incrementMangaRead(long userId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Stats SET total_manga = total_manga + 1 WHERE user_id=?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addChaptersRead(long userId, int chapters) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Stats SET chapters_read = chapters_read + ? WHERE user_id=?")) {
            ps.setInt(1, chapters);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Legge e stampa le stats
    public static void printStats(long userId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM Stats WHERE user_id=?")) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Anime visti: " + rs.getInt("total_anime"));
                System.out.println("Episodi guardati: " + rs.getInt("episodes_watched"));
                System.out.println("Manga letti: " + rs.getInt("total_manga"));
                System.out.println("Capitoli letti: " + rs.getInt("chapters_read"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
