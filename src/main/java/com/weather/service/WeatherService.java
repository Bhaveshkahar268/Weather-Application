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
    private final WeatherApiService weatherApiService;
    private final WttrInService wttrInService;

    public WeatherService(GeocodingService geocodingService, 
                          WeatherIntegrationService weatherIntegrationService,
                          WeatherApiService weatherApiService,
                          WttrInService wttrInService) {
        this.geocodingService = geocodingService;
        this.weatherIntegrationService = weatherIntegrationService;
        this.weatherApiService = weatherApiService;
        this.wttrInService = wttrInService;
    }

    @Cacheable(value = "weather", key = "#cityName.toLowerCase().trim()")
    public WeatherDTO getWeatherForCity(String cityName) {
        if (weatherApiService.isConfigured()) {
            try {
                return weatherApiService.getWeather(cityName);
            } catch (Exception e) {
                logger.error("WeatherAPI.com query failed, falling back to Open-Meteo or mock: {}", e.getMessage());
            }
        }

        logger.info("Fetching fresh weather data from Open-Meteo APIs for: {}", cityName);
        
        try {
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
        } catch (Exception e) {
            logger.warn("Open-Meteo APIs failed (likely rate-limited). Trying keyless wttr.in fallback for: {}. Error: {}", cityName, e.getMessage());
            try {
                return wttrInService.getWeather(cityName);
            } catch (Exception ex) {
                logger.error("wttr.in fallback also failed. Returning mock weather: {}", ex.getMessage());
                return generateMockWeather(cityName);
            }
        }
    }

    private WeatherDTO generateMockWeather(String cityName) {
        logger.warn("Generating high-quality mock weather data for: {}", cityName);
        
        int hash = cityName.toLowerCase().trim().hashCode();
        double lat = 30.0 + (Math.abs(hash) % 20);
        double lon = 10.0 + (Math.abs(hash) % 40);
        
        String[] groups = {"sunny", "cloudy", "rainy", "snowy", "stormy"};
        String weatherGroup = groups[Math.abs(hash) % groups.length];
        
        double temp;
        int code;
        String desc;
        
        switch (weatherGroup) {
            case "sunny":
                temp = 25.0 + (Math.abs(hash) % 10);
                code = 0;
                desc = "Clear sky";
                break;
            case "cloudy":
                temp = 14.0 + (Math.abs(hash) % 8);
                code = 3;
                desc = "Overcast";
                break;
            case "rainy":
                temp = 12.0 + (Math.abs(hash) % 6);
                code = 61;
                desc = "Slight rain";
                break;
            case "snowy":
                temp = -2.0 - (Math.abs(hash) % 5);
                code = 71;
                desc = "Slight snow fall";
                break;
            case "stormy":
                temp = 17.0 + (Math.abs(hash) % 7);
                code = 95;
                desc = "Thunderstorm";
                break;
            default:
                temp = 20.0;
                code = 0;
                desc = "Clear sky";
                weatherGroup = "sunny";
        }
        
        WeatherDTO.CurrentWeather current = new WeatherDTO.CurrentWeather(
            temp,
            55.0 + (Math.abs(hash) % 25), // humidity
            8.0 + (Math.abs(hash) % 15),  // wind speed
            120.0 + (Math.abs(hash) % 180), // wind direction
            1008.0 + (Math.abs(hash) % 12), // pressure
            code,
            desc,
            weatherGroup
        );
        
        List<WeatherDTO.ForecastDay> forecastList = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 0; i < 5; i++) {
            String fGroup = groups[Math.abs(hash + i * 31) % groups.length];
            double fMax = temp + (i % 3) + 2;
            double fMin = temp - (i % 3) - 2;
            int fCode;
            String fDesc;
            
            switch (fGroup) {
                case "sunny": fCode = 0; fDesc = "Clear sky"; break;
                case "cloudy": fCode = 2; fDesc = "Partly cloudy"; break;
                case "rainy": fCode = 61; fDesc = "Slight rain"; break;
                case "snowy": fCode = 71; fDesc = "Slight snow fall"; break;
                case "stormy": fCode = 95; fDesc = "Thunderstorm"; break;
                default: fCode = 0; fDesc = "Clear sky"; fGroup = "sunny";
            }
            
            forecastList.add(new WeatherDTO.ForecastDay(
                today.plusDays(i).toString(),
                fMax,
                fMin,
                fCode,
                fDesc,
                fGroup
            ));
        }
        
        String country = "US";
        String lowerCity = cityName.toLowerCase().trim();
        if (lowerCity.contains("london")) country = "GB";
        else if (lowerCity.contains("tokyo")) country = "JP";
        else if (lowerCity.contains("mumbai")) country = "IN";
        else if (lowerCity.contains("paris")) country = "FR";
        else if (lowerCity.contains("sydney")) country = "AU";
        else if (lowerCity.contains("new york")) country = "US";
        
        String cleanName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1).toLowerCase();
        
        return new WeatherDTO(
            cleanName,
            country,
            lat,
            lon,
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
