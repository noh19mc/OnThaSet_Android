package com.onthaset.app.events

import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Mirrors iOS SupabaseManager.cutoffDate. Events with `date < cutoff` are considered expired.
 *
 * Why: Friend's iOS app keeps weekend events visible through Monday 8am ET so riders see them
 * the next morning. Android needs to match so both apps show the same set of events.
 */
fun computeEventCutoff(now: Instant = Clock.System.now()): Instant {
    val et = TimeZone.of("America/New_York")
    val nowEt: LocalDateTime = now.toLocalDateTime(et)
    val todayAt8amEt: Instant = nowEt.date.atTime(LocalTime(8, 0)).toInstant(et)

    val baseTime = if (now < todayAt8amEt) todayAt8amEt.minus(1.days) else todayAt8amEt

    return when (nowEt.date.dayOfWeek) {
        DayOfWeek.SUNDAY -> baseTime.minus(2.days)
        DayOfWeek.SATURDAY -> baseTime.minus(1.days)
        else -> now.minus(24.hours)
    }
}
