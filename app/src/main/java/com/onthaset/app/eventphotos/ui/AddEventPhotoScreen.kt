package com.onthaset.app.eventphotos.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.eventphotos.EventPhotosViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventPhotoScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EventPhotosViewModel = hiltViewModel(),
) {
    val create by viewModel.create.collectAsStateWithLifecycle()

    LaunchedEffect(create.justSaved) {
        if (create.justSaved) {
            viewModel.resetCreate()
            onSaved()
        }
    }

    var eventName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Clock.System.now()) }
    var showDate by remember { mutableStateOf(false) }

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { viewModel.setPicked(it) }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Upload Photo", color = Color.White, fontWeight = FontWeight.Bold) },
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
            PhotoSlot(
                uri = create.pickedUri,
                enabled = !create.isUploading,
                onPick = { pickPhoto.launch(imageOnly) },
            )

            DarkField("Event Name", eventName, capitalize = KeyboardCapitalization.Words) { eventName = it }
            DateRow(date = date, onPick = { showDate = true })
            DarkField("Location", location, capitalize = KeyboardCapitalization.Words) { location = it }
            DarkField("Caption", caption, singleLine = false, capitalize = KeyboardCapitalization.Sentences) { caption = it }

            create.error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp) }

            Button(
                onClick = { viewModel.submit(eventName, date, location, caption) },
                enabled = !create.isUploading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
            ) {
                if (create.isUploading) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Upload", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDate) {
        val ds = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMilliseconds())
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    ds.selectedDateMillis?.let { millis ->
                        val zone = TimeZone.currentSystemDefault()
                        val newDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).date
                        date = LocalDateTime(newDate, LocalTime(12, 0)).toInstant(zone)
                    }
                    showDate = false
                }) { Text("OK", color = Yellow) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel", color = Yellow) } },
        ) { DatePicker(state = ds) }
    }
}

@Composable
private fun PhotoSlot(uri: android.net.Uri?, enabled: Boolean, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .clickable(enabled = enabled, onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        if (uri != null) {
            AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text("Tap to pick a photo", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DateRow(date: Instant, onPick: () -> Unit) {
    val ldt = date.toLocalDateTime(TimeZone.currentSystemDefault())
    val text = "${ldt.month.name.take(3)} ${ldt.dayOfMonth}, ${ldt.year}"
    Surface(onClick = onPick, shape = RoundedCornerShape(10.dp), color = FieldBg, modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
            Column {
                Text("Event Date", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DarkField(
    label: String,
    value: String,
    singleLine: Boolean = true,
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
