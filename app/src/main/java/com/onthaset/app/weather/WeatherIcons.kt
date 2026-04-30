package com.onthaset.app.weather

/**
 * Maps Open-Meteo WMO weather codes to a representative emoji.
 * https://open-meteo.com/en/docs#weathervariables
 */
fun emojiForWeatherCode(code: Int): String = when (code) {
    0 -> "☀️"
    1, 2, 3 -> "⛅"
    45, 48 -> "🌫️"
    in 51..67 -> "🌧️"
    in 71..77 -> "🌨️"
    in 80..82 -> "🌧️"
    in 85..86 -> "🌨️"
    in 95..99 -> "⛈️"
    else -> "☁️"
}
