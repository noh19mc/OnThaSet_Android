package com.onthaset.app.events.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.onthaset.app.BuildConfig
import com.onthaset.app.events.CalendarUiState
import com.onthaset.app.events.CalendarViewModel
import com.onthaset.app.events.Event

private val Yellow = Color(0xFFFFD600)

// Center of contiguous US so first frame frames most events at once.
private val UsCenter = LatLng(39.8283, -98.5795)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarMapScreen(
    onEventClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CALENDAR MAP", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text(
                            "${com.onthaset.app.events.monthName(filter.month)} ${filter.year}",
                            color = Color.Gray,
                            fontSize = 11.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (BuildConfig.MAPS_API_KEY.isBlank()) {
                MapKeyMissing()
            } else {
                when (val s = state) {
                    CalendarUiState.Loading ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Yellow)
                        }
                    is CalendarUiState.Error -> Centered(s.message, Color(0xFFFF6B6B))
                    is CalendarUiState.Ready -> Map(events = s.events, onEventClick = onEventClick)
                }
            }
        }
    }
}

@Composable
private fun Map(events: List<Event>, onEventClick: (String) -> Unit) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(UsCenter, 3.5f)
    }
    val mapProperties = remember { MapProperties(mapType = MapType.NORMAL) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
    ) {
        events.forEach { event ->
            val cat = event.categoryEnum ?: return@forEach
            if (event.latitude == 0.0 && event.longitude == 0.0) return@forEach
            Marker(
                state = MarkerState(LatLng(event.latitude, event.longitude)),
                title = event.title,
                snippet = event.locationName,
                icon = BitmapDescriptorFactory.defaultMarker(hueFor(cat.pinColor)),
                onInfoWindowClick = { event.id?.let(onEventClick) },
            )
        }
    }
}

private fun hueFor(color: Color): Float {
    val argb = color.toArgb()
    val r = ((argb shr 16) and 0xff) / 255f
    val g = ((argb shr 8) and 0xff) / 255f
    val b = (argb and 0xff) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val d = max - min
    if (d == 0f) return BitmapDescriptorFactory.HUE_AZURE
    val h = when (max) {
        r -> ((g - b) / d) % 6
        g -> ((b - r) / d) + 2
        else -> ((r - g) / d) + 4
    }
    val deg = (h * 60f + 360f) % 360f
    return deg
}

@Composable
private fun MapKeyMissing() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MAP UNAVAILABLE", color = Yellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Add MAPS_API_KEY to local.properties to enable the National Calendar map.\n\n" +
                    "Get a key at console.cloud.google.com/google/maps-apis and restrict it to Maps SDK for Android.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Centered(text: String, color: Color) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
