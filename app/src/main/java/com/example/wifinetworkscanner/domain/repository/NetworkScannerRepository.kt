package com.example.wifinetworkscanner.domain.repository

import com.example.wifinetworkscanner.domain.model.NetworkScanEvent
import kotlinx.coroutines.flow.Flow

interface NetworkScannerRepository {

    fun scanConnectedDevices(
        maxHosts: Int,
        timeoutMillis: Int,
        parallelism: Int
    ): Flow<NetworkScanEvent>
}