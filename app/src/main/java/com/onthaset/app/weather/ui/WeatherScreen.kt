package com.onthaset.app.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.weather.DayForecast
import com.onthaset.app.weather.RideSafety
import com.onthaset.app.weather.WeatherViewModel
import com.onthaset.app.weather.emojiForWeatherCode

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)
private val CardBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onBack: () -> Unit,
    initialLat: Double? = null,
    initialLng: Double? = null,
    initialLabel: String? = null,
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(initialLat, initialLng) {
        if (initialLat != null && initialLng != null && state.daily.isEmpty()) {
            viewModel.loadFor(initialLat, initialLng, initialLabel ?: "Event Location")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.loadCurrentLocation()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("RIDE FORECAST", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("5-day weather for any city", color = Color.Gray, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { viewModel.search(query) },
                onUseMyLocation = {
                    if (viewModel.locationPermissionGranted) {
                        viewModel.loadCurrentLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
            when {
                state.isLoading -> CenteredLoader()
                state.error != null -> Centered(state.error!!, color = Color(0xFFFF6B6B))
                state.daily.isEmpty() -> Centered("Search for a city to see the forecast.", color = Color.Gray)
                else -> Forecast(
                    cityName = state.cityName,
                    currentTempF = state.currentTempF ?: 0,
                    currentWindMph = state.currentWindMph ?: 0,
                    currentCode = state.currentCode,
                    safety = state.safety,
                    daily = state.daily,
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onUseMyLocation: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("City, ZIP, or town", color = Color.Gray.copy(alpha = 0.5f)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Search,
            ),
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Yellow)
                }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg,
                cursorColor = Yellow,
                focusedBorderColor = Yellow,
                unfocusedBorderColor = Yellow.copy(alpha = 0.3f),
            ),
        )
        IconButton(onClick = onUseMyLocation) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Use my location", tint = Yellow)
        }
    }
}

@Composable
private fun Forecast(
    cityName: String,
    currentTempF: Int,
    currentWindMph: Int,
    currentCode: Int,
    safety: RideSafety?,
    daily: List<DayForecast>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(cityName.uppercase(), color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(emojiForWeatherCode(currentCode), fontSize = 56.sp)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("$currentTempF°F", color = Color.White, fontWeight = FontWeight.Black, fontSize = 36.sp)
                            Text("Wind $currentWindMph mph", color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                    safety?.let { SafetyChip(it) }
                }
            }
        }
        items(items = daily, key = { it.day }) { d ->
            DayRow(d)
        }
    }
}

@Composable
private fun SafetyChip(safety: RideSafety) {
    val (bg, fg) = when (safety) {
        RideSafety.Optimal -> Color(0xFF1E8449) to Color.White
        RideSafety.Sticky -> Color(0xFFB7950B) to Color.Black
        RideSafety.Dangerous -> Color(0xFFB03A2E) to Color.White
    }
    Spacer(Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(safety.message, color = fg, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
private fun DayRow(d: DayForecast) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(d.day, color = Yellow, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.size(80.dp, 24.dp))
            Text(emojiForWeatherCode(d.code), fontSize = 22.sp)
            Spacer(Modifier.size(12.dp))
            Text("${d.highF}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.size(8.dp))
            Text("${d.lowF}°", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CenteredLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Yellow)
    }
}

@Composable
private fun Centered(text: String, color: Color) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = color, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
