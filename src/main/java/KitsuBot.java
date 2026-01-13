import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

public class KitsuBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient = new OkHttpTelegramClient(ConfigReader.get("BOT_TOKEN"));
    private KitsuApi kitsuApi = new KitsuApi();

    private Map<Long, String> userMode = new HashMap<>();

    @Override
    public void consume(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long userId = update.getMessage().getFrom().getId();
        String chatId = update.getMessage().getChatId().toString();
        String text = update.getMessage().getText().trim();

        // Salva utente
        String username = update.getMessage().getFrom().getUserName();
        String firstName = update.getMessage().getFrom().getFirstName();
        UserDAO.saveUser(userId, username, firstName);

        // Crea stats se non esistono
        StatsDAO.createStatsIfNotExist(userId);

        System.out.println(firstName + "(" + userId + "): " + text);

        try {
            // --- /start ---
            if (text.equalsIgnoreCase("/start")) {
                String welcomeMessage = "Benvenuto! Usa i comandi /anime o /manga per cercare.\n"
                        + "Puoi anche usare /help per vedere tutti i comandi.";
                telegramClient.execute(new SendMessage(chatId, welcomeMessage));
                userMode.put(userId, null);
                return;
            }

            // --- /help ---
            if (text.equalsIgnoreCase("/help")) {
                String helpText = """
                        📚 Comandi disponibili:
                        
                        🔍 Ricerca:
                        /anime - Cerca un anime
                        /manga - Cerca un manga
                        
                        📝 Watchlist:
                        /addwatch <anime/manga> <titolo> - Aggiungi alla watchlist
                        /done <anime/manga> <titolo> - Segna come completato
                        /remove <anime/manga> <titolo> - Rimuovi dalla watchlist
                        /listwatch - Mostra anime/manga da guardare
                        /listwatched - Mostra anime/manga già visti
                        
                        ⚙️ Gestione:
                        /progress <anime/manga> <titolo> <numero> - Aggiorna progress
                        /rate <anime/manga> <titolo> <1-10> - Vota
                        /note <anime/manga> <titolo> <testo> - Aggiungi nota
                        """;
                telegramClient.execute(new SendMessage(chatId, helpText));
                return;
            }

            // --- Modalità ricerca ---
            if (text.equalsIgnoreCase("/anime")) {
                telegramClient.execute(new SendMessage(chatId, "Scrivi il nome dell'anime che vuoi cercare:"));
                userMode.put(userId, "anime");
                return;
            }

            if (text.equalsIgnoreCase("/manga")) {
                telegramClient.execute(new SendMessage(chatId, "Scrivi il nome del manga che vuoi cercare:"));
                userMode.put(userId, "manga");
                return;
            }

            // --- Liste (senza parametri) ---
            if (text.equalsIgnoreCase("/listwatch")) {
                handleListWatch(chatId, userId);
                return;
            }

            if (text.equalsIgnoreCase("/listwatched")) {
                handleListWatched(chatId, userId);
                return;
            }

            // --- Comandi con parametri ---
            if (text.startsWith("/")) {
                handleCommand(chatId, userId, text);
                return;
            }

            // --- Ricerca anime/manga ---
            if (userMode.containsKey(userId) && userMode.get(userId) != null) {
                handleSearch(chatId, userId, text);
                return;
            }

            // --- Echo di default ---
            telegramClient.execute(new SendMessage(chatId, "Hai scritto: " + text));

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // ========== METODI HELPER ==========

    private void handleListWatch(String chatId, long userId) throws TelegramApiException {
        List<WatchlistItem> animeList = WatchlistDAO.listWatching(userId, "anime");
        List<WatchlistItem> mangaList = WatchlistDAO.listWatching(userId, "manga");

        List<WatchlistItem> allList = new ArrayList<>();
        allList.addAll(animeList);
        allList.addAll(mangaList);

        String message = allList.isEmpty() ? "📭 Nessun anime/manga da guardare!" :
                "📺 LA TUA WATCHLIST:\n\n" + allList.stream()
                        .map(i -> String.format("📌 %s: %s\n   Progress: %d%s%s",
                                i.getType().toUpperCase(),
                                i.getTitle(),
                                i.getProgress(),
                                i.getRating() != null ? " • ⭐ " + i.getRating() + "/10" : "",
                                i.getNote() != null && !i.getNote().isEmpty() ? "\n   💭 " + i.getNote() : ""))
                        .collect(Collectors.joining("\n\n"));

        telegramClient.execute(new SendMessage(chatId, message));
    }

    private void handleListWatched(String chatId, long userId) throws TelegramApiException {
        List<WatchlistItem> animeList = WatchlistDAO.listWatched(userId, "anime");
        List<WatchlistItem> mangaList = WatchlistDAO.listWatched(userId, "manga");

        List<WatchlistItem> allList = new ArrayList<>();
        allList.addAll(animeList);
        allList.addAll(mangaList);

        String message = allList.isEmpty() ? "📭 Nessun anime/manga completato!" :
                "✅ COMPLETATI:\n\n" + allList.stream()
                        .map(i -> String.format("✔️ %s: %s%s%s",
                                i.getType().toUpperCase(),
                                i.getTitle(),
                                i.getRating() != null ? " • ⭐ " + i.getRating() + "/10" : "",
                                i.getNote() != null && !i.getNote().isEmpty() ? "\n   💭 " + i.getNote() : ""))
                        .collect(Collectors.joining("\n\n"));

        telegramClient.execute(new SendMessage(chatId, message));
    }

    private void handleCommand(String chatId, long userId, String text) throws TelegramApiException {
        String[] parts = text.split(" ", 3);
        String command = parts[0].toLowerCase();

        if (parts.length < 3) {
            String usage = switch (command) {
                case "/addwatch" -> "Uso corretto: /addwatch <anime/manga> <titolo>";
                case "/done" -> "Uso corretto: /done <anime/manga> <titolo>";
                case "/remove" -> "Uso corretto: /remove <anime/manga> <titolo>";
                case "/progress" -> "Uso corretto: /progress <anime/manga> <titolo> <numero>";
                case "/rate" -> "Uso corretto: /rate <anime/manga> <titolo> <1-10>";
                case "/note" -> "Uso corretto: /note <anime/manga> <titolo> <testo>";
                default -> "Comando non riconosciuto. Usa /help per la lista comandi.";
            };
            telegramClient.execute(new SendMessage(chatId, usage));
            return;
        }

        String type = parts[1].toLowerCase();
        String rest = parts[2].trim();

        if (!type.equals("anime") && !type.equals("manga")) {
            telegramClient.execute(new SendMessage(chatId, "⚠️ Tipo non valido! Usa 'anime' o 'manga'."));
            return;
        }

        SendMessage reply;

        switch (command) {
            case "/addwatch" -> {
                String title = rest;

                if (WatchlistDAO.existsInWatchlist(userId, type, title)) {
                    String status = WatchlistDAO.getStatus(userId, type, title);

                    if ("completed".equalsIgnoreCase(status)) {
                        reply = new SendMessage(chatId,
                                "⚠️ Questo " + type + " è già nella tua watchlist come *Completato*.\n" +
                                        "💡 Usa /remove " + type + " " + title + " se vuoi rimuoverlo prima di riaggiungerlo.");
                    } else {
                        reply = new SendMessage(chatId,
                                "⚠️ Questo " + type + " è già nella tua watchlist come *In corso*.");
                    }
                } else if (WatchlistDAO.addToWatchlist(userId, type, title)) {
                    reply = new SendMessage(chatId, "✅ Aggiunto alla tua watchlist: " + title);
                } else {
                    reply = new SendMessage(chatId, "❌ Errore durante l'aggiunta alla watchlist.");
                }
                telegramClient.execute(reply);
            }

            case "/done" -> {
                String title = rest;

                if (!WatchlistDAO.existsInWatchlist(userId, type, title)) {
                    reply = new SendMessage(chatId, "⚠️ Titolo non trovato nella tua watchlist.");
                } else if (WatchlistDAO.markAsDone(userId, type, title)) {
                    // Aggiorna le statistiche
                    if (type.equals("anime")) {
                        StatsDAO.incrementAnimeWatched(userId);
                    } else {
                        StatsDAO.incrementMangaRead(userId);
                    }

                    reply = new SendMessage(chatId, "✅ Segnato come completato: " + title);
                } else {
                    reply = new SendMessage(chatId, "❌ Errore durante l'operazione.");
                }
                telegramClient.execute(reply);
            }

            case "/remove" -> {
                String title = rest;

                if (!WatchlistDAO.existsInWatchlist(userId, type, title)) {
                    reply = new SendMessage(chatId, "⚠️ Titolo non trovato nella tua watchlist.");
                } else if (WatchlistDAO.removeFromWatchlist(userId, type, title)) {
                    reply = new SendMessage(chatId, "🗑️ Rimosso dalla tua watchlist: " + title);
                } else {
                    reply = new SendMessage(chatId, "❌ Errore durante la rimozione.");
                }
                telegramClient.execute(reply);
            }

            case "/progress" -> {
                String[] progressParts = rest.split(" ");

                if (progressParts.length < 2) {
                    reply = new SendMessage(chatId, "⚠️ Uso corretto: /progress <anime/manga> <titolo> <numero>");
                    telegramClient.execute(reply);
                    return;
                }

                try {
                    int progress = Integer.parseInt(progressParts[progressParts.length - 1]);
                    String title = String.join(" ", Arrays.copyOf(progressParts, progressParts.length - 1));

                    if (progress < 0) {
                        reply = new SendMessage(chatId, "⚠️ Il progress deve essere un numero positivo!");
                    } else if (!WatchlistDAO.existsInWatchlist(userId, type, title)) {
                        reply = new SendMessage(chatId, "⚠️ Titolo non trovato nella tua watchlist.");
                    } else if (WatchlistDAO.updateProgress(userId, type, title, progress)) {
                        reply = new SendMessage(chatId, "📊 Progress aggiornato a " + progress + " per: " + title);
                    } else {
                        reply = new SendMessage(chatId, "❌ Errore durante l'aggiornamento.");
                    }
                } catch (NumberFormatException e) {
                    reply = new SendMessage(chatId, "⚠️ Il progress deve essere un numero valido!");
                }
                telegramClient.execute(reply);
            }

            case "/rate" -> {
                String[] rateParts = rest.split(" ");

                if (rateParts.length < 2) {
                    reply = new SendMessage(chatId, "⚠️ Uso corretto: /rate <anime/manga> <titolo> <1-10>");
                    telegramClient.execute(reply);
                    return;
                }

                try {
                    int rating = Integer.parseInt(rateParts[rateParts.length - 1]);
                    String title = String.join(" ", Arrays.copyOf(rateParts, rateParts.length - 1));

                    if (rating < 1 || rating > 10) {
                        reply = new SendMessage(chatId, "⚠️ Il voto deve essere tra 1 e 10!");
                    } else if (!WatchlistDAO.existsInWatchlist(userId, type, title)) {
                        reply = new SendMessage(chatId, "⚠️ Titolo non trovato nella tua watchlist.");
                    } else if (WatchlistDAO.updateRating(userId, type, title, rating)) {
                        reply = new SendMessage(chatId, "⭐ Voto aggiornato a " + rating + "/10 per: " + title);
                    } else {
                        reply = new SendMessage(chatId, "❌ Errore durante l'aggiornamento.");
                    }
                } catch (NumberFormatException e) {
                    reply = new SendMessage(chatId, "⚠️ Il voto deve essere un numero valido!");
                }
                telegramClient.execute(reply);
            }

            case "/note" -> {
                List<WatchlistItem> userItems = WatchlistDAO.listByStatus(userId, type, "watching");
                userItems.addAll(WatchlistDAO.listByStatus(userId, type, "completed"));

                String foundTitle = null;
                String noteText = null;

                for (WatchlistItem item : userItems) {
                    if (rest.toLowerCase().startsWith(item.getTitle().toLowerCase())) {
                        if (foundTitle == null || item.getTitle().length() > foundTitle.length()) {
                            foundTitle = item.getTitle();
                            noteText = rest.substring(foundTitle.length()).trim();
                        }
                    }
                }

                if (foundTitle == null || noteText == null || noteText.isEmpty()) {
                    reply = new SendMessage(chatId,
                            "⚠️ Titolo non trovato o nota mancante.\n" +
                                    "Uso: /note <anime/manga> <titolo> <testo>\n" +
                                    "Esempio: /note anime Death Note questa è una nota");
                } else if (WatchlistDAO.updateNote(userId, type, foundTitle, noteText)) {
                    reply = new SendMessage(chatId, "📝 Nota aggiornata per: " + foundTitle);
                } else {
                    reply = new SendMessage(chatId, "❌ Errore durante l'aggiornamento della nota.");
                }
                telegramClient.execute(reply);
            }

            default -> {
                reply = new SendMessage(chatId, "❌ Comando non riconosciuto. Usa /help per la lista comandi.");
                telegramClient.execute(reply);
            }
        }
    }

    private void handleSearch(String chatId, long userId, String text) throws TelegramApiException {
        String mode = userMode.get(userId);
        String message;

        if (mode.equals("anime")) {
            Anime anime = kitsuApi.searchAnime(text);
            if (anime != null) {
                AnimeDAO.saveAnime(anime);
                String epText = (anime.episodes == null || anime.episodes == 0) ? "In corso" : anime.episodes.toString();
                message = "*Titolo:* " + anime.title + "\n"
                        + "*Episodi:* " + epText + "\n"
                        + "*Rating:* " + anime.rating + "\n"
                        + "*Data inizio:* " + (anime.startDate.isEmpty() ? "N/A" : anime.startDate) + "\n"
                        + "*Trailer:* " + (anime.trailerUrl.isEmpty() ? "N/A" : anime.trailerUrl) + "\n"
                        + "*Sito ufficiale:* " + (anime.officialSite.isEmpty() ? "N/A" : anime.officialSite) + "\n"
                        + "*Trama:* " + anime.synopsis + "\n"
                        + "*Poster:* " + (anime.imageUrl.isEmpty() ? "N/A" : anime.imageUrl);
            } else {
                message = "❌ Anime non trovato!";
            }
        } else {
            Manga manga = kitsuApi.searchManga(text);
            if (manga != null) {
                MangaDAO.saveManga(manga);
                String chapText = (manga.chapters == null || manga.chapters == 0) ? "In corso" : manga.chapters.toString();
                message = "*Titolo:* " + manga.title + "\n"
                        + "*Capitoli:* " + chapText + "\n"
                        + "*Rating:* " + manga.rating + "\n"
                        + "*Data inizio:* " + (manga.startDate.isEmpty() ? "N/A" : manga.startDate) + "\n"
                        + "*Sito ufficiale:* " + (manga.officialSite.isEmpty() ? "N/A" : manga.officialSite) + "\n"
                        + "*Trama:* " + manga.synopsis + "\n"
                        + "*Copertina:* " + (manga.imageUrl.isEmpty() ? "N/A" : manga.imageUrl);
            } else {
                message = "❌ Manga non trovato!";
            }
        }

        telegramClient.execute(new SendMessage(chatId, message));
        userMode.put(userId, null);
    }
}