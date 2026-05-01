package com.onthaset.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.onthaset.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Wrapper around Google Play BillingClient. Handles two product types:
 *   SUBS  — the $2.99/mo subscription (4 posts per cycle).
 *   INAPP — a single-event $0.99 one-time post pass.
 *
 * Subscription purchases are acknowledged so Play doesn't auto-refund. INAPP purchases
 * for the single-post pass are *consumed* immediately (so the user can buy it again
 * the next time they want to post). Each consumed purchase grants one credit via
 * PostCreditsRepository.
 *
 * Server-side validation is intentionally out of scope for v1 — we trust the BillingClient
 * signature. Production should add a Play Real-Time Developer Notifications webhook.
 */
@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val postCredits: PostCreditsRepository,
) : PurchasesUpdatedListener {

    private val subscriptionId: String = BuildConfig.BILLING_SUBSCRIPTION_PRODUCT_ID
    private val singlePostId: String = BuildConfig.BILLING_SINGLE_POST_PRODUCT_ID

    val isSubscriptionConfigured: Boolean = subscriptionId.isNotBlank()
    val isSinglePostConfigured: Boolean = singlePostId.isNotBlank()

    /** True if at least one of the two products is configured. */
    val isConfigured: Boolean = isSubscriptionConfigured || isSinglePostConfigured

    private val client: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _events = MutableStateFlow<BillingEvent?>(null)
    val events: StateFlow<BillingEvent?> = _events.asStateFlow()

    private suspend fun ensureConnected(): Boolean = suspendCancellableCoroutine { cont ->
        if (client.isReady) {
            cont.resume(true); return@suspendCancellableCoroutine
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
            }
            override fun onBillingServiceDisconnected() {
                if (cont.isActive) cont.resume(false)
            }
        })
    }

    suspend fun fetchSubscription(): ProductDetails? = fetchProduct(subscriptionId, BillingClient.ProductType.SUBS)

    suspend fun fetchSinglePost(): ProductDetails? = fetchProduct(singlePostId, BillingClient.ProductType.INAPP)

    private suspend fun fetchProduct(id: String, type: String): ProductDetails? {
        if (id.isBlank()) return null
        if (!ensureConnected()) return null
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(type)
                        .build()
                )
            )
            .build()
        val result = client.queryProductDetails(params)
        return result.productDetailsList?.firstOrNull()
    }

    fun launchSubscription(activity: Activity, product: ProductDetails) {
        val offer = product.subscriptionOfferDetails?.firstOrNull() ?: return
        client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .setOfferToken(offer.offerToken)
                            .build()
                    )
                )
                .build(),
        )
    }

    fun launchSinglePost(activity: Activity, product: ProductDetails) {
        client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .build()
                    )
                )
                .build(),
        )
    }

    suspend fun activeSubscription(): Boolean {
        if (!isSubscriptionConfigured) return false
        if (!ensureConnected()) return false
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = client.queryPurchasesAsync(params)
        return result.purchasesList.any { p ->
            p.purchaseState == Purchase.PurchaseState.PURCHASED &&
                p.products.contains(subscriptionId)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        scope.launch { handlePurchased(purchase) }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _events.value = BillingEvent.Cancelled
            else ->
                _events.value = BillingEvent.Error(result.debugMessage.ifBlank { "Billing error ${result.responseCode}" })
        }
    }

    private suspend fun handlePurchased(purchase: Purchase) {
        val isSinglePost = purchase.products.any { it == singlePostId }
        if (isSinglePost) {
            // Consume so the user can re-buy. Grant a credit only after consumption succeeds.
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val consumeResult = client.consumePurchase(consumeParams)
            if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                postCredits.grant(1)
                _events.value = BillingEvent.SinglePostGranted
            } else {
                _events.value = BillingEvent.Error("Could not redeem purchase — contact support.")
            }
        } else {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                client.acknowledgePurchase(params) { /* fire-and-forget */ }
            }
            _events.value = BillingEvent.SubscriptionPurchased(purchase.purchaseToken)
        }
    }

    fun clearEvent() { _events.value = null }
}

sealed interface BillingEvent {
    data class SubscriptionPurchased(val token: String) : BillingEvent
    data object SinglePostGranted : BillingEvent
    data object Cancelled : BillingEvent
    data class Error(val message: String) : BillingEvent
}
