package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observa todas as varreduras salvas para uma rede específica.
 */
class ObserveScansByNetworkUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    operator fun invoke(networkIdentifier: String): Flow<List<ScanHistory>> {
        require(networkIdentifier.isNotBlank()) {
            "O identificador da rede é obrigatório."
        }

        return scanHistoryRepository.observeScanHistoryByNetworkIdentifier(
            networkIdentifier = networkIdentifier
        )
    }
}