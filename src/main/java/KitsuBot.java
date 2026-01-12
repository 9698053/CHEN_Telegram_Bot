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

    private Map<Long, String> userMode = new HashMap<>(); // "anime", "manga" o null

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

        System.out.println(firstName + "(" + userId + "): " + text);

        try {
            // --- /start ---
            if (text.equalsIgnoreCase("/start")) {
                String welcomeMessage = "Benvenuto! Usa i comandi /anime o /manga per cercare.\n"
                        + "Puoi anche usare /help per vedere tutti i comandi.";
                telegramClient.execute(new SendMessage(chatId, welcomeMessage));
                userMode.put(userId, null);
            }

            // --- /help ---
            else if (text.equalsIgnoreCase("/help")) {
                String helpText = """
                        Comandi disponibili:
                        /anime - Cerca un anime
                        /manga - Cerca un manga
                        /addwatch <anime/manga> <titolo> - Aggiungi alla tua watchlist
                        /done <anime/manga> <titolo> - Segna come completato
                        /remove <anime/manga> <titolo> - Rimuovi dalla watchlist
                        /listwatch - Mostra anime/manga da guardare
                        /listwatched - Mostra anime/manga già visti
                        /progress <anime/manga> <titolo> <numero> - Aggiorna progress
                        /rate <anime/manga> <titolo> <1-10> - Vota
                        /note <anime/manga> <titolo> <testo> - Aggiungi nota
                        """;
                telegramClient.execute(new SendMessage(chatId, helpText));
            }

            // --- Modalità ricerca ---
            else if (text.equalsIgnoreCase("/anime")) {
                telegramClient.execute(new SendMessage(chatId, "Scrivi il nome dell'anime che vuoi cercare:"));
                userMode.put(userId, "anime");
            }
            else if (text.equalsIgnoreCase("/manga")) {
                telegramClient.execute(new SendMessage(chatId, "Scrivi il nome del manga che vuoi cercare:"));
                userMode.put(userId, "manga");
            }

            // --- Watchlist commands ---
            else if (text.startsWith("/")) {
                String[] parts = text.split(" ", 3); // comando, tipo, titolo/altro
                String command = parts[0].toLowerCase();

                if (parts.length >= 2) {
                    String type = parts[1].toLowerCase();
                    String rest = parts.length == 3 ? parts[2] : "";

                    SendMessage reply;

                    switch (command) {
                        case "/addwatch": {
                            if (rest.isEmpty()) {
                                telegramClient.execute(new SendMessage(chatId, "Uso corretto: /addwatch <anime/manga> <titolo>"));
                                break;
                            }

                            String title = rest.trim(); // titolo = tutto il resto
                            if (!type.equals("anime") && !type.equals("manga")) {
                                telegramClient.execute(new SendMessage(chatId, "Tipo non valido! Usa 'anime' o 'manga'."));
                                break;
                            }

                            if (WatchlistDAO.existsInWatchlist(userId, type, title)) {
                                telegramClient.execute(new SendMessage(chatId, "Questo anime/manga è già nella tua watchlist!"));
                                break;
                            }

                            WatchlistDAO.addToWatchlist(userId, type, title);
                            telegramClient.execute(new SendMessage(chatId, "Aggiunto alla tua watchlist!"));
                            break;
                        }

                        case "/done":
                            if (rest.isEmpty()) {
                                reply = new SendMessage(chatId, "Uso corretto: /done <anime/manga> <titolo>");
                            } else {
                                if (WatchlistDAO.markAsDone(userId, type, rest))
                                    reply = new SendMessage(chatId, "Segnato come completato!");
                                else
                                    reply = new SendMessage(chatId, "Errore: titolo non trovato nella tua watchlist.");
                            }
                            telegramClient.execute(reply);
                            break;

                        case "/remove":
                            if (rest.isEmpty()) reply = new SendMessage(chatId, "Uso corretto: /remove <anime/manga> <titolo>");
                            else if (WatchlistDAO.removeFromWatchlist(userId, type, rest)) reply = new SendMessage(chatId, "Rimosso dalla tua watchlist!");
                            else reply = new SendMessage(chatId, "Errore: titolo non trovato nella tua watchlist.");
                            telegramClient.execute(reply);
                            break;

                        case "/progress":
                            if (rest.isEmpty()) {
                                reply = new SendMessage(chatId, "Uso corretto: /progress <anime/manga> <titolo> <numero>");
                            } else {
                                String[] partsProg = rest.split(" ");
                                if (partsProg.length < 2) {
                                    reply = new SendMessage(chatId, "Uso corretto: /progress <anime/manga> <titolo> <numero>");
                                } else {
                                    try {
                                        int prog = Integer.parseInt(partsProg[partsProg.length - 1]); // ultimo elemento = numero
                                        String titleProg = String.join(" ", Arrays.copyOf(partsProg, partsProg.length - 1)); // resto = titolo

                                        if (WatchlistDAO.updateProgress(userId, type, titleProg, prog))
                                            reply = new SendMessage(chatId, "Progress aggiornato!");
                                        else
                                            reply = new SendMessage(chatId, "Errore: titolo non trovato nella tua watchlist.");
                                    } catch (NumberFormatException e) {
                                        reply = new SendMessage(chatId, "Il progress deve essere un numero!");
                                    }
                                }
                            }
                            telegramClient.execute(reply);
                            break;

                        case "/rate":
                            if (rest.isEmpty()) {
                                reply = new SendMessage(chatId, "Uso corretto: /rate <anime/manga> <titolo> <1-10>");
                            } else {
                                String[] partsRate = rest.split(" ");
                                if (partsRate.length < 2) {
                                    reply = new SendMessage(chatId, "Uso corretto: /rate <anime/manga> <titolo> <1-10>");
                                } else {
                                    try {
                                        int rating = Integer.parseInt(partsRate[partsRate.length - 1]); // ultimo = voto
                                        String titleRate = String.join(" ", Arrays.copyOf(partsRate, partsRate.length - 1)); // resto = titolo

                                        if (rating < 1 || rating > 10)
                                            reply = new SendMessage(chatId, "Il voto deve essere tra 1 e 10!");
                                        else if (WatchlistDAO.updateRating(userId, type, titleRate, rating))
                                            reply = new SendMessage(chatId, "Voto aggiornato!");
                                        else
                                            reply = new SendMessage(chatId, "Errore: titolo non trovato nella tua watchlist.");
                                    } catch (NumberFormatException e) {
                                        reply = new SendMessage(chatId, "Il voto deve essere un numero!");
                                    }
                                }
                            }
                            telegramClient.execute(reply);
                            break;

                        case "/note":
                            if (rest.isEmpty()) {
                                reply = new SendMessage(chatId, "Uso corretto: /note <anime/manga> <titolo> <testo>");
                            } else {
                                String[] partsNote = rest.split(" ", 2); // divide in titolo + testo
                                if (partsNote.length < 2) {
                                    reply = new SendMessage(chatId, "Uso corretto: /note <anime/manga> <titolo> <testo>");
                                } else {
                                    String titleNote = partsNote[0];      // prima parola come titolo
                                    String noteText = partsNote[1];       // resto come nota
                                    if (WatchlistDAO.updateNote(userId, type, titleNote, noteText))
                                        reply = new SendMessage(chatId, "Nota aggiornata!");
                                    else
                                        reply = new SendMessage(chatId, "Errore: titolo non trovato nella tua watchlist.");
                                }
                            }
                            telegramClient.execute(reply);
                            break;
                    }
                }
            }

            // --- Liste ---
            else if (text.equalsIgnoreCase("/listwatch")) {
                List<WatchlistItem> animeList = WatchlistDAO.listWatching(userId, "anime");
                List<WatchlistItem> mangaList = WatchlistDAO.listWatching(userId, "manga");

                List<WatchlistItem> allList = new ArrayList<>();
                allList.addAll(animeList);
                allList.addAll(mangaList);

                String message = allList.isEmpty() ? "Nessun anime/manga da guardare!" :
                        allList.stream()
                                .map(i -> i.getType() + ": " + i.getTitle())
                                .collect(Collectors.joining("\n"));

                telegramClient.execute(new SendMessage(chatId, message));
            }

            else if (text.equalsIgnoreCase("/listwatched")) {
                List<WatchlistItem> animeList = WatchlistDAO.listWatched(userId, "anime");
                List<WatchlistItem> mangaList = WatchlistDAO.listWatched(userId, "manga");

                List<WatchlistItem> allList = new ArrayList<>();
                allList.addAll(animeList);
                allList.addAll(mangaList);

                String message = allList.isEmpty() ? "Nessun anime/manga visto!" :
                        allList.stream()
                                .map(i -> i.getType() + ": " + i.getTitle())
                                .collect(Collectors.joining("\n"));

                telegramClient.execute(new SendMessage(chatId, message));
            }


            // --- Ricerca anime/manga ---
            else if (userMode.containsKey(userId)) {
                String mode = userMode.get(userId);
                if (mode != null) {
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
                        } else message = "Anime non trovato!";
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
                        } else message = "Manga non trovato!";
                    }
                    telegramClient.execute(new SendMessage(chatId, message));
                    userMode.put(userId, null);
                }
            }

            // --- Echo di default ---
            else {
                telegramClient.execute(new SendMessage(chatId, "Hai scritto: " + text));
            }

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}


/*
sistemare:
/note che nn funzionano con piu di il titolo con piu di 2 aprole
/listwatched e /listwatch non funzionano
e una sistema migliore di gestione per i vari commandi

 */