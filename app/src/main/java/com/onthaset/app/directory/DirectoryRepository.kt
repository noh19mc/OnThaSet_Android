package com.onthaset.app.directory

import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectoryRepository @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun activeAds(): List<BusinessAd> =
        postgrest.from("ads").select {
            filter { eq("status", "active") }
        }.decodeList()
}
