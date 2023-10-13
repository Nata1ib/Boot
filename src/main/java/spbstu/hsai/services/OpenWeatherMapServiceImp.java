package spbstu.hsai.services;

import com.fasterxml.jackson.databind.JsonNode;
import spbstu.hsai.weatherElements.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class OpenWeatherMapServiceImp implements OpenWeatherMapService {

    @Value("${open.weather.map.id}")
    private String appId;

    @Value("${yandex.geocoder}")
    private String yandexId;

    private final MessageSource messageSource;

    public OpenWeatherMapServiceImp(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getWeatherInfo(String message) {
        try {
            return createString(getResponse(message));
        } catch (FileNotFoundException e) {
            return messageSource.getMessage("notFound", null, Locale.ENGLISH);
        } catch (IOException e) {
            return "Something went wrong :(";
        }
    }

    public String YandexMapsJSONParser(String url) {
        String name = null;
        try {
            HttpClient httpClient = HttpClient.newHttpClient();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            String jsonString = httpResponse.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            JsonNode geoObjectNode = rootNode
                    .path("response")
                    .path("GeoObjectCollection")
                    .path("featureMember")
                    .get(0)
                    .path("GeoObject");

            name = geoObjectNode.path("name").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public String getWeatherInfoByLocation(double latitude, double longitude) {
        String s = String.format("https://geocode-maps.yandex.ru/1.x?format=json&lang=en_US&kind=locality&geocode=%s,%s&apikey=%s", latitude, longitude, yandexId);
        String city = YandexMapsJSONParser(s);
        try {
            return createString(getResponse(city));
        } catch (FileNotFoundException e) {
            return messageSource.getMessage("notFound", null, Locale.ENGLISH);
        } catch (IOException e) {
            return "Something went wrong :(";
        }
    }

    private Result getResponse(String message) throws IOException {
        String s = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric", message, appId);
        var url = new URL(s);
        return new ObjectMapper().readValue(url, Result.class);
    }

    private String createString(Result result) {
        String main = result.getName();
        double temp = result.getMain().getTemp();
        String description = result.getWeather().get(0).getDescription();
        int humidity = result.getMain().getHumidity();
        String sunrise = getTime(Long.sum(result.getSys().getSunrise(), result.getTimezone()));
        String sunset = getTime(Long.sum(result.getSys().getSunset(), result.getTimezone()));

        return "<i>Place: </i><b>" + main + "</b>\n" +
                "<i>Temperature: </i><b>" + temp + " C</b>\n" +
                "<i>Description: </i><b>" + description + "</b>\n" +
                "<i>Humidity: </i><b>" + humidity + "%</b>\n" +
                "<i>Sunrise: </i><b>" + sunrise + "</b>\n" +
                "<i>Sunset: </i><b>" + sunset + "</b>";
    }

    private String getTime(long time) {
        var formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
                .withZone(ZoneId.of("UTC"));
        return formatter.format(Instant.ofEpochSecond(time));
    }
}
