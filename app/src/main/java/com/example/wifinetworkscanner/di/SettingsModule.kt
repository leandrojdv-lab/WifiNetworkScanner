package com.example.wifinetworkscanner.di

import com.example.wifinetworkscanner.data.repository.ScanSettingsRepositoryImpl
import com.example.wifinetworkscanner.domain.repository.ScanSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindScanSettingsRepository(
        implementation: ScanSettingsRepositoryImpl
    ): ScanSettingsRepository
}