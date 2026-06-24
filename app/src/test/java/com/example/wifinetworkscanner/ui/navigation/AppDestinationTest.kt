package com.example.wifinetworkscanner.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppDestinationTest {

    @Test
    fun topLevelDestinations_whenRead_shouldContainExpectedItems() {
        assertEquals(
            listOf(
                AppDestination.Scanner,
                AppDestination.History,
                AppDestination.Search,
                AppDestination.Settings,
                AppDestination.About
            ),
            AppDestination.TOP_LEVEL_DESTINATIONS
        )
    }

    @Test
    fun scannerAndHistory_whenCompared_shouldHaveDifferentRoutes() {
        assertNotEquals(
            AppDestination.Scanner.route,
            AppDestination.History.route
        )
    }

    @Test
    fun search_whenRead_shouldUseExpectedRoute() {
        assertEquals(
            "search",
            AppDestination.Search.route
        )
    }

    @Test
    fun networkScans_whenRouteIsRead_shouldUseQueryParameter() {
        assertEquals(
            "history/network?networkIdentifier={networkIdentifier}",
            AppDestination.NetworkScans.route
        )
    }

    @Test
    fun networkScans_whenCreateRoute_shouldEncodeNetworkIdentifierAsQueryParameter() {
        assertEquals(
            "history/network?networkIdentifier=ssid%3Aminha%20rede",
            AppDestination.NetworkScans.createRoute(
                networkIdentifier = "ssid:minha rede"
            )
        )
    }

    @Test
    fun networkScans_whenCreateRouteContainsSlash_shouldEncodeSlashWithoutChangingPathSegments() {
        assertEquals(
            "history/network?networkIdentifier=ssid%3Arede%2Fvisitantes",
            AppDestination.NetworkScans.createRoute(
                networkIdentifier = "ssid:rede/visitantes"
            )
        )
    }

    @Test
    fun historyDetail_whenCreateRoute_shouldIncludeScanHistoryId() {
        assertEquals(
            "history/10",
            AppDestination.HistoryDetail.createRoute(scanHistoryId = 10L)
        )
    }

    @Test
    fun selectedTopLevelDestination_whenRouteIsSearch_shouldSelectSearch() {
        assertEquals(
            AppDestination.Search,
            AppDestination.selectedTopLevelDestination("search")
        )
    }

    @Test
    fun selectedTopLevelDestination_whenRouteIsNetworkScans_shouldSelectHistory() {
        assertEquals(
            AppDestination.History,
            AppDestination.selectedTopLevelDestination(
                "history/network?networkIdentifier={networkIdentifier}"
            )
        )
    }

    @Test
    fun selectedTopLevelDestination_whenRouteIsHistoryDetail_shouldSelectHistory() {
        assertEquals(
            AppDestination.History,
            AppDestination.selectedTopLevelDestination("history/10")
        )
    }
}