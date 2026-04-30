package com.onthaset.app.events

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.auth.AuthRepository
import com.onthaset.app.auth.AuthState
import com.onthaset.app.imaging.Buckets
import com.onthaset.app.imaging.StorageRepository
import com.onthaset.app.imaging.toCompressedJpeg
import com.onthaset.app.profile.ProfileRepository
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

data class CreateEventState(
    val isUploading: Boolean = false,
    val error: String? = null,
    val flyerUri: Uri? = null,
    val justSaved: Boolean = false,
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val events: EventsRepository,
    private val profiles: ProfileRepository,
    private val auth: AuthRepository,
    private val storage: StorageRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventState())
    val state: StateFlow<CreateEventState> = _state.asStateFlow()

    fun setFlyer(uri: Uri?) { _state.value = _state.value.copy(flyerUri = uri) }
    fun reset() { _state.value = CreateEventState() }

    fun submit(
        title: String,
        date: Instant,
        category: EventCategory,
        venue: String,
        street: String,
        city: String,
        state: String,
        zip: String,
        details: String,
        price: String,
    ) = viewModelScope.launch {
        if (title.isBlank()) {
            _state.value = _state.value.copy(error = "Add a title.")
            return@launch
        }
        if (city.isBlank() || state.isBlank()) {
            _state.value = _state.value.copy(error = "City and state are required.")
            return@launch
        }
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
        if (signedIn == null) {
            _state.value = _state.value.copy(error = "Sign in to post.")
            return@launch
        }

        _state.value = _state.value.copy(isUploading = true, error = null)
        runCatching {
            // Pipe-delimited location matches the iOS schema; the iOS calendar parses parts[3]
            // (state) for state filtering on the national map.
            val combinedLocation = listOf(venue, street, city, state, zip).joinToString("|")

            val flyerUrl: String? = _state.value.flyerUri?.let { uri ->
                val bytes = withContext(Dispatchers.IO) {
                    appContext.contentResolver.toCompressedJpeg(uri)
                }
                val fileName = "flyer-${signedIn.userId}-${System.currentTimeMillis()}.jpg"
                storage.upload(Buckets.EVENT_FLYERS, fileName, bytes)
            }

            val displayName = profiles.byUserId(signedIn.userId)?.displayName?.ifBlank { null }
                ?: signedIn.email?.substringBefore("@")
                ?: "Anonymous"

            events.create(
                title = title.trim(),
                date = date,
                category = category.raw,
                locationName = combinedLocation,
                details = details.trim(),
                price = price.trim().ifBlank { "0.00" },
                latitude = 0.0,
                longitude = 0.0,
                postedByUserId = signedIn.userId,
                postedByName = displayName,
                imageUrl = flyerUrl,
            )
        }.onFailure { e ->
            _state.value = _state.value.copy(isUploading = false, error = e.message ?: "Post failed")
        }.onSuccess {
            _state.value = CreateEventState(justSaved = true)
        }
    }
}
