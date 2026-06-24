package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject

/**
 * Remove uma varredura salva pelo identificador local.
 */
class DeleteScanHistoryUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    suspend operator fun invoke(scanHistoryId: Long) {
        require(scanHistoryId > 0L) {
            "O identificador do histórico é inválido."
        }

        scanHistoryRepository.deleteScanHistory(scanHistoryId = scanHistoryId)
    }
}