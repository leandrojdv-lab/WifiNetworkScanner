package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.ScanSettingsRepository
import javax.inject.Inject

class UpdateScanSettingsUseCase @Inject constructor(
    private val scanSettingsRepository: ScanSettingsRepository
) {

    suspend operator fun invoke(scanSettings: ScanSettings) {
        require(scanSettings.maxHosts in ScanSettings.MIN_HOSTS..ScanSettings.MAX_HOSTS_LIMIT) {
            "maxHosts deve estar entre ${ScanSettings.MIN_HOSTS} e ${ScanSettings.MAX_HOSTS_LIMIT}."
        }

        require(scanSettings.timeoutMillis in ScanSettings.MIN_TIMEOUT_MILLIS..ScanSettings.MAX_TIMEOUT_MILLIS) {
            "timeoutMillis deve estar entre ${ScanSettings.MIN_TIMEOUT_MILLIS} e ${ScanSettings.MAX_TIMEOUT_MILLIS}."
        }

        require(scanSettings.parallelism in ScanSettings.MIN_PARALLELISM..ScanSettings.MAX_PARALLELISM) {
            "parallelism deve estar entre ${ScanSettings.MIN_PARALLELISM} e ${ScanSettings.MAX_PARALLELISM}."
        }

        scanSettingsRepository.updateScanSettings(scanSettings)
    }
}