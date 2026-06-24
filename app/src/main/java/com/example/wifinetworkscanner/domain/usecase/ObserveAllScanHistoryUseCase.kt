package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observa todas as varreduras salvas localmente.
 */
class ObserveAllScanHistoryUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    operator fun invoke(): Flow<List<ScanHistory>> {
        return scanHistoryRepository.observeAllScanHistory()
    }
}