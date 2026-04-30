package com.onthaset.app.events.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.events.CalendarUiState
import com.onthaset.app.events.CalendarViewModel
import com.onthaset.app.events.Event
import com.onthaset.app.events.EventCategory
import com.onthaset.app.events.formatEventDay
import com.onthaset.app.events.formatEventTime
import com.onthaset.app.events.monthName

private val Yellow = Color(0xFFFFD600)
private val Surface10 = Color(0x1AFFFFFF)
private val CardBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NationalCalendarScreen(
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
                        Text("🗺️ NATIONAL RUN CALENDAR", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Coast to Coast • All Clubs Welcome", color = Color.Gray, fontSize = 11.sp)
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
                .padding(padding),
        ) {
            MonthBar(
                month = filter.month,
                year = filter.year,
                onPrev = { viewModel.shiftMonth(-1) },
                onNext = { viewModel.shiftMonth(1) },
            )
            CategoryChips(
                selected = filter.category,
                onSelect = viewModel::selectCategory,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (val s = state) {
                    CalendarUiState.Loading -> CenteredLoaderCal()
                    is CalendarUiState.Error -> Centered("Error: ${s.message}")
                    is CalendarUiState.Ready ->
                        if (s.events.isEmpty()) Centered("No national events this month.\nTry another month or category.")
                        else CalendarList(events = s.events, onEventClick = onEventClick)
                }
            }
        }
    }
}

@Composable
private fun MonthBar(
    month: Int,
    year: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = Yellow)
        }
        Text(
            "${monthName(month)} $year",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = Yellow)
        }
    }
}

@Composable
private fun CategoryChips(
    selected: EventCategory?,
    onSelect: (EventCategory?) -> Unit,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip(text = "ALL", active = selected == null, onClick = { onSelect(null) })
        EventCategory.national.forEach { cat ->
            Chip(
                text = cat.raw,
                active = selected == cat,
                pinColor = cat.pinColor,
                onClick = { onSelect(if (selected == cat) null else cat) },
            )
        }
    }
}

@Composable
private fun Chip(
    text: String,
    active: Boolean,
    pinColor: Color? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = if (active) Yellow else Surface10,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (pinColor != null) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(pinColor))
                Spacer(Modifier.size(6.dp))
            }
            Text(
                text,
                color = if (active) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun CalendarList(
    events: List<Event>,
    onEventClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = events, key = { it.id ?: it.title + it.date.toString() }) { event ->
            CalendarRow(event = event, onClick = { event.id?.let(onEventClick) })
        }
    }
}

@Composable
private fun CalendarRow(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            event.categoryEnum?.let { cat ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(cat.pinColor),
                )
                Spacer(Modifier.size(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${event.date.formatEventDay()} · ${event.date.formatEventTime()}",
                    color = Yellow,
                    fontSize = 12.sp,
                )
                if (event.locationName.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(event.locationName, color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CenteredLoaderCal() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Yellow)
    }
}

@Composable
private fun Centered(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Gray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
