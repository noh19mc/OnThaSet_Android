package com.onthaset.app.bikes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.onthaset.app.bikes.BikeBuild
import com.onthaset.app.bikes.BikesUiState
import com.onthaset.app.bikes.BikesViewModel

private val Yellow = Color(0xFFFFD600)
private val CardBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikesScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    viewModel: BikesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Bike Builds", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Yellow, contentColor = Color.Black) {
                Icon(Icons.Filled.Add, contentDescription = "New build")
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding),
        ) {
            when (val s = state) {
                BikesUiState.Loading -> CenteredLoader()
                is BikesUiState.Error -> CenteredText("Error: ${s.message}", Color(0xFFFF6B6B))
                is BikesUiState.Ready ->
                    if (s.builds.isEmpty()) CenteredText("No bike builds yet — be first to post.", Color.Gray)
                    else BikesList(s.builds)
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter)) { AdMobBanner() }
        }
    }
}

@Composable
private fun BikesList(builds: List<BikeBuild>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items = builds, key = { it.id ?: it.modificationTitle + it.createdAt.toString() }) { b ->
            BuildCard(b)
        }
    }
}

@Composable
private fun BuildCard(b: BikeBuild) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(b.modificationTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            val bikeLine = listOf(b.bikeYear, b.bikeMake, b.bikeModel).filter { it.isNotBlank() }.joinToString(" ")
            if (bikeLine.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(bikeLine, color = Yellow, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledImage(label = "BEFORE", url = b.beforeImageUrl, modifier = Modifier.weight(1f))
                LabeledImage(label = "AFTER", url = b.afterImageUrl, modifier = Modifier.weight(1f))
            }
            if (b.note.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(b.note, color = Color.LightGray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun LabeledImage(label: String, url: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        AsyncImage(
            model = url.ifBlank { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
        )
    }
}

@Composable
private fun CenteredLoader() {
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
