package com.onthaset.app.directory

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
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

enum class AdPlan(val raw: String, val title: String, val price: String, val perks: String) {
    Basic("basic", "Basic", "$19.99/mo", "Standard listing in directory."),
    Featured("featured", "Featured", "$39.99/mo", "Highlighted card · floats above Basic."),
    Premium("premium", "Premium", "$79.99/mo", "Top placement · 👑 badge · always shown."),
}

data class SubmitAdState(
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val justSubmitted: Boolean = false,
    val flyerUri: Uri? = null,
)

@HiltViewModel
class SubmitAdViewModel @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: StorageRepository,
    private val auth: AuthRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitAdState())
    val state: StateFlow<SubmitAdState> = _state.asStateFlow()

    fun setFlyer(uri: Uri?) { _state.value = _state.value.copy(flyerUri = uri) }
    fun reset() { _state.value = SubmitAdState() }

    fun submit(
        businessName: String,
        tagline: String,
        category: String,
        plan: AdPlan,
        phone: String,
        websiteUrl: String,
        address: String,
    ) = viewModelScope.launch {
        if (businessName.isBlank()) {
            _state.value = _state.value.copy(error = "Business name is required.")
            return@launch
        }
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
        if (signedIn == null) {
            _state.value = _state.value.copy(error = "Sign in to submit an ad.")
            return@launch
        }
        _state.value = _state.value.copy(isSubmitting = true, error = null)
        runCatching {
            val imageUrl: String? = _state.value.flyerUri?.let { uri ->
                val bytes = withContext(Dispatchers.IO) {
                    appContext.contentResolver.toCompressedJpeg(uri)
                }
                val fileName = "ad-${signedIn.userId}-${System.currentTimeMillis()}.jpg"
                storage.upload(Buckets.AD_BANNERS, fileName, bytes)
            }
            postgrest.from("ads").insert(
                AdInsert(
                    businessName = businessName.trim(),
                    tagline = tagline.trim(),
                    category = category.trim(),
                    phone = phone.trim().ifBlank { null },
                    websiteUrl = websiteUrl.trim().ifBlank { null },
                    address = address.trim().ifBlank { null },
                    imageUrl = imageUrl,
                    advertiserEmail = signedIn.email,
                    plan = plan.raw,
                    status = "pending",
                    paymentStatus = "unpaid",
                )
            )
        }.onFailure { e ->
            _state.value = _state.value.copy(isSubmitting = false, error = e.message ?: "Submit failed")
        }.onSuccess {
            _state.value = SubmitAdState(justSubmitted = true)
        }
    }
}

@Serializable
internal data class AdInsert(
    @SerialName("business_name") val businessName: String,
    val tagline: String,
    val category: String,
    val phone: String?,
    @SerialName("website_url") val websiteUrl: String?,
    val address: String?,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("advertiser_email") val advertiserEmail: String?,
    val plan: String,
    val status: String,
    @SerialName("payment_status") val paymentStatus: String,
)
