package com.example.wifinetworkscanner.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed interface AppDestination {

    val route: String
    val title: String

    data object Scanner : AppDestination {
        override val route: String = "scanner"
        override val title: String = "Scanner"
    }

    data object History : AppDestination {
        override val route: String = "history"
        override val title: String = "Histórico"
    }

    data object Search : AppDestination {
        override val route: String = "search"
        override val title: String = "Pesquisa"
    }

    data object Settings : AppDestination {
        override val route: String = "settings"
        override val title: String = "Configurações"
    }

    data object NetworkScans : AppDestination {
        const val ARG_NETWORK_IDENTIFIER = "networkIdentifier"

        override val route: String = "history/network?$ARG_NETWORK_IDENTIFIER={$ARG_NETWORK_IDENTIFIER}"
        override val title: String = "Varreduras da rede"

        fun createRoute(networkIdentifier: String): String {
            val encodedIdentifier = encodeRouteQueryValue(value = networkIdentifier)

            return "history/network?$ARG_NETWORK_IDENTIFIER=$encodedIdentifier"
        }
    }

    data object About : AppDestination {
        override val route: String = "about"
        override val title: String = "Sobre"
    }

    data object HistoryDetail : AppDestination {
        const val ARG_SCAN_HISTORY_ID = "scanHistoryId"

        override val route: String = "history/{$ARG_SCAN_HISTORY_ID}"
        override val title: String = "Detalhes"

        fun createRoute(scanHistoryId: Long): String {
            return "history/$scanHistoryId"
        }
    }

    companion object {
        val TOP_LEVEL_DESTINATIONS: List<AppDestination> = listOf(
            Scanner,
            History,
            Search,
            Settings,
            About
        )

        fun selectedTopLevelDestination(route: String?): AppDestination {
            return when {
                route == History.route -> History
                route == Search.route -> Search
                route == Settings.route -> Settings
                route == About.route -> About
                route?.startsWith("history/") == true -> History
                else -> Scanner
            }
        }

        private fun encodeRouteQueryValue(value: String): String {
            return URLEncoder
                .encode(value, StandardCharsets.UTF_8.name())
                .replace(ENCODED_SPACE_AS_PLUS, ENCODED_SPACE)
        }

        private const val ENCODED_SPACE_AS_PLUS = "+"
        private const val ENCODED_SPACE = "%20"
    }
}