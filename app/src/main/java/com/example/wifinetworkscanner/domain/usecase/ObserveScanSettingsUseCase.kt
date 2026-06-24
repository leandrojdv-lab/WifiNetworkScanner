package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.ScanSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveScanSettingsUseCase @Inject constructor(
    private val scanSettingsRepository: ScanSettingsRepository
) {

    operator fun invoke(): Flow<ScanSettings> {
        return scanSettingsRepository.observeScanSettings()
    }
}