package com.onthaset.app.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class CalendarFilter(
    val month: Int, // 1..12
    val year: Int,
    val category: EventCategory? = null,
    /** Full state name (e.g. "Texas"). null = no state filter applied. */
    val state: String? = null,
)

sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Ready(val events: List<Event>) : CalendarUiState
    data class Error(val message: String) : CalendarUiState
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: EventsRepository,
    private val statesGeoJson: StatesGeoJson,
) : ViewModel() {

    private val _states = MutableStateFlow<List<StatePolygon>>(emptyList())
    val states: StateFlow<List<StatePolygon>> = _states.asStateFlow()

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val _filter = MutableStateFlow(
        CalendarFilter(month = now.monthNumber, year = now.year)
    )
    val filter: StateFlow<CalendarFilter> = _filter.asStateFlow()

    private val _state = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    private var allNational: List<Event> = emptyList()

    init {
        load()
        loadStates()
    }

    private fun loadStates() = viewModelScope.launch {
        _states.value = statesGeoJson.load()
    }

    fun load() = viewModelScope.launch {
        _state.value = CalendarUiState.Loading
        runCatching { repo.upcoming() }
            .onSuccess { all ->
                allNational = all.filter {
                    val cat = it.categoryEnum ?: return@filter false
                    cat.isNational && (it.latitude != 0.0 || it.longitude != 0.0)
                }
                applyFilter()
            }
            .onFailure { _state.value = CalendarUiState.Error(it.message ?: "Failed to load") }
    }

    fun selectMonth(month: Int, year: Int) {
        _filter.update { it.copy(month = month, year = year) }
        applyFilter()
    }

    fun shiftMonth(delta: Int) {
        val current = _filter.value
        val zero = (current.month - 1 + delta).floorMod(12)
        val carry = (current.month - 1 + delta).floorDiv(12)
        selectMonth(month = zero + 1, year = current.year + carry)
    }

    fun selectCategory(category: EventCategory?) {
        _filter.update { it.copy(category = category) }
        applyFilter()
    }

    fun selectState(state: String?) {
        _filter.update { it.copy(state = state) }
        applyFilter()
    }

    private fun applyFilter() {
        val f = _filter.value
        val zone = TimeZone.currentSystemDefault()
        _state.value = CalendarUiState.Ready(
            allNational.filter { event ->
                val ldt = event.date.toLocalDateTime(zone)
                val matchMonth = ldt.monthNumber == f.month && ldt.year == f.year
                val matchCategory = f.category == null || event.categoryEnum == f.category
                val matchState = f.state == null || event.matchesState(f.state)
                matchMonth && matchCategory && matchState
            }
        )
    }
}

/**
 * Mirrors the iOS `eventsInSelectedState` parsing: state lives at parts[3] of the pipe-
 * delimited locationName. Match by full name OR 2-letter abbreviation since iOS users
 * may have entered either.
 */
private fun Event.matchesState(stateName: String): Boolean {
    val parts = locationName.split('|')
    val raw = parts.getOrNull(3)?.trim().orEmpty()
    if (raw.equals(stateName, ignoreCase = true)) return true
    val abbr = stateAbbreviations[stateName] ?: return false
    return raw.equals(abbr, ignoreCase = true)
}

private val stateAbbreviations = mapOf(
    "Alabama" to "AL", "Alaska" to "AK", "Arizona" to "AZ", "Arkansas" to "AR",
    "California" to "CA", "Colorado" to "CO", "Connecticut" to "CT", "Delaware" to "DE",
    "Florida" to "FL", "Georgia" to "GA", "Hawaii" to "HI", "Idaho" to "ID",
    "Illinois" to "IL", "Indiana" to "IN", "Iowa" to "IA", "Kansas" to "KS",
    "Kentucky" to "KY", "Louisiana" to "LA", "Maine" to "ME", "Maryland" to "MD",
    "Massachusetts" to "MA", "Michigan" to "MI", "Minnesota" to "MN", "Mississippi" to "MS",
    "Missouri" to "MO", "Montana" to "MT", "Nebraska" to "NE", "Nevada" to "NV",
    "New Hampshire" to "NH", "New Jersey" to "NJ", "New Mexico" to "NM", "New York" to "NY",
    "North Carolina" to "NC", "North Dakota" to "ND", "Ohio" to "OH", "Oklahoma" to "OK",
    "Oregon" to "OR", "Pennsylvania" to "PA", "Rhode Island" to "RI", "South Carolina" to "SC",
    "South Dakota" to "SD", "Tennessee" to "TN", "Texas" to "TX", "Utah" to "UT",
    "Vermont" to "VT", "Virginia" to "VA", "Washington" to "WA", "West Virginia" to "WV",
    "Wisconsin" to "WI", "Wyoming" to "WY", "District of Columbia" to "DC",
)

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
private fun Int.floorDiv(other: Int): Int = if (this >= 0 || this % other == 0) this / other else this / other - 1

fun monthName(month: Int): String = Month.entries[month - 1].name.lowercase().replaceFirstChar { it.uppercase() }
