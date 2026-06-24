package com.example.wifinetworkscanner.domain.repository

import com.example.wifinetworkscanner.domain.model.ScanSettings
import kotlinx.coroutines.flow.Flow

interface ScanSettingsRepository {

    fun observeScanSettings(): Flow<ScanSettings>

    suspend fun updateScanSettings(scanSettings: ScanSettings)
}