package com.onthaset.app.bikes

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BikesRepository @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun feed(limit: Int = 50): List<BikeBuild> =
        postgrest.from("bike_builds").select {
            order("created_at", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList()

    suspend fun byUser(userId: String): List<BikeBuild> =
        postgrest.from("bike_builds").select {
            filter { eq("user_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    suspend fun create(
        userId: String,
        modificationTitle: String,
        note: String,
        beforeImageUrl: String,
        afterImageUrl: String,
        bikeMake: String,
        bikeModel: String,
        bikeYear: String,
    ) {
        postgrest.from("bike_builds").insert(
            BikeBuildInsert(
                userId = userId,
                modificationTitle = modificationTitle,
                note = note,
                beforeImageUrl = beforeImageUrl,
                afterImageUrl = afterImageUrl,
                bikeMake = bikeMake,
                bikeModel = bikeModel,
                bikeYear = bikeYear,
            )
        )
    }
}
