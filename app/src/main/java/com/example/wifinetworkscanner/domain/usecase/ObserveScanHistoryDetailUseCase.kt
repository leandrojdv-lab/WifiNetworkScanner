package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observa os detalhes de uma varredura salva pelo identificador local.
 */
class ObserveScanHistoryDetailUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    operator fun invoke(scanHistoryId: Long): Flow<ScanHistory?> {
        require(scanHistoryId > 0L) {
            "O identificador do histórico é inválido."
        }

        return scanHistoryRepository.observeScanHistoryById(scanHistoryId = scanHistoryId)
    }
}