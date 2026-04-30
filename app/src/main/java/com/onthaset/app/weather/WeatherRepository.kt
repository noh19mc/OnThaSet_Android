package com.onthaset.app.weather

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun geocode(query: String): GeocodingResponse.Location? {
        val resp: GeocodingResponse = client.get("https://geocoding-api.open-meteo.com/v1/search") {
            parameter("name", query)
            parameter("count", 1)
        }.body()
        return resp.results?.firstOrNull()
    }

    suspend fun forecast(latitude: Double, longitude: Double): ForecastResponse =
        client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current_weather", true)
            parameter("daily", "weathercode,temperature_2m_max,temperature_2m_min")
            parameter("timezone", "auto")
            parameter("temperature_unit", "fahrenheit")
            parameter("windspeed_unit", "mph")
        }.body()
}
