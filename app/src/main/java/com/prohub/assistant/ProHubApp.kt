@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.prohub.assistant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.prohub.assistant.data.auth.AuthManager
import com.prohub.assistant.ui.screens.ai.AIChatScreen
import com.prohub.assistant.ui.screens.auth.AuthDialog
import com.prohub.assistant.ui.screens.fitness.FitnessScreen
import com.prohub.assistant.ui.screens.home.HomeScreen
import com.prohub.assistant.ui.screens.notifications.NotificationsScreen
import com.prohub.assistant.ui.screens.onboarding.PermissionsOnboardingScreen
import com.prohub.assistant.ui.screens.settings.SettingsScreen
import com.prohub.assistant.ui.screens.timetable.TimetableScreen
import com.prohub.assistant.ui.screens.todos.TodoListScreen
import com.prohub.assistant.ui.theme.ProHubColors
import kotlinx.coroutines.launch

@Composable
fun ProHubApp(
    authManager: AuthManager = hiltViewModel<AuthViewModel>().authManager
) {
    val navController = rememberNavController()
    val authState by authManager.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (authState.requiresAuth) {
        AuthDialog(
            isVisible = true,
            hasPin = authState.hasPin,
            onDismiss = { authManager.dismissAuth() },
            onPinSet = { pin -> authManager.setPin(pin) },
            onPinVerify = { pin -> authManager.verifyPin(pin) }
        )
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomNav = currentDestination?.route in listOf(
                "home", "todos", "timetable", "fitness", "ai", "notifications", "settings"
            )

            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar(
                    containerColor = ProHubColors.Surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                if (item.route == "settings" && authState.hasPin && !authState.isAuthenticated) {
                                    authManager.requestAuthForSettings()
                                    return@NavigationBarItem
                                }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ProHubColors.Indigo,
                                selectedTextColor = ProHubColors.Indigo,
                                unselectedIconColor = ProHubColors.Text2,
                                unselectedTextColor = ProHubColors.Text2,
                                indicatorColor = ProHubColors.Indigo.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ProHubColors.Bg
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "onboarding",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("onboarding") { PermissionsOnboardingScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("todos") { TodoListScreen(navController) }
            composable("timetable") { TimetableScreen(navController) }
            composable("fitness") { FitnessScreen(navController) }
            composable("ai") { AIChatScreen(navController) }
            composable("notifications") { NotificationsScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Default.Home),
    BottomNavItem("todos", "Tasks", Icons.Default.List),
    BottomNavItem("timetable", "Schedule", Icons.Default.CalendarMonth),
    BottomNavItem("fitness", "Fitness", Icons.Default.FitnessCenter),
    BottomNavItem("ai", "AI", Icons.Default.SmartToy),
    BottomNavItem("notifications", "Alerts", Icons.Default.Notifications),
    BottomNavItem("settings", "Settings", Icons.Default.Settings)
)
