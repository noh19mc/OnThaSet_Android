package com.onthaset.app.eventphotos

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventPhotosRepository @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun feed(limit: Int = 50): List<EventPhoto> =
        postgrest.from("event_photos").select {
            order("event_date", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList()

    suspend fun create(
        uploadedBy: String,
        eventName: String,
        eventDate: Instant,
        location: String,
        caption: String,
        imageUrl: String,
    ) {
        postgrest.from("event_photos").insert(
            EventPhotoInsert(
                uploadedBy = uploadedBy,
                eventName = eventName,
                eventDate = eventDate,
                location = location,
                caption = caption,
                imageUrl = imageUrl,
            )
        )
    }
}
