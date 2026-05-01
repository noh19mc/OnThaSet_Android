package com.onthaset.app.events

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.auth.AuthRepository
import com.onthaset.app.auth.AuthState
import com.onthaset.app.billing.PostCreditsRepository
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
    val justDeleted: Boolean = false,
    val needsSubscription: Boolean = false,
    /** Set when the screen is in edit mode — pre-populates the form. */
    val editing: Event? = null,
    val isLoadingEdit: Boolean = false,
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val events: EventsRepository,
    private val profiles: ProfileRepository,
    private val auth: AuthRepository,
    private val storage: StorageRepository,
    private val geocoder: Geocoder,
    private val postCredits: PostCreditsRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventState())
    val state: StateFlow<CreateEventState> = _state.asStateFlow()

    fun setFlyer(uri: Uri?) { _state.value = _state.value.copy(flyerUri = uri) }
    fun reset() { _state.value = CreateEventState() }

    fun loadForEdit(eventId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoadingEdit = true, error = null)
        runCatching { events.byId(eventId) }
            .onSuccess { e ->
                _state.value = _state.value.copy(isLoadingEdit = false, editing = e)
            }
            .onFailure {
                _state.value = _state.value.copy(isLoadingEdit = false, error = it.message ?: "Couldn't load event")
            }
    }

    fun delete(eventId: String) = viewModelScope.launch {
        runCatching { events.delete(eventId) }
            .onSuccess { _state.value = CreateEventState(justDeleted = true) }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "Delete failed") }
    }

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
        editingId: String? = null,
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

        // Match iOS: subscribers post freely; everyone else needs at least one
        // single-post credit (purchased via the $0.99 IAP). Edits don't burn a credit
        // since the original post already paid for it.
        val isEdit = editingId != null
        val profile = runCatching { profiles.byUserId(signedIn.userId) }.getOrNull()
        val hasSubscription = profile?.hasSubscription == true
        val hasCredit = !isEdit && !hasSubscription && postCredits.count() > 0
        if (!isEdit && !hasSubscription && !hasCredit) {
            _state.value = _state.value.copy(
                error = "Posting events requires a subscription or a single-post pass.",
                needsSubscription = true,
            )
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

            // Geocode the full street-level address so the event lands on the map. iOS
            // uses CLGeocoder; we use Nominatim. Best-effort — on failure we fall through
            // to (0, 0), matching iOS's behavior when CLGeocoder returns no placemark.
            val fullAddress = listOf(street, city, state, zip).filter { it.isNotBlank() }.joinToString(", ")
            val coords = geocoder.geocode(fullAddress)

            // For edits, fall back to the existing image URL when no new flyer was picked.
            val finalImageUrl = flyerUrl ?: _state.value.editing?.imageUrl

            if (editingId != null) {
                events.update(
                    id = editingId,
                    title = title.trim(),
                    date = date,
                    category = category.raw,
                    locationName = combinedLocation,
                    details = details.trim(),
                    price = price.trim().ifBlank { "0.00" },
                    latitude = coords?.latitude ?: 0.0,
                    longitude = coords?.longitude ?: 0.0,
                    imageUrl = finalImageUrl,
                )
            } else {
                events.create(
                    title = title.trim(),
                    date = date,
                    category = category.raw,
                    locationName = combinedLocation,
                    details = details.trim(),
                    price = price.trim().ifBlank { "0.00" },
                    latitude = coords?.latitude ?: 0.0,
                    longitude = coords?.longitude ?: 0.0,
                    postedByUserId = signedIn.userId,
                    postedByName = displayName,
                    imageUrl = flyerUrl,
                )
            }
        }.onFailure { e ->
            _state.value = _state.value.copy(isUploading = false, error = e.message ?: "Post failed")
        }.onSuccess {
            // Burn the credit only after the create actually lands. Edits don't cost.
            if (!isEdit && !hasSubscription) postCredits.consumeOne()
            _state.value = CreateEventState(justSaved = true)
        }
    }
}
