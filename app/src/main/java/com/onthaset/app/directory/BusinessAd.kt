package com.onthaset.app.directory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessAd(
    val id: String? = null,
    @SerialName("business_name") val businessName: String,
    val tagline: String = "",
    val category: String = "",
    val phone: String? = null,
    @SerialName("website_url") val websiteUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val address: String? = null,
    val status: String = "active",
    val plan: String = "standard",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val sponsored: Boolean? = null,
    @SerialName("advertiser_email") val advertiserEmail: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("paid_until") val paidUntil: String? = null,
)
