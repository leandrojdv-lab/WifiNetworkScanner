package com.example.wifinetworkscanner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wifinetworkscanner.R
import kotlinx.coroutines.launch

@Composable
fun AppDrawerScaffold(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val selectedDestination = AppDestination.selectedTopLevelDestination(currentRoute)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                selectedDestination = selectedDestination,
                onDestinationClick = { destination ->
                    coroutineScope.launch {
                        drawerState.close()
                    }

                    navController.navigateToTopLevelDestination(destination)
                }
            )
        },
        content = {
            AppNavGraph(
                navController = navController,
                onOpenDrawer = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                },
                modifier = modifier
            )
        }
    )
}

@Composable
private fun AppDrawerContent(
    selectedDestination: AppDestination,
    onDestinationClick: (AppDestination) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(336.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 16.dp)
        ) {
            DrawerHeader()

            Spacer(modifier = Modifier.padding(top = 16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.padding(top = 12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppDestination.TOP_LEVEL_DESTINATIONS.forEach { destination ->
                    ModernDrawerItem(
                        destination = destination,
                        selected = selectedDestination == destination,
                        onClick = {
                            onDestinationClick(destination)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            DrawerFooter()
        }
    }
}

@Composable
private fun DrawerHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.drawer_header_badge),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(id = R.string.drawer_header_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = stringResource(id = R.string.drawer_header_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ModernDrawerItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val itemVisual = destination.toDrawerItemVisual()

    NavigationDrawerItem(
        label = {
            Column {
                Text(
                    text = itemVisual.title,
                    fontWeight = if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )

                Text(
                    text = itemVisual.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        selected = selected,
        onClick = onClick,
        icon = {
            DrawerItemIcon(
                initials = itemVisual.initials,
                selected = selected
            )
        },
        badge = {
            if (destination == AppDestination.Search) {
                Text(
                    text = stringResource(id = R.string.drawer_badge_new),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedContainerColor = MaterialTheme.colorScheme.surface,
            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DrawerItemIcon(
    initials: String,
    selected: Boolean
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DrawerFooter() {
    HorizontalDivider()

    Spacer(modifier = Modifier.padding(top = 12.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.drawer_footer_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            AssistChip(
                onClick = {},
                label = {
                    Text(text = stringResource(id = R.string.drawer_footer_chip))
                }
            )

            Text(
                text = stringResource(id = R.string.drawer_footer_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun NavHostController.navigateToTopLevelDestination(
    destination: AppDestination
) {
    val currentRoute = currentBackStackEntry?.destination?.route

    if (currentRoute == destination.route) {
        return
    }

    navigate(destination.route) {
        popUpTo(AppDestination.Scanner.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppDestination.toDrawerItemVisual(): DrawerItemVisual {
    return when (this) {
        AppDestination.Scanner -> DrawerItemVisual(
            title = stringResource(id = R.string.drawer_scanner_title),
            initials = "SC",
            description = stringResource(id = R.string.drawer_scanner_description)
        )

        AppDestination.History -> DrawerItemVisual(
            title = stringResource(id = R.string.drawer_history_title),
            initials = "HI",
            description = stringResource(id = R.string.drawer_history_description)
        )

        AppDestination.Search -> DrawerItemVisual(
            title = stringResource(id = R.string.drawer_search_title),
            initials = "PE",
            description = stringResource(id = R.string.drawer_search_description)
        )

        AppDestination.Settings -> DrawerItemVisual(
            title = stringResource(id = R.string.drawer_settings_title),
            initials = "CF",
            description = stringResource(id = R.string.drawer_settings_description)
        )

        AppDestination.About -> DrawerItemVisual(
            title = stringResource(id = R.string.drawer_about_title),
            initials = "SB",
            description = stringResource(id = R.string.drawer_about_description)
        )

        AppDestination.NetworkScans -> DrawerItemVisual(
            title = stringResource(id = R.string.drawer_network_scans_title),
            initials = "VR",
            description = stringResource(id = R.string.drawer_network_scans_description)
        )

        AppDestination.HistoryDetail -> DrawerItemVisual(
            title = stringResource(id = R.string.drawer_history_detail_title),
            initials = "DT",
            description = stringResource(id = R.string.drawer_history_detail_description)
        )
    }
}

private data class DrawerItemVisual(
    val title: String,
    val initials: String,
    val description: String
)