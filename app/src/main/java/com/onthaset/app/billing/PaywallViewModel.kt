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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isLoading: Boolean = true,
    val subscription: ProductDetails? = null,
    val subscriptionPrice: String? = null,
    val subscriptionPeriod: String? = null,
    val singlePost: ProductDetails? = null,
    val singlePostPrice: String? = null,
    val notConfigured: Boolean = false,
    val alreadySubscribed: Boolean = false,
    val justSubscribed: Boolean = false,
    val singlePostCredits: Int = 0,
    val justGrantedSinglePost: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: BillingRepository,
    private val profiles: ProfileRepository,
    private val auth: AuthRepository,
    private val postCredits: PostCreditsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallUiState())
    val state: StateFlow<PaywallUiState> = _state.asStateFlow()

    init {
        if (!billing.isConfigured) {
            _state.value = PaywallUiState(isLoading = false, notConfigured = true)
        } else {
            load()
            observeEvents()
            observeCredits()
        }
    }

    private fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        if (billing.activeSubscription()) {
            _state.value = _state.value.copy(isLoading = false, alreadySubscribed = true)
            syncSubscriptionFlag(true)
            return@launch
        }
        val sub = billing.fetchSubscription()
        val subOffer = sub?.subscriptionOfferDetails?.firstOrNull()
        val subPhase = subOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        val single = billing.fetchSinglePost()
        val singleOffer = single?.oneTimePurchaseOfferDetails

        _state.value = _state.value.copy(
            isLoading = false,
            subscription = sub,
            subscriptionPrice = subPhase?.formattedPrice,
            subscriptionPeriod = humanReadableBillingPeriod(subPhase?.billingPeriod),
            singlePost = single,
            singlePostPrice = singleOffer?.formattedPrice,
        )
    }

    private fun observeEvents() = viewModelScope.launch {
        billing.events.collect { event ->
            when (event) {
                is BillingEvent.SubscriptionPurchased -> {
                    syncSubscriptionFlag(true)
                    _state.value = _state.value.copy(justSubscribed = true)
                    billing.clearEvent()
                }
                BillingEvent.SinglePostGranted -> {
                    _state.value = _state.value.copy(justGrantedSinglePost = true)
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

    private fun observeCredits() = viewModelScope.launch {
        postCredits.credits.collect { count ->
            _state.value = _state.value.copy(singlePostCredits = count)
        }
    }

    fun launchSubscription(activity: Activity) {
        val product = _state.value.subscription ?: return
        billing.launchSubscription(activity, product)
    }

    fun launchSinglePost(activity: Activity) {
        val product = _state.value.singlePost ?: return
        billing.launchSinglePost(activity, product)
    }

    fun acknowledgeSinglePost() {
        _state.value = _state.value.copy(justGrantedSinglePost = false)
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
