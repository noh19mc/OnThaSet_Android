package com.onthaset.app.admin

import com.onthaset.app.directory.BusinessAd
import com.onthaset.app.imaging.Buckets
import com.onthaset.app.imaging.StorageRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** Bucketed view the admin Ads tab needs to render. */
data class AdminAdsBuckets(
    val pending: List<BusinessAd>,
    val active: List<BusinessAd>,
    val deactivated: List<BusinessAd>,
    val expired: List<BusinessAd>,
)

@Singleton
class AdminAdsRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: StorageRepository,
) {
    /** Loads every ad row, runs the bulk-expiry sweep, and returns it sorted into buckets. */
    suspend fun loadAll(): AdminAdsBuckets {
        bulkExpireOverdue()
        val all: List<BusinessAd> = postgrest.from("ads").select {
            order("created_at", Order.DESCENDING)
        }.decodeList()
        return AdminAdsBuckets(
            pending = all.filter { it.status == "pending" || it.status == "approved" },
            active = all.filter { it.status == "active" },
            deactivated = all.filter { it.status == "rejected" || it.status == "inactive" },
            expired = all.filter { it.status == "expired" },
        )
    }

    /** Idempotent: PATCHes any active+non-sponsored ad with paid_until before today to "expired". */
    suspend fun bulkExpireOverdue() {
        val today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        runCatching {
            postgrest.from("ads").update(mapOf("status" to "expired")) {
                filter {
                    eq("status", "active")
                    eq("sponsored", false)
                    lt("paid_until", today.toString())
                }
            }
        }
    }

    /**
     * Approve flow per the iOS rules:
     *   sponsored=true → straight to "active" (free, no payment needed)
     *   otherwise → "approved" (waits for Stripe webhook to flip to "active")
     */
    suspend fun approve(ad: BusinessAd) {
        val newStatus = if (ad.sponsored == true) "active" else "approved"
        setStatus(ad.id ?: return, newStatus)
    }

    suspend fun reject(id: String) = setStatus(id, "rejected")
    suspend fun deactivate(id: String) = setStatus(id, "inactive")
    suspend fun activate(id: String) = setStatus(id, "active")

    suspend fun delete(ad: BusinessAd) {
        val id = ad.id ?: return
        ad.imageUrl?.takeIf { it.isNotBlank() }?.let { storage.deleteByUrl(Buckets.AD_BANNERS, it) }
        postgrest.from("ads").delete {
            filter { eq("id", id) }
        }
    }

    private suspend fun setStatus(id: String, status: String) {
        postgrest.from("ads").update(mapOf("status" to status)) {
            filter { eq("id", id) }
        }
    }
}
