package live.hails.hailsdotgo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import live.hails.hailsdotgo.ui.auth.LoginScreen
import live.hails.hailsdotgo.ui.box.PokemonBoxScreen
import live.hails.hailsdotgo.ui.dashboard.DashboardScreen
import live.hails.hailsdotgo.ui.events.EventsScreen
import live.hails.hailsdotgo.ui.iv.IVResultScreen
import live.hails.hailsdotgo.ui.raid.RaidFinderScreen
import live.hails.hailsdotgo.ui.raid.RaidLobbyScreen
import live.hails.hailsdotgo.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Login     : Screen("login")
    object Dashboard : Screen("dashboard")
    object IVResult  : Screen("iv/result")
    object Box       : Screen("box")
    object Raids     : Screen("raids")
    object RaidLobby : Screen("raids/lobby/{lobbyId}") {
        fun withId(id: String) = "raids/lobby/$id"
    }
    object Events    : Screen("events")
    object Settings  : Screen("settings")
}

private val bottomNavRoutes = setOf(
    Screen.Box.route, Screen.Raids.route, Screen.Events.route, Screen.Dashboard.route, Screen.Settings.route,
)

@Composable
fun NavGraph(
    isLoggedIn    : Boolean  = false,
    onStartScanner: () -> Unit = {},
    scanCompleted : Boolean  = false,
    onScanConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    // Navigate to IV Result screen when a scan completes from the overlay
    LaunchedEffect(scanCompleted) {
        if (scanCompleted && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.IVResult.route)
            onScanConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Box.route,
                        onClick  = { navController.navigate(Screen.Box.route) },
                        icon     = { Icon(Icons.AutoMirrored.Filled.ViewList, null) },
                        label    = { Text("IV Box") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Raids.route,
                        onClick  = { navController.navigate(Screen.Raids.route) },
                        icon     = { Icon(Icons.Default.People, null) },
                        label    = { Text("Raids") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Events.route,
                        onClick  = { navController.navigate(Screen.Events.route) },
                        icon     = { Icon(Icons.Default.CalendarMonth, null) },
                        label    = { Text("Events") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Dashboard.route,
                        onClick  = { navController.navigate(Screen.Dashboard.route) },
                        icon     = { Icon(Icons.Default.Home, null) },
                        label    = { Text("Home") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick  = { navController.navigate(Screen.Settings.route) },
                        icon     = { Icon(Icons.Default.Settings, null) },
                        label    = { Text("Settings") },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
            modifier         = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(onStartScanner = onStartScanner)
            }
            composable(Screen.IVResult.route) {
                IVResultScreen()
            }
            composable(Screen.Box.route) {
                PokemonBoxScreen()
            }
            composable(Screen.Raids.route) {
                RaidFinderScreen(onLobbySelected = { id ->
                    navController.navigate(Screen.RaidLobby.withId(id))
                })
            }
            composable(Screen.Events.route) {
                EventsScreen()
            }
            composable(Screen.RaidLobby.route) { backStack ->
                val lobbyId = backStack.arguments?.getString("lobbyId") ?: return@composable
                RaidLobbyScreen(
                    lobbyId = lobbyId,
                    onBack  = { navController.popBackStack() },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}
