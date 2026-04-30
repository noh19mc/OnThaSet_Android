package com.onthaset.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level destinations for the bottom nav. Order here is the order of tabs left→right.
 * Subroute prefixes let us highlight the parent tab on detail screens (e.g. "event/{id}"
 * keeps Events highlighted).
 */
enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val matchesPrefixes: List<String>,
) {
    Events("events", "Events", Icons.Filled.Whatshot, listOf("events", "event/")),
    Calendar(Routes.NATIONAL_RUN_CALENDAR, "Calendar", Icons.Filled.CalendarMonth, listOf("calendar")),
    Forecast(Routes.WEATHER, "Forecast", Icons.Filled.WbSunny, listOf("weather")),
    Profile(Routes.PROFILE, "Profile", Icons.Filled.Person, listOf("profile", "rider/")),
    More(Routes.MORE, "More", Icons.Filled.MoreHoriz, listOf("more", "bikes", "event-photos", "directory", "subscribe", "admin"));

    fun matches(currentRoute: String?): Boolean {
        if (currentRoute == null) return false
        return matchesPrefixes.any { prefix -> currentRoute == prefix || currentRoute.startsWith("$prefix") }
    }
}

/** Routes where the bottom nav should NOT render (auth/gate/onboarding take over the screen). */
val routesWithoutBottomNav: Set<String> = setOf(
    Routes.GATE,
    Routes.AUTH,
    Routes.ONBOARDING,
)
