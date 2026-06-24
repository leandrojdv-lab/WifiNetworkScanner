package com.example.wifinetworkscanner.data.repository

import com.example.wifinetworkscanner.data.local.dao.ScanHistoryDao
import com.example.wifinetworkscanner.data.mapper.toDomain
import com.example.wifinetworkscanner.data.mapper.toEntity
import com.example.wifinetworkscanner.di.IoDispatcher
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class ScanHistoryRepositoryImpl @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ScanHistoryRepository {

    override fun observeNetworkScanGroups(): Flow<List<NetworkScanGroup>> {
        return scanHistoryDao.observeNetworkScanGroups()
            .map { projections ->
                projections.map { projection ->
                    projection.toDomain()
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun observeAllScanHistory(): Flow<List<ScanHistory>> {
        return scanHistoryDao.observeAllScanHistory()
            .map { scanHistoryWithDevices ->
                scanHistoryWithDevices.map { item ->
                    item.toDomain()
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun observeRecentScanHistory(limit: Int): Flow<List<ScanHistory>> {
        return scanHistoryDao.observeRecentScanHistory(limit = limit)
            .map { scanHistoryWithDevices ->
                scanHistoryWithDevices.map { item ->
                    item.toDomain()
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun observeScanHistoryById(scanHistoryId: Long): Flow<ScanHistory?> {
        return scanHistoryDao.observeScanHistoryById(scanHistoryId = scanHistoryId)
            .map { scanHistoryWithDevices ->
                scanHistoryWithDevices?.toDomain()
            }
            .flowOn(ioDispatcher)
    }

    override fun observeScanHistoryByNetworkIdentifier(
        networkIdentifier: String
    ): Flow<List<ScanHistory>> {
        return scanHistoryDao.observeScanHistoryByNetworkIdentifier(
            networkIdentifier = networkIdentifier
        )
            .map { scanHistoryWithDevices ->
                scanHistoryWithDevices.map { item ->
                    item.toDomain()
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun saveScanHistory(scanHistory: ScanHistory): Long {
        return withContext(ioDispatcher) {
            scanHistoryDao.insertScanHistoryWithDevices(
                scanHistoryEntity = scanHistory.toEntity(),
                scanDeviceEntities = scanHistory.devices.map { networkDevice ->
                    networkDevice.toEntity()
                }
            )
        }
    }

    override suspend fun deleteScanHistory(scanHistoryId: Long) {
        withContext(ioDispatcher) {
            scanHistoryDao.deleteScanHistoryById(scanHistoryId = scanHistoryId)
        }
    }

    override suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String) {
        withContext(ioDispatcher) {
            scanHistoryDao.deleteScanHistoryByNetworkIdentifier(
                networkIdentifier = networkIdentifier
            )
        }
    }

    override suspend fun deleteAllScanHistory() {
        withContext(ioDispatcher) {
            scanHistoryDao.deleteAllScanHistory()
        }
    }
}