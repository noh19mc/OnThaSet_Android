package com.onthaset.app.billing.ui

import android.app.Activity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.billing.PaywallViewModel

private val Yellow = Color(0xFFFFD600)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Subscription", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(color = Yellow)
                state.notConfigured -> NotConfigured()
                state.alreadySubscribed -> AlreadySubscribed()
                else -> Offer(
                    price = state.priceText,
                    period = state.billingPeriodText,
                    error = state.error,
                    onSubscribe = { viewModel.launchPurchase(context as Activity) },
                )
            }
        }
    }

    if (state.justSubscribed) {
        AlertDialog(
            onDismissRequest = onBack,
            confirmButton = { TextButton(onClick = onBack) { Text("Sweet") } },
            title = { Text("You're In") },
            text = { Text("Subscription is active. Post events anytime.") },
        )
    }
}

@Composable
private fun NotConfigured() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("BILLING NOT CONFIGURED", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Spacer(Modifier.size(8.dp))
        Text(
            "Set BILLING_SUBSCRIPTION_PRODUCT_ID in local.properties and matching subscription " +
                "product in Google Play Console to enable subscriptions.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AlreadySubscribed() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("✓", color = Yellow, fontSize = 48.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.size(8.dp))
        Text("You're subscribed", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Spacer(Modifier.size(4.dp))
        Text(
            "Manage or cancel from Google Play → Account → Subscriptions.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Offer(
    price: String?,
    period: String?,
    error: String?,
    onSubscribe: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("ON THA SET PRO", color = Yellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text("Post up to 4 events per month", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
        Text("Plus profile photos, bike builds, ride photos.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)

        Spacer(Modifier.size(12.dp))

        if (price != null) {
            Text(price, color = Yellow, fontWeight = FontWeight.Black, fontSize = 32.sp)
            period?.let { Text(it, color = Color.Gray, fontSize = 13.sp) }
        }

        error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp) }

        Spacer(Modifier.size(12.dp))
        Button(
            onClick = onSubscribe,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
        ) {
            Text("Subscribe", fontWeight = FontWeight.Bold)
        }
        Text(
            "Billed through Google Play. Cancel anytime.",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
    }
}
