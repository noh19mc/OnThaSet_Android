package com.onthaset.app.directory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DirectoryUiState {
    data object Loading : DirectoryUiState
    data class Ready(val ads: List<BusinessAd>) : DirectoryUiState
    data class Error(val message: String) : DirectoryUiState
}

@HiltViewModel
class DirectoryViewModel @Inject constructor(
    private val repo: DirectoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<DirectoryUiState>(DirectoryUiState.Loading)
    val state: StateFlow<DirectoryUiState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = DirectoryUiState.Loading
        runCatching { repo.activeAds() }
            .onSuccess { _state.value = DirectoryUiState.Ready(sponsoredFirst(it)) }
            .onFailure { _state.value = DirectoryUiState.Error(it.message ?: "Failed to load") }
    }

    /** Sponsored / premium plan ads float to the top (matches iOS sort). */
    private fun sponsoredFirst(ads: List<BusinessAd>): List<BusinessAd> = ads.sortedWith(
        compareByDescending<BusinessAd> { it.sponsored == true }
            .thenByDescending { it.plan == "premium" }
            .thenByDescending { it.plan == "featured" }
            .thenBy { it.businessName.lowercase() }
    )
}
