package com.onthaset.app.profile

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Count
import javax.inject.Inject
import javax.inject.Singleton

/** Per-user counts surfaced on profile screens (matches the iOS RR HAYES stats grid). */
data class ProfileStats(val events: Int, val photos: Int, val builds: Int) {
    companion object { val Empty = ProfileStats(0, 0, 0) }
}

@Singleton
class ProfileStatsRepository @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun forUser(userId: String): ProfileStats {
        val events = countOrZero("events", "posted_by_user_id", userId)
        val photos = countOrZero("event_photos", "uploaded_by", userId)
        val builds = countOrZero("bike_builds", "user_id", userId)
        return ProfileStats(events = events, photos = photos, builds = builds)
    }

    /**
     * Postgrest's count Prefer header returns the row count alongside the body. Falls back
     * to 0 on any failure (RLS, network) so a missing count never blocks the screen.
     */
    private suspend fun countOrZero(table: String, column: String, value: String): Int =
        runCatching {
            postgrest.from(table).select {
                count(Count.EXACT)
                filter { eq(column, value) }
            }.countOrNull()?.toInt() ?: 0
        }.getOrDefault(0)
}
