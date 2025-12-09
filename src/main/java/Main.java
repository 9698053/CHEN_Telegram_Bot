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
