import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WatchlistDAO {

    // --- Aggiunge anime/manga alla watchlist ---
    public static boolean addToWatchlist(long userId, String type, String title) {
        String sql = "INSERT INTO Watchlist(user_id, type, title, status, progress) " +
                "VALUES (?, ?, ?, 'watching', 0)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, type.toLowerCase());
            ps.setString(3, title);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Segna anime/manga come completato ---
    public static boolean markAsDone(long userId, String type, String title) {
        String sql = "UPDATE Watchlist SET status='completed' WHERE user_id=? AND type=? AND title=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, type.toLowerCase());
            ps.setString(3, title);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Rimuove anime/manga dalla watchlist ---
    public static boolean removeFromWatchlist(long userId, String type, String title) {
        String sql = "DELETE FROM Watchlist WHERE user_id=? AND type=? AND title=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, type.toLowerCase());
            ps.setString(3, title);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Aggiorna il progresso ---
    public static boolean updateProgress(long userId, String type, String title, int progress) {
        String sql = "UPDATE Watchlist SET progress=? WHERE user_id=? AND type=? AND title=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, progress);
            ps.setLong(2, userId);
            ps.setString(3, type.toLowerCase());
            ps.setString(4, title);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Aggiorna rating personale ---
    public static boolean updateRating(long userId, String type, String title, int rating) {
        String sql = "UPDATE Watchlist SET rating=? WHERE user_id=? AND type=? AND title=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, rating);
            ps.setLong(2, userId);
            ps.setString(3, type.toLowerCase());
            ps.setString(4, title);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Aggiorna nota personale ---
    public static boolean updateNote(long userId, String type, String title, String note) {
        String sql = "UPDATE Watchlist SET note=? WHERE user_id=? AND type=? AND title=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, note);
            ps.setLong(2, userId);
            ps.setString(3, type.toLowerCase());
            ps.setString(4, title);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Lista anime/manga in watchlist in base allo status ("watching" o "completed") ---
    public static List<WatchlistItem> listByStatus(long userId, String type, String status) {
        List<WatchlistItem> list = new ArrayList<>();
        String sql = "SELECT * FROM Watchlist WHERE user_id=? AND type=? AND status=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, type.toLowerCase());
            ps.setString(3, status.toLowerCase());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                WatchlistItem item = new WatchlistItem(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getInt("item_id"),
                        rs.getString("type"),
                        rs.getString("title"),    // adesso includiamo il titolo
                        rs.getString("status"),
                        rs.getInt("progress"),
                        rs.getInt("rating"),
                        rs.getString("note")
                );
                list.add(item);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- Shortcut per listWatching ---
    public static List<WatchlistItem> listWatching(long userId, String type) {
        return listByStatus(userId, type, "watching");
    }

    // --- Shortcut per listWatched ---
    public static List<WatchlistItem> listWatched(long userId, String type) {
        return listByStatus(userId, type, "completed");
    }
    // Controlla se un anime/manga è già presente nella watchlist
    public static boolean existsInWatchlist(long userId, String type, String title) {
        String sql = "SELECT COUNT(*) FROM Watchlist WHERE user_id=? AND type=? AND title=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, type.toLowerCase());
            ps.setString(3, title);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // se il count > 0 significa che esiste
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // Ottiene lo status di un anime/manga nella watchlist
    public static String getStatus(long userId, String type, String title) {
        String sql = "SELECT status FROM Watchlist WHERE user_id=? AND type=? AND title=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, type.toLowerCase());
            ps.setString(3, title);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                rs.close();
                return status;
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
