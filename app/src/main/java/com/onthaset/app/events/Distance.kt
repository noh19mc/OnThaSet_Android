package com.onthaset.app.events

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_MILES = 3958.8

/**
 * Great-circle distance in miles. Mirrors the iOS `haversineDistance` in SupabaseManager
 * — we deliberately match the exact formula so distance numbers shown to users on iOS
 * and Android line up.
 */
fun haversineMiles(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    return EARTH_RADIUS_MILES * 2 * atan2(sqrt(a), sqrt(1 - a))
}
