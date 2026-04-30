package com.onthaset.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { Placeholder("Home — On Tha Set") }
        composable(Routes.LOGIN) { Placeholder("Login") }
        composable(Routes.EVENTS) { Placeholder("Events") }
        composable(Routes.NATIONAL_RUN_CALENDAR) { Placeholder("National Run Calendar") }
        composable(Routes.PROFILE) { Placeholder("Profile") }
        composable(Routes.BIKE_BUILDS) { Placeholder("Bike Builds") }
        composable(Routes.WEATHER) { Placeholder("Ride Forecast") }
    }
}

@Composable
private fun Placeholder(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label)
    }
}
