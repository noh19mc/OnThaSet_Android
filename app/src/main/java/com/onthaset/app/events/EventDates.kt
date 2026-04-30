package com.onthaset.app.events

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

fun Instant.formatEventDay(zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val ldt = toLocalDateTime(zone).toJavaLocalDateTime()
    return dayFormatter.format(ldt)
}

fun Instant.formatEventTime(zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val ldt = toLocalDateTime(zone).toJavaLocalDateTime()
    return timeFormatter.format(ldt)
}

private fun kotlinx.datetime.LocalDateTime.toJavaLocalDateTime(): java.time.LocalDateTime =
    java.time.LocalDateTime.of(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)
