import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) throws Exception {

        try {
            String botToken = ConfigReader.get("BOT_TOKEN");
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new KitsuBot());
            System.out.println("Bot avviato!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}





/*public void consume(Update update) {

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
*/