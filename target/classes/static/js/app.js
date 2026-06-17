// Global State & Constants
let currentWeatherData = null;
let useFahrenheit = false;
let animationFrameId = null;

// DOM Elements
const searchForm = document.getElementById('search-form');
const cityInput = document.getElementById('city-input');
const unitToggle = document.getElementById('unit-toggle');
const weatherDashboard = document.getElementById('weather-dashboard');
const errorCard = document.getElementById('error-card');
const errorMessage = document.getElementById('error-message');
const errorCloseBtn = document.getElementById('error-close-btn');
const loadingOverlay = document.getElementById('loading-overlay');
const quickTags = document.querySelectorAll('.tag-btn');

// Weather fields
const cityNameEl = document.getElementById('city-name');
const countryBadgeEl = document.getElementById('country-badge');
const currentDateEl = document.getElementById('current-date');
const mainTempEl = document.getElementById('main-temp');
const weatherDescEl = document.getElementById('weather-desc');
const weatherIconContainer = document.getElementById('weather-icon-container');
const windSpeedEl = document.getElementById('wind-speed');
const windArrowEl = document.getElementById('wind-arrow');
const windDegEl = document.getElementById('wind-deg');
const humidityValEl = document.getElementById('humidity-val');
const humidityBarEl = document.getElementById('humidity-bar');
const pressureValEl = document.getElementById('pressure-val');
const coordsValEl = document.getElementById('coords-val');
const forecastContainer = document.getElementById('forecast-container');

// Canvas Setup
const canvas = document.getElementById('weather-canvas');
const ctx = canvas.getContext('2d');
let particles = [];

// Initialize Page
window.addEventListener('DOMContentLoaded', () => {
    // Initialize Lucide Icons
    lucide.createIcons();
    
    // Set up canvas sizing
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);
    
    // Fetch default city
    fetchWeatherData('London');
    
    // Setup event listeners
    searchForm.addEventListener('submit', handleSearchSubmit);
    unitToggle.addEventListener('change', handleUnitToggle);
    errorCloseBtn.addEventListener('click', hideError);
    
    quickTags.forEach(tag => {
        tag.addEventListener('click', (e) => {
            const city = e.target.getAttribute('data-city');
            cityInput.value = city;
            fetchWeatherData(city);
        });
    });
});

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    if (currentWeatherData) {
        initParticleSystem(currentWeatherData.current.weatherGroup);
    }
}

// Form Handlers
function handleSearchSubmit(e) {
    e.preventDefault();
    const city = cityInput.value.trim();
    if (city) {
        fetchWeatherData(city);
    }
}

function handleUnitToggle(e) {
    useFahrenheit = e.target.checked;
    if (currentWeatherData) {
        updateTemperatures();
    }
}

// Fetch Logic
async function fetchWeatherData(city) {
    showLoading(true);
    hideError();
    
    try {
        const response = await fetch(`/api/weather?city=${encodeURIComponent(city)}`);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || 'Failed to fetch weather data');
        }
        
        currentWeatherData = data;
        renderWeather();
        
    } catch (err) {
        console.error(err);
        showError(err.message);
    } finally {
        showLoading(false);
    }
}

// Render Logic
function renderWeather() {
    if (!currentWeatherData) return;
    
    const { cityName, country, latitude, longitude, current, forecast } = currentWeatherData;
    
    // 1. Change Weather Theme on Body
    document.body.className = `weather-${current.weatherGroup}`;
    
    // 2. Set Up Canvas Particle System
    initParticleSystem(current.weatherGroup);
    
    // 3. Update Text Content
    cityNameEl.textContent = cityName;
    countryBadgeEl.textContent = country;
    currentDateEl.textContent = formatDate(new Date());
    
    weatherDescEl.textContent = current.weatherDescription;
    
    // Set Main Icon
    const iconName = getIconName(current.weatherGroup);
    weatherIconContainer.innerHTML = `<i data-lucide="${iconName}" class="weather-main-icon text-${current.weatherGroup}"></i>`;
    
    // Set parameters
    updateTemperatures();
    
    // Wind Direction
    const isMetricSpeed = !useFahrenheit; // We can show speed in km/h or mph
    const speedVal = isMetricSpeed ? current.windSpeed : current.windSpeed * 0.621371;
    const speedUnit = isMetricSpeed ? 'km/h' : 'mph';
    windSpeedEl.textContent = `${speedVal.toFixed(1)} ${speedUnit}`;
    windArrowEl.style.transform = `rotate(${current.windDirection}deg)`;
    windDegEl.textContent = `${current.windDirection}°`;
    
    // Humidity
    humidityValEl.textContent = `${current.humidity}%`;
    humidityBarEl.style.width = `${current.humidity}%`;
    
    // Pressure & Coords
    pressureValEl.textContent = `${current.pressure.toFixed(0)} hPa`;
    coordsValEl.textContent = `${Math.abs(latitude).toFixed(2)}°${latitude >= 0 ? 'N' : 'S'}, ${Math.abs(longitude).toFixed(2)}°${longitude >= 0 ? 'E' : 'W'}`;
    
    // 4. Render 5-Day Forecast
    renderForecast(forecast);
    
    // Re-trigger Lucide icon generation
    lucide.createIcons();
}

function updateTemperatures() {
    if (!currentWeatherData) return;
    
    // Main Temp
    const mainTemp = currentWeatherData.current.temp;
    mainTempEl.textContent = formatTemp(mainTemp);
    
    // Forecast Temps
    const forecastCards = forecastContainer.querySelectorAll('.forecast-card');
    forecastCards.forEach((card, idx) => {
        const dayData = currentWeatherData.forecast[idx];
        if (dayData) {
            const maxTempEl = card.querySelector('.forecast-temp-max');
            const minTempEl = card.querySelector('.forecast-temp-min');
            
            maxTempEl.textContent = `${formatTemp(dayData.maxTemp)}°`;
            minTempEl.textContent = `${formatTemp(dayData.minTemp)}°`;
        }
    });
}

function renderForecast(forecastList) {
    forecastContainer.innerHTML = '';
    
    forecastList.forEach(day => {
        const dateObj = new Date(day.date);
        const dayName = dateObj.toLocaleDateString('en-US', { weekday: 'short' });
        const dayDesc = day.weatherDescription;
        const iconName = getIconName(day.weatherGroup);
        
        const card = document.createElement('div');
        card.className = 'forecast-card fade-in';
        card.innerHTML = `
            <span class="forecast-day">${dayName}</span>
            <i data-lucide="${iconName}" class="forecast-icon"></i>
            <div class="forecast-temps">
                <span class="forecast-temp-max">${formatTemp(day.maxTemp)}°</span>
                <span class="forecast-temp-min">${formatTemp(day.minTemp)}°</span>
            </div>
            <span class="forecast-desc" title="${dayDesc}">${dayDesc}</span>
        `;
        forecastContainer.appendChild(card);
    });
}

// Helpers
function formatTemp(tempCelsius) {
    if (useFahrenheit) {
        return Math.round((tempCelsius * 9/5) + 32);
    }
    return Math.round(tempCelsius);
}

function formatDate(date) {
    const options = { weekday: 'long', month: 'short', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

function getIconName(group) {
    switch(group) {
        case 'sunny': return 'sun';
        case 'cloudy': return 'cloud';
        case 'rainy': return 'cloud-rain';
        case 'snowy': return 'snowflake';
        case 'stormy': return 'cloud-lightning';
        default: return 'sun';
    }
}

function showLoading(show) {
    if (show) {
        loadingOverlay.classList.remove('hidden');
    } else {
        loadingOverlay.classList.add('hidden');
    }
}

function showError(msg) {
    errorMessage.textContent = msg;
    errorCard.classList.remove('hidden');
}

function hideError() {
    errorCard.classList.add('hidden');
}

// Canvas Weather Particle Animation Engine
function initParticleSystem(weatherGroup) {
    // Stop any existing animation
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
    }
    
    particles = [];
    let count = 0;
    
    if (weatherGroup === 'rainy' || weatherGroup === 'stormy') {
        count = weatherGroup === 'stormy' ? 120 : 70;
        for (let i = 0; i < count; i++) {
            particles.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                length: Math.random() * 20 + 10,
                speed: Math.random() * 8 + 6,
                opacity: Math.random() * 0.3 + 0.1,
                weight: Math.random() * 1 + 1
            });
        }
        animateRain(weatherGroup === 'stormy');
        
    } else if (weatherGroup === 'snowy') {
        count = 60;
        for (let i = 0; i < count; i++) {
            particles.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                r: Math.random() * 3 + 1,
                d: Math.random() * count, // density (for movement sine wave)
                speed: Math.random() * 1.5 + 0.5,
                opacity: Math.random() * 0.5 + 0.2
            });
        }
        animateSnow();
        
    } else if (weatherGroup === 'cloudy') {
        count = 6;
        for (let i = 0; i < count; i++) {
            particles.push({
                x: Math.random() * (canvas.width + 200) - 100,
                y: Math.random() * (canvas.height * 0.45),
                r: Math.random() * 80 + 60,
                speed: Math.random() * 0.3 + 0.1,
                opacity: Math.random() * 0.08 + 0.03
            });
        }
        animateClouds();
        
    } else if (weatherGroup === 'sunny') {
        count = 3;
        for (let i = 0; i < count; i++) {
            particles.push({
                x: canvas.width * (0.3 + i * 0.2) + (Math.random() * 50 - 25),
                y: canvas.height * (0.2 + i * 0.1) + (Math.random() * 50 - 25),
                r: Math.random() * 150 + 100,
                speed: Math.random() * 0.02 + 0.01,
                angle: Math.random() * Math.PI,
                opacity: Math.random() * 0.04 + 0.01
            });
        }
        animateSunny();
    }
}

// Particle Loops
function animateRain(isStormy) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Lightning trigger logic for storm
    let flash = false;
    if (isStormy && Math.random() < 0.003) {
        flash = true;
    }
    
    if (flash) {
        ctx.fillStyle = 'rgba(235, 220, 255, 0.45)';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
    }
    
    ctx.strokeStyle = 'rgba(174,194,224,0.5)';
    ctx.lineWidth = 1.5;
    
    particles.forEach(p => {
        ctx.beginPath();
        ctx.moveTo(p.x, p.y);
        ctx.lineTo(p.x + (p.speed * 0.1), p.y + p.length);
        ctx.strokeStyle = `rgba(174, 194, 224, ${p.opacity})`;
        ctx.stroke();
        
        p.y += p.speed;
        p.x += p.speed * 0.1;
        
        if (p.y > canvas.height) {
            p.y = -p.length;
            p.x = Math.random() * canvas.width;
        }
    });
    
    animationFrameId = requestAnimationFrame(() => animateRain(isStormy));
}

function animateSnow() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
    
    particles.forEach(p => {
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2, true);
        ctx.fillStyle = `rgba(255, 255, 255, ${p.opacity})`;
        ctx.fill();
        
        // Sine wave horizontal wiggle + drop speed
        p.y += p.speed;
        p.x += Math.sin(p.d) * 0.4;
        p.d += 0.01;
        
        if (p.y > canvas.height) {
            p.y = -10;
            p.x = Math.random() * canvas.width;
        }
    });
    
    animationFrameId = requestAnimationFrame(animateSnow);
}

function animateClouds() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    particles.forEach(p => {
        const grad = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.r);
        grad.addColorStop(0, `rgba(255, 255, 255, ${p.opacity})`);
        grad.addColorStop(0.8, `rgba(255, 255, 255, ${p.opacity * 0.5})`);
        grad.addColorStop(1, 'rgba(255, 255, 255, 0)');
        
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fill();
        
        p.x += p.speed;
        if (p.x - p.r > canvas.width) {
            p.x = -p.r;
            p.y = Math.random() * (canvas.height * 0.45);
        }
    });
    
    animationFrameId = requestAnimationFrame(animateClouds);
}

function animateSunny() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    particles.forEach(p => {
        p.angle += p.speed;
        
        const grad = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.r * (1 + Math.sin(p.angle) * 0.08));
        grad.addColorStop(0, `rgba(245, 158, 11, ${p.opacity * 1.5})`);
        grad.addColorStop(0.4, `rgba(245, 158, 11, ${p.opacity})`);
        grad.addColorStop(1, 'rgba(245, 158, 11, 0)');
        
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r * (1 + Math.sin(p.angle) * 0.08), 0, Math.PI * 2);
        ctx.fill();
    });
    
    animationFrameId = requestAnimationFrame(animateSunny);
}
