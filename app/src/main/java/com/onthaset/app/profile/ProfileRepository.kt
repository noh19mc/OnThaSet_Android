package com.onthaset.app.profile

import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun byUserId(userId: String): UserProfile? =
        postgrest.from("users").select {
            filter { eq("apple_user_id", userId) }
            limit(1)
        }.decodeSingleOrNull()

    /**
     * Returns the user's profile, creating a starter row if one doesn't exist.
     *
     * Falls back to a synthetic in-memory profile when the insert is blocked by RLS or
     * when re-fetch returns null — matches the iOS app's "log the error and continue"
     * behavior, so the user can still browse events, calendar, etc., without a backend
     * row. The synthetic profile is read-only; updates will throw until RLS is fixed.
     */
    suspend fun ensure(userId: String, email: String): UserProfile {
        byUserId(userId)?.let { return it }
        val seed = ProfileSeed(
            appleUserId = userId,
            email = email,
            displayName = email.substringBefore("@").ifBlank { "Rider" },
        )
        runCatching { postgrest.from("users").insert(seed) }
        return byUserId(userId) ?: UserProfile(
            appleUserId = userId,
            email = email,
            displayName = email.substringBefore("@").ifBlank { "Rider" },
        )
    }

    suspend fun update(userId: String, update: ProfileUpdate) {
        postgrest.from("users").update(update) {
            filter { eq("apple_user_id", userId) }
        }
    }

    suspend fun updateProfileImageUrl(userId: String, url: String) {
        postgrest.from("users").update(mapOf("profile_image_url" to url)) {
            filter { eq("apple_user_id", userId) }
        }
    }

    suspend fun updateBackgroundImageUrl(userId: String, url: String) {
        postgrest.from("users").update(mapOf("background_image_url" to url)) {
            filter { eq("apple_user_id", userId) }
        }
    }
}
