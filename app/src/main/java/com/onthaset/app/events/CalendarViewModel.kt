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
)

sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Ready(val events: List<Event>) : CalendarUiState
    data class Error(val message: String) : CalendarUiState
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: EventsRepository,
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val _filter = MutableStateFlow(
        CalendarFilter(month = now.monthNumber, year = now.year)
    )
    val filter: StateFlow<CalendarFilter> = _filter.asStateFlow()

    private val _state = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    private var allNational: List<Event> = emptyList()

    init { load() }

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

    private fun applyFilter() {
        val f = _filter.value
        val zone = TimeZone.currentSystemDefault()
        _state.value = CalendarUiState.Ready(
            allNational.filter { event ->
                val ldt = event.date.toLocalDateTime(zone)
                ldt.monthNumber == f.month && ldt.year == f.year &&
                    (f.category == null || event.categoryEnum == f.category)
            }
        )
    }
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
private fun Int.floorDiv(other: Int): Int = if (this >= 0 || this % other == 0) this / other else this / other - 1

fun monthName(month: Int): String = Month.entries[month - 1].name.lowercase().replaceFirstChar { it.uppercase() }
