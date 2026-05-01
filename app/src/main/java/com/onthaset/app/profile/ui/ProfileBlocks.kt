package com.onthaset.app.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onthaset.app.profile.ProfileStats
import com.onthaset.app.profile.UserProfile

private val Yellow = Color(0xFFFFD600)
private val CardBg = Color(0x14FFFFFF)

private const val PHOTO_LIMIT = 50 // matches iOS UserProfile.totalPhotoLimit

@Composable
fun ProfileStatsGrid(stats: ProfileStats, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCell(icon = "📅", value = "${stats.events}", label = "Events")
            StatCell(icon = "🖼️", value = "${stats.photos}/$PHOTO_LIMIT", label = "Photos")
            StatCell(icon = "🔧", value = "${stats.builds}", label = "Builds")
        }
    }
}

@Composable
fun RidingInfoCard(profile: UserProfile, modifier: Modifier = Modifier) {
    val rows = listOfNotNull(
        profile.favoriteRide.takeIf { it.isNotBlank() }?.let { "Bike" to it },
        profile.ridingSince.takeIf { it.isNotBlank() }?.let { "Riding Since" to it },
        profile.preferredRideType.takeIf { it.isNotBlank() }?.let { "Preferred Ride" to it },
        profile.favoriteRoute.takeIf { it.isNotBlank() }?.let { "Favorite Route" to it },
        profile.club.takeIf { it.isNotBlank() }?.let { "Club" to it },
    )
    if (rows.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚲", fontSize = 16.sp)
                Spacer(Modifier.size(8.dp))
                Text("RIDING INFO", color = Yellow, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
            rows.forEach { (label, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        label,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.size(width = 110.dp, height = 16.dp),
                    )
                    Text(value, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Three colored shortcut tiles matching the iOS profile bottom row. Show on every profile
 * (own and public) — they're "actions the viewer can take", not "things this user has done".
 */
@Composable
fun QuickActionTiles(
    onPostEvent: () -> Unit,
    onUploadPhoto: () -> Unit,
    onPostBuild: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionTile(
            label = "Post Event",
            icon = "+",
            background = Color(0xFFFFD600),
            foreground = Color.Black,
            onClick = onPostEvent,
            modifier = Modifier.weight(1f),
        )
        ActionTile(
            label = "Event Photo",
            icon = "📷",
            background = Color(0xFFB066FF),
            foreground = Color.White,
            onClick = onUploadPhoto,
            modifier = Modifier.weight(1f),
        )
        ActionTile(
            label = "Bike Build",
            icon = "🔧",
            background = Color(0xFFFF8800),
            foreground = Color.White,
            onClick = onPostBuild,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCell(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ActionTile(
    label: String,
    icon: String,
    background: Color,
    foreground: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = background,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    ) {
        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(icon, color = foreground, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(label, color = foreground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
