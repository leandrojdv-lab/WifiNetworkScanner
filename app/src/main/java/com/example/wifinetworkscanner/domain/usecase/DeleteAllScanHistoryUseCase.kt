package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject

/**
 * Remove todo o histórico de varreduras salvo localmente.
 */
class DeleteAllScanHistoryUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    suspend operator fun invoke() {
        scanHistoryRepository.deleteAllScanHistory()
    }
}