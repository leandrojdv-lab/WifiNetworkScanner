package com.example.wifinetworkscanner.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wifinetworkscanner.data.local.entity.ScanDeviceEntity
import com.example.wifinetworkscanner.data.local.entity.ScanHistoryEntity

data class ScanHistoryWithDevices(
    @Embedded
    val scanHistory: ScanHistoryEntity,

    @Relation(
        parentColumn = "scan_history_id",
        entityColumn = "scan_history_owner_id"
    )
    val devices: List<ScanDeviceEntity>
)