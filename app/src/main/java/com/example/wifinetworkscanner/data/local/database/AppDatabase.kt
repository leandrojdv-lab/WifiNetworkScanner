package com.example.wifinetworkscanner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wifinetworkscanner.data.local.dao.ScanHistoryDao
import com.example.wifinetworkscanner.data.local.entity.ScanDeviceEntity
import com.example.wifinetworkscanner.data.local.entity.ScanHistoryEntity

@Database(
    entities = [
        ScanHistoryEntity::class,
        ScanDeviceEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scanHistoryDao(): ScanHistoryDao
}