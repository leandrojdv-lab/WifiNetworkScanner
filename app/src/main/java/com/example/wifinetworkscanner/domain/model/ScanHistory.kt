package com.example.wifinetworkscanner.domain.model

data class ScanHistory(
    val id: Long,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long,
    val networkName: String,
    val networkIdentifier: String,
    val localIpAddress: String,
    val gatewayIpAddress: String?,
    val prefixLength: Int,
    val totalHostCount: Int,
    val scannedHostCount: Int,
    val scanSettings: ScanSettings,
    val devices: List<NetworkDevice>
) {

    val foundDeviceCount: Int
        get() = devices.size
}