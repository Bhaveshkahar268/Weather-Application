package com.weather.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class WeatherIntegrationService {
    private final RestTemplate restTemplate = new RestTemplate();

    public OpenMeteoResponse fetchWeather(double latitude, double longitude) {
        // Fetch weather from open-meteo
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,relative_humidity_2m,weather_code,pressure_msl,wind_speed_10m,wind_direction_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto",
            latitude, longitude
        );
        try {
            return restTemplate.getForObject(url, OpenMeteoResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather from Open-Meteo: " + e.getMessage(), e);
        }
    }

    public static class OpenMeteoResponse {
        private Current current;
        private Daily daily;

        public Current getCurrent() { return current; }
        public void setCurrent(Current current) { this.current = current; }
        public Daily getDaily() { return daily; }
        public void setDaily(Daily daily) { this.daily = daily; }
    }

    public static class Current {
        private double temperature_2m;
        private double relative_humidity_2m;
        private double wind_speed_10m;
        private double wind_direction_10m;
        private double pressure_msl;
        private int weather_code;

        public double getTemperature_2m() { return temperature_2m; }
        public void setTemperature_2m(double temperature_2m) { this.temperature_2m = temperature_2m; }
        public double getRelative_humidity_2m() { return relative_humidity_2m; }
        public void setRelative_humidity_2m(double relative_humidity_2m) { this.relative_humidity_2m = relative_humidity_2m; }
        public double getWind_speed_10m() { return wind_speed_10m; }
        public void setWind_speed_10m(double wind_speed_10m) { this.wind_speed_10m = wind_speed_10m; }
        public double getWind_direction_10m() { return wind_direction_10m; }
        public void setWind_direction_10m(double wind_direction_10m) { this.wind_direction_10m = wind_direction_10m; }
        public double getPressure_msl() { return pressure_msl; }
        public void setPressure_msl(double pressure_msl) { this.pressure_msl = pressure_msl; }
        public int getWeather_code() { return weather_code; }
        public void setWeather_code(int weather_code) { this.weather_code = weather_code; }
    }

    public static class Daily {
        private List<String> time;
        private List<Integer> weather_code;
        private List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;

        public List<String> getTime() { return time; }
        public void setTime(List<String> time) { this.time = time; }
        public List<Integer> getWeather_code() { return weather_code; }
        public void setWeather_code(List<Integer> weather_code) { this.weather_code = weather_code; }
        public List<Double> getTemperature_2m_max() { return temperature_2m_max; }
        public void setTemperature_2m_max(List<Double> temperature_2m_max) { this.temperature_2m_max = temperature_2m_max; }
        public List<Double> getTemperature_2m_min() { return temperature_2m_min; }
        public void setTemperature_2m_min(List<Double> temperature_2m_min) { this.temperature_2m_min = temperature_2m_min; }
    }
}
