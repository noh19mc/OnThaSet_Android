package com.onthaset.app.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PublicProfileUiState {
    data object Loading : PublicProfileUiState
    data class Ready(val profile: UserProfile) : PublicProfileUiState
    data object NotFound : PublicProfileUiState
    data class Error(val message: String) : PublicProfileUiState
}

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val profiles: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<PublicProfileUiState>(PublicProfileUiState.Loading)
    val state: StateFlow<PublicProfileUiState> = _state.asStateFlow()

    fun load(userId: String) = viewModelScope.launch {
        _state.value = PublicProfileUiState.Loading
        runCatching { profiles.byUserId(userId) }
            .onSuccess { p ->
                _state.value = if (p == null) PublicProfileUiState.NotFound else PublicProfileUiState.Ready(p)
            }
            .onFailure { _state.value = PublicProfileUiState.Error(it.message ?: "Failed") }
    }
}
