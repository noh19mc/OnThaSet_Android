package com.onthaset.app.events.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.events.NearbyEvent
import com.onthaset.app.events.NearbyEventsViewModel
import com.onthaset.app.events.NearbyUiState
import com.onthaset.app.events.RadiusMiles
import com.onthaset.app.events.TimeRange
import com.onthaset.app.events.formatEventDay
import com.onthaset.app.events.formatEventTime

private val Yellow = Color(0xFFFFD600)
private val Surface10 = Color(0x14FFFFFF)
private val CardBg = Color(0x14FFFFFF)

@Composable
fun NearbyEventsScreen(
    onEventClick: (String) -> Unit,
    viewModel: NearbyEventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.onPermissionGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Time range chips
        ChipRow {
            TimeRange.entries.forEach { t ->
                FilterChip(
                    label = t.label,
                    selected = filter.time == t,
                    onClick = { viewModel.setTime(t) },
                )
            }
        }
        Text(
            "Search Radius: ${if (filter.radius == RadiusMiles.Max) "any distance" else "${filter.radius.miles} miles"}",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        ChipRow {
            RadiusMiles.entries.forEach { r ->
                FilterChip(
                    label = r.label,
                    selected = filter.radius == r,
                    onClick = { viewModel.setRadius(r) },
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val s = state) {
                NearbyUiState.NeedsPermission -> NeedsLocation {
                    locationPermission.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    )
                }
                NearbyUiState.Loading ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Yellow)
                    }
                is NearbyUiState.Error ->
                    Centered(s.message, Color(0xFFFF6B6B))
                is NearbyUiState.Ready -> if (s.events.isEmpty()) {
                    Empty(onExpand = viewModel::expandRadius, onShowAll = viewModel::showAll)
                } else {
                    NearbyList(s.events, onEventClick)
                }
            }
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = if (selected) Yellow else Surface10,
    ) {
        Text(
            label,
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun NearbyList(events: List<NearbyEvent>, onEventClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = events, key = { it.event.id ?: it.event.title + it.event.date.toString() }) { ne ->
            Card(
                onClick = { ne.event.id?.let(onEventClick) },
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!ne.event.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ne.event.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray),
                        )
                        Spacer(Modifier.size(10.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ne.event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${ne.event.date.formatEventDay()} · ${ne.event.date.formatEventTime()}",
                            color = Yellow,
                            fontSize = 12.sp,
                        )
                        if (ne.event.locationName.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                ne.event.locationName.split('|').filter { it.isNotBlank() }.joinToString(", "),
                                color = Color.LightGray,
                                fontSize = 12.sp,
                            )
                        }
                    }
                    Text(
                        "%.0f mi".format(ne.distanceMiles),
                        color = Yellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun NeedsLocation(onAllow: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📍", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text("Find rides near you", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Allow location to see motorcycle events within range.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = onAllow,
                shape = RoundedCornerShape(12.dp),
                color = Yellow,
            ) {
                Text(
                    "Allow Location",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun Empty(onExpand: () -> Unit, onShowAll: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📍", fontSize = 48.sp)
            Text("No events found", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("No events within this radius / time range.", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onExpand) { Text("EXPAND RADIUS", color = Yellow, fontWeight = FontWeight.Bold) }
                TextButton(onClick = onShowAll) { Text("SHOW ALL", color = Yellow, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun Centered(text: String, color: Color) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = color, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
