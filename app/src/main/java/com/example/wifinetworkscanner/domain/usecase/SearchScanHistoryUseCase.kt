package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanHistorySearchFilter
import javax.inject.Inject

/**
 * Aplica filtros de pesquisa sobre o histórico salvo.
 */
class SearchScanHistoryUseCase @Inject constructor() {

    operator fun invoke(
        scanHistoryList: List<ScanHistory>,
        filter: ScanHistorySearchFilter
    ): List<ScanHistory> {
        if (!filter.isOpenPortValid) {
            return emptyList()
        }

        return scanHistoryList
            .filter { scanHistory ->
                scanHistory.matchesQuery(filter.normalizedQuery) &&
                        scanHistory.matchesOpenPort(filter.openPort)
            }
            .sortedByDescending { scanHistory ->
                scanHistory.completedAtEpochMillis
            }
    }

    private fun ScanHistory.matchesQuery(query: String): Boolean {
        if (query.isBlank()) {
            return true
        }

        val normalizedQuery = query.lowercase()

        return networkName.containsNormalized(normalizedQuery) ||
                networkIdentifier.containsNormalized(normalizedQuery) ||
                localIpAddress.containsNormalized(normalizedQuery) ||
                gatewayIpAddress.orEmpty().containsNormalized(normalizedQuery) ||
                devices.any { device ->
                    device.matchesQuery(normalizedQuery)
                }
    }

    private fun NetworkDevice.matchesQuery(query: String): Boolean {
        return ipAddress.containsNormalized(query) ||
                label.orEmpty().containsNormalized(query) ||
                detectionMethod.name.containsNormalized(query) ||
                openPorts.any { port ->
                    port.toString().contains(query)
                }
    }

    private fun ScanHistory.matchesOpenPort(openPort: Int?): Boolean {
        if (openPort == null) {
            return true
        }

        return devices.any { device ->
            openPort in device.openPorts
        }
    }

    private fun String.containsNormalized(query: String): Boolean {
        return lowercase().contains(query)
    }
}