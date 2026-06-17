package com.weather.dto;

import java.util.List;

public class WeatherDTO {
    private String cityName;
    private String country;
    private double latitude;
    private double longitude;
    private CurrentWeather current;
    private List<ForecastDay> forecast;

    public WeatherDTO() {}

    public WeatherDTO(String cityName, String country, double latitude, double longitude, CurrentWeather current, List<ForecastDay> forecast) {
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.current = current;
        this.forecast = forecast;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public CurrentWeather getCurrent() {
        return current;
    }

    public void setCurrent(CurrentWeather current) {
        this.current = current;
    }

    public List<ForecastDay> getForecast() {
        return forecast;
    }

    public void setForecast(List<ForecastDay> forecast) {
        this.forecast = forecast;
    }

    public static class CurrentWeather {
        private double temp;
        private double humidity;
        private double windSpeed;
        private double windDirection;
        private double pressure;
        private int weatherCode;
        private String weatherDescription;
        private String weatherGroup;

        public CurrentWeather() {}

        public CurrentWeather(double temp, double humidity, double windSpeed, double windDirection, double pressure, int weatherCode, String weatherDescription, String weatherGroup) {
            this.temp = temp;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
            this.pressure = pressure;
            this.weatherCode = weatherCode;
            this.weatherDescription = weatherDescription;
            this.weatherGroup = weatherGroup;
        }

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindDirection() {
            return windDirection;
        }

        public void setWindDirection(double windDirection) {
            this.windDirection = windDirection;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public int getWeatherCode() {
            return weatherCode;
        }

        public void setWeatherCode(int weatherCode) {
            this.weatherCode = weatherCode;
        }

        public String getWeatherDescription() {
            return weatherDescription;
        }

        public void setWeatherDescription(String weatherDescription) {
            this.weatherDescription = weatherDescription;
        }

        public String getWeatherGroup() {
            return weatherGroup;
        }

        public void setWeatherGroup(String weatherGroup) {
            this.weatherGroup = weatherGroup;
        }
    }

    public static class ForecastDay {
        private String date;
        private double maxTemp;
        private double minTemp;
        private int weatherCode;
        private String weatherDescription;
        private String weatherGroup;

        public ForecastDay() {}

        public ForecastDay(String date, double maxTemp, double minTemp, int weatherCode, String weatherDescription, String weatherGroup) {
            this.date = date;
            this.maxTemp = maxTemp;
            this.minTemp = minTemp;
            this.weatherCode = weatherCode;
            this.weatherDescription = weatherDescription;
            this.weatherGroup = weatherGroup;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getMaxTemp() {
            return maxTemp;
        }

        public void setMaxTemp(double maxTemp) {
            this.maxTemp = maxTemp;
        }

        public double getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(double minTemp) {
            this.minTemp = minTemp;
        }

        public int getWeatherCode() {
            return weatherCode;
        }

        public void setWeatherCode(int weatherCode) {
            this.weatherCode = weatherCode;
        }

        public String getWeatherDescription() {
            return weatherDescription;
        }

        public void setWeatherDescription(String weatherDescription) {
            this.weatherDescription = weatherDescription;
        }

        public String getWeatherGroup() {
            return weatherGroup;
        }

        public void setWeatherGroup(String weatherGroup) {
            this.weatherGroup = weatherGroup;
        }
    }
}
