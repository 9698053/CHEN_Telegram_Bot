import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
public class KitsuBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient = new OkHttpTelegramClient(ConfigReader.get("BOT_TOKEN"));

    @Override
    public void consume(Update update) {

        // stampa il messa che ha scritto il utnete nel log
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("utente: "+ update.getMessage().getText());
        }

        // legge quello che ha scritto i utente e ritorna indietro
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            SendMessage reply =
                    new SendMessage(chatId.toString(), "Hai scritto: " + text);

            try {
                telegramClient.execute(reply);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }
}
