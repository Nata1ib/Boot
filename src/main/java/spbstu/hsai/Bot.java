package spbstu.hsai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import spbstu.hsai.services.SendMessageService;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Bot extends TelegramLongPollingBot {

    private static final Logger LOGGER = Logger.getLogger(Bot.class.getName());

    private final String botUsername;
    private final String botToken;
    private final SendMessageService sendMessageService;

    public Bot(SendMessageService sendMessageService, @Value("${telegram.bot.token}") String botToken,
               @Value("${telegram.bot.username}") String botUsername) {
        LOGGER.log(Level.INFO, "Initializing Bot...");
        this.sendMessageService = sendMessageService;
        this.botToken = botToken;
        this.botUsername = botUsername;
        LOGGER.log(Level.INFO, "Bot Initialized.");
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
                LOGGER.log(Level.INFO, "Sending message...");
                this.execute(sm);
                LOGGER.log(Level.INFO, "Message sent successfully.");
            } catch (TelegramApiException e) {
                LOGGER.log(Level.SEVERE, "Failed to send message.", e);
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
