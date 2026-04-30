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

    /** Creates the row if missing, returns the resulting profile. */
    suspend fun ensure(userId: String, email: String): UserProfile {
        byUserId(userId)?.let { return it }
        val seed = ProfileSeed(
            appleUserId = userId,
            email = email,
            displayName = email.substringBefore("@").ifBlank { "Rider" },
        )
        postgrest.from("users").insert(seed)
        return byUserId(userId) ?: error("Failed to create profile for $userId")
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
