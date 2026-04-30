package com.onthaset.app.events

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Free OpenStreetMap-based geocoder. Returns null on any failure — callers should fall
 * back to the iOS app's "lat=0, lng=0 means coords unknown" convention so events without
 * resolvable addresses still post (they just won't appear on the National Map).
 *
 * Nominatim's usage policy requires an identifying User-Agent and limits to ~1 req/sec.
 * We're well under that volume (one geocode per event-post), so no rate limiting needed.
 */
@Singleton
class Geocoder @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun geocode(address: String): Coordinates? {
        if (address.isBlank()) return null
        val results: List<NominatimResult> = try {
            client.get("https://nominatim.openstreetmap.org/search") {
                parameter("format", "json")
                parameter("q", address)
                parameter("limit", 1)
                parameter("addressdetails", 0)
                header("User-Agent", "OnThaSet-Android/0.1.0 (https://github.com/noh19mc/OnThaSet_Android)")
            }.body()
        } catch (t: Throwable) {
            return null
        }
        val first = results.firstOrNull() ?: return null
        val lat = first.lat.toDoubleOrNull() ?: return null
        val lon = first.lon.toDoubleOrNull() ?: return null
        return Coordinates(lat, lon)
    }
}

data class Coordinates(val latitude: Double, val longitude: Double)

@Serializable
internal data class NominatimResult(
    val lat: String,
    val lon: String,
)
