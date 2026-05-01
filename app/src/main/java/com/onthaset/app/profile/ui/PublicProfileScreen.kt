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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.profile.PublicProfileUiState
import com.onthaset.app.profile.PublicProfileViewModel
import com.onthaset.app.profile.UserProfile

private val Yellow = Color(0xFFFFD600)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onPostEvent: () -> Unit,
    onUploadPhoto: () -> Unit,
    onPostBuild: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) { viewModel.load(userId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats by viewModel.statsFlow.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Rider", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(padding)) {
            when (val s = state) {
                PublicProfileUiState.Loading ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Yellow)
                    }
                PublicProfileUiState.NotFound -> Centered("Rider hasn't set up a profile yet.")
                is PublicProfileUiState.Error -> Centered(s.message)
                is PublicProfileUiState.Ready -> Body(
                    p = s.profile,
                    stats = stats,
                    onPostEvent = onPostEvent,
                    onUploadPhoto = onUploadPhoto,
                    onPostBuild = onPostBuild,
                )
            }
        }
    }
}

@Composable
private fun Body(
    p: UserProfile,
    stats: com.onthaset.app.profile.ProfileStats,
    onPostEvent: () -> Unit,
    onUploadPhoto: () -> Unit,
    onPostBuild: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
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

        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                p.displayName.ifBlank { "Rider" },
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
            )
            if (p.hometown.isNotBlank()) Text(p.hometown, color = Yellow, fontSize = 14.sp)
            if (p.bio.isNotBlank()) Text(p.bio, color = Color.LightGray, fontSize = 14.sp)
        }

        ProfileStatsGrid(stats = stats, modifier = Modifier.padding(horizontal = 20.dp))
        RidingInfoCard(profile = p, modifier = Modifier.padding(horizontal = 20.dp))
        QuickActionTiles(
            onPostEvent = onPostEvent,
            onUploadPhoto = onUploadPhoto,
            onPostBuild = onPostBuild,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Header("Social")
            ReadField("Instagram", p.instagramHandle)
            ReadField("TikTok", p.tiktokHandle)
            ReadField("YouTube", p.youtubeChannel)
            ReadField("Facebook", p.facebookHandle)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Header(title: String) {
    Text(title.uppercase(), color = Yellow, fontWeight = FontWeight.Black, fontSize = 13.sp)
}

@Composable
private fun ReadField(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun Centered(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Gray, fontSize = 14.sp)
    }
}
