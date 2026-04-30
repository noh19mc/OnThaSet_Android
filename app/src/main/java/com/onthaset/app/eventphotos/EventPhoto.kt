package com.onthaset.app.eventphotos

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventPhoto(
    val id: String? = null,
    @SerialName("uploaded_by") val uploadedBy: String,
    @SerialName("event_name") val eventName: String,
    @SerialName("event_date") val eventDate: Instant? = null,
    val location: String = "",
    val caption: String = "",
    @SerialName("image_url") val imageUrl: String,
    @SerialName("created_at") val createdAt: Instant? = null,
)

@Serializable
internal data class EventPhotoInsert(
    @SerialName("uploaded_by") val uploadedBy: String,
    @SerialName("event_name") val eventName: String,
    @SerialName("event_date") val eventDate: Instant,
    val location: String,
    val caption: String,
    @SerialName("image_url") val imageUrl: String,
)
