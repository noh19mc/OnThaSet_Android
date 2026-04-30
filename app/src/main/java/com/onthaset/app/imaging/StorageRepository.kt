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

    /**
     * Best-effort delete by public URL. Extracts the filename after "/<bucket>/" so we can
     * call storage.delete with the path portion. Silently swallows failures — useful when
     * cascading a row delete that probably-already-removed the file.
     */
    suspend fun deleteByUrl(bucket: String, url: String) {
        if (url.isBlank()) return
        val marker = "/$bucket/"
        val idx = url.indexOf(marker)
        if (idx < 0) return
        var path = url.substring(idx + marker.length)
        // Strip the cache-busting query we add on profile uploads.
        path = path.substringBefore('?')
        if (path.isBlank()) return
        runCatching { storage.from(bucket).delete(path) }
    }
}

object Buckets {
    const val PROFILE_IMAGES = "profile-images"
    const val EVENT_FLYERS = "event-flyers"
    const val EVENT_PHOTOS = "event-photos"
    const val BIKE_PROGRESS = "bike-progress"
    const val AD_BANNERS = "ad-banners"
}

object StoragePaths {
    fun profile(userId: String) = "profile-${userId.lowercase()}.jpg"
    fun background(userId: String) = "background-${userId.lowercase()}.jpg"

    /** Each bike-build entry needs a unique pair of filenames, so include a timestamp + tag. */
    fun bikeBefore(userId: String, stamp: Long) = "before-${userId.lowercase()}-$stamp.jpg"
    fun bikeAfter(userId: String, stamp: Long) = "after-${userId.lowercase()}-$stamp.jpg"
}
