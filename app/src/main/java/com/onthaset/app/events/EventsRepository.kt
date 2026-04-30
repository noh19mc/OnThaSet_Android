package com.onthaset.app.events

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsRepository @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun upcoming(): List<Event> {
        val cutoff = computeEventCutoff()
        return postgrest.from("events").select {
            filter { gte("date", cutoff.toString()) }
            order("date", Order.ASCENDING)
        }.decodeList()
    }

    suspend fun byId(id: String): Event? =
        postgrest.from("events").select {
            filter { eq("id", id) }
            limit(1)
        }.decodeSingleOrNull()

    suspend fun all(): List<Event> =
        postgrest.from("events").select {
            order("date", Order.ASCENDING)
        }.decodeList()

    suspend fun delete(id: String) {
        postgrest.from("events").delete {
            filter { eq("id", id) }
        }
    }

    suspend fun create(
        title: String,
        date: Instant,
        category: String,
        locationName: String,
        details: String,
        price: String,
        latitude: Double,
        longitude: Double,
        postedByUserId: String,
        postedByName: String,
        imageUrl: String?,
    ) {
        postgrest.from("events").insert(
            EventInsert(
                title = title,
                date = date,
                category = category,
                locationName = locationName,
                details = details,
                price = price,
                latitude = latitude,
                longitude = longitude,
                postedByUserId = postedByUserId,
                postedByName = postedByName,
                imageUrl = imageUrl,
            )
        )
    }
}

@Serializable
internal data class EventInsert(
    val title: String,
    val date: Instant,
    val category: String,
    @SerialName("location_name") val locationName: String,
    val details: String,
    val price: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("posted_by_user_id") val postedByUserId: String,
    @SerialName("posted_by_name") val postedByName: String,
    @SerialName("image_url") val imageUrl: String?,
)
