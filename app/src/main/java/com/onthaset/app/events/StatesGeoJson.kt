package com.onthaset.app.events

import com.google.android.gms.maps.model.LatLng
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** A single closed-ring polygon representing one state piece (states like FL/HI have many). */
data class StatePolygon(
    val state: String,
    val outline: List<LatLng>,
)

@Singleton
class StatesGeoJson @Inject constructor(
    private val client: HttpClient,
) {
    /** Same source iOS uses — public-domain US states GeoJSON. */
    private val url = "https://raw.githubusercontent.com/PublicaMundi/MappingAPI/master/data/geojson/us-states.json"

    private var cache: List<StatePolygon>? = null

    suspend fun load(): List<StatePolygon> {
        cache?.let { return it }
        val raw = try {
            client.get(url).bodyAsText()
        } catch (_: Throwable) {
            return emptyList()
        }
        val parsed = parse(raw)
        cache = parsed
        return parsed
    }

    private fun parse(raw: String): List<StatePolygon> {
        val root = Json.parseToJsonElement(raw).jsonObject
        val features = root["features"]?.jsonArray ?: return emptyList()
        val out = mutableListOf<StatePolygon>()
        for (feature in features) {
            val obj = feature.jsonObject
            val name = obj["properties"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull
                ?: continue
            val geometry = obj["geometry"]?.jsonObject ?: continue
            when (geometry["type"]?.jsonPrimitive?.contentOrNull) {
                "Polygon" -> {
                    val coords = geometry["coordinates"]?.jsonArray ?: continue
                    coords.firstOrNull()?.let { ring -> out += StatePolygon(name, ring.toLatLngList()) }
                }
                "MultiPolygon" -> {
                    val coords = geometry["coordinates"]?.jsonArray ?: continue
                    coords.forEach { polygon ->
                        polygon.jsonArray.firstOrNull()?.let { ring ->
                            out += StatePolygon(name, ring.toLatLngList())
                        }
                    }
                }
            }
        }
        return out
    }
}

private fun JsonElement.toLatLngList(): List<LatLng> = jsonArray.mapNotNull { coord ->
    val arr = coord.jsonArray
    val lng = arr.getOrNull(0)?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null
    val lat = arr.getOrNull(1)?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null
    LatLng(lat, lng)
}

private val JsonElement.jsonPrimitiveOrNull get() = runCatching { jsonPrimitive }.getOrNull()
private val kotlinx.serialization.json.JsonPrimitive.doubleOrNull get() = runCatching { content.toDouble() }.getOrNull()
private val kotlinx.serialization.json.JsonPrimitive.contentOrNull get() = runCatching { content }.getOrNull()
