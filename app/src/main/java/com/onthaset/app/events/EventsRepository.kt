package com.onthaset.app.events

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
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
}
