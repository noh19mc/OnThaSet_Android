package com.onthaset.app.imaging

import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.upload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storage: Storage,
) {
    /** Uploads (with upsert) and returns the public URL. */
    suspend fun upload(bucket: String, path: String, bytes: ByteArray): String {
        storage.from(bucket).upload(path = path, data = bytes) { upsert = true }
        return storage.from(bucket).publicUrl(path)
    }
}

object Buckets {
    const val PROFILE_IMAGES = "profile-images"
    const val EVENT_FLYERS = "event-flyers"
}

object StoragePaths {
    fun profile(userId: String) = "profile-${userId.lowercase()}.jpg"
    fun background(userId: String) = "background-${userId.lowercase()}.jpg"
}
