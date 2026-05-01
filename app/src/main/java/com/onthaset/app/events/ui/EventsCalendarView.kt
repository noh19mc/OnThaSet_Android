package com.onthaset.app.events.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.onthaset.app.events.Event
import com.onthaset.app.events.formatEventTime
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.YearMonth

private val Yellow = Color(0xFFFFD600)
private val CardBg = Color(0x14FFFFFF)

@Composable
fun EventsCalendarView(
    events: List<Event>,
    onEventClick: (String) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val state = rememberCalendarState(
        startMonth = YearMonth.from(today).minusMonths(12),
        endMonth = YearMonth.from(today).plusMonths(24),
        firstVisibleMonth = YearMonth.from(today),
        firstDayOfWeek = daysOfWeek().first(),
    )
    var selected by remember { mutableStateOf(today) }
    val scope = rememberCoroutineScope()

    val daysWithEvents = remember(events) { events.toDaysIndex() }
    val eventsForSelected = remember(selected, events) {
        daysWithEvents[selected].orEmpty().sortedBy { it.date }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        MonthHeader(
            month = state.firstVisibleMonth.yearMonth,
            onPrev = { scope.launch { state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1)) } },
            onNext = { scope.launch { state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1)) } },
        )
        DaysOfWeekRow()
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    day = day,
                    isSelected = day.date == selected,
                    isToday = day.date == today,
                    hasEvents = daysWithEvents.containsKey(day.date),
                    onClick = { selected = day.date },
                )
            },
        )
        SelectedDayHeader(selected, eventsForSelected.size)
        if (eventsForSelected.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No events on this day.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = eventsForSelected, key = { it.id ?: it.title + it.date.toString() }) { event ->
                    EventRowCompact(event, onClick = { event.id?.let(onEventClick) })
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous", tint = Yellow) }
        Text(
            "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
        )
        IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = Yellow) }
    }
}

@Composable
private fun DaysOfWeekRow() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        daysOfWeek().forEach { dow ->
            Text(
                dow.name.take(3),
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
) {
    val faded = day.position != DayPosition.MonthDate
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Yellow
                    isToday -> Color(0x33FFD600)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = !faded, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.Black
                    faded -> Color.DarkGray
                    else -> Color.White
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            if (hasEvents && !isSelected) {
                Spacer(Modifier.size(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Yellow),
                )
            }
        }
    }
}

@Composable
private fun SelectedDayHeader(date: LocalDate, count: Int) {
    Text(
        "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth} · $count event${if (count == 1) "" else "s"}",
        color = Yellow,
        fontWeight = FontWeight.Black,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun EventRowCompact(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(event.categoryEnum?.icon ?: "🏍️", fontSize = 18.sp)
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text(event.date.formatEventTime(), color = Yellow, fontSize = 11.sp)
            }
        }
    }
}

private fun List<Event>.toDaysIndex(): Map<LocalDate, List<Event>> {
    val zone = TimeZone.currentSystemDefault()
    val map = mutableMapOf<LocalDate, MutableList<Event>>()
    for (event in this) {
        val ldt = event.date.toLocalDateTime(zone)
        val key = LocalDate.of(ldt.year, ldt.monthNumber, ldt.dayOfMonth)
        map.getOrPut(key) { mutableListOf() }.add(event)
    }
    return map
}

