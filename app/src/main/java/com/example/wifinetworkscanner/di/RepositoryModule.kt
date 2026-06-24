package com.example.wifinetworkscanner.di

import com.example.wifinetworkscanner.data.repository.NetworkScannerRepositoryImpl
import com.example.wifinetworkscanner.data.repository.ScanHistoryRepositoryImpl
import com.example.wifinetworkscanner.data.repository.ScanReportFileRepositoryImpl
import com.example.wifinetworkscanner.domain.repository.NetworkScannerRepository
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import com.example.wifinetworkscanner.domain.repository.ScanReportFileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNetworkScannerRepository(
        networkScannerRepositoryImpl: NetworkScannerRepositoryImpl
    ): NetworkScannerRepository

    @Binds
    @Singleton
    abstract fun bindScanHistoryRepository(
        scanHistoryRepositoryImpl: ScanHistoryRepositoryImpl
    ): ScanHistoryRepository

    @Binds
    @Singleton
    abstract fun bindScanReportFileRepository(
        scanReportFileRepositoryImpl: ScanReportFileRepositoryImpl
    ): ScanReportFileRepository
}