package com.onthaset.app.weather

import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val current_weather: CurrentWeather,
    val daily: DailyForecast,
) {
    @Serializable
    data class CurrentWeather(
        val temperature: Double,
        val windspeed: Double,
        val weathercode: Int,
    )

    @Serializable
    data class DailyForecast(
        val time: List<String>,
        val weathercode: List<Int>,
        val temperature_2m_max: List<Double>,
        val temperature_2m_min: List<Double>,
    )
}

@Serializable
data class GeocodingResponse(
    val results: List<Location>? = null,
) {
    @Serializable
    data class Location(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val admin1: String? = null,
        val country: String? = null,
    )
}

data class DayForecast(
    val day: String,
    val highF: Int,
    val lowF: Int,
    val code: Int,
)

enum class RideSafety(val message: String) {
    Optimal("CLEAR TO RIDE: OPTIMAL"),
    Sticky("CAUTION: STICKY CONDITIONS"),
    Dangerous("DANGEROUS WINDS: HIGH RISK"),
}

fun rideSafetyForWind(mph: Double): RideSafety = when {
    mph > 20 -> RideSafety.Dangerous
    mph > 12 -> RideSafety.Sticky
    else -> RideSafety.Optimal
}
