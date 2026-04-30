package com.onthaset.app.bikes

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface BikesUiState {
    data object Loading : BikesUiState
    data class Ready(val builds: List<BikeBuild>) : BikesUiState
    data class Error(val message: String) : BikesUiState
}

data class CreateBikeBuildState(
    val isUploading: Boolean = false,
    val error: String? = null,
    val beforeUri: Uri? = null,
    val afterUri: Uri? = null,
    val justSavedId: String? = null,
)

@HiltViewModel
class BikesViewModel @Inject constructor(
    private val repo: BikesRepository,
    private val auth: AuthRepository,
    private val storage: StorageRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow<BikesUiState>(BikesUiState.Loading)
    val state: StateFlow<BikesUiState> = _state.asStateFlow()

    private val _create = MutableStateFlow(CreateBikeBuildState())
    val create: StateFlow<CreateBikeBuildState> = _create.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = BikesUiState.Loading
        runCatching { repo.feed() }
            .onSuccess { _state.value = BikesUiState.Ready(it) }
            .onFailure { _state.value = BikesUiState.Error(it.message ?: "Failed to load") }
    }

    fun setBefore(uri: Uri?) { _create.value = _create.value.copy(beforeUri = uri) }
    fun setAfter(uri: Uri?) { _create.value = _create.value.copy(afterUri = uri) }
    fun clearError() { _create.value = _create.value.copy(error = null) }
    fun resetCreate() { _create.value = CreateBikeBuildState() }

    fun submit(
        modificationTitle: String,
        note: String,
        bikeMake: String,
        bikeModel: String,
        bikeYear: String,
    ) = viewModelScope.launch {
        if (modificationTitle.isBlank()) {
            _create.value = _create.value.copy(error = "Add a title.")
            return@launch
        }
        val before = _create.value.beforeUri
        val after = _create.value.afterUri
        if (before == null || after == null) {
            _create.value = _create.value.copy(error = "Pick both a before and an after photo.")
            return@launch
        }
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
        if (signedIn == null) {
            _create.value = _create.value.copy(error = "Sign in to post a build.")
            return@launch
        }

        _create.value = _create.value.copy(isUploading = true, error = null)
        runCatching {
            val stamp = System.currentTimeMillis()
            val (beforeBytes, afterBytes) = withContext(Dispatchers.IO) {
                appContext.contentResolver.toCompressedJpeg(before) to
                    appContext.contentResolver.toCompressedJpeg(after)
            }
            val beforeUrl = storage.upload(
                Buckets.BIKE_PROGRESS,
                StoragePaths.bikeBefore(signedIn.userId, stamp),
                beforeBytes,
            )
            val afterUrl = storage.upload(
                Buckets.BIKE_PROGRESS,
                StoragePaths.bikeAfter(signedIn.userId, stamp),
                afterBytes,
            )
            repo.create(
                userId = signedIn.userId,
                modificationTitle = modificationTitle.trim(),
                note = note.trim(),
                beforeImageUrl = beforeUrl,
                afterImageUrl = afterUrl,
                bikeMake = bikeMake.trim(),
                bikeModel = bikeModel.trim(),
                bikeYear = bikeYear.trim(),
            )
        }.onFailure { e ->
            _create.value = _create.value.copy(isUploading = false, error = e.message ?: "Post failed")
        }.onSuccess {
            _create.value = CreateBikeBuildState(justSavedId = "ok")
            load()
        }
    }
}
