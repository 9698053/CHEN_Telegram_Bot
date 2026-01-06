import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
public class KitsuBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient = new OkHttpTelegramClient(ConfigReader.get("BOT_TOKEN"));
    private KitsuApi KitsuApi = new KitsuApi();

    @Override
    public void consume(Update update) {

        //controlla se quello che viene contine un messaggio, stampa nel console
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("utente: "+ update.getMessage().getText());
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            try {
                if (text.startsWith("/anime ")) {
                    String query = text.substring(7).trim();
                    Anime anime = KitsuApi.searchAnime(query);

                    String message;
                    if (anime != null) {
                        String epText = (anime.episodes == null || anime.episodes == 0)
                                ? "In corso"
                                : anime.episodes.toString();

                        message = "*Titolo:* " + anime.title + "\n"
                                + "*Episodi:* " + epText + "\n"
                                + "*Trama:* " + anime.synopsis + "\n"
                                + "*Poster:* " + anime.imageUrl;
                    } else {
                        message = "Anime non trovato!";
                    }

                    SendMessage reply = new SendMessage(chatId, message);
                    //reply.enableMarkdown(true);
                    telegramClient.execute(reply);

                } else {
                    // echo per qualsiasi altro messaggio
                    SendMessage reply = new SendMessage(chatId, "Hai scritto: " + text);
                    telegramClient.execute(reply);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}

/*
1. Kitsu nn riesce a riconoscere la quantita dei episodi di un anime che è ancora in corso, quindi al posto di int episodes, ho messo Integer episodes
 */