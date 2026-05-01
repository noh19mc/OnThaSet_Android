package com.onthaset.app.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LegalAcceptanceViewModel @Inject constructor(
    private val repo: LegalAcceptanceRepository,
) : ViewModel() {

    val accepted: StateFlow<Boolean?> = repo.accepted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null, // null = still loading from DataStore
    )

    fun accept() = viewModelScope.launch { repo.accept() }
}
