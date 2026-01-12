import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:animebot.db";
    private static Connection connection;

    // Inizializza connessione e crea tabelle
    public static void init() {
        System.out.println("Inizializzo il database...");
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("Database pronto!");
        } catch (Exception e) {
            System.out.println("ERRORE nel database!");
            e.printStackTrace();
        }
    }


    // Restituisce sempre una connessione valida
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    private static void createTables() throws Exception {
        Statement stmt = connection.createStatement();

        // Tabella utenti
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Users (
                user_id INTEGER PRIMARY KEY,
                username TEXT,
                first_name TEXT,
                join_date TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """);

        // Tabella anime
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Anime (
                anime_id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT UNIQUE,
                synopsis TEXT,
                episodes INTEGER,
                image_url TEXT,
                rating REAL,
                start_date TEXT,
                trailer_url TEXT,
                official_site TEXT
            );
        """);

        // Tabella manga
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Manga (
                manga_id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT UNIQUE,
                synopsis TEXT,
                chapters INTEGER,
                image_url TEXT,
                rating REAL,
                start_date TEXT,
                official_site TEXT
            );
        """);

        // Watchlist → aggiunte colonne per progress, note e rating
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Watchlist (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                item_id INTEGER,
                type TEXT CHECK(type IN ('anime','manga')),
                title TEXT NOT NULL, -- NUOVA COLONNA PER IL TITOLO
                status TEXT CHECK(status IN ('watching','completed')) DEFAULT 'watching',
                progress INTEGER DEFAULT 0,
                rating INTEGER CHECK(rating BETWEEN 1 AND 10),
                note TEXT,
                added_date TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES Users(user_id)
            );
        """);

        // Statistiche utenti → user_id come PRIMARY KEY
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Stats (
                user_id INTEGER PRIMARY KEY,
                total_anime INTEGER DEFAULT 0,
                total_manga INTEGER DEFAULT 0,
                episodes_watched INTEGER DEFAULT 0,
                chapters_read INTEGER DEFAULT 0,
                FOREIGN KEY(user_id) REFERENCES Users(user_id)
            );
        """);
/*
        // stato del utente

        stmt.execute("""
           CREATE TABLE IF NOT EXISTS UserState (
                  user_id INTEGER PRIMARY KEY,
                  state TEXT,
                  FOREIGN KEY(user_id) REFERENCES Users(user_id)
              );
        """);

        stmt.close();

 */
    }
    // Chiude la connessione quando il bot termina
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connessione DB chiusa.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
