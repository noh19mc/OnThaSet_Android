package com.onthaset.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.BuildConfig
import com.onthaset.app.ads.AdMobBanner
import com.onthaset.app.auth.AuthState
import com.onthaset.app.auth.AuthViewModel
import com.onthaset.app.auth.ui.OnThaSetShield
import com.onthaset.app.profile.ProfileViewModel

@Composable
fun HomeScreen(
    onOpenEvents: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenWeather: () -> Unit,
    onOpenBikes: () -> Unit,
    onOpenEventPhotos: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.authState.collectAsStateWithLifecycle()
    val needsSetup by profileViewModel.needsSetup.collectAsStateWithLifecycle()
    val signedIn = state as? AuthState.SignedIn

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        OnThaSetShield(size = 84.dp)
        Text(
            "Welcome",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
        )
        Text(
            signedIn?.email ?: "Guest mode",
            color = Color.Gray,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(4.dp))
        if (signedIn != null && needsSetup) {
            PrimaryTile("Finish Setting Up Your Profile", onOpenOnboarding)
        }
        PrimaryTile("Browse Events", onOpenEvents)
        SecondaryTile("National Run Calendar", onOpenCalendar)
        SecondaryTile("Ride Forecast", onOpenWeather)
        SecondaryTile("Bike Builds", onOpenBikes)
        SecondaryTile("Ride Photos", onOpenEventPhotos)
        SecondaryTile("Local Businesses", onOpenDirectory)
        if (signedIn != null) SecondaryTile("Subscription", onOpenPaywall)
        SecondaryTile("My Profile", onOpenProfile)
        if (BuildConfig.ADMIN_PIN.isNotBlank()) SecondaryTile("Admin", onOpenAdmin)
        SecondaryTile(
            label = if (signedIn != null) "Sign Out" else "Back to Sign In",
            onClick = onSignOut,
        )
        Spacer(Modifier.height(8.dp))
        AdMobBanner()
    }
}

private val Yellow = Color(0xFFFFD600)
private val TileHeight = 46.dp

@Composable
private fun PrimaryTile(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(TileHeight),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun SecondaryTile(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(TileHeight),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(label, color = Yellow, fontSize = 14.sp)
    }
}
