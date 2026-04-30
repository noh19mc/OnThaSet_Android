package com.onthaset.app.navigation

object Routes {
    const val GATE = "gate"
    const val AUTH = "auth"
    const val HOME = "home"
    const val EVENTS = "events"
    const val EVENT_DETAIL = "event/{id}"
    const val CREATE_EVENT = "events/new"
    const val NATIONAL_RUN_CALENDAR = "calendar"
    const val NATIONAL_RUN_CALENDAR_MAP = "calendar/map"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "profile/edit"
    const val ONBOARDING = "profile/onboarding"
    const val PUBLIC_PROFILE = "rider/{userId}"

    fun publicProfile(userId: String) = "rider/$userId"
    const val BIKE_BUILDS = "bikes"
    const val ADD_BIKE_BUILD = "bikes/new"
    const val WEATHER = "weather"
    const val EVENT_PHOTOS = "event-photos"
    const val ADD_EVENT_PHOTO = "event-photos/new"
    const val ADMIN = "admin"

    fun eventDetail(id: String) = "event/$id"
}
