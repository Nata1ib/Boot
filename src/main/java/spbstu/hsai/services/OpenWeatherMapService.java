package spbstu.hsai.services;

public interface OpenWeatherMapService {

    String getWeatherInfo(String message);

    String getWeatherInfoByLocation(double latitude, double longitude);
}
