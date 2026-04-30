package com.onthaset.app.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.admin.AdminAdsBuckets
import com.onthaset.app.admin.AdminUiState
import com.onthaset.app.admin.AdminViewModel
import com.onthaset.app.directory.BusinessAd
import com.onthaset.app.events.Event
import com.onthaset.app.events.formatEventDay
import com.onthaset.app.reports.EventReport
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)
private val CardBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Admin", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.lock()
                        onBack()
                    }) { Text("Back", color = Yellow) }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(padding)) {
            when (val s = state) {
                AdminUiState.Locked -> if (viewModel.pinConfigured) PinGate(viewModel::tryUnlock) else NoPin()
                AdminUiState.Loading -> Loader()
                is AdminUiState.Error -> CenteredText(s.message, Color(0xFFFF6B6B))
                is AdminUiState.Ready -> AdminTabs(
                    events = s.events,
                    reports = s.reports,
                    ads = s.ads,
                    onDeleteEvent = viewModel::deleteEvent,
                    onDismissReport = viewModel::dismissReport,
                    onApproveAd = viewModel::approveAd,
                    onRejectAd = viewModel::rejectAd,
                    onDeactivateAd = viewModel::deactivateAd,
                    onActivateAd = viewModel::activateAd,
                    onDeleteAd = viewModel::deleteAd,
                )
            }
        }
    }
}

@Composable
private fun PinGate(onSubmit: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ENTER ADMIN PIN", color = Yellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(8) },
            placeholder = { Text("••••", color = Color.Gray.copy(alpha = 0.5f)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                autoCorrectEnabled = false,
            ),
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
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                error = if (onSubmit(pin)) null else "Wrong PIN."
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
        ) {
            Text("Unlock", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NoPin() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ADMIN DISABLED", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Set ADMIN_PIN in local.properties and rebuild to enable admin tools.",
            color = Color.Gray,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun AdminTabs(
    events: List<Event>,
    reports: List<EventReport>,
    ads: AdminAdsBuckets,
    onDeleteEvent: (String) -> Unit,
    onDismissReport: (String) -> Unit,
    onApproveAd: (BusinessAd) -> Unit,
    onRejectAd: (String) -> Unit,
    onDeactivateAd: (String) -> Unit,
    onActivateAd: (String) -> Unit,
    onDeleteAd: (BusinessAd) -> Unit,
) {
    var tab by remember { mutableStateOf(0) }
    val adCount = ads.pending.size + ads.active.size + ads.deactivated.size + ads.expired.size
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab, containerColor = Color.Black, contentColor = Yellow) {
            Tab(selected = tab == 0, onClick = { tab = 0 }) {
                Text(
                    "EVENTS (${events.size})",
                    color = if (tab == 0) Yellow else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            Tab(selected = tab == 1, onClick = { tab = 1 }) {
                Text(
                    "REPORTS (${reports.size})",
                    color = if (tab == 1) Yellow else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            Tab(selected = tab == 2, onClick = { tab = 2 }) {
                Text(
                    "ADS ($adCount)",
                    color = if (tab == 2) Yellow else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }
        when (tab) {
            0 -> EventsModeration(events, onDeleteEvent)
            1 -> ReportsModeration(reports, events, onDismissReport, onDeleteEvent)
            2 -> AdsModeration(ads, onApproveAd, onRejectAd, onDeactivateAd, onActivateAd, onDeleteAd)
        }
    }
}

@Composable
private fun AdsModeration(
    ads: AdminAdsBuckets,
    onApprove: (BusinessAd) -> Unit,
    onReject: (String) -> Unit,
    onDeactivate: (String) -> Unit,
    onActivate: (String) -> Unit,
    onDelete: (BusinessAd) -> Unit,
) {
    if (ads.pending.isEmpty() && ads.active.isEmpty() && ads.deactivated.isEmpty() && ads.expired.isEmpty()) {
        CenteredText("No ads in the system yet.", Color.Gray)
        return
    }
    var pendingDelete by remember { mutableStateOf<BusinessAd?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (ads.pending.isNotEmpty()) {
            item { SectionHeader("PENDING (${ads.pending.size})") }
            items(items = ads.pending, key = { it.id ?: it.businessName + "_p" }) { ad ->
                AdCard(ad = ad) {
                    AdActionButton("Approve", Color(0xFF1E8449), Color.White) { onApprove(ad) }
                    AdActionButton("Reject", Color(0xFFB03A2E), Color.White) { ad.id?.let(onReject) }
                    AdActionButton("Delete", Color(0x44FF6B6B), Color.White) { pendingDelete = ad }
                }
            }
        }
        if (ads.active.isNotEmpty()) {
            item { SectionHeader("ACTIVE (${ads.active.size})") }
            items(items = ads.active, key = { it.id ?: it.businessName + "_a" }) { ad ->
                AdCard(ad = ad) {
                    AdActionButton("Deactivate", Color(0x14FFFFFF), Yellow) { ad.id?.let(onDeactivate) }
                    AdActionButton("Delete", Color(0x44FF6B6B), Color.White) { pendingDelete = ad }
                }
            }
        }
        if (ads.deactivated.isNotEmpty()) {
            item { SectionHeader("DEACTIVATED (${ads.deactivated.size})") }
            items(items = ads.deactivated, key = { it.id ?: it.businessName + "_d" }) { ad ->
                AdCard(ad = ad) {
                    AdActionButton("Reactivate", Color(0xFF1E8449), Color.White) { ad.id?.let(onActivate) }
                    AdActionButton("Delete", Color(0x44FF6B6B), Color.White) { pendingDelete = ad }
                }
            }
        }
        if (ads.expired.isNotEmpty()) {
            item { SectionHeader("EXPIRED (${ads.expired.size})") }
            items(items = ads.expired, key = { it.id ?: it.businessName + "_e" }) { ad ->
                AdCard(ad = ad) {
                    AdActionButton("Reactivate", Color(0xFF1E8449), Color.White) { ad.id?.let(onActivate) }
                    AdActionButton("Delete", Color(0x44FF6B6B), Color.White) { pendingDelete = ad }
                }
            }
        }
    }
    pendingDelete?.let { ad ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ad?") },
            text = { Text("This permanently removes \"${ad.businessName}\" and its banner image.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(ad)
                    pendingDelete = null
                }) { Text("Delete", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        color = Yellow,
        fontWeight = FontWeight.Black,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun AdCard(ad: BusinessAd, actions: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ad.plan == "premium" || ad.sponsored == true) Text("👑", fontSize = 14.sp)
                Text(ad.businessName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            if (ad.tagline.isNotBlank()) {
                Spacer(Modifier.size(2.dp))
                Text(ad.tagline, color = Color.LightGray, fontSize = 12.sp)
            }
            val meta = listOfNotNull(
                ad.category.takeIf { it.isNotBlank() },
                ad.plan.takeIf { it.isNotBlank() },
                ad.paidUntil?.let { "paid until $it" },
                ad.paymentStatus?.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Spacer(Modifier.size(2.dp))
                Text(meta, color = Color.Gray, fontSize = 11.sp)
            }
            ad.advertiserEmail?.takeIf { it.isNotBlank() }?.let { email ->
                Spacer(Modifier.size(2.dp))
                Text(email, color = Yellow, fontSize = 11.sp)
            }
            Spacer(Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { actions() }
        }
    }
}

@Composable
private fun AdActionButton(label: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = bg,
    ) {
        Text(
            label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ReportsModeration(
    reports: List<EventReport>,
    events: List<Event>,
    onDismiss: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
) {
    if (reports.isEmpty()) {
        CenteredText("No pending reports — community is behaving.", Color.Gray)
        return
    }
    val eventsById = remember(events) { events.associateBy { it.id ?: "" } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = reports, key = { it.id ?: it.eventId + it.createdAt.toString() }) { r ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(r.reason.uppercase(), color = Color(0xFFFF6B6B), fontWeight = FontWeight.Black, fontSize = 11.sp)
                    Spacer(Modifier.size(2.dp))
                    Text(r.eventTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (r.additionalNotes.isNotBlank()) {
                        Spacer(Modifier.size(4.dp))
                        Text("“${r.additionalNotes}”", color = Color.LightGray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.size(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { r.id?.let(onDismiss) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x14FFFFFF),
                        ) {
                            Text(
                                "Dismiss",
                                color = Yellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                        if (eventsById.containsKey(r.eventId)) {
                            Surface(
                                onClick = { onDeleteEvent(r.eventId) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x44FF6B6B),
                            ) {
                                Text(
                                    "Delete event",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventsModeration(events: List<Event>, onDelete: (String) -> Unit) {
    var pendingDelete by remember { mutableStateOf<Event?>(null) }
    if (events.isEmpty()) {
        CenteredText("No events to moderate.", Color.Gray)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = events, key = { it.id ?: it.title + it.date.toString() }) { event ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.size(2.dp))
                        Text(event.date.formatEventDay(), color = Yellow, fontSize = 11.sp)
                        if (event.postedByName.isNotBlank()) {
                            Spacer(Modifier.size(2.dp))
                            Text("by ${event.postedByName}", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = { pendingDelete = event }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFF6B6B))
                    }
                }
            }
        }
    }

    pendingDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete event?") },
            text = { Text("This permanently removes \"${event.title}\" from the shared backend.") },
            confirmButton = {
                TextButton(onClick = {
                    event.id?.let(onDelete)
                    pendingDelete = null
                }) { Text("Delete", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
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
