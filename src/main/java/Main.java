import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.TelegramBotsApi;

public class Main {
    public static void main(String[] args) throws Exception {

        // 🔑 Inserisci il token ottenuto da BotFather
        String token = "INSERISCI_IL_TUO_TOKEN_QUI";

        TelegramBotsApi api = new TelegramBotsApi();
        api.registerBot(new OkHttpTelegramClient(token), new KiTsuBot());

        System.out.println("Bot avviato!");
    }
}
