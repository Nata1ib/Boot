package spbstu.hsai;

import org.springframework.beans.factory.annotation.Autowired;
import spbstu.hsai.services.SendMessageService;
import spbstu.hsai.services.SendMessageServiceImp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

   private final String botToken;

    @Autowired
    private final SendMessageService sendMessageService;

    public Bot(SendMessageServiceImp sendMessageService, @Value("${telegram.bot.token}") String botToken){
        super(botToken);
        this.botToken = botToken;
        this.sendMessageService = sendMessageService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            var sm = sendMessageService.send(update.getMessage());
            try {
                this.execute(sm);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
