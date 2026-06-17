package com.weather.controller;

import com.weather.dto.WeatherDTO;
import com.weather.exception.CityNotFoundException;
import com.weather.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public ResponseEntity<?> getWeather(@RequestParam String city) {
        if (city == null || city.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "City name must not be empty.");
            return ResponseEntity.badRequest().body(error);
        }
        try {
            WeatherDTO weather = weatherService.getWeatherForCity(city);
            return ResponseEntity.ok(weather);
        } catch (CityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve weather details. Please try again.");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
