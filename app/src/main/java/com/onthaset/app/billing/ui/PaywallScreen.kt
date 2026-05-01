package com.onthaset.app.billing.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
                title = { Text("CHOOSE YOUR PLAN", color = Color.White, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Close", color = Yellow) } },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.isLoading ->
                    Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Yellow)
                    }
                state.notConfigured -> NotConfigured()
                state.alreadySubscribed -> AlreadySubscribed()
                else -> {
                    Text(
                        "Select how you'd like to post events to the community.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )

                    if (state.singlePostCredits > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33FFD600), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                        ) {
                            Text(
                                "You have ${state.singlePostCredits} single-post credit(s) ready to use.",
                                color = Yellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    if (state.subscription != null) {
                        SubscriptionCard(
                            price = state.subscriptionPrice,
                            period = state.subscriptionPeriod,
                            onSubscribe = { viewModel.launchSubscription(context as Activity) },
                        )
                    }

                    if (state.subscription != null && state.singlePost != null) {
                        Text("or", color = Color.Gray, fontSize = 13.sp)
                    }

                    if (state.singlePost != null) {
                        SinglePostCard(
                            price = state.singlePostPrice,
                            onPurchase = { viewModel.launchSinglePost(context as Activity) },
                        )
                    }

                    state.error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp) }

                    TextButton(onClick = onBack) {
                        Text(
                            "GO BACK",
                            color = Yellow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .border(1.dp, Yellow, RoundedCornerShape(8.dp))
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                }
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
    if (state.justGrantedSinglePost) {
        AlertDialog(
            onDismissRequest = {
                viewModel.acknowledgeSinglePost()
                onBack()
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.acknowledgeSinglePost()
                    onBack()
                }) { Text("Post It") }
            },
            title = { Text("Credit Added") },
            text = { Text("You've got 1 single-post credit. Tap Post Event when you're ready.") },
        )
    }
}

@Composable
private fun SubscriptionCard(price: String?, period: String?, onSubscribe: () -> Unit) {
    Card(
        onClick = onSubscribe,
        colors = CardDefaults.cardColors(containerColor = Yellow, contentColor = Color.Black),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 14.sp)
                Spacer(Modifier.size(4.dp))
                Text("BEST VALUE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
            Text("Monthly Subscription", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(price ?: "—", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text(
                "4 posts ${period ?: "monthly"} · Cancel anytime",
                color = Color.Black,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun SinglePostCard(price: String?, onPurchase: () -> Unit) {
    Card(
        onClick = onPurchase,
        colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF), contentColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Yellow, RoundedCornerShape(14.dp)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Single Event Post", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(price ?: "—", color = Yellow, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text(
                "One-time payment · Use it on your next event",
                color = Color.LightGray,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun NotConfigured() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("BILLING NOT CONFIGURED", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Spacer(Modifier.size(8.dp))
        Text(
            "Set BILLING_SUBSCRIPTION_PRODUCT_ID and/or BILLING_SINGLE_POST_PRODUCT_ID in " +
                "local.properties (with matching products in Google Play Console) to enable purchases.",
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
