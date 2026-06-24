package com.example.wifinetworkscanner.ui.screens.networkscanner

import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.model.WifiNetworkInfo
import com.example.wifinetworkscanner.ui.text.UiText

data class NetworkScannerUiState(
    val isScanning: Boolean = false,
    val networkName: String = WifiNetworkInfo.UNKNOWN_NETWORK_NAME,
    val networkIdentifier: String = WifiNetworkInfo.UNKNOWN_NETWORK_IDENTIFIER,
    val localIpAddress: String = "",
    val gatewayIpAddress: String = "",
    val networkPrefixLength: Int = 0,
    val scannedHostCount: Int = 0,
    val totalHostCount: Int = 0,
    val devices: List<NetworkDevice> = emptyList(),
    val scanSettings: ScanSettings = ScanSettings.default(),
    val currentScanStartedAtEpochMillis: Long = 0L,
    val errorMessage: UiText? = null
) {

    val progress: Float
        get() {
            if (totalHostCount <= 0) {
                return 0f
            }

            return scannedHostCount.toFloat() / totalHostCount.toFloat()
        }
}

sealed interface NetworkScannerUiEffect {

    data class ShowMessage(
        val message: UiText
    ) : NetworkScannerUiEffect
}