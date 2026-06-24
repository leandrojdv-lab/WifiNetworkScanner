package com.example.wifinetworkscanner.di

import android.content.Context
import androidx.room.Room
import com.example.wifinetworkscanner.data.local.dao.ScanHistoryDao
import com.example.wifinetworkscanner.data.local.database.AppDatabase
import com.example.wifinetworkscanner.data.local.database.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()
    }

    @Provides
    fun provideScanHistoryDao(
        appDatabase: AppDatabase
    ): ScanHistoryDao {
        return appDatabase.scanHistoryDao()
    }

    private const val DATABASE_NAME = "wifi_network_scanner.db"
}