package com.onthaset.app.reports

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRepository @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun report(
        eventId: String,
        eventTitle: String,
        reportedByUserId: String,
        reason: ReportReason,
        notes: String,
    ) {
        postgrest.from("event_reports").insert(
            EventReportInsert(
                eventId = eventId,
                eventTitle = eventTitle,
                reportedByUserId = reportedByUserId,
                reason = reason.raw,
                additionalNotes = notes,
            )
        )
    }
}

@Serializable
internal data class EventReportInsert(
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String,
    @SerialName("reported_by_user_id") val reportedByUserId: String,
    val reason: String,
    @SerialName("additional_notes") val additionalNotes: String,
)
