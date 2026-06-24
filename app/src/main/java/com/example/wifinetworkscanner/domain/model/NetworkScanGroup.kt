package com.example.wifinetworkscanner.domain.model

data class NetworkScanGroup(
    val networkIdentifier: String,
    val networkName: String,
    val latestGatewayIpAddress: String?,
    val scanCount: Long,
    val latestCompletedAtEpochMillis: Long,
    val totalFoundDeviceCount: Long
)