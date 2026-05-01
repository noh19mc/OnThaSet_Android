package com.onthaset.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.onthaset.app.auth.AuthState
import com.onthaset.app.auth.AuthViewModel
import com.onthaset.app.auth.ui.AuthScreen
import com.onthaset.app.auth.ui.GateScreen
import com.onthaset.app.events.ui.CalendarMapScreen
import com.onthaset.app.events.ui.CreateEventScreen
import com.onthaset.app.events.ui.EventDetailScreen
import com.onthaset.app.events.ui.EventsScreen
import com.onthaset.app.events.ui.NationalCalendarScreen
import com.onthaset.app.home.MoreScreen
import com.onthaset.app.legal.LegalAcceptanceViewModel
import com.onthaset.app.legal.ui.LegalAcceptanceScreen
import com.onthaset.app.profile.ui.EditProfileScreen
import com.onthaset.app.profile.ui.OnboardingScreen
import com.onthaset.app.profile.ui.ProfileScreen
import com.onthaset.app.profile.ui.PublicProfileScreen
import com.onthaset.app.reports.ui.ReportEventScreen
import com.onthaset.app.bikes.ui.AddBikeBuildScreen
import com.onthaset.app.bikes.ui.BikesScreen
import com.onthaset.app.admin.ui.AdminScreen
import com.onthaset.app.billing.ui.PaywallScreen
import com.onthaset.app.directory.ui.DirectoryScreen
import com.onthaset.app.directory.ui.SubmitAdScreen
import com.onthaset.app.eventphotos.ui.AddEventPhotoScreen
import com.onthaset.app.eventphotos.ui.EventPhotosScreen
import com.onthaset.app.weather.ui.WeatherScreen

@Composable
fun AppNavigation() {
    val legalViewModel: LegalAcceptanceViewModel = hiltViewModel()
    val legalAccepted by legalViewModel.accepted.collectAsStateWithLifecycle()

    // Block everything else until the EVENT LIABILITY NOTICE has been acknowledged.
    // null = still reading from DataStore; show splash. false = show acceptance screen.
    when (legalAccepted) {
        null -> {
            SplashLoading()
            return
        }
        false -> {
            LegalAcceptanceScreen(
                onAccepted = { /* StateFlow update will flip this branch on next recompose. */ },
                viewModel = legalViewModel,
            )
            return
        }
        true -> Unit // fall through to the rest of the app
    }

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    var guestMode by remember { mutableStateOf(false) }

    LaunchedEffect(authState, guestMode) {
        when (authState) {
            is AuthState.SignedIn -> navController.navigateSingleTopTo(Routes.EVENTS)
            is AuthState.SignedOut -> {
                if (guestMode) navController.navigateSingleTopTo(Routes.EVENTS)
                else navController.navigateSingleTopTo(Routes.GATE)
            }
            AuthState.Loading -> Unit
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomNav = authState is AuthState.SignedIn || (authState is AuthState.SignedOut && guestMode)
    val isAuthRoute = currentRoute in routesWithoutBottomNav

    when (authState) {
        AuthState.Loading -> SplashLoading()
        else -> Scaffold(
            containerColor = Color.Black,
            bottomBar = {
                if (showBottomNav && !isAuthRoute) {
                    NavigationBar(containerColor = Color(0xFF111111)) {
                        BottomTab.entries.forEach { tab ->
                            val selected = tab.matches(currentRoute)
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color(0xFFFFD600),
                                    indicatorColor = Color(0xFFFFD600),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.GATE,
                modifier = Modifier.padding(padding),
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
            composable(Routes.MORE) {
                MoreScreen(
                    onOpenBikes = { navController.navigate(Routes.BIKE_BUILDS) },
                    onOpenEventPhotos = { navController.navigate(Routes.EVENT_PHOTOS) },
                    onOpenDirectory = { navController.navigate(Routes.DIRECTORY) },
                    onOpenPaywall = { navController.navigate(Routes.PAYWALL) },
                    onOpenAdmin = { navController.navigate(Routes.ADMIN) },
                    onOpenOnboarding = { navController.navigate(Routes.ONBOARDING) },
                    // Guest mode is a nav-layer flag separate from the auth session, so
                    // a real signOut() call does nothing for guests. Clear both here
                    // so "Sign Out" routes back to the gate either way.
                    onSignOut = {
                        guestMode = false
                        authViewModel.signOut()
                    },
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onSkip = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(Routes.ADMIN) {
                AdminScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.DIRECTORY) {
                DirectoryScreen(
                    onBack = { navController.popBackStack() },
                    onSubmitAd = { navController.navigate(Routes.SUBMIT_AD) },
                )
            }
            composable(Routes.SUBMIT_AD) {
                SubmitAdScreen(
                    onBack = { navController.popBackStack() },
                    onSubmitted = { navController.popBackStack() },
                )
            }
            composable(Routes.PAYWALL) {
                PaywallScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.EVENTS) {
                EventsScreen(
                    onEventClick = { id -> navController.navigate(Routes.eventDetail(id)) },
                    onCreate = { navController.navigate(Routes.CREATE_EVENT) },
                )
            }
            composable(Routes.CREATE_EVENT) {
                CreateEventScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onSubscribe = {
                        navController.popBackStack()
                        navController.navigate(Routes.PAYWALL)
                    },
                )
            }
            composable(
                route = Routes.EVENT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                EventDetailScreen(
                    eventId = id,
                    onBack = { navController.popBackStack() },
                    onOpenPoster = { userId -> navController.navigate(Routes.publicProfile(userId)) },
                    onReport = { eventId, title -> navController.navigate(Routes.reportEvent(eventId, title)) },
                    onOpenWeather = { lat, lng, label ->
                        navController.navigate(Routes.weatherFor(lat, lng, label))
                    },
                )
            }
            composable(
                route = Routes.REPORT_EVENT,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                val title = backStackEntry.arguments?.getString("title").orEmpty()
                ReportEventScreen(
                    eventId = id,
                    eventTitle = title,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.PUBLIC_PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                PublicProfileScreen(userId = userId, onBack = { navController.popBackStack() })
            }
            composable(Routes.NATIONAL_RUN_CALENDAR) {
                NationalCalendarScreen(
                    onEventClick = { id -> navController.navigate(Routes.eventDetail(id)) },
                    onBack = { navController.popBackStack() },
                    onOpenMap = { navController.navigate(Routes.NATIONAL_RUN_CALENDAR_MAP) },
                )
            }
            composable(Routes.NATIONAL_RUN_CALENDAR_MAP) {
                val parentEntry = remember(it) { navController.getBackStackEntry(Routes.NATIONAL_RUN_CALENDAR) }
                CalendarMapScreen(
                    onEventClick = { id -> navController.navigate(Routes.eventDetail(id)) },
                    onBack = { navController.popBackStack() },
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(parentEntry),
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
            composable(Routes.BIKE_BUILDS) {
                BikesScreen(
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(Routes.ADD_BIKE_BUILD) },
                )
            }
            composable(Routes.ADD_BIKE_BUILD) {
                val parentEntry = remember(it) { navController.getBackStackEntry(Routes.BIKE_BUILDS) }
                AddBikeBuildScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(parentEntry),
                )
            }
            composable(Routes.EVENT_PHOTOS) {
                EventPhotosScreen(
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(Routes.ADD_EVENT_PHOTO) },
                )
            }
            composable(Routes.ADD_EVENT_PHOTO) {
                val parentEntry = remember(it) { navController.getBackStackEntry(Routes.EVENT_PHOTOS) }
                AddEventPhotoScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(parentEntry),
                )
            }
            composable(Routes.WEATHER) {
                WeatherScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.WEATHER_FOR_LOCATION,
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lng") { type = NavType.StringType },
                    navArgument("label") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
                val label = backStackEntry.arguments?.getString("label").orEmpty()
                WeatherScreen(
                    onBack = { navController.popBackStack() },
                    initialLat = lat,
                    initialLng = lng,
                    initialLabel = label.ifBlank { null },
                )
            }
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
