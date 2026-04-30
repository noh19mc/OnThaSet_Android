package com.onthaset.app.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.BuildConfig
import com.onthaset.app.directory.BusinessAd
import com.onthaset.app.events.Event
import com.onthaset.app.events.EventsRepository
import com.onthaset.app.reports.EventReport
import com.onthaset.app.reports.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AdminUiState {
    data object Locked : AdminUiState
    data object Loading : AdminUiState
    data class Ready(
        val events: List<Event>,
        val reports: List<EventReport>,
        val ads: AdminAdsBuckets,
    ) : AdminUiState
    data class Error(val message: String) : AdminUiState
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val events: EventsRepository,
    private val reports: ReportsRepository,
    private val ads: AdminAdsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AdminUiState>(AdminUiState.Locked)
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    val pinConfigured: Boolean = BuildConfig.ADMIN_PIN.isNotBlank()

    fun tryUnlock(pin: String): Boolean {
        if (!pinConfigured) return false
        if (pin.trim() != BuildConfig.ADMIN_PIN) return false
        load()
        return true
    }

    fun load() = viewModelScope.launch {
        _state.value = AdminUiState.Loading
        runCatching {
            val ev = events.all()
            val rp = runCatching { reports.all() }.getOrDefault(emptyList())
            val ad = runCatching { ads.loadAll() }
                .getOrDefault(AdminAdsBuckets(emptyList(), emptyList(), emptyList(), emptyList()))
            Triple(ev, rp, ad)
        }
            .onSuccess { (ev, rp, ad) -> _state.value = AdminUiState.Ready(ev, rp, ad) }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Failed to load") }
    }

    fun deleteEvent(id: String) = viewModelScope.launch {
        runCatching { events.delete(id) }
            .onSuccess { load() }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Delete failed") }
    }

    fun dismissReport(id: String) = viewModelScope.launch {
        runCatching { reports.dismiss(id) }
            .onSuccess { load() }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Dismiss failed") }
    }

    fun approveAd(ad: BusinessAd) = viewModelScope.launch {
        runCatching { ads.approve(ad) }.onSuccess { load() }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Approve failed") }
    }

    fun rejectAd(id: String) = viewModelScope.launch {
        runCatching { ads.reject(id) }.onSuccess { load() }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Reject failed") }
    }

    fun deactivateAd(id: String) = viewModelScope.launch {
        runCatching { ads.deactivate(id) }.onSuccess { load() }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Deactivate failed") }
    }

    fun activateAd(id: String) = viewModelScope.launch {
        runCatching { ads.activate(id) }.onSuccess { load() }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Activate failed") }
    }

    fun deleteAd(ad: BusinessAd) = viewModelScope.launch {
        runCatching { ads.delete(ad) }.onSuccess { load() }
            .onFailure { _state.value = AdminUiState.Error(it.message ?: "Delete failed") }
    }

    fun lock() { _state.value = AdminUiState.Locked }
}
