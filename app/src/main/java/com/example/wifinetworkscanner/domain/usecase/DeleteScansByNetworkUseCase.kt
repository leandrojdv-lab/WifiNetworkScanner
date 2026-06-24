package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject

/**
 * Remove todas as varreduras salvas de uma rede específica.
 */
class DeleteScansByNetworkUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    suspend operator fun invoke(networkIdentifier: String) {
        require(networkIdentifier.isNotBlank()) {
            "O identificador da rede é obrigatório."
        }

        scanHistoryRepository.deleteScanHistoryByNetworkIdentifier(
            networkIdentifier = networkIdentifier
        )
    }
}