import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

public class KiTsuBot implements LongPollingSingleThreadUpdateConsumer {

    @Override
    public void consume(Update update) {

        // Controlla che ci sia un messaggio e che contenga testo
        if (update.hasMessage() && update.getMessage().hasText()) {

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Per ora stampiamo ciò che manda l'utente
            System.out.println("Messaggio ricevuto: " + text);

            // In futuro qui metterai la risposta del bot
        }
    }
}
