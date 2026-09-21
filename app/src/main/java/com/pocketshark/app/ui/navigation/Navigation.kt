package com.pocketshark.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pocketshark.app.ui.screens.*
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.PacketsViewModel

/**
 * Navigation routes for PocketShark.
 */
object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val CAPTURE = "capture"
    const val PACKETS = "packets"
    const val PACKET_DETAIL = "packet_detail/{packetId}"
    const val ANALYTICS = "analytics"
    const val CONNECTIONS = "connections"
    const val SECURITY = "security"
    const val PCAP_FILES = "pcap_files"
    const val SETTINGS = "settings"
    const val STATS_OVERVIEW = "stats_overview"
    const val PROTOCOL_DETAILS = "protocol_details"

    fun packetDetail(packetId: Long) = "packet_detail/$packetId"
}

/**
 * Bottom nav items.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.CAPTURE, "Capture", Icons.Filled.Sensors, Icons.Outlined.Sensors),
    BottomNavItem(Routes.PACKETS, "Packets", Icons.Filled.ListAlt, Icons.Outlined.ListAlt),
    BottomNavItem(Routes.ANALYTICS, "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    BottomNavItem(Routes.CONNECTIONS, "Connect", Icons.Filled.Hub, Icons.Outlined.Hub),
    BottomNavItem(Routes.SECURITY, "Security", Icons.Filled.Shield, Icons.Outlined.Shield),
)

/**
 * Main navigation host.
 */
@Composable
fun PocketSharkNavigation() {
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                Routes.SPLASH,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                SplashScreen(
                    onNavigateToDashboard = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                Routes.DASHBOARD,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                DashboardScreen(
                    onNavigateToPacketDetail = { packetId ->
                        navController.navigate(Routes.packetDetail(packetId))
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onViewAllClick = {
                        navController.navigate(Routes.STATS_OVERVIEW)
                    },
                    onViewDetailsClick = {
                        navController.navigate(Routes.PROTOCOL_DETAILS)
                    }
                )
            }

            composable(
                Routes.PACKET_DETAIL,
                enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
                exitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200)) }
            ) { backStackEntry ->
                val packetId = backStackEntry.arguments?.getString("packetId")?.toLongOrNull()
                val packetsVm: PacketsViewModel = viewModel()
                val packet = packetId?.let { packetsVm.getPacketById(it) }

                PacketDetailScreen(
                    packet = packet,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.PCAP_FILES,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                PcapFilesScreen()
            }

            composable(
                Routes.SETTINGS,
                enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
                exitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200)) }
            ) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.STATS_OVERVIEW,
                enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
                exitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200)) }
            ) {
                StatsOverviewScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.PROTOCOL_DETAILS,
                enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
                exitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200)) }
            ) {
                ProtocolDetailsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
