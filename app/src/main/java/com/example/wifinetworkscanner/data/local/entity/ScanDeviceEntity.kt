package com.example.wifinetworkscanner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabela_dispositivos_varredura",
    foreignKeys = [
        ForeignKey(
            entity = ScanHistoryEntity::class,
            parentColumns = ["scan_history_id"],
            childColumns = ["scan_history_owner_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scan_history_owner_id"]),
        Index(value = ["ip_address"])
    ]
)
data class ScanDeviceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "scan_device_id")
    val scanDeviceId: Long = 0L,

    @ColumnInfo(name = "scan_history_owner_id")
    val scanHistoryOwnerId: Long,

    @ColumnInfo(name = "ip_address")
    val ipAddress: String,

    @ColumnInfo(name = "latency_millis")
    val latencyMillis: Long,

    @ColumnInfo(name = "scanned_at_epoch_millis")
    val scannedAtEpochMillis: Long,

    @ColumnInfo(name = "detection_method")
    val detectionMethod: String,

    @ColumnInfo(name = "open_ports_csv")
    val openPortsCsv: String,

    @ColumnInfo(name = "label")
    val label: String?
)