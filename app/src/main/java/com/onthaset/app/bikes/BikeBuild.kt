package com.onthaset.app.bikes

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BikeBuild(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("modification_title") val modificationTitle: String,
    val note: String = "",
    @SerialName("before_image_url") val beforeImageUrl: String = "",
    @SerialName("after_image_url") val afterImageUrl: String = "",
    @SerialName("bike_make") val bikeMake: String = "",
    @SerialName("bike_model") val bikeModel: String = "",
    @SerialName("bike_year") val bikeYear: String = "",
    @SerialName("created_at") val createdAt: Instant? = null,
)

@Serializable
internal data class BikeBuildInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("modification_title") val modificationTitle: String,
    val note: String,
    @SerialName("before_image_url") val beforeImageUrl: String,
    @SerialName("after_image_url") val afterImageUrl: String,
    @SerialName("bike_make") val bikeMake: String,
    @SerialName("bike_model") val bikeModel: String,
    @SerialName("bike_year") val bikeYear: String,
)
