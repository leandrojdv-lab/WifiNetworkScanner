package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject

/**
 * Salva uma varredura concluída no histórico local.
 */
class SaveScanHistoryUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {

    suspend operator fun invoke(scanHistory: ScanHistory): Long {
        require(scanHistory.startedAtEpochMillis > 0L) {
            "A data de início da varredura é inválida."
        }

        require(scanHistory.completedAtEpochMillis >= scanHistory.startedAtEpochMillis) {
            "A data de conclusão da varredura é inválida."
        }

        require(scanHistory.networkName.isNotBlank()) {
            "O nome da rede é obrigatório."
        }

        require(scanHistory.networkIdentifier.isNotBlank()) {
            "O identificador da rede é obrigatório."
        }

        require(scanHistory.localIpAddress.isNotBlank()) {
            "O IP local é obrigatório."
        }

        require(scanHistory.prefixLength in 1..32) {
            "O prefixo da rede é inválido."
        }

        require(scanHistory.totalHostCount >= 0) {
            "O total de hosts é inválido."
        }

        require(scanHistory.scannedHostCount >= 0) {
            "O total de hosts verificados é inválido."
        }

        return scanHistoryRepository.saveScanHistory(scanHistory = scanHistory)
    }
}