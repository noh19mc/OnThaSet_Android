package com.onthaset.app.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class WeatherUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cityName: String = "",
    val currentTempF: Int? = null,
    val currentWindMph: Int? = null,
    val currentCode: Int = 0,
    val safety: RideSafety? = null,
    val daily: List<DayForecast> = emptyList(),
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repo: WeatherRepository,
    private val location: LocationProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    val locationPermissionGranted: Boolean get() = location.hasPermission()

    fun loadCurrentLocation() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        val loc = location.current()
        if (loc == null) {
            _state.value = _state.value.copy(isLoading = false, error = "Couldn't read your location.")
            return@launch
        }
        runCatching { repo.forecast(loc.latitude, loc.longitude) }
            .onSuccess { _state.value = render(it, "Your Location") }
            .onFailure { e -> _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed") }
    }

    fun search(city: String) = viewModelScope.launch {
        if (city.isBlank()) return@launch
        _state.value = _state.value.copy(isLoading = true, error = null)
        runCatching {
            val location = repo.geocode(city.trim())
                ?: error("Location not found")
            val forecast = repo.forecast(location.latitude, location.longitude)
            forecast to location
        }.onSuccess { (forecast, location) ->
            _state.value = render(forecast, location.displayName())
        }.onFailure { e ->
            _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed to load weather")
        }
    }

    fun loadFor(latitude: Double, longitude: Double, label: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        runCatching { repo.forecast(latitude, longitude) }
            .onSuccess { _state.value = render(it, label) }
            .onFailure { e -> _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed") }
    }

    private fun render(f: ForecastResponse, name: String): WeatherUiState {
        val days = (f.daily.time.indices).map { i ->
            DayForecast(
                day = if (i == 0) "TODAY" else dayLabel(f.daily.time[i]),
                highF = f.daily.temperature_2m_max[i].toInt(),
                lowF = f.daily.temperature_2m_min[i].toInt(),
                code = f.daily.weathercode[i],
            )
        }
        return WeatherUiState(
            isLoading = false,
            error = null,
            cityName = name,
            currentTempF = f.current_weather.temperature.toInt(),
            currentWindMph = f.current_weather.windspeed.toInt(),
            currentCode = f.current_weather.weathercode,
            safety = rideSafetyForWind(f.current_weather.windspeed),
            daily = days,
        )
    }

    private fun dayLabel(yyyyMmDd: String): String =
        runCatching {
            LocalDate.parse(yyyyMmDd).format(DateTimeFormatter.ofPattern("EEE", Locale.US)).uppercase()
        }.getOrDefault(yyyyMmDd)
}

private fun GeocodingResponse.Location.displayName(): String =
    listOfNotNull(name, admin1).joinToString(", ")
