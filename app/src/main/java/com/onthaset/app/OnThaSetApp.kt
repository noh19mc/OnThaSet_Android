package com.onthaset.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OnThaSetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // SDK init reads APPLICATION_ID from the manifest meta-data and is fast/no-op when
        // running with the official test app ID. Real banners only render if a real
        // ADMOB_BANNER_UNIT_ID is configured in local.properties.
        MobileAds.initialize(this) {}
    }
}
