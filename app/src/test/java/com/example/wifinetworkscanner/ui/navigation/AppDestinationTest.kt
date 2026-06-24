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
    fun networkScans_whenCreateRoute_shouldEncodeNetworkIdentifier() {
        assertEquals(
            "history/network/ssid%3Aminha+rede",
            AppDestination.NetworkScans.createRoute(
                networkIdentifier = "ssid:minha rede"
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
            AppDestination.selectedTopLevelDestination("history/network/{networkIdentifier}")
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