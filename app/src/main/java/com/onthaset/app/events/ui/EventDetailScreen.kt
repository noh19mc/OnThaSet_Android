package com.onthaset.app.events.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.events.EventDetailViewModel
import com.onthaset.app.events.formatEventDay
import com.onthaset.app.events.formatEventTime

private val Yellow = Color(0xFFFFD600)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onOpenPoster: (String) -> Unit,
    onReport: (String, String) -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(eventId) { viewModel.load(eventId) }
    val event by viewModel.event.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Event", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back", color = Yellow) }
                },
                actions = {
                    val current = event
                    if (current != null) {
                        TextButton(onClick = {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, buildShareText(current))
                            }
                            context.startActivity(Intent.createChooser(share, "Share event"))
                        }) {
                            Text("Share", color = Yellow)
                        }
                    }
                    if (current?.id != null) {
                        TextButton(onClick = { onReport(current.id, current.title) }) {
                            Text("Report", color = Color(0xFFFF6B6B))
                        }
                    }
                },
            )
        },
    ) { padding ->
        val e = event
        if (e == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Yellow)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                e.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    e.categoryEnum?.let {
                        Text(it.icon, fontSize = 26.sp)
                        Spacer(Modifier.height(0.dp))
                    }
                    Spacer(modifier = Modifier.padding(end = 8.dp))
                    Text(
                        e.title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                    )
                }
                e.categoryEnum?.let {
                    Text(it.raw, color = Yellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                LabelValue("When", "${e.date.formatEventDay()} · ${e.date.formatEventTime()}")
                LabelValue("Where", e.locationName)
                if (e.price.isNotBlank()) LabelValue("Price", e.price)
                if (e.details.isNotBlank()) LabelValue("Details", e.details)
                ClickableLabelValue(
                    label = "Posted by",
                    value = e.postedByName.ifBlank { "Unknown rider" },
                    onClick = if (e.postedByUserId.isNotBlank()) {
                        { onOpenPoster(e.postedByUserId) }
                    } else null,
                )
            }
        }
    }
}

private fun buildShareText(e: com.onthaset.app.events.Event): String {
    // Pipe-delimited location is a backend convention; users only want the human bits.
    val location = e.locationName.split('|').filter { it.isNotBlank() }.joinToString(", ")
    val icon = e.categoryEnum?.icon ?: "🏍️"
    return buildString {
        append("$icon ${e.title}\n")
        append("📅 ${e.date.formatEventDay()} at ${e.date.formatEventTime()}\n")
        if (location.isNotBlank()) append("📍 $location\n")
        if (e.price.isNotBlank() && e.price != "0.00") append("💵 ${e.price}\n")
        if (e.details.isNotBlank()) append("\n${e.details}\n")
        append("\nShared from On Tha Set")
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(label.uppercase(), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
private fun ClickableLabelValue(label: String, value: String, onClick: (() -> Unit)?) {
    Column(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Text(label.uppercase(), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            color = if (onClick != null) Yellow else Color.White,
            fontSize = 15.sp,
            fontWeight = if (onClick != null) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
