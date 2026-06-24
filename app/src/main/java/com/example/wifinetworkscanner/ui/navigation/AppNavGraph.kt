package com.example.wifinetworkscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wifinetworkscanner.ui.screens.about.AboutScreen
import com.example.wifinetworkscanner.ui.screens.history.HistoryScreen
import com.example.wifinetworkscanner.ui.screens.historydetail.HistoryDetailScreen
import com.example.wifinetworkscanner.ui.screens.networkscanner.NetworkScannerScreen
import com.example.wifinetworkscanner.ui.screens.networkscans.NetworkScansScreen
import com.example.wifinetworkscanner.ui.screens.search.SearchScreen
import com.example.wifinetworkscanner.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Scanner.route,
        modifier = modifier
    ) {
        composable(route = AppDestination.Scanner.route) {
            NetworkScannerScreen(
                onMenuClick = onOpenDrawer
            )
        }

        composable(route = AppDestination.History.route) {
            HistoryScreen(
                onMenuClick = onOpenDrawer,
                onNetworkGroupClick = { networkIdentifier ->
                    navController.navigate(
                        AppDestination.NetworkScans.createRoute(
                            networkIdentifier = networkIdentifier
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestination.Search.route) {
            SearchScreen(
                onMenuClick = onOpenDrawer,
                onScanHistoryClick = { scanHistoryId ->
                    navController.navigate(
                        AppDestination.HistoryDetail.createRoute(
                            scanHistoryId = scanHistoryId
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestination.Settings.route) {
            SettingsScreen(
                onMenuClick = onOpenDrawer
            )
        }

        composable(
            route = AppDestination.NetworkScans.route,
            arguments = listOf(
                navArgument(AppDestination.NetworkScans.ARG_NETWORK_IDENTIFIER) {
                    type = NavType.StringType
                }
            )
        ) {
            NetworkScansScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onScanHistoryClick = { scanHistoryId ->
                    navController.navigate(
                        AppDestination.HistoryDetail.createRoute(
                            scanHistoryId = scanHistoryId
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestination.About.route) {
            AboutScreen(
                onMenuClick = onOpenDrawer
            )
        }

        composable(
            route = AppDestination.HistoryDetail.route,
            arguments = listOf(
                navArgument(AppDestination.HistoryDetail.ARG_SCAN_HISTORY_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            HistoryDetailScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}