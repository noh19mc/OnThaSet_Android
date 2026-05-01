package com.onthaset.app.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.weather.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

enum class TimeRange(val label: String) {
    Today("Today"),
    ThisWeek("This Week"),
    Month("Month"),
    AllUpcoming("All Upcoming"),
}

enum class RadiusMiles(val miles: Int, val label: String) {
    R100(100, "100mi"),
    R200(200, "200mi"),
    R300(300, "300mi"),
    R400(400, "400mi"),
    R500(500, "500mi"),
    Max(Int.MAX_VALUE, "MAX"),
}

data class NearbyEvent(val event: Event, val distanceMiles: Double)

data class NearbyFilter(val time: TimeRange = TimeRange.Month, val radius: RadiusMiles = RadiusMiles.R100)

sealed interface NearbyUiState {
    data object NeedsPermission : NearbyUiState
    data object Loading : NearbyUiState
    data class Ready(val events: List<NearbyEvent>) : NearbyUiState
    data class Error(val message: String) : NearbyUiState
}

@HiltViewModel
class NearbyEventsViewModel @Inject constructor(
    private val repo: EventsRepository,
    private val location: LocationProvider,
) : ViewModel() {

    private val _filter = MutableStateFlow(NearbyFilter())
    val filter: StateFlow<NearbyFilter> = _filter.asStateFlow()

    private val _state = MutableStateFlow<NearbyUiState>(NearbyUiState.NeedsPermission)
    val state: StateFlow<NearbyUiState> = _state.asStateFlow()

    private var allUpcoming: List<Event> = emptyList()
    private var lastLat: Double? = null
    private var lastLng: Double? = null

    init {
        if (location.hasPermission()) load()
    }

    fun load() = viewModelScope.launch {
        _state.value = NearbyUiState.Loading
        val loc = location.current()
        if (loc == null) {
            _state.value = NearbyUiState.Error("Couldn't read your location.")
            return@launch
        }
        lastLat = loc.latitude
        lastLng = loc.longitude
        runCatching { repo.upcoming() }
            .onSuccess {
                allUpcoming = it
                applyFilter()
            }
            .onFailure { _state.value = NearbyUiState.Error(it.message ?: "Failed to load") }
    }

    fun setTime(time: TimeRange) {
        _filter.value = _filter.value.copy(time = time)
        applyFilter()
    }

    fun setRadius(radius: RadiusMiles) {
        _filter.value = _filter.value.copy(radius = radius)
        applyFilter()
    }

    /** Used by the empty-state "EXPAND RADIUS" button — bumps to the next-larger radius. */
    fun expandRadius() {
        val current = _filter.value.radius
        val next = RadiusMiles.entries.firstOrNull { it.miles > current.miles } ?: return
        setRadius(next)
    }

    fun showAll() {
        setRadius(RadiusMiles.Max)
        setTime(TimeRange.AllUpcoming)
    }

    private fun applyFilter() {
        val lat = lastLat ?: return
        val lng = lastLng ?: return
        val f = _filter.value
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(zone).date
        val (start, end) = when (f.time) {
            TimeRange.Today -> {
                val s = today.atStartOfDayIn(zone)
                s to s.plus(1.days)
            }
            TimeRange.ThisWeek -> {
                // Week ends Sunday night.
                val daysToSunday = ((DayOfWeek.SUNDAY.isoDayNumber - today.dayOfWeek.isoDayNumber + 7) % 7)
                val sunday = today.plus(daysToSunday, DateTimeUnit.DAY)
                val start = today.atStartOfDayIn(zone)
                val end = sunday.atStartOfDayIn(zone).plus(1.days)
                start to end
            }
            TimeRange.Month -> {
                val start = today.atStartOfDayIn(zone)
                val end = start.plus(30.days)
                start to end
            }
            TimeRange.AllUpcoming -> now to now.plus(3650.days) // 10 years out, effectively unlimited
        }

        val withDistance = allUpcoming.mapNotNull { event ->
            if (event.latitude == 0.0 && event.longitude == 0.0) return@mapNotNull null
            if (event.date < start || event.date >= end) return@mapNotNull null
            val miles = haversineMiles(lat, lng, event.latitude, event.longitude)
            if (miles > f.radius.miles) return@mapNotNull null
            NearbyEvent(event, miles)
        }.sortedBy { it.distanceMiles }
        _state.value = NearbyUiState.Ready(withDistance)
    }

    fun onPermissionGranted() {
        load()
    }
}

private val DayOfWeek.isoDayNumber: Int get() = ordinal + 1 // MONDAY=1..SUNDAY=7 (kotlinx-datetime)
