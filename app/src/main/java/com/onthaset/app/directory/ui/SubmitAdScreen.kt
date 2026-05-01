package com.onthaset.app.directory.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.onthaset.app.directory.AdPlan
import com.onthaset.app.directory.SubmitAdViewModel

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)
private val CardBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAdScreen(
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: SubmitAdViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var businessName by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf(AdPlan.Basic) }

    val pickFlyer = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { viewModel.setFlyer(it) }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    LaunchedEffect(state.justSubmitted) {
        // Acknowledge handled in dialog button below
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Advertise With Us", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Text("📣", fontSize = 32.sp)
            Text("Reach thousands of riders", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(
                "Pick a plan, fill in your details, and your listing goes to the moderators for approval.",
                color = Color.Gray,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(8.dp))
            Text("PLAN", color = Yellow, fontWeight = FontWeight.Black, fontSize = 12.sp)
            AdPlan.entries.forEach { p ->
                PlanCard(plan = p, selected = plan == p, onSelect = { plan = p })
            }

            Spacer(Modifier.height(8.dp))
            Text("BUSINESS", color = Yellow, fontWeight = FontWeight.Black, fontSize = 12.sp)
            FlyerSlot(uri = state.flyerUri, enabled = !state.isSubmitting, onPick = { pickFlyer.launch(imageOnly) })
            DarkField("Business Name", businessName, capitalize = KeyboardCapitalization.Words) { businessName = it }
            DarkField("Tagline", tagline, capitalize = KeyboardCapitalization.Sentences) { tagline = it }
            DarkField("Category (e.g. Tattoo Shop, Apparel)", category, capitalize = KeyboardCapitalization.Words) { category = it }
            DarkField("Phone", phone) { phone = it }
            DarkField("Website (https://…)", website) { website = it }
            DarkField("Address", address, singleLine = false, capitalize = KeyboardCapitalization.Words) { address = it }

            state.error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp) }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.submit(
                        businessName = businessName,
                        tagline = tagline,
                        category = category,
                        plan = plan,
                        phone = phone,
                        websiteUrl = website,
                        address = address,
                    )
                },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Submit for Review", fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "Payment is collected outside the app once your listing is approved. " +
                    "You'll get an email at your account address with next steps.",
                color = Color.Gray,
                fontSize = 11.sp,
            )
        }
    }

    if (state.justSubmitted) {
        AlertDialog(
            onDismissRequest = {
                viewModel.reset()
                onSubmitted()
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reset()
                    onSubmitted()
                }) { Text("Got it") }
            },
            title = { Text("Submitted") },
            text = { Text("Thanks — your listing is in the moderation queue. We'll contact you about payment once it's approved.") },
        )
    }
}

@Composable
private fun PlanCard(plan: AdPlan, selected: Boolean, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = if (selected) Yellow else CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (plan == AdPlan.Premium) Text("👑", fontSize = 16.sp)
                Text(
                    plan.title,
                    color = if (selected) Color.Black else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    plan.price,
                    color = if (selected) Color.Black else Yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(plan.perks, color = if (selected) Color.Black else Color.LightGray, fontSize = 12.sp)
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
            AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text("Tap to add logo / banner (optional)", color = Color.Gray, fontSize = 13.sp)
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
