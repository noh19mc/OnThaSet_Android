package com.onthaset.app.reports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.reports.ReportEventViewModel
import com.onthaset.app.reports.ReportReason

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportEventScreen(
    eventId: String,
    eventTitle: String,
    onBack: () -> Unit,
    viewModel: ReportEventViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var reason by remember { mutableStateOf<ReportReason?>(null) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Report Event", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Text("Reporting:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(eventTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))
            Text("WHAT'S THE PROBLEM?", color = Yellow, fontWeight = FontWeight.Black, fontSize = 13.sp)
            ReportReason.entries.forEach { r ->
                ReasonRow(r, selected = reason == r, onClick = { reason = r })
            }

            Spacer(Modifier.height(8.dp))
            Text("EXTRA DETAIL (OPTIONAL)", color = Yellow, fontWeight = FontWeight.Black, fontSize = 13.sp)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("What should the moderators know?", color = Color.Gray.copy(alpha = 0.5f)) },
                singleLine = false,
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = false,
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
                ),
            )

            state.error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp) }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.submit(eventId, eventTitle, reason, notes) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (state.submitted) {
        AlertDialog(
            onDismissRequest = onBack,
            confirmButton = { TextButton(onClick = onBack) { Text("OK") } },
            title = { Text("Report Submitted") },
            text = { Text("Thanks — moderators will take a look.") },
        )
    }
}

@Composable
private fun ReasonRow(reason: ReportReason, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Yellow else FieldBg,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(reason.emoji, fontSize = 18.sp)
            Spacer(Modifier.size(10.dp))
            Text(
                reason.raw,
                color = if (selected) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}
