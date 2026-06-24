package com.example.wifinetworkscanner.data.local.projection

import androidx.room.ColumnInfo

data class ScanNetworkGroupProjection(
    @ColumnInfo(name = "networkIdentifier")
    val networkIdentifier: String,

    @ColumnInfo(name = "networkName")
    val networkName: String,

    @ColumnInfo(name = "latestGatewayIpAddress")
    val latestGatewayIpAddress: String?,

    @ColumnInfo(name = "scanCount")
    val scanCount: Long,

    @ColumnInfo(name = "latestCompletedAtEpochMillis")
    val latestCompletedAtEpochMillis: Long,

    @ColumnInfo(name = "totalFoundDeviceCount")
    val totalFoundDeviceCount: Long
)