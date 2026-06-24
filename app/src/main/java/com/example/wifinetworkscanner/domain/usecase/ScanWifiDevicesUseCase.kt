package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.NetworkScanEvent
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.NetworkScannerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ScanWifiDevicesUseCase @Inject constructor(
    private val networkScannerRepository: NetworkScannerRepository
) {

    operator fun invoke(
        scanSettings: ScanSettings
    ): Flow<NetworkScanEvent> {
        require(scanSettings.maxHosts in ScanSettings.MIN_HOSTS..ScanSettings.MAX_HOSTS_LIMIT) {
            "maxHosts deve estar entre ${ScanSettings.MIN_HOSTS} e ${ScanSettings.MAX_HOSTS_LIMIT}."
        }

        require(scanSettings.timeoutMillis in ScanSettings.MIN_TIMEOUT_MILLIS..ScanSettings.MAX_TIMEOUT_MILLIS) {
            "timeoutMillis deve estar entre ${ScanSettings.MIN_TIMEOUT_MILLIS} e ${ScanSettings.MAX_TIMEOUT_MILLIS}."
        }

        require(scanSettings.parallelism in ScanSettings.MIN_PARALLELISM..ScanSettings.MAX_PARALLELISM) {
            "parallelism deve estar entre ${ScanSettings.MIN_PARALLELISM} e ${ScanSettings.MAX_PARALLELISM}."
        }

        return networkScannerRepository.scanConnectedDevices(
            maxHosts = scanSettings.maxHosts,
            timeoutMillis = scanSettings.timeoutMillis,
            parallelism = scanSettings.parallelism
        )
    }
}