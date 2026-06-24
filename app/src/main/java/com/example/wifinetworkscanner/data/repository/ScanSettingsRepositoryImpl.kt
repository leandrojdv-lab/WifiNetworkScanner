package com.example.wifinetworkscanner.data.repository

import com.example.wifinetworkscanner.data.local.ScanSettingsDataStore
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.ScanSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanSettingsRepositoryImpl @Inject constructor(
    private val scanSettingsDataStore: ScanSettingsDataStore
) : ScanSettingsRepository {

    override fun observeScanSettings(): Flow<ScanSettings> {
        return scanSettingsDataStore.observeScanSettings()
    }

    override suspend fun updateScanSettings(scanSettings: ScanSettings) {
        scanSettingsDataStore.updateScanSettings(scanSettings)
    }
}