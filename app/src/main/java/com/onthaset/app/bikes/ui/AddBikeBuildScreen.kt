package com.onthaset.app.bikes.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.onthaset.app.bikes.BikesViewModel

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBikeBuildScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BikesViewModel = hiltViewModel(),
) {
    val create by viewModel.create.collectAsStateWithLifecycle()

    LaunchedEffect(create.justSavedId) {
        if (create.justSavedId != null) {
            viewModel.resetCreate()
            onSaved()
        }
    }

    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    val pickBefore = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { viewModel.setBefore(it) }
    val pickAfter = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { viewModel.setAfter(it) }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Post a Build", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhotoSlot(
                    label = "BEFORE",
                    uri = create.beforeUri,
                    enabled = !create.isUploading,
                    onPick = { pickBefore.launch(imageOnly) },
                    modifier = Modifier.weight(1f),
                )
                PhotoSlot(
                    label = "AFTER",
                    uri = create.afterUri,
                    enabled = !create.isUploading,
                    onPick = { pickAfter.launch(imageOnly) },
                    modifier = Modifier.weight(1f),
                )
            }

            DarkField("Title", title, capitalize = KeyboardCapitalization.Sentences) { title = it }
            DarkField("Note", note, singleLine = false, capitalize = KeyboardCapitalization.Sentences) { note = it }
            DarkField("Make", make, capitalize = KeyboardCapitalization.Words) { make = it }
            DarkField("Model", model, capitalize = KeyboardCapitalization.Words) { model = it }
            DarkField("Year", year) { year = it }

            create.error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp) }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.submit(title, note, make, model, year) },
                enabled = !create.isUploading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
            ) {
                if (create.isUploading) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Post Build", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PhotoSlot(
    label: String,
    uri: android.net.Uri?,
    enabled: Boolean,
    onPick: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.DarkGray)
                .clickable(enabled = enabled, onClick = onPick),
        ) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tap to select", color = Color.Gray, fontSize = 12.sp)
                }
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
