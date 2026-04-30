package com.onthaset.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.onthaset.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Wrapper around Google Play BillingClient. Surfaces purchase results as a Flow that the
 * ViewModel can observe to react to a successful subscription. Acknowledgement is required
 * within 3 days or the purchase auto-refunds — handled inline here.
 *
 * Server-side validation is intentionally out of scope: for v1 we trust the client-side
 * BillingClient signature (Play guarantees authenticity) and write has_subscription via
 * the user's Supabase session. A real production deployment should add a webhook from
 * Play's Real-Time Developer Notifications + service-role upsert.
 */
@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : PurchasesUpdatedListener {

    private val productId: String = BuildConfig.BILLING_SUBSCRIPTION_PRODUCT_ID

    val isConfigured: Boolean = productId.isNotBlank()

    private val client: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

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

    suspend fun fetchProduct(): ProductDetails? {
        if (!isConfigured) return null
        if (!ensureConnected()) return null
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        val result = client.queryProductDetails(params)
        return result.productDetailsList?.firstOrNull()
    }

    fun launchPurchase(activity: Activity, product: ProductDetails) {
        val offer = product.subscriptionOfferDetails?.firstOrNull() ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offer.offerToken)
                        .build()
                )
            )
            .build()
        client.launchBillingFlow(activity, params)
    }

    suspend fun activeSubscription(): Boolean {
        if (!isConfigured) return false
        if (!ensureConnected()) return false
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = client.queryPurchasesAsync(params)
        return result.purchasesList.any { p ->
            p.purchaseState == Purchase.PurchaseState.PURCHASED &&
                p.products.contains(productId)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) acknowledge(purchase)
                        _events.value = BillingEvent.Purchased(purchase.purchaseToken)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _events.value = BillingEvent.Cancelled
            else ->
                _events.value = BillingEvent.Error(result.debugMessage.ifBlank { "Billing error ${result.responseCode}" })
        }
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(params) { /* fire and forget; Play retries on its own */ }
    }

    fun clearEvent() { _events.value = null }
}

sealed interface BillingEvent {
    data class Purchased(val token: String) : BillingEvent
    data object Cancelled : BillingEvent
    data class Error(val message: String) : BillingEvent
}
