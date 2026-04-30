package com.onthaset.app.eventphotos

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.auth.AuthRepository
import com.onthaset.app.auth.AuthState
import com.onthaset.app.imaging.Buckets
import com.onthaset.app.imaging.StorageRepository
import com.onthaset.app.imaging.toCompressedJpeg
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import javax.inject.Inject

sealed interface EventPhotosUiState {
    data object Loading : EventPhotosUiState
    data class Ready(val photos: List<EventPhoto>) : EventPhotosUiState
    data class Error(val message: String) : EventPhotosUiState
}

data class CreateEventPhotoState(
    val isUploading: Boolean = false,
    val error: String? = null,
    val pickedUri: Uri? = null,
    val justSaved: Boolean = false,
)

@HiltViewModel
class EventPhotosViewModel @Inject constructor(
    private val repo: EventPhotosRepository,
    private val auth: AuthRepository,
    private val storage: StorageRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow<EventPhotosUiState>(EventPhotosUiState.Loading)
    val state: StateFlow<EventPhotosUiState> = _state.asStateFlow()

    private val _create = MutableStateFlow(CreateEventPhotoState())
    val create: StateFlow<CreateEventPhotoState> = _create.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = EventPhotosUiState.Loading
        runCatching { repo.feed() }
            .onSuccess { _state.value = EventPhotosUiState.Ready(it) }
            .onFailure { _state.value = EventPhotosUiState.Error(it.message ?: "Failed to load") }
    }

    fun setPicked(uri: Uri?) { _create.value = _create.value.copy(pickedUri = uri) }
    fun resetCreate() { _create.value = CreateEventPhotoState() }

    fun submit(
        eventName: String,
        eventDate: Instant,
        location: String,
        caption: String,
    ) = viewModelScope.launch {
        if (eventName.isBlank()) {
            _create.value = _create.value.copy(error = "Add an event name.")
            return@launch
        }
        val uri = _create.value.pickedUri
        if (uri == null) {
            _create.value = _create.value.copy(error = "Pick a photo.")
            return@launch
        }
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
        if (signedIn == null) {
            _create.value = _create.value.copy(error = "Sign in to upload.")
            return@launch
        }

        _create.value = _create.value.copy(isUploading = true, error = null)
        runCatching {
            val bytes = withContext(Dispatchers.IO) {
                appContext.contentResolver.toCompressedJpeg(uri)
            }
            val fileName = "photo-${signedIn.userId}-${System.currentTimeMillis()}.jpg"
            val url = storage.upload(Buckets.EVENT_PHOTOS, fileName, bytes)
            repo.create(
                uploadedBy = signedIn.userId,
                eventName = eventName.trim(),
                eventDate = eventDate,
                location = location.trim(),
                caption = caption.trim(),
                imageUrl = url,
            )
        }.onFailure { e ->
            _create.value = _create.value.copy(isUploading = false, error = e.message ?: "Upload failed")
        }.onSuccess {
            _create.value = CreateEventPhotoState(justSaved = true)
            load()
        }
    }
}
