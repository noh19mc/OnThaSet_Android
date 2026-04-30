package com.onthaset.app.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.onthaset.app.auth.AuthRepository
import com.onthaset.app.auth.AuthState
import com.onthaset.app.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isLoading: Boolean = true,
    val product: ProductDetails? = null,
    val priceText: String? = null,
    val billingPeriodText: String? = null,
    val notConfigured: Boolean = false,
    val alreadySubscribed: Boolean = false,
    val justSubscribed: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: BillingRepository,
    private val profiles: ProfileRepository,
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallUiState())
    val state: StateFlow<PaywallUiState> = _state.asStateFlow()

    init {
        if (!billing.isConfigured) {
            _state.value = PaywallUiState(isLoading = false, notConfigured = true)
        } else {
            load()
            observeEvents()
        }
    }

    private fun load() = viewModelScope.launch {
        _state.value = PaywallUiState(isLoading = true)
        if (billing.activeSubscription()) {
            _state.value = PaywallUiState(isLoading = false, alreadySubscribed = true)
            // Sync to backend in case user re-installed without us knowing.
            syncSubscriptionFlag(true)
            return@launch
        }
        val product = billing.fetchProduct()
        if (product == null) {
            _state.value = PaywallUiState(isLoading = false, error = "No subscription product found.")
            return@launch
        }
        val offer = product.subscriptionOfferDetails?.firstOrNull()
        val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        _state.value = PaywallUiState(
            isLoading = false,
            product = product,
            priceText = phase?.formattedPrice,
            billingPeriodText = humanReadableBillingPeriod(phase?.billingPeriod),
        )
    }

    private fun observeEvents() = viewModelScope.launch {
        billing.events.collect { event ->
            when (event) {
                is BillingEvent.Purchased -> {
                    syncSubscriptionFlag(true)
                    _state.value = _state.value.copy(justSubscribed = true)
                    billing.clearEvent()
                }
                BillingEvent.Cancelled -> billing.clearEvent()
                is BillingEvent.Error -> {
                    _state.value = _state.value.copy(error = event.message)
                    billing.clearEvent()
                }
                null -> Unit
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val product = _state.value.product ?: return
        billing.launchPurchase(activity, product)
    }

    private fun syncSubscriptionFlag(active: Boolean) = viewModelScope.launch {
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
            ?: return@launch
        runCatching { profiles.setSubscriptionActive(signedIn.userId, active) }
    }

    private fun humanReadableBillingPeriod(iso: String?): String? = when (iso) {
        "P1W" -> "weekly"
        "P1M" -> "monthly"
        "P3M" -> "every 3 months"
        "P6M" -> "every 6 months"
        "P1Y" -> "yearly"
        else -> iso
    }
}
