package com.example.wifinetworkscanner.domain.model

data class NetworkDevice(
    val ipAddress: String,
    val latencyMillis: Long,
    val scannedAtEpochMillis: Long,
    val detectionMethod: DeviceDetectionMethod,
    val openPorts: List<Int> = emptyList(),
    val label: String? = null
)