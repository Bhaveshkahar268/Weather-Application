package com.weather.service;

import com.weather.dto.WeatherDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WttrInService {
    private static final Logger logger = LoggerFactory.getLogger(WttrInService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public WeatherDTO getWeather(String cityName) {
        logger.info("Fetching live weather from wttr.in for: {}", cityName);
        String url = "https://wttr.in/{city}?format=j1";
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, cityName.trim());
            if (response == null) {
                throw new RuntimeException("Empty response from wttr.in");
            }
            
            // Parse location from nearest area
            List<Map<String, Object>> nearestAreaList = (List<Map<String, Object>>) response.get("nearest_area");
            if (nearestAreaList == null || nearestAreaList.isEmpty()) {
                throw new RuntimeException("Could not find location details in wttr.in response");
            }
            Map<String, Object> area = nearestAreaList.get(0);
            
            String name = cityName.substring(0, 1).toUpperCase() + cityName.substring(1).toLowerCase();
            try {
                name = (String) ((List<Map<String, Object>>) area.get("areaName")).get(0).get("value");
            } catch (Exception ignored) {}
            
            String country = "US";
            try {
                country = (String) ((List<Map<String, Object>>) area.get("country")).get(0).get("value");
            } catch (Exception ignored) {}
            
            double lat = Double.parseDouble((String) area.get("latitude"));
            double lon = Double.parseDouble((String) area.get("longitude"));
            
            // Parse Current Condition
            List<Map<String, Object>> currentList = (List<Map<String, Object>>) response.get("current_condition");
            if (currentList == null || currentList.isEmpty()) {
                throw new RuntimeException("No current condition data in wttr.in response");
            }
            Map<String, Object> currentMap = currentList.get(0);
            
            double temp = Double.parseDouble((String) currentMap.get("temp_C"));
            double humidity = Double.parseDouble((String) currentMap.get("humidity"));
            double windSpeed = Double.parseDouble((String) currentMap.get("windspeedKmph"));
            double windDir = Double.parseDouble((String) currentMap.get("winddirDegree"));
            double pressure = Double.parseDouble((String) currentMap.get("pressure"));
            
            List<Map<String, Object>> descList = (List<Map<String, Object>>) currentMap.get("weatherDesc");
            String desc = (descList != null && !descList.isEmpty()) ? (String) descList.get(0).get("value") : "Clear";
            int code = Integer.parseInt((String) currentMap.get("weatherCode"));
            String group = mapWttrCodeToGroup(code);
            
            WeatherDTO.CurrentWeather current = new WeatherDTO.CurrentWeather(
                temp, humidity, windSpeed, windDir, pressure, code, desc, group
            );
            
            // Parse 5-Day Forecast
            List<WeatherDTO.ForecastDay> forecastList = new ArrayList<>();
            List<Map<String, Object>> weatherDays = (List<Map<String, Object>>) response.get("weather");
            
            if (weatherDays != null) {
                int limit = Math.min(5, weatherDays.size());
                for (int i = 0; i < limit; i++) {
                    Map<String, Object> dayMap = weatherDays.get(i);
                    String date = (String) dayMap.get("date");
                    double maxTemp = Double.parseDouble((String) dayMap.get("maxtempC"));
                    double minTemp = Double.parseDouble((String) dayMap.get("mintempC"));
                    
                    // Get midday forecast (index 4 of 8 hourly parameters corresponds to 12:00 PM)
                    List<Map<String, Object>> hourly = (List<Map<String, Object>>) dayMap.get("hourly");
                    Map<String, Object> midDay = (hourly != null && hourly.size() > 4) ? hourly.get(4) : hourly.get(0);
                    
                    int fCode = Integer.parseInt((String) midDay.get("weatherCode"));
                    List<Map<String, Object>> fDescList = (List<Map<String, Object>>) midDay.get("weatherDesc");
                    String fDesc = (fDescList != null && !fDescList.isEmpty()) ? (String) fDescList.get(0).get("value") : "Clear";
                    String fGroup = mapWttrCodeToGroup(fCode);
                    
                    forecastList.add(new WeatherDTO.ForecastDay(
                        date, maxTemp, minTemp, fCode, fDesc, fGroup
                    ));
                }
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
            throw new RuntimeException("wttr.in query failed: " + e.getMessage(), e);
        }
    }

    public static String mapWttrCodeToGroup(int code) {
        // Wttr.in uses WorldWeatherOnline condition codes.
        // 113: Sunny/Clear (group: "sunny")
        // 116: Partly Cloudy (group: "cloudy")
        // 119: Cloudy (group: "cloudy")
        // 122: Overcast (group: "cloudy")
        // 143, 248, 260: Mist/Fog (group: "cloudy")
        // 176, 263, 266, 293, 296, 299, 302, 305, 308, 353, 356, 359: Rain (group: "rainy")
        // 179, 182, 185, 227, 230, 311, 314, 317, 320, 323, 326, 329, 332, 335, 338, 350, 362, 365, 368, 371, 392, 395: Snow/Sleet (group: "snowy")
        // 200, 386, 389, 398: Storm (group: "stormy")
        
        switch (code) {
            case 113:
                return "sunny";
            case 116:
            case 119:
            case 122:
            case 143:
            case 248:
            case 260:
                return "cloudy";
            case 176:
            case 263:
            case 266:
            case 293:
            case 296:
            case 299:
            case 302:
            case 305:
            case 308:
            case 353:
            case 356:
            case 359:
                return "rainy";
            case 179:
            case 182:
            case 185:
            case 227:
            case 230:
            case 311:
            case 314:
            case 317:
            case 320:
            case 323:
            case 326:
            case 329:
            case 332:
            case 335:
            case 338:
            case 350:
            case 362:
            case 365:
            case 368:
            case 371:
            case 392:
            case 395:
                return "snowy";
            case 200:
            case 386:
            case 389:
            case 398:
                return "stormy";
            default:
                return "sunny";
        }
    }
}
