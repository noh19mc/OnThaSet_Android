package com.onthaset.app.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.BuildConfig
import com.onthaset.app.R
import com.onthaset.app.ads.AdMobBanner
import com.onthaset.app.auth.AuthState
import com.onthaset.app.auth.AuthViewModel
import com.onthaset.app.auth.ui.OnThaSetShield
import com.onthaset.app.profile.ProfileViewModel

/**
 * "More" destination — landing for everything that doesn't fit in the bottom nav.
 * Hosts the iOS-style hero photo, an "Exit Demo" banner for guests, the secondary
 * destinations (Bikes / Photos / Directory / Subscription / Admin / Sign Out), and
 * a prominent ADVERTISE WITH US tile near the bottom matching the iOS card.
 */
@Composable
fun MoreScreen(
    onOpenBikes: () -> Unit,
    onOpenEventPhotos: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenSubmitAd: () -> Unit,
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val needsSetup by profileViewModel.needsSetup.collectAsStateWithLifecycle()
    val signedIn = authState as? AuthState.SignedIn
    val isGuest = signedIn == null // bottom nav only renders when guest mode is on, so this == guest

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        if (isGuest) {
            ExitDemoBanner(onSignOut = onSignOut)
            Spacer(Modifier.height(2.dp))
        }

        OnThaSetShield(size = 64.dp)

        // Hero — same image iOS shows on the landing carousel.
        Image(
            painter = painterResource(id = R.drawable.onthaset_hero),
            contentDescription = "On Tha Set",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp)),
        )

        Text("What's On Tha Set Nearby", color = Yellow, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(signedIn?.email ?: "Browsing as guest", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))

        if (signedIn != null && needsSetup) {
            PrimaryTile("Finish Setting Up Your Profile", onOpenOnboarding)
        }
        SecondaryTile("Bike Builds", onOpenBikes)
        SecondaryTile("Ride Photos", onOpenEventPhotos)
        SecondaryTile("Local Businesses", onOpenDirectory)
        if (signedIn != null) SecondaryTile("Subscription", onOpenPaywall)
        if (BuildConfig.ADMIN_PIN.isNotBlank()) SecondaryTile("Admin", onOpenAdmin)

        Spacer(Modifier.height(6.dp))
        AdvertiseWithUsTile(onClick = onOpenSubmitAd)

        SecondaryTile(
            label = if (signedIn != null) "Sign Out" else "Back to Sign In",
            onClick = onSignOut,
        )
        Spacer(Modifier.height(8.dp))
        AdMobBanner()
    }
}

@Composable
private fun ExitDemoBanner(onSignOut: () -> Unit) {
    Surface(
        onClick = onSignOut,
        shape = RoundedCornerShape(20.dp),
        color = Yellow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("←", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(Modifier.size(8.dp))
            Text(
                "Exit Demo — Sign In to Continue",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun AdvertiseWithUsTile(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFD600)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Yellow, RoundedCornerShape(14.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📣", fontSize = 18.sp)
                Spacer(Modifier.size(8.dp))
                Text("ADVERTISE WITH US", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Spacer(Modifier.size(8.dp))
                Text("›", color = Yellow, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Text(
                "Reach thousands of riders across the community",
                color = Color.White,
                fontSize = 12.sp,
            )
            Spacer(Modifier.size(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Plans from $19.99/mo · ",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                )
                Text("Basic ", color = Color.LightGray, fontSize = 11.sp)
                Text("⭐ Featured ", color = Color.LightGray, fontSize = 11.sp)
                Text("👑 Premium", color = Color.LightGray, fontSize = 11.sp)
            }
        }
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
