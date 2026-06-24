package com.example.wifinetworkscanner.domain.model

sealed interface NetworkScanEvent {

    data class Started(
        val networkInfo: WifiNetworkInfo
    ) : NetworkScanEvent

    data class DeviceFound(
        val device: NetworkDevice
    ) : NetworkScanEvent

    data class Progress(
        val scannedHostCount: Int,
        val totalHostCount: Int
    ) : NetworkScanEvent

    data class Completed(
        val devices: List<NetworkDevice>
    ) : NetworkScanEvent

    data class Failed(
        val message: String
    ) : NetworkScanEvent
}