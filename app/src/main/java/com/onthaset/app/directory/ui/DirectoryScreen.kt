package com.onthaset.app.directory.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.onthaset.app.directory.BusinessAd
import com.onthaset.app.directory.DirectoryUiState
import com.onthaset.app.directory.DirectoryViewModel

private val Yellow = Color(0xFFFFD600)
private val CardBg = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
    onBack: () -> Unit,
    viewModel: DirectoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LOCAL BUSINESSES", color = Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Bike-friendly shops & services", color = Color.Gray, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Yellow) } },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(padding)) {
            when (val s = state) {
                DirectoryUiState.Loading -> Loader()
                is DirectoryUiState.Error -> CenteredText("Error: ${s.message}", Color(0xFFFF6B6B))
                is DirectoryUiState.Ready ->
                    if (s.ads.isEmpty()) CenteredText("No active listings nearby.", Color.Gray)
                    else AdsList(s.ads)
            }
        }
    }
}

@Composable
private fun AdsList(ads: List<BusinessAd>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = ads, key = { it.id ?: it.businessName }) { ad -> AdCard(ad) }
    }
}

@Composable
private fun AdCard(ad: BusinessAd) {
    val context = LocalContext.current
    val premium = ad.plan == "premium" || ad.sponsored == true
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ad.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (premium) Text("👑", fontSize = 13.sp)
                    Text(
                        ad.businessName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
                if (ad.tagline.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(ad.tagline, color = Color.LightGray, fontSize = 13.sp)
                }
                if (ad.category.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(ad.category.uppercase(), color = Yellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ad.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri()))
                        }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("Call", color = Yellow, fontSize = 12.sp)
                        }
                    }
                    ad.websiteUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("Website", color = Yellow, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
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
