package com.weather.service;

import com.weather.dto.WeatherDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WeatherApiService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherApiService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.api.key:}")
    private String apiKey;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public WeatherDTO getWeather(String cityName) {
        logger.info("Fetching live weather from WeatherAPI.com using API key for: {}", cityName);
        String url = "http://api.weatherapi.com/v1/forecast.json?key={key}&q={q}&days=5&aqi=no&alerts=no";
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, apiKey, cityName.trim());
            if (response == null) {
                throw new RuntimeException("Empty response from WeatherAPI.com");
            }
            
            // Parse Location
            Map<String, Object> location = (Map<String, Object>) response.get("location");
            String name = (String) location.get("name");
            String country = (String) location.get("country");
            double lat = ((Number) location.get("lat")).doubleValue();
            double lon = ((Number) location.get("lon")).doubleValue();
            
            // Parse Current Weather
            Map<String, Object> currentMap = (Map<String, Object>) response.get("current");
            double temp = ((Number) currentMap.get("temp_c")).doubleValue();
            double humidity = ((Number) currentMap.get("humidity")).doubleValue();
            double windSpeed = ((Number) currentMap.get("wind_kph")).doubleValue();
            double windDir = ((Number) currentMap.get("wind_degree")).doubleValue();
            double pressure = ((Number) currentMap.get("pressure_mb")).doubleValue();
            
            Map<String, Object> condition = (Map<String, Object>) currentMap.get("condition");
            String desc = (String) condition.get("text");
            int code = ((Number) condition.get("code")).intValue();
            String group = mapWeatherApiCodeToGroup(code);
            
            WeatherDTO.CurrentWeather current = new WeatherDTO.CurrentWeather(
                temp, humidity, windSpeed, windDir, pressure, code, desc, group
            );
            
            // Parse Forecast
            List<WeatherDTO.ForecastDay> forecastList = new ArrayList<>();
            Map<String, Object> forecastMap = (Map<String, Object>) response.get("forecast");
            List<Map<String, Object>> forecastDays = (List<Map<String, Object>>) forecastMap.get("forecastday");
            
            for (Map<String, Object> dayMap : forecastDays) {
                String date = (String) dayMap.get("date");
                Map<String, Object> dayDetails = (Map<String, Object>) dayMap.get("day");
                double maxTemp = ((Number) dayDetails.get("maxtemp_c")).doubleValue();
                double minTemp = ((Number) dayDetails.get("mintemp_c")).doubleValue();
                
                Map<String, Object> dayCond = (Map<String, Object>) dayDetails.get("condition");
                String fDesc = (String) dayCond.get("text");
                int fCode = ((Number) dayCond.get("code")).intValue();
                String fGroup = mapWeatherApiCodeToGroup(fCode);
                
                forecastList.add(new WeatherDTO.ForecastDay(
                    date, maxTemp, minTemp, fCode, fDesc, fGroup
                ));
            }
            
            // Format country name to short code
            String shortCountry = country;
            if (country.equalsIgnoreCase("United Kingdom")) shortCountry = "GB";
            else if (country.equalsIgnoreCase("United States of America") || country.equalsIgnoreCase("United States")) shortCountry = "US";
            else if (country.equalsIgnoreCase("India")) shortCountry = "IN";
            else if (country.equalsIgnoreCase("Japan")) shortCountry = "JP";
            else if (country.equalsIgnoreCase("France")) shortCountry = "FR";
            else if (country.equalsIgnoreCase("Germany")) shortCountry = "DE";
            else if (country.equalsIgnoreCase("Australia")) shortCountry = "AU";
            else if (country.length() > 3) {
                shortCountry = country.substring(0, 3).toUpperCase();
            }
            
            return new WeatherDTO(name, shortCountry, lat, lon, current, forecastList);
            
        } catch (Exception e) {
            throw new RuntimeException("WeatherAPI.com query failed: " + e.getMessage(), e);
        }
    }

    public static String mapWeatherApiCodeToGroup(int code) {
        if (code == 1000) return "sunny";
        if (code == 1003 || code == 1006 || code == 1009 || code == 1030 || code == 1135 || code == 1147) return "cloudy";
        if (code == 1063 || (code >= 1150 && code <= 1201) || (code >= 1240 && code <= 1246) || code == 1249 || code == 1252) return "rainy";
        if (code == 1066 || code == 1069 || code == 1072 || (code >= 1204 && code <= 1237) || (code >= 1255 && code <= 1264)) return "snowy";
        if (code == 1087 || (code >= 1273 && code <= 1282)) return "stormy";
        return "sunny";
    }
}
