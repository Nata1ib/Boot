package spbstu.hsai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Locale;

@Service
public class SendMessageServiceImp implements SendMessageService {

    private final OpenWeatherMapService openWeatherMapService;
    private final MessageSource messageSource;


    @Autowired
    public SendMessageServiceImp(OpenWeatherMapServiceImp openWeatherMapService,
                                  MessageSource messageSource) {
        this.openWeatherMapService = openWeatherMapService;
        this.messageSource = messageSource;
    }

    @Override
    public SendMessage send(Message message) {
        String result;

        if ("/start".equals(message.getText())) {
            result = messageSource.getMessage("greeting", null, Locale.ENGLISH);
        } else if (message.hasLocation()) {
            double latitude = message.getLocation().getLatitude();
            double longitude = message.getLocation().getLongitude();
            result = openWeatherMapService.getWeatherInfoByLocation(latitude, longitude);
        } else {
            result = openWeatherMapService.getWeatherInfo(message.getText());
        }

        return SendMessage.builder()
                .text(result)
                .parseMode("HTML")
                .chatId(String.valueOf(message.getChatId()))
                .build();
    }
}
