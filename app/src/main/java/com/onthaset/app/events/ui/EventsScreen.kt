package com.onthaset.app.events.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.ads.AdMobBanner
import com.onthaset.app.events.Event
import com.onthaset.app.events.EventsUiState
import com.onthaset.app.events.EventsViewModel
import com.onthaset.app.events.formatEventDay
import com.onthaset.app.events.formatEventTime

private val Yellow = Color(0xFFFFD600)
private val CardBg = Color(0x14FFFFFF)
private val SegmentBg = Color(0x14FFFFFF)

private enum class EventsTab(val label: String) {
    Nearby("Nearby"),
    All("All"),
    Calendar("Calendar"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onEventClick: (String) -> Unit,
    onCreate: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(EventsTab.Nearby) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Events", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                actions = {
                    if (tab == EventsTab.All) {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Yellow)
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate, containerColor = Yellow, contentColor = Color.Black) {
                Icon(Icons.Filled.Add, contentDescription = "New event")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding),
        ) {
            SegmentedTabs(selected = tab, onSelect = { tab = it })
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    EventsTab.Nearby -> NearbyEventsScreen(onEventClick = onEventClick)
                    EventsTab.All -> AllEventsBody(
                        state = state,
                        refreshing = refreshing,
                        onClick = onEventClick,
                        onRetry = viewModel::refresh,
                    )
                    EventsTab.Calendar -> {
                        val ready = state as? EventsUiState.Ready
                        if (ready != null) {
                            EventsCalendarView(events = ready.events, onEventClick = onEventClick)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Yellow)
                            }
                        }
                    }
                }
            }
            AdMobBanner()
        }
    }
}

@Composable
private fun SegmentedTabs(selected: EventsTab, onSelect: (EventsTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(SegmentBg, RoundedCornerShape(10.dp)),
    ) {
        EventsTab.entries.forEach { t ->
            val active = t == selected
            Surface(
                onClick = { onSelect(t) },
                color = if (active) Yellow else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        t.label,
                        color = if (active) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AllEventsBody(
    state: EventsUiState,
    refreshing: Boolean,
    onClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is EventsUiState.Loading -> CenteredLoader()
        is EventsUiState.Error -> ErrorPane(state.message, onRetry = onRetry)
        is EventsUiState.Ready -> {
            if (state.events.isEmpty()) EmptyPane(onRefresh = onRetry)
            else EventsList(events = state.events, refreshing = refreshing, onClick = onClick)
        }
    }
}

@Composable
private fun EventsList(
    events: List<Event>,
    refreshing: Boolean,
    onClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (refreshing) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Yellow, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                }
            }
        }
        items(items = events, key = { it.id ?: it.title + it.date.toString() }) { event ->
            EventRow(event = event, onClick = { event.id?.let(onClick) })
        }
    }
}

@Composable
private fun EventRow(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            event.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(10.dp)),
                )
                Spacer(Modifier.height(10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                event.categoryEnum?.let { Text(it.icon, fontSize = 22.sp) }
                Spacer(Modifier.size(8.dp))
                Text(
                    event.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${event.date.formatEventDay()} · ${event.date.formatEventTime()}",
                color = Yellow,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(event.locationName.split('|').filter { it.isNotBlank() }.joinToString(", "),
                color = Color.LightGray, fontSize = 13.sp)
            if (event.price.isNotBlank() && event.price != "0.00") {
                Spacer(Modifier.height(2.dp))
                Text(event.price, color = Color.Gray, fontSize = 12.sp)
            }
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
private fun EmptyPane(onRefresh: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No upcoming events.", color = Color.Gray)
            TextButton(onClick = onRefresh) { Text("Refresh", color = Yellow) }
        }
    }
}

@Composable
private fun ErrorPane(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Couldn't load events", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(message, color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("Try Again", color = Yellow) }
        }
    }
}
