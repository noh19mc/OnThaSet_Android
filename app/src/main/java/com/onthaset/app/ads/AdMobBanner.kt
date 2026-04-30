package com.onthaset.app.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.onthaset.app.BuildConfig

/**
 * Anchored banner that renders only when ADMOB_BANNER_UNIT_ID is configured. Without a
 * unit ID we render nothing rather than Google's test ad — saves vertical space in dev
 * builds and avoids confusion. Set the key in local.properties to enable.
 */
@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    val unitId = BuildConfig.ADMOB_BANNER_UNIT_ID
    if (unitId.isBlank()) return

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = unitId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
