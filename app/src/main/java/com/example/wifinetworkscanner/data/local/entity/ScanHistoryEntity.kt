package com.example.wifinetworkscanner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabela_historico_varreduras",
    indices = [
        Index(value = ["started_at_epoch_millis"]),
        Index(value = ["network_identifier"])
    ]
)
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "scan_history_id")
    val scanHistoryId: Long = 0L,

    @ColumnInfo(name = "started_at_epoch_millis")
    val startedAtEpochMillis: Long,

    @ColumnInfo(name = "completed_at_epoch_millis")
    val completedAtEpochMillis: Long,

    @ColumnInfo(name = "network_name", defaultValue = "'Rede desconhecida'")
    val networkName: String,

    @ColumnInfo(name = "network_identifier", defaultValue = "'unknown_network'")
    val networkIdentifier: String,

    @ColumnInfo(name = "local_ip_address")
    val localIpAddress: String,

    @ColumnInfo(name = "gateway_ip_address")
    val gatewayIpAddress: String?,

    @ColumnInfo(name = "network_prefix_length")
    val networkPrefixLength: Int,

    @ColumnInfo(name = "total_host_count")
    val totalHostCount: Int,

    @ColumnInfo(name = "scanned_host_count")
    val scannedHostCount: Int,

    @ColumnInfo(name = "found_device_count")
    val foundDeviceCount: Int,

    @ColumnInfo(name = "max_hosts")
    val maxHosts: Int,

    @ColumnInfo(name = "timeout_millis")
    val timeoutMillis: Int,

    @ColumnInfo(name = "parallelism")
    val parallelism: Int
)