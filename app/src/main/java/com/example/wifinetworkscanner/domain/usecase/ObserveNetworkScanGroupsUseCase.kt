package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observa o histórico agrupado por rede Wi-Fi.
 */
class ObserveNetworkScanGroupsUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    operator fun invoke(): Flow<List<NetworkScanGroup>> {
        return scanHistoryRepository.observeNetworkScanGroups()
    }
}