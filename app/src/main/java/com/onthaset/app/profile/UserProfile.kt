package com.onthaset.app.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the Supabase `users` row used by the iOS app. The iOS code stores the Supabase
 * auth UUID in `apple_user_id` for both Apple-Sign-In and email-signup users, so Android
 * looks up profiles by that same column.
 */
@Serializable
data class UserProfile(
    @SerialName("apple_user_id") val appleUserId: String,
    val email: String = "",
    @SerialName("display_name") val displayName: String = "",
    val bio: String = "",
    val hometown: String = "",
    val club: String = "",
    @SerialName("favorite_ride") val favoriteRide: String = "",
    @SerialName("riding_since") val ridingSince: String = "",
    @SerialName("preferred_ride_type") val preferredRideType: String = "",
    @SerialName("favorite_route") val favoriteRoute: String = "",
    @SerialName("instagram_handle") val instagramHandle: String = "",
    @SerialName("tiktok_handle") val tiktokHandle: String = "",
    @SerialName("youtube_channel") val youtubeChannel: String = "",
    @SerialName("facebook_handle") val facebookHandle: String = "",
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    @SerialName("background_image_url") val backgroundImageUrl: String? = null,
    @SerialName("has_subscription") val hasSubscription: Boolean = false,
)

@Serializable
data class ProfileUpdate(
    @SerialName("display_name") val displayName: String,
    val bio: String,
    val hometown: String,
    val club: String,
    @SerialName("favorite_ride") val favoriteRide: String,
    @SerialName("riding_since") val ridingSince: String,
    @SerialName("preferred_ride_type") val preferredRideType: String,
    @SerialName("favorite_route") val favoriteRoute: String,
    @SerialName("instagram_handle") val instagramHandle: String,
    @SerialName("tiktok_handle") val tiktokHandle: String,
    @SerialName("youtube_channel") val youtubeChannel: String,
    @SerialName("facebook_handle") val facebookHandle: String,
)

@Serializable
internal data class ProfileSeed(
    @SerialName("apple_user_id") val appleUserId: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
)
