package com.onthaset.app.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EventsUiState {
    data object Loading : EventsUiState
    data class Ready(val events: List<Event>) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repo: EventsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val state: StateFlow<EventsUiState> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init { load(initial = true) }

    fun refresh() = load(initial = false)

    private fun load(initial: Boolean) = viewModelScope.launch {
        if (initial) _state.value = EventsUiState.Loading else _refreshing.value = true
        runCatching { repo.upcoming() }
            .onSuccess { _state.value = EventsUiState.Ready(it) }
            .onFailure { _state.value = EventsUiState.Error(it.message ?: "Failed to load events") }
        _refreshing.update { false }
    }
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val repo: EventsRepository,
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    fun load(id: String) = viewModelScope.launch {
        _event.value = runCatching { repo.byId(id) }.getOrNull()
    }
}
