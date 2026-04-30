package com.onthaset.app.events

import androidx.compose.ui.graphics.Color

enum class EventCategory(
    val raw: String,
    val icon: String,
    val isNational: Boolean,
    val pinColor: Color,
) {
    Community("Community", "🤝", false, Color(0xFF8E8E93)),
    Charity("Charity Event / Fundraiser", "❤️", true, Color(0xFF007AFF)),
    WeeklyClubhouse("Weekly Clubhouse", "🏠", false, Color(0xFF8E8E93)),
    BikeNight("Bike Night", "🏍️", false, Color(0xFF8E8E93)),
    Rally("Motorcycle Rally", "🎪", true, Color(0xFFFF3B30)),
    McAnnual("MC Annual", "🎉", true, Color(0xFFFFD60A)),
    ScAnnual("Social Club Annual", "🌹", true, Color(0xFFFF69B4)),
    RcAnnual("Riding Club Annual", "🚦", true, Color(0xFF34C759)),
    UnityRun("Unity Run", "🤝", true, Color(0xFFAF52DE));

    companion object {
        fun fromRaw(raw: String?): EventCategory? =
            raw?.let { v -> entries.firstOrNull { it.raw == v } }

        val national: List<EventCategory> get() = entries.filter { it.isNational }
    }
}
