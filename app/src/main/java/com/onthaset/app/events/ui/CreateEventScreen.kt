package com.onthaset.app.events.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.events.CreateEventViewModel
import com.onthaset.app.events.EventCategory
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSubscribe: () -> Unit,
    editingEventId: String? = null,
    viewModel: CreateEventViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(editingEventId) {
        if (editingEventId != null && state.editing?.id != editingEventId) {
            viewModel.loadForEdit(editingEventId)
        }
    }
    LaunchedEffect(state.justSaved) {
        if (state.justSaved) {
            viewModel.reset()
            onSaved()
        }
    }
    LaunchedEffect(state.needsSubscription) {
        if (state.needsSubscription) {
            viewModel.reset()
            onSubscribe()
        }
    }

    val editing = state.editing
    val isEdit = editingEventId != null
    val initialParts = remember(editing) { editing?.locationName?.split('|').orEmpty() }

    var title by remember(editing) { mutableStateOf(editing?.title ?: "") }
    var category by remember(editing) {
        mutableStateOf(editing?.categoryEnum ?: EventCategory.Community)
    }
    val now = remember { kotlinx.datetime.Clock.System.now() }
    var dateTime by remember(editing) { mutableStateOf(editing?.date ?: now) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var venue by remember(editing) { mutableStateOf(initialParts.getOrNull(0)?.trim().orEmpty()) }
    var street by remember(editing) { mutableStateOf(initialParts.getOrNull(1)?.trim().orEmpty()) }
    var city by remember(editing) { mutableStateOf(initialParts.getOrNull(2)?.trim().orEmpty()) }
    var stateName by remember(editing) { mutableStateOf(initialParts.getOrNull(3)?.trim().orEmpty()) }
    var zip by remember(editing) { mutableStateOf(initialParts.getOrNull(4)?.trim().orEmpty()) }
    var details by remember(editing) { mutableStateOf(editing?.details.orEmpty()) }
    var price by remember(editing) { mutableStateOf(editing?.price?.takeIf { it != "0.00" }.orEmpty()) }

    val pickFlyer = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { viewModel.setFlyer(it) }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Event" else "Post an Event",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Cancel", color = Yellow) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlyerSlot(
                uri = state.flyerUri,
                enabled = !state.isUploading,
                onPick = { pickFlyer.launch(imageOnly) },
            )

            DarkField("Title", title, capitalize = KeyboardCapitalization.Sentences) { title = it }

            CategoryRow(selected = category, onSelect = { category = it })

            DateTimeRow(
                instant = dateTime,
                onPickDate = { showDate = true },
                onPickTime = { showTime = true },
            )

            DarkField("Venue (e.g. The Roadhouse)", venue, capitalize = KeyboardCapitalization.Words) { venue = it }
            DarkField("Street Address", street, capitalize = KeyboardCapitalization.Words) { street = it }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(2f)) {
                    DarkField("City", city, capitalize = KeyboardCapitalization.Words) { city = it }
                }
                Box(modifier = Modifier.weight(1f)) {
                    DarkField("State", stateName, capitalize = KeyboardCapitalization.Characters) {
                        stateName = it.uppercase().take(2)
                    }
                }
            }
            DarkField("ZIP", zip, keyboardType = KeyboardType.Number) { zip = it.filter(Char::isDigit).take(5) }

            DarkField("Details", details, singleLine = false, capitalize = KeyboardCapitalization.Sentences) { details = it }
            DarkField("Price (e.g. Free, \$10, \$0.00)", price, capitalize = KeyboardCapitalization.None) { price = it }

            state.error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp) }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.submit(
                        title = title,
                        date = dateTime,
                        category = category,
                        venue = venue,
                        street = street,
                        city = city,
                        state = stateName,
                        zip = zip,
                        details = details,
                        price = price,
                        editingId = editingEventId,
                    )
                },
                enabled = !state.isUploading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
            ) {
                if (state.isUploading) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text(if (isEdit) "Save Changes" else "Post Event", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDate) {
        val ds = rememberDatePickerState(initialSelectedDateMillis = dateTime.toEpochMilliseconds())
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    ds.selectedDateMillis?.let { millis ->
                        val zone = TimeZone.currentSystemDefault()
                        val newDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).date
                        val keepTime = dateTime.toLocalDateTime(zone).time
                        dateTime = LocalDateTime(newDate, keepTime).toInstant(zone)
                    }
                    showDate = false
                }) { Text("OK", color = Yellow) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel", color = Yellow) } },
        ) {
            DatePicker(state = ds)
        }
    }

    if (showTime) {
        val zone = TimeZone.currentSystemDefault()
        val ldt = dateTime.toLocalDateTime(zone)
        val ts = rememberTimePickerState(initialHour = ldt.hour, initialMinute = ldt.minute, is24Hour = false)
        DatePickerDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = kotlinx.datetime.LocalTime(ts.hour, ts.minute)
                    dateTime = LocalDateTime(ldt.date, newTime).toInstant(zone)
                    showTime = false
                }) { Text("OK", color = Yellow) }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Cancel", color = Yellow) } },
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                TimePicker(state = ts)
            }
        }
    }
}

@Composable
private fun FlyerSlot(uri: android.net.Uri?, enabled: Boolean, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .clickable(enabled = enabled, onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text("Tap to add flyer (optional)", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CategoryRow(selected: EventCategory, onSelect: (EventCategory) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EventCategory.entries.forEach { cat ->
            val active = cat == selected
            Surface(
                onClick = { onSelect(cat) },
                shape = RoundedCornerShape(15.dp),
                color = if (active) Yellow else Color(0x14FFFFFF),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(cat.icon, fontSize = 14.sp)
                    Spacer(Modifier.size(4.dp))
                    Text(
                        cat.raw,
                        color = if (active) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DateTimeRow(
    instant: Instant,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
) {
    val zone = TimeZone.currentSystemDefault()
    val ldt = instant.toLocalDateTime(zone)
    val date = "${ldt.month.name.take(3)} ${ldt.dayOfMonth}, ${ldt.year}"
    val hour12 = ((ldt.hour + 11) % 12) + 1
    val ampm = if (ldt.hour < 12) "AM" else "PM"
    val time = "%d:%02d %s".format(hour12, ldt.minute, ampm)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            onClick = onPickDate,
            shape = RoundedCornerShape(10.dp),
            color = FieldBg,
            modifier = Modifier.weight(1f).height(56.dp),
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                Column { Label("Date"); Text(date, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
        Surface(
            onClick = onPickTime,
            shape = RoundedCornerShape(10.dp),
            color = FieldBg,
            modifier = Modifier.weight(1f).height(56.dp),
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                Column { Label("Time"); Text(time, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun DarkField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalize: KeyboardCapitalization = KeyboardCapitalization.None,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = Color.Gray) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalize,
            autoCorrectEnabled = false,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = FieldBg,
            unfocusedContainerColor = FieldBg,
            cursorColor = Yellow,
            focusedBorderColor = Yellow,
            unfocusedBorderColor = Yellow.copy(alpha = 0.3f),
            focusedLabelColor = Yellow,
        ),
    )
}
