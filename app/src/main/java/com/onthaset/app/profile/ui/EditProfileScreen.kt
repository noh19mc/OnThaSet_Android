package com.onthaset.app.profile.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.onthaset.app.profile.ImageKind
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
            onPickProfileImage = { uri -> uri?.let { viewModel.uploadImage(ImageKind.Profile, it) } },
            onPickBackgroundImage = { uri -> uri?.let { viewModel.uploadImage(ImageKind.Background, it) } },
            uploading = viewModel.uploading.collectAsStateWithLifecycle().value,
        )
    }
}

@Composable
private fun EditForm(
    initial: UserProfile,
    saving: Boolean,
    uploading: ImageKind?,
    modifier: Modifier,
    onSave: (ProfileUpdate) -> Unit,
    onPickProfileImage: (android.net.Uri?) -> Unit,
    onPickBackgroundImage: (android.net.Uri?) -> Unit,
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

    val pickProfile = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> onPickProfileImage(uri) }
    val pickBackground = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> onPickBackgroundImage(uri) }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ImagePickers(
            profileUrl = initial.profileImageUrl,
            backgroundUrl = initial.backgroundImageUrl,
            uploading = uploading,
            onPickProfile = { pickProfile.launch(imageOnly) },
            onPickBackground = { pickBackground.launch(imageOnly) },
        )

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
private fun ImagePickers(
    profileUrl: String?,
    backgroundUrl: String?,
    uploading: ImageKind?,
    onPickProfile: () -> Unit,
    onPickBackground: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.5f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
                .clickable(enabled = uploading == null, onClick = onPickBackground),
        ) {
            if (backgroundUrl != null) {
                AsyncImage(
                    model = backgroundUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (uploading == ImageKind.Background) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Yellow)
                }
            }
            Text(
                "Tap to change cover",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0x80000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, top = 0.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .padding(3.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .clickable(enabled = uploading == null, onClick = onPickProfile),
        ) {
            if (profileUrl != null) {
                AsyncImage(
                    model = profileUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            }
            if (uploading == ImageKind.Profile) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Yellow, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
    Spacer(Modifier.height(48.dp)) // breathing room under the avatar
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
