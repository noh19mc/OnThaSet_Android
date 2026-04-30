package com.onthaset.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.onthaset.app.auth.AuthState
import com.onthaset.app.auth.AuthViewModel
import com.onthaset.app.auth.ui.AuthScreen
import com.onthaset.app.auth.ui.GateScreen
import com.onthaset.app.events.ui.EventDetailScreen
import com.onthaset.app.events.ui.EventsScreen
import com.onthaset.app.events.ui.NationalCalendarScreen
import com.onthaset.app.home.HomeScreen
import com.onthaset.app.profile.ui.EditProfileScreen
import com.onthaset.app.profile.ui.ProfileScreen
import com.onthaset.app.weather.ui.WeatherScreen

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    var guestMode by remember { mutableStateOf(false) }

    LaunchedEffect(authState, guestMode) {
        when (authState) {
            is AuthState.SignedIn -> navController.navigateSingleTopTo(Routes.HOME)
            is AuthState.SignedOut -> {
                if (guestMode) navController.navigateSingleTopTo(Routes.HOME)
                else navController.navigateSingleTopTo(Routes.GATE)
            }
            AuthState.Loading -> Unit
        }
    }

    when (authState) {
        AuthState.Loading -> SplashLoading()
        else -> NavHost(
            navController = navController,
            startDestination = Routes.GATE,
        ) {
            composable(Routes.GATE) {
                GateScreen(
                    onContinueWithEmail = { navController.navigate(Routes.AUTH) },
                    onSkip = { guestMode = true },
                )
            }
            composable(Routes.AUTH) {
                AuthScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenEvents = { navController.navigate(Routes.EVENTS) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) },
                    onOpenCalendar = { navController.navigate(Routes.NATIONAL_RUN_CALENDAR) },
                    onOpenWeather = { navController.navigate(Routes.WEATHER) },
                )
            }
            composable(Routes.EVENTS) {
                EventsScreen(
                    onEventClick = { id -> navController.navigate(Routes.eventDetail(id)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.EVENT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                EventDetailScreen(eventId = id, onBack = { navController.popBackStack() })
            }
            composable(Routes.NATIONAL_RUN_CALENDAR) {
                NationalCalendarScreen(
                    onEventClick = { id -> navController.navigate(Routes.eventDetail(id)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.EDIT_PROFILE) },
                )
            }
            composable(Routes.EDIT_PROFILE) {
                val parentEntry = remember(it) { navController.getBackStackEntry(Routes.PROFILE) }
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(parentEntry),
                )
            }
            composable(Routes.BIKE_BUILDS) { Placeholder("Bike Builds") }
            composable(Routes.WEATHER) {
                WeatherScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun SplashLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color(0xFFFFD600))
    }
}

@Composable
private fun Placeholder(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White)
    }
}
