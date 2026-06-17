package com.weather.service;

import com.weather.exception.CityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Geolocation getCoordinates(String cityName) {
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + cityName + "&count=1&language=en&format=json";
        
        try {
            GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);
            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                throw new CityNotFoundException("City not found: " + cityName);
            }
            GeocodingResult result = response.getResults().get(0);
            return new Geolocation(result.getName(), result.getCountry_code(), result.getLatitude(), result.getLongitude());
        } catch (Exception e) {
            if (e instanceof CityNotFoundException) {
                throw e;
            }
            throw new RuntimeException("Error contacting geocoding service: " + e.getMessage(), e);
        }
    }

    public static class Geolocation {
        private final String name;
        private final String countryCode;
        private final double latitude;
        private final double longitude;

        public Geolocation(String name, String countryCode, double latitude, double longitude) {
            this.name = name;
            this.countryCode = countryCode;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() { return name; }
        public String getCountryCode() { return countryCode; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    // Geocoding Response Mapping Classes
    public static class GeocodingResponse {
        private List<GeocodingResult> results;
        public List<GeocodingResult> getResults() { return results; }
        public void setResults(List<GeocodingResult> results) { this.results = results; }
    }

    public static class GeocodingResult {
        private String name;
        private String country_code;
        private double latitude;
        private double longitude;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCountry_code() { return country_code; }
        public void setCountry_code(String country_code) { this.country_code = country_code; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
}
