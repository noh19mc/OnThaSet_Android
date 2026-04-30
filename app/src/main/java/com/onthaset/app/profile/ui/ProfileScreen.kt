package com.onthaset.app.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.profile.ProfileUiState
import com.onthaset.app.profile.ProfileViewModel
import com.onthaset.app.profile.UserProfile

private val Yellow = Color(0xFFFFD600)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
                actions = {
                    if (state is ProfileUiState.Ready) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Yellow)
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding),
        ) {
            when (val s = state) {
                ProfileUiState.Loading -> Loader()
                ProfileUiState.NotSignedIn -> Centered("Sign in to see your profile.")
                is ProfileUiState.Error -> ErrorPane(s.message, onRetry = viewModel::load)
                is ProfileUiState.Ready -> ProfileBody(s.profile)
            }
        }
    }
}

@Composable
private fun ProfileBody(p: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.5f)
                    .background(Color.DarkGray),
            ) {
                if (!p.backgroundImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = p.backgroundImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            AsyncImage(
                model = p.profileImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .offset(y = 48.dp)
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
            )
        }
        Spacer(Modifier.height(48.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            p.displayName.ifBlank { "Rider" },
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
        )
        if (p.hometown.isNotBlank()) Text(p.hometown, color = Yellow, fontSize = 14.sp)
        if (p.bio.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(p.bio, color = Color.LightGray, fontSize = 14.sp)
        }

        Spacer(Modifier.height(8.dp))

        Section("Riding") {
            Field("Club", p.club)
            Field("Bike", p.favoriteRide)
            Field("Riding Since", p.ridingSince)
            Field("Preferred Ride", p.preferredRideType)
            Field("Favorite Route", p.favoriteRoute)
        }

        Section("Social") {
            Field("Instagram", p.instagramHandle)
            Field("TikTok", p.tiktokHandle)
            Field("YouTube", p.youtubeChannel)
            Field("Facebook", p.facebookHandle)
        }

        Section("Account") {
            Field("Email", p.email)
        }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title.uppercase(), color = Yellow, fontWeight = FontWeight.Black, fontSize = 13.sp)
        content()
    }
}

@Composable
private fun Field(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun Loader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Yellow)
    }
}

@Composable
private fun Centered(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Gray)
    }
}

@Composable
private fun ErrorPane(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Couldn't load profile", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(message, color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("Try Again", color = Yellow) }
        }
    }
}
