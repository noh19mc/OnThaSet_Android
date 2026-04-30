package com.onthaset.app.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.profile.ProfileUiState
import com.onthaset.app.profile.ProfileUpdate
import com.onthaset.app.profile.ProfileViewModel
import com.onthaset.app.profile.UserProfile

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val profile = (state as? ProfileUiState.Ready)?.profile

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Cancel", color = Yellow) } },
            )
        },
    ) { padding ->
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Yellow)
            }
            return@Scaffold
        }
        EditForm(
            initial = profile,
            saving = saving,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding),
            onSave = { update -> viewModel.save(update, onSaved) },
        )
    }
}

@Composable
private fun EditForm(
    initial: UserProfile,
    saving: Boolean,
    modifier: Modifier,
    onSave: (ProfileUpdate) -> Unit,
) {
    var displayName by remember { mutableStateOf(initial.displayName) }
    var bio by remember { mutableStateOf(initial.bio) }
    var hometown by remember { mutableStateOf(initial.hometown) }
    var club by remember { mutableStateOf(initial.club) }
    var favoriteRide by remember { mutableStateOf(initial.favoriteRide) }
    var ridingSince by remember { mutableStateOf(initial.ridingSince) }
    var preferredRideType by remember { mutableStateOf(initial.preferredRideType) }
    var favoriteRoute by remember { mutableStateOf(initial.favoriteRoute) }
    var instagram by remember { mutableStateOf(initial.instagramHandle) }
    var tiktok by remember { mutableStateOf(initial.tiktokHandle) }
    var youtube by remember { mutableStateOf(initial.youtubeChannel) }
    var facebook by remember { mutableStateOf(initial.facebookHandle) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionHeader("About")
        DarkField("Display Name", displayName) { displayName = it }
        DarkField("Bio", bio, singleLine = false) { bio = it }
        DarkField("Hometown", hometown) { hometown = it }

        SectionHeader("Riding")
        DarkField("Club", club) { club = it }
        DarkField("Bike", favoriteRide) { favoriteRide = it }
        DarkField("Riding Since (year)", ridingSince) { ridingSince = it }
        DarkField("Preferred Ride Type", preferredRideType) { preferredRideType = it }
        DarkField("Favorite Route", favoriteRoute) { favoriteRoute = it }

        SectionHeader("Social")
        DarkField("Instagram", instagram) { instagram = it }
        DarkField("TikTok", tiktok) { tiktok = it }
        DarkField("YouTube", youtube) { youtube = it }
        DarkField("Facebook", facebook) { facebook = it }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                onSave(
                    ProfileUpdate(
                        displayName = displayName.trim(),
                        bio = bio.trim(),
                        hometown = hometown.trim(),
                        club = club.trim(),
                        favoriteRide = favoriteRide.trim(),
                        ridingSince = ridingSince.trim(),
                        preferredRideType = preferredRideType.trim(),
                        favoriteRoute = favoriteRoute.trim(),
                        instagramHandle = instagram.trim(),
                        tiktokHandle = tiktok.trim(),
                        youtubeChannel = youtube.trim(),
                        facebookHandle = facebook.trim(),
                    )
                )
            },
            enabled = !saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
        ) {
            if (saving) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title.uppercase(), color = Yellow, fontWeight = FontWeight.Black, fontSize = 13.sp)
}

@Composable
private fun DarkField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = Color.Gray) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = FieldBg,
            unfocusedContainerColor = FieldBg,
            cursorColor = Yellow,
            focusedBorderColor = Yellow,
            unfocusedBorderColor = Yellow.copy(alpha = 0.3f),
            focusedLabelColor = Yellow,
        ),
    )
}
