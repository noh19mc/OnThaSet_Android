package com.onthaset.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.auth.AuthState
import com.onthaset.app.auth.AuthViewModel
import com.onthaset.app.auth.ui.OnThaSetShield

@Composable
fun HomeScreen(
    onOpenEvents: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenWeather: () -> Unit,
    onOpenBikes: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.authState.collectAsStateWithLifecycle()
    val signedIn = state as? AuthState.SignedIn

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OnThaSetShield(size = 120.dp)
            Text(
                "Welcome",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
            )
            Text(
                signedIn?.email ?: "Guest mode",
                color = Color.Gray,
                fontSize = 14.sp,
            )
            Button(
                onClick = onOpenEvents,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD600),
                    contentColor = Color.Black,
                ),
            ) {
                Text("Browse Events", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onOpenCalendar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("National Run Calendar", color = Color(0xFFFFD600))
            }
            OutlinedButton(
                onClick = onOpenWeather,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Ride Forecast", color = Color(0xFFFFD600))
            }
            OutlinedButton(
                onClick = onOpenBikes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Bike Builds", color = Color(0xFFFFD600))
            }
            OutlinedButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("My Profile", color = Color(0xFFFFD600))
            }
            OutlinedButton(
                onClick = viewModel::signOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Sign Out", color = Color(0xFFFFD600))
            }
        }
    }
}
