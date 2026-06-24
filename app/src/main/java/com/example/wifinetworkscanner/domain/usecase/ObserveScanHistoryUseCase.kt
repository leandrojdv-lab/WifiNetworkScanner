package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observa as varreduras salvas mais recentes.
 */
class ObserveScanHistoryUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    operator fun invoke(limit: Int = ScanHistoryRepository.DEFAULT_HISTORY_LIMIT): Flow<List<ScanHistory>> {
        require(limit > 0) {
            "O limite de histórico deve ser maior que zero."
        }

        return scanHistoryRepository.observeRecentScanHistory(limit = limit)
    }
}