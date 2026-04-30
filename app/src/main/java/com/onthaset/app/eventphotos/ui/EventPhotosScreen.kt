package com.onthaset.app.eventphotos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.onthaset.app.ads.AdMobBanner
import com.onthaset.app.eventphotos.EventPhoto
import com.onthaset.app.eventphotos.EventPhotosUiState
import com.onthaset.app.eventphotos.EventPhotosViewModel
import com.onthaset.app.events.formatEventDay

private val Yellow = Color(0xFFFFD600)
private val CardBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPhotosScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    viewModel: EventPhotosViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Ride Photos", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Yellow, contentColor = Color.Black) {
                Icon(Icons.Filled.Add, contentDescription = "Upload photo")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(padding)) {
            when (val s = state) {
                EventPhotosUiState.Loading -> Loader()
                is EventPhotosUiState.Error -> CenteredText("Error: ${s.message}", Color(0xFFFF6B6B))
                is EventPhotosUiState.Ready ->
                    if (s.photos.isEmpty()) CenteredText("No ride photos yet — be first.", Color.Gray)
                    else PhotoFeed(s.photos)
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter)) { AdMobBanner() }
        }
    }
}

@Composable
private fun PhotoFeed(photos: List<EventPhoto>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items = photos, key = { it.id ?: it.imageUrl }) { p -> PhotoCard(p) }
    }
}

@Composable
private fun PhotoCard(p: EventPhoto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            AsyncImage(
                model = p.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(p.eventName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (p.eventDate != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(p.eventDate.formatEventDay(), color = Yellow, fontSize = 12.sp)
                }
                if (p.location.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(p.location, color = Color.LightGray, fontSize = 12.sp)
                }
                if (p.caption.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(p.caption, color = Color.LightGray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun Loader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Yellow)
    }
}

@Composable
private fun CenteredText(text: String, color: Color) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = color, fontSize = 14.sp)
    }
}
