package com.onthaset.app.events

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String? = null,
    val title: String,
    val date: Instant,
    val category: String,
    @SerialName("location_name") val locationName: String,
    val details: String,
    val price: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerialName("posted_by_user_id") val postedByUserId: String,
    @SerialName("posted_by_name") val postedByName: String,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("image_url") val imageUrl: String? = null,
) {
    val categoryEnum: EventCategory? get() = EventCategory.fromRaw(category)
}
