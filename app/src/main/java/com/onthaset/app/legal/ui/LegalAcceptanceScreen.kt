package com.onthaset.app.legal.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.onthaset.app.auth.ui.OnThaSetShield
import com.onthaset.app.legal.LegalAcceptanceViewModel

private val Yellow = Color(0xFFFFD600)
private val Orange = Color(0xFFFF9800)
private val WarningBg = Color(0x33FF9800)
private val NoticeBg = Color(0x14FFFFFF)

@Composable
fun LegalAcceptanceScreen(
    onAccepted: () -> Unit,
    viewModel: LegalAcceptanceViewModel = hiltViewModel(),
) {
    var agreesToTerms by remember { mutableStateOf(false) }
    var agreesToLiability by remember { mutableStateOf(false) }
    val canContinue = agreesToTerms && agreesToLiability
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OnThaSetShield(size = 96.dp)
        Text("Welcome to On Tha Set", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text("The Motorcycle Community Platform", color = Color.Gray, fontSize = 13.sp)

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NoticeBg, RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 18.sp)
                    Spacer(Modifier.size(8.dp))
                    Text("IMPORTANT NOTICE", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                Text(
                    "Before using On Tha Set, please read and acknowledge the following:",
                    color = Color.White,
                    fontSize = 13.sp,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarningBg, RoundedCornerShape(8.dp))
                        .border(1.dp, Orange, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠", color = Orange, fontSize = 14.sp)
                            Spacer(Modifier.size(6.dp))
                            Text("EVENT LIABILITY NOTICE", color = Orange, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        Text(
                            "On Tha Set is a technology platform only. We do not organize, host, " +
                                "sponsor, or control any events listed on this platform. Attendance at " +
                                "any event discovered through On Tha Set is entirely at your own risk.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                        )
                        Text(
                            "On Tha Set, its owners, operators, and affiliates are NOT responsible for " +
                                "any injury, death, property damage, criminal acts, accidents, or harm of " +
                                "any kind that occurs in connection with events posted on this platform.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

        ConsentRow(checked = agreesToTerms, onChange = { agreesToTerms = it }) {
            Column {
                Text(
                    "I agree to the Terms of Service and Privacy Policy",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Read Terms",
                        color = Yellow,
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { context.openExternal(TERMS_URL) },
                    )
                    Text(
                        "Read Privacy Policy",
                        color = Yellow,
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { context.openExternal(PRIVACY_URL) },
                    )
                }
            }
        }

        ConsentRow(checked = agreesToLiability, onChange = { agreesToLiability = it }) {
            Text(
                "I understand that On Tha Set is not responsible for any harm, injury, " +
                    "death, or damage that occurs at or in connection with any event posted " +
                    "on this platform. I attend all events at my own risk.",
                color = Color.White,
                fontSize = 13.sp,
            )
        }

        if (!canContinue) {
            Text(
                "You must agree to both statements to continue",
                color = Orange,
                fontSize = 12.sp,
            )
        }

        Button(
            onClick = {
                viewModel.accept()
                onAccepted()
            },
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = Color.Black,
                disabledContainerColor = Color(0x33FFFFFF),
                disabledContentColor = Color.Gray,
            ),
        ) {
            Text(
                if (canContinue) "✓ AGREE TO CONTINUE" else "🔒 AGREE TO CONTINUE",
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ConsentRow(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NoticeBg, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Yellow,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.Black,
            ),
        )
        Spacer(Modifier.size(8.dp))
        Box(modifier = Modifier.weight(1f)) { content() }
    }
}

private const val TERMS_URL = "https://onthaset.carrd.co/"
private const val PRIVACY_URL = "https://onthaset.carrd.co/"

private fun android.content.Context.openExternal(url: String) {
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
}
