package com.weather.service;

import com.weather.dto.WeatherDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final GeocodingService geocodingService;
    private final WeatherIntegrationService weatherIntegrationService;

    public WeatherService(GeocodingService geocodingService, WeatherIntegrationService weatherIntegrationService) {
        this.geocodingService = geocodingService;
        this.weatherIntegrationService = weatherIntegrationService;
    }

    @Cacheable(value = "weather", key = "#cityName.toLowerCase().trim()")
    public WeatherDTO getWeatherForCity(String cityName) {
        logger.info("Fetching fresh weather data from APIs for: {}", cityName);
        
        // 1. Resolve city to latitude & longitude
        GeocodingService.Geolocation loc = geocodingService.getCoordinates(cityName);
        
        // 2. Fetch raw weather data
        WeatherIntegrationService.OpenMeteoResponse rawWeather = weatherIntegrationService.fetchWeather(loc.getLatitude(), loc.getLongitude());
        
        // 3. Map to clean DTO
        int currentCode = rawWeather.getCurrent().getWeather_code();
        WeatherDTO.CurrentWeather current = new WeatherDTO.CurrentWeather(
            rawWeather.getCurrent().getTemperature_2m(),
            rawWeather.getCurrent().getRelative_humidity_2m(),
            rawWeather.getCurrent().getWind_speed_10m(),
            rawWeather.getCurrent().getWind_direction_10m(),
            rawWeather.getCurrent().getPressure_msl(),
            currentCode,
            getWeatherDescription(currentCode),
            getWeatherGroup(currentCode)
        );

        List<WeatherDTO.ForecastDay> forecastList = new ArrayList<>();
        WeatherIntegrationService.Daily daily = rawWeather.getDaily();
        if (daily != null && daily.getTime() != null) {
            int limit = Math.min(5, daily.getTime().size());
            for (int i = 0; i < limit; i++) {
                int code = daily.getWeather_code().get(i);
                forecastList.add(new WeatherDTO.ForecastDay(
                    daily.getTime().get(i),
                    daily.getTemperature_2m_max().get(i),
                    daily.getTemperature_2m_min().get(i),
                    code,
                    getWeatherDescription(code),
                    getWeatherGroup(code)
                ));
            }
        }

        return new WeatherDTO(
            loc.getName(),
            loc.getCountryCode(),
            loc.getLatitude(),
            loc.getLongitude(),
            current,
            forecastList
        );
    }

    public static String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: return "Foggy";
            case 48: return "Depositing rime fog";
            case 51: return "Light drizzle";
            case 53: return "Moderate drizzle";
            case 55: return "Dense drizzle";
            case 56: return "Light freezing drizzle";
            case 57: return "Dense freezing drizzle";
            case 61: return "Slight rain";
            case 63: return "Moderate rain";
            case 65: return "Heavy rain";
            case 66: return "Light freezing rain";
            case 67: return "Heavy freezing rain";
            case 71: return "Slight snow fall";
            case 73: return "Moderate snow fall";
            case 75: return "Heavy snow fall";
            case 77: return "Snow grains";
            case 80: return "Slight rain showers";
            case 81: return "Moderate rain showers";
            case 82: return "Violent rain showers";
            case 85: return "Slight snow showers";
            case 86: return "Heavy snow showers";
            case 95: return "Thunderstorm";
            case 96: return "Thunderstorm with slight hail";
            case 99: return "Thunderstorm with heavy hail";
            default: return "Unknown weather";
        }
    }

    public static String getWeatherGroup(int code) {
        switch (code) {
            case 0:
            case 1:
                return "sunny";
            case 2:
            case 3:
            case 45:
            case 48:
                return "cloudy";
            case 51:
            case 53:
            case 55:
            case 61:
            case 63:
            case 65:
            case 80:
            case 81:
            case 82:
                return "rainy";
            case 56:
            case 57:
            case 66:
            case 67:
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return "snowy";
            case 95:
            case 96:
            case 99:
                return "stormy";
            default:
                return "sunny";
        }
    }
}
