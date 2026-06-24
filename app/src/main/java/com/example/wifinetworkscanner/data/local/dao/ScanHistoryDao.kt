package com.example.wifinetworkscanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.wifinetworkscanner.data.local.entity.ScanDeviceEntity
import com.example.wifinetworkscanner.data.local.entity.ScanHistoryEntity
import com.example.wifinetworkscanner.data.local.projection.ScanNetworkGroupProjection
import com.example.wifinetworkscanner.data.local.relation.ScanHistoryWithDevices
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Query(
        """
        SELECT
            history.network_identifier AS networkIdentifier,
            COALESCE(
                (
                    SELECT latest.network_name
                    FROM tabela_historico_varreduras AS latest
                    WHERE latest.network_identifier = history.network_identifier
                    ORDER BY latest.completed_at_epoch_millis DESC
                    LIMIT 1
                ),
                'Rede desconhecida'
            ) AS networkName,
            (
                SELECT latest_gateway.gateway_ip_address
                FROM tabela_historico_varreduras AS latest_gateway
                WHERE latest_gateway.network_identifier = history.network_identifier
                ORDER BY latest_gateway.completed_at_epoch_millis DESC
                LIMIT 1
            ) AS latestGatewayIpAddress,
            COUNT(*) AS scanCount,
            MAX(history.completed_at_epoch_millis) AS latestCompletedAtEpochMillis,
            COALESCE(SUM(history.found_device_count), 0) AS totalFoundDeviceCount
        FROM tabela_historico_varreduras AS history
        GROUP BY history.network_identifier
        ORDER BY latestCompletedAtEpochMillis DESC
        """
    )
    fun observeNetworkScanGroups(): Flow<List<ScanNetworkGroupProjection>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM tabela_historico_varreduras
        ORDER BY completed_at_epoch_millis DESC
        """
    )
    fun observeAllScanHistory(): Flow<List<ScanHistoryWithDevices>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM tabela_historico_varreduras
        ORDER BY completed_at_epoch_millis DESC
        LIMIT :limit
        """
    )
    fun observeRecentScanHistory(limit: Int): Flow<List<ScanHistoryWithDevices>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM tabela_historico_varreduras
        WHERE scan_history_id = :scanHistoryId
        LIMIT 1
        """
    )
    fun observeScanHistoryById(scanHistoryId: Long): Flow<ScanHistoryWithDevices?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM tabela_historico_varreduras
        WHERE network_identifier = :networkIdentifier
        ORDER BY completed_at_epoch_millis DESC
        """
    )
    fun observeScanHistoryByNetworkIdentifier(
        networkIdentifier: String
    ): Flow<List<ScanHistoryWithDevices>>

    @Insert
    suspend fun insertScanHistory(scanHistoryEntity: ScanHistoryEntity): Long

    @Insert
    suspend fun insertScanDevices(scanDeviceEntities: List<ScanDeviceEntity>)

    @Transaction
    suspend fun insertScanHistoryWithDevices(
        scanHistoryEntity: ScanHistoryEntity,
        scanDeviceEntities: List<ScanDeviceEntity>
    ): Long {
        val scanHistoryId = insertScanHistory(scanHistoryEntity)

        if (scanDeviceEntities.isNotEmpty()) {
            insertScanDevices(
                scanDeviceEntities.map { scanDeviceEntity ->
                    scanDeviceEntity.copy(scanHistoryOwnerId = scanHistoryId)
                }
            )
        }

        return scanHistoryId
    }

    @Query(
        """
        DELETE FROM tabela_historico_varreduras
        WHERE scan_history_id = :scanHistoryId
        """
    )
    suspend fun deleteScanHistoryById(scanHistoryId: Long)

    @Query(
        """
        DELETE FROM tabela_historico_varreduras
        WHERE network_identifier = :networkIdentifier
        """
    )
    suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String)

    @Query("DELETE FROM tabela_historico_varreduras")
    suspend fun deleteAllScanHistory()
}