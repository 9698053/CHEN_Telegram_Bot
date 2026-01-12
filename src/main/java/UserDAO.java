import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserDAO {

    public static void saveUser(long userId, String username, String firstName) {
        String sql = """
            INSERT OR IGNORE INTO Users(user_id, username, first_name)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, username);
            ps.setString(3, firstName);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}