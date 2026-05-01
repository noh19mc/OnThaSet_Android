package com.onthaset.app.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.auth.AuthRepository
import com.onthaset.app.auth.AuthState
import com.onthaset.app.imaging.Buckets
import com.onthaset.app.imaging.StoragePaths
import com.onthaset.app.imaging.StorageRepository
import com.onthaset.app.imaging.toCompressedJpeg
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Ready(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data object NotSignedIn : ProfileUiState
}

enum class ImageKind { Profile, Background }

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profiles: ProfileRepository,
    private val stats: ProfileStatsRepository,
    private val auth: AuthRepository,
    private val storage: StorageRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _stats = MutableStateFlow(ProfileStats.Empty)
    val statsFlow: StateFlow<ProfileStats> = _stats.asStateFlow()

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _uploading = MutableStateFlow<ImageKind?>(null)
    val uploading: StateFlow<ImageKind?> = _uploading.asStateFlow()

    /**
     * Heuristic for "needs the welcome wizard". Mirrors the iOS isProfileComplete check —
     * a brand-new user fresh out of ensure() has all three of these blank.
     */
    val needsSetup: StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false).also { flow ->
        viewModelScope.launch {
            _state.collect { s ->
                flow.value = (s as? ProfileUiState.Ready)?.profile?.let { p ->
                    p.bio.isBlank() && p.hometown.isBlank() && p.favoriteRide.isBlank()
                } ?: false
            }
        }
    }

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ProfileUiState.Loading
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
        if (signedIn == null) {
            _state.value = ProfileUiState.NotSignedIn
            return@launch
        }
        runCatching { profiles.ensure(signedIn.userId, signedIn.email.orEmpty()) }
            .onSuccess {
                _state.value = ProfileUiState.Ready(it)
                _stats.value = stats.forUser(signedIn.userId)
            }
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

    fun uploadImage(kind: ImageKind, uri: Uri) = viewModelScope.launch {
        val current = _state.value
        if (current !is ProfileUiState.Ready) return@launch
        _uploading.value = kind
        runCatching {
            val bytes = withContext(Dispatchers.IO) {
                appContext.contentResolver.toCompressedJpeg(uri)
            }
            val userId = current.profile.appleUserId
            val path = when (kind) {
                ImageKind.Profile -> StoragePaths.profile(userId)
                ImageKind.Background -> StoragePaths.background(userId)
            }
            val url = storage.upload(Buckets.PROFILE_IMAGES, path, bytes)
            // Bust CDN cache so the new image shows immediately
            val cacheBusted = "$url?v=${System.currentTimeMillis()}"
            when (kind) {
                ImageKind.Profile -> {
                    profiles.updateProfileImageUrl(userId, cacheBusted)
                    _state.update { ProfileUiState.Ready(current.profile.copy(profileImageUrl = cacheBusted)) }
                }
                ImageKind.Background -> {
                    profiles.updateBackgroundImageUrl(userId, cacheBusted)
                    _state.update { ProfileUiState.Ready(current.profile.copy(backgroundImageUrl = cacheBusted)) }
                }
            }
        }.onFailure { e ->
            _state.value = ProfileUiState.Error(e.message ?: "Upload failed")
        }
        _uploading.value = null
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
