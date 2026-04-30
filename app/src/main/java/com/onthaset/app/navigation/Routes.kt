package com.onthaset.app.navigation

object Routes {
    const val GATE = "gate"
    const val AUTH = "auth"
    const val HOME = "home"
    const val EVENTS = "events"
    const val EVENT_DETAIL = "event/{id}"
    const val NATIONAL_RUN_CALENDAR = "calendar"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "profile/edit"
    const val BIKE_BUILDS = "bikes"
    const val WEATHER = "weather"
    const val ADMIN = "admin"

    fun eventDetail(id: String) = "event/$id"
}
