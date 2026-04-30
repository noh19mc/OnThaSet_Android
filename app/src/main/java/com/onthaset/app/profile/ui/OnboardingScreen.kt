package com.onthaset.app.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.profile.ProfileUiState
import com.onthaset.app.profile.ProfileUpdate
import com.onthaset.app.profile.ProfileViewModel
import com.onthaset.app.profile.UserProfile

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val profile = (state as? ProfileUiState.Ready)?.profile

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Set Up Your Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onSkip) { Text("Skip", color = Yellow) } },
            )
        },
    ) { padding ->
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Yellow)
            }
            return@Scaffold
        }
        Form(
            initial = profile,
            saving = saving,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding),
            onSave = { update -> viewModel.save(update, onSaved) },
        )
    }
}

@Composable
private fun Form(
    initial: UserProfile,
    saving: Boolean,
    modifier: Modifier,
    onSave: (ProfileUpdate) -> Unit,
) {
    var displayName by remember { mutableStateOf(initial.displayName) }
    var bio by remember { mutableStateOf(initial.bio) }
    var hometown by remember { mutableStateOf(initial.hometown) }
    var club by remember { mutableStateOf(initial.club) }
    var favoriteRide by remember { mutableStateOf(initial.favoriteRide) }
    var ridingSince by remember { mutableStateOf(initial.ridingSince) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Welcome to On Tha Set 🤝",
            color = Yellow,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
        )
        Text(
            "Tell other riders who you are. You can always edit this later.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Start,
        )
        Spacer(Modifier.size(8.dp))

        Header("THE BASICS")
        Field("Display Name", displayName, capitalize = KeyboardCapitalization.Words) { displayName = it }
        Field("Hometown", hometown, capitalize = KeyboardCapitalization.Words) { hometown = it }
        Field("Bio", bio, singleLine = false, capitalize = KeyboardCapitalization.Sentences) { bio = it }

        Header("YOUR RIDE")
        Field("Bike (year, make, model)", favoriteRide, capitalize = KeyboardCapitalization.Words) { favoriteRide = it }
        Field("Riding Since (year)", ridingSince) { ridingSince = it.filter(Char::isDigit).take(4) }
        Field("Club / Affiliation", club, capitalize = KeyboardCapitalization.Words) { club = it }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                onSave(
                    ProfileUpdate(
                        displayName = displayName.trim(),
                        bio = bio.trim(),
                        hometown = hometown.trim(),
                        club = club.trim(),
                        favoriteRide = favoriteRide.trim(),
                        ridingSince = ridingSince.trim(),
                        preferredRideType = initial.preferredRideType,
                        favoriteRoute = initial.favoriteRoute,
                        instagramHandle = initial.instagramHandle,
                        tiktokHandle = initial.tiktokHandle,
                        youtubeChannel = initial.youtubeChannel,
                        facebookHandle = initial.facebookHandle,
                    )
                )
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
        ) {
            if (saving) {
                CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Save & Continue", fontWeight = FontWeight.Bold)
            }
        }
        Text(
            "You can add a profile photo, social handles, and more from Edit Profile.",
            color = Color.Gray,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun Header(title: String) {
    Text(title, color = Yellow, fontWeight = FontWeight.Black, fontSize = 13.sp)
}

@Composable
private fun Field(
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
