package com.onthaset.app.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.auth.AuthRepository
import com.onthaset.app.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Ready(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data object NotSignedIn : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profiles: ProfileRepository,
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ProfileUiState.Loading
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
        if (signedIn == null) {
            _state.value = ProfileUiState.NotSignedIn
            return@launch
        }
        runCatching { profiles.ensure(signedIn.userId, signedIn.email.orEmpty()) }
            .onSuccess { _state.value = ProfileUiState.Ready(it) }
            .onFailure { _state.value = ProfileUiState.Error(it.message ?: "Failed to load profile") }
    }

    fun save(update: ProfileUpdate, onDone: () -> Unit) = viewModelScope.launch {
        val current = _state.value
        if (current !is ProfileUiState.Ready) return@launch
        _saving.value = true
        runCatching { profiles.update(current.profile.appleUserId, update) }
            .onSuccess {
                _state.update { ProfileUiState.Ready(applyUpdate(current.profile, update)) }
                onDone()
            }
            .onFailure { e -> _state.value = ProfileUiState.Error(e.message ?: "Save failed") }
        _saving.value = false
    }

    private fun applyUpdate(p: UserProfile, u: ProfileUpdate) = p.copy(
        displayName = u.displayName,
        bio = u.bio,
        hometown = u.hometown,
        club = u.club,
        favoriteRide = u.favoriteRide,
        ridingSince = u.ridingSince,
        preferredRideType = u.preferredRideType,
        favoriteRoute = u.favoriteRoute,
        instagramHandle = u.instagramHandle,
        tiktokHandle = u.tiktokHandle,
        youtubeChannel = u.youtubeChannel,
        facebookHandle = u.facebookHandle,
    )
}
