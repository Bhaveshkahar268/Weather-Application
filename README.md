# Nimbus Weather Application

An attractive, interactive, and modern weather forecasting web application built with a Java Spring Boot backend and an elegant HTML5/CSS3/JavaScript glassmorphic frontend.

---

## Key Features

- **Premium Glassmorphic UI:** A sleek dark-mode user interface designed with frosted-glass containers, vibrant glowing highlights, and premium Inter/Montserrat typography.
- **Dynamic Backgrounds & Particles Canvas:** The interface changes its backdrop color gradients and triggers corresponding weather particles (floating clouds, raindrops, falling snow, or warm sunrays) dynamically depending on the current weather condition of the searched city.
- **Interactive Details Grid:** Displays current temperature, wind speed, surface pressure, humidity percentage, and coordinates. Features an SVG wind compass that rotates dynamically to represent wind direction.
- **5-Day Forecast:** Shows a horizontal timeline view of daily forecasts with distinct weather condition descriptions and icons.
- **Celsius/Fahrenheit Toggle:** Converts all temperatures on the fly without making duplicate API requests.
- **In-Memory Caching:** Uses Spring Cache to cache weather responses for 15 minutes, improving speed and avoiding API limits.
- **No API Keys Required:** Uses the open-access Open-Meteo Geocoding and Forecast REST APIs, allowing the application to work out of the box.

---

## Technology Stack

- **Frontend:** HTML5, CSS3 (Vanilla CSS), JavaScript (Vanilla ES6), [Lucide Icons](https://lucide.dev/) (loaded via CDN)
- **Backend:** Java 17, Spring Boot 3.2.x, Spring Web, Spring Cache
- **Build Tool:** Maven

---

## Directory Structure

```text
TO-DO-List/
├── pom.xml                               # Maven Project dependencies
├── README.md                             # Project Documentation
└── src/
    ├── main/
    │   ├── java/com/weather/
    │   │   ├── WeatherApplication.java   # Spring Boot Entry Point
    │   │   ├── controller/
    │   │   │   └── WeatherController.java # REST Endpoint (/api/weather)
    │   │   ├── dto/
    │   │   │   └── WeatherDTO.java       # Unified Weather JSON Model
    │   │   ├── exception/
    │   │   │   └── CityNotFoundException.java # Validation exception
    │   │   └── service/
    │   │       ├── GeocodingService.java  # Translates city name to coords
    │   │       ├── WeatherIntegrationService.java # Fetches forecasts from API
    │   │       └── WeatherService.java   # Orchestrates services and caches responses
    │   └── resources/
    │       ├── application.properties     # Port & App configuration
    │       └── static/                   # Frontend assets
    │           ├── index.html            # Core layout page
    │           ├── css/
    │           │   └── style.css         # Styling, glassmorphic layout, themes
    │           └── js/
    │               └── app.js            # Fetch calls, unit logic, Canvas system
    └── test/
        └── java/com/weather/
            └── WeatherApplicationTests.java # Context loading verification tests
```

---

## Getting Started

### Prerequisites
- **Java JDK 17** or higher installed.
- **Apache Maven** installed.

### How to Run

1. **Clone or navigate to the project directory:**
   ```bash
   cd /Users/aditikahar/bhavesh/TO-DO-List
   ```

2. **Build and start the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **View in browser:**
   Open your browser and navigate to:
   [http://localhost:8080/](http://localhost:8080/)

---

## API References

This application integrates the following free API services:
- **Geocoding API:** [https://geocoding-api.open-meteo.com/v1/search](https://geocoding-api.open-meteo.com/v1/search)
- **Forecast API:** [https://api.open-meteo.com/v1/forecast](https://api.open-meteo.com/v1/forecast)
