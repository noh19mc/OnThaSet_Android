package com.onthaset.app.events

enum class EventCategory(
    val raw: String,
    val icon: String,
    val isNational: Boolean,
) {
    Community("Community", "🤝", false),
    Charity("Charity Event / Fundraiser", "❤️", true),
    WeeklyClubhouse("Weekly Clubhouse", "🏠", false),
    BikeNight("Bike Night", "🏍️", false),
    Rally("Motorcycle Rally", "🎪", true),
    McAnnual("MC Annual", "🎉", true),
    ScAnnual("Social Club Annual", "🌹", true),
    RcAnnual("Riding Club Annual", "🚦", true),
    UnityRun("Unity Run", "🤝", true);

    companion object {
        fun fromRaw(raw: String?): EventCategory? =
            raw?.let { v -> entries.firstOrNull { it.raw == v } }
    }
}
