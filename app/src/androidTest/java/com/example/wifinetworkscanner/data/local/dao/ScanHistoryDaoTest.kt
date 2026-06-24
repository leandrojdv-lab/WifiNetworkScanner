package com.example.wifinetworkscanner.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wifinetworkscanner.data.local.database.AppDatabase
import com.example.wifinetworkscanner.data.local.entity.ScanDeviceEntity
import com.example.wifinetworkscanner.data.local.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanHistoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var scanHistoryDao: ScanHistoryDao

    @Before
    fun setUp() {
        val context: Context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        scanHistoryDao = database.scanHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertScanHistoryWithDevices_whenObservedById_shouldReturnHistoryWithDevices() = runBlocking {
        val scanHistoryId = scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                networkName = "Minha Rede",
                networkIdentifier = NETWORK_IDENTIFIER_HOME,
                foundDeviceCount = 2
            ),
            scanDeviceEntities = listOf(
                createScanDeviceEntity(ipAddress = "192.168.1.20"),
                createScanDeviceEntity(ipAddress = "192.168.1.30")
            )
        )

        val result = requireNotNull(
            scanHistoryDao.observeScanHistoryById(scanHistoryId).first()
        )

        assertEquals(scanHistoryId, result.scanHistory.scanHistoryId)
        assertEquals("Minha Rede", result.scanHistory.networkName)
        assertEquals(NETWORK_IDENTIFIER_HOME, result.scanHistory.networkIdentifier)
        assertEquals(2, result.devices.size)
        assertTrue(
            result.devices.all { device ->
                device.scanHistoryOwnerId == scanHistoryId
            }
        )
    }

    @Test
    fun observeAllScanHistory_whenMultipleRowsExist_shouldOrderByCompletedAtDescending() = runBlocking {
        val olderScanHistoryId = scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                completedAtEpochMillis = 2_000L,
                networkIdentifier = NETWORK_IDENTIFIER_HOME
            ),
            scanDeviceEntities = emptyList()
        )

        val newerScanHistoryId = scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                completedAtEpochMillis = 5_000L,
                networkIdentifier = NETWORK_IDENTIFIER_WORK
            ),
            scanDeviceEntities = emptyList()
        )

        val result = scanHistoryDao.observeAllScanHistory().first()

        assertEquals(
            listOf(newerScanHistoryId, olderScanHistoryId),
            result.map { item ->
                item.scanHistory.scanHistoryId
            }
        )
    }

    @Test
    fun deleteScanHistoryById_whenHistoryHasDevices_shouldDeleteDevicesByCascade() = runBlocking {
        val scanHistoryId = scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                networkIdentifier = NETWORK_IDENTIFIER_HOME,
                foundDeviceCount = 2
            ),
            scanDeviceEntities = listOf(
                createScanDeviceEntity(ipAddress = "192.168.1.20"),
                createScanDeviceEntity(ipAddress = "192.168.1.30")
            )
        )

        assertEquals(2, countStoredDevices())

        scanHistoryDao.deleteScanHistoryById(scanHistoryId = scanHistoryId)

        assertEquals(null, scanHistoryDao.observeScanHistoryById(scanHistoryId).first())
        assertEquals(0, countStoredDevices())
    }

    @Test
    fun deleteScanHistoryByNetworkIdentifier_whenCalled_shouldRemoveOnlySelectedNetwork() = runBlocking {
        scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                networkName = "Casa",
                networkIdentifier = NETWORK_IDENTIFIER_HOME,
                completedAtEpochMillis = 2_000L
            ),
            scanDeviceEntities = listOf(
                createScanDeviceEntity(ipAddress = "192.168.1.20")
            )
        )

        scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                networkName = "Trabalho",
                networkIdentifier = NETWORK_IDENTIFIER_WORK,
                completedAtEpochMillis = 3_000L
            ),
            scanDeviceEntities = listOf(
                createScanDeviceEntity(ipAddress = "10.0.0.20")
            )
        )

        scanHistoryDao.deleteScanHistoryByNetworkIdentifier(
            networkIdentifier = NETWORK_IDENTIFIER_HOME
        )

        val result = scanHistoryDao.observeAllScanHistory().first()

        assertEquals(1, result.size)
        assertEquals(NETWORK_IDENTIFIER_WORK, result.first().scanHistory.networkIdentifier)
        assertEquals("Trabalho", result.first().scanHistory.networkName)
        assertEquals(1, countStoredDevices())
    }

    @Test
    fun observeNetworkScanGroups_whenHistoriesExist_shouldAggregateByNetwork() = runBlocking {
        scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                networkName = "Casa Antiga",
                networkIdentifier = NETWORK_IDENTIFIER_HOME,
                gatewayIpAddress = "192.168.1.1",
                completedAtEpochMillis = 1_000L,
                foundDeviceCount = 2
            ),
            scanDeviceEntities = emptyList()
        )

        scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                networkName = "Casa Atual",
                networkIdentifier = NETWORK_IDENTIFIER_HOME,
                gatewayIpAddress = "192.168.1.254",
                completedAtEpochMillis = 4_000L,
                foundDeviceCount = 3
            ),
            scanDeviceEntities = emptyList()
        )

        scanHistoryDao.insertScanHistoryWithDevices(
            scanHistoryEntity = createScanHistoryEntity(
                networkName = "Trabalho",
                networkIdentifier = NETWORK_IDENTIFIER_WORK,
                gatewayIpAddress = "10.0.0.1",
                completedAtEpochMillis = 2_000L,
                foundDeviceCount = 1
            ),
            scanDeviceEntities = emptyList()
        )

        val result = scanHistoryDao.observeNetworkScanGroups().first()

        assertEquals(2, result.size)

        val homeGroup = result[0]
        assertEquals(NETWORK_IDENTIFIER_HOME, homeGroup.networkIdentifier)
        assertEquals("Casa Atual", homeGroup.networkName)
        assertEquals("192.168.1.254", homeGroup.latestGatewayIpAddress)
        assertEquals(2L, homeGroup.scanCount)
        assertEquals(4_000L, homeGroup.latestCompletedAtEpochMillis)
        assertEquals(5L, homeGroup.totalFoundDeviceCount)

        val workGroup = result[1]
        assertEquals(NETWORK_IDENTIFIER_WORK, workGroup.networkIdentifier)
        assertEquals("Trabalho", workGroup.networkName)
        assertEquals("10.0.0.1", workGroup.latestGatewayIpAddress)
        assertEquals(1L, workGroup.scanCount)
        assertEquals(2_000L, workGroup.latestCompletedAtEpochMillis)
        assertEquals(1L, workGroup.totalFoundDeviceCount)
    }

    private fun createScanHistoryEntity(
        startedAtEpochMillis: Long = 1_000L,
        completedAtEpochMillis: Long = 2_000L,
        networkName: String = "Minha Rede",
        networkIdentifier: String = NETWORK_IDENTIFIER_HOME,
        localIpAddress: String = "192.168.1.10",
        gatewayIpAddress: String? = "192.168.1.1",
        networkPrefixLength: Int = 24,
        totalHostCount: Int = 254,
        scannedHostCount: Int = 254,
        foundDeviceCount: Int = 1,
        maxHosts: Int = 254,
        timeoutMillis: Int = 700,
        parallelism: Int = 8
    ): ScanHistoryEntity {
        return ScanHistoryEntity(
            startedAtEpochMillis = startedAtEpochMillis,
            completedAtEpochMillis = completedAtEpochMillis,
            networkName = networkName,
            networkIdentifier = networkIdentifier,
            localIpAddress = localIpAddress,
            gatewayIpAddress = gatewayIpAddress,
            networkPrefixLength = networkPrefixLength,
            totalHostCount = totalHostCount,
            scannedHostCount = scannedHostCount,
            foundDeviceCount = foundDeviceCount,
            maxHosts = maxHosts,
            timeoutMillis = timeoutMillis,
            parallelism = parallelism
        )
    }

    private fun createScanDeviceEntity(
        scanHistoryOwnerId: Long = TEMPORARY_SCAN_HISTORY_OWNER_ID,
        ipAddress: String = "192.168.1.20",
        latencyMillis: Long = 12L,
        scannedAtEpochMillis: Long = 1_500L,
        detectionMethod: String = "REACHABLE_AND_TCP_PORT",
        openPortsCsv: String = "80,443",
        label: String? = "Notebook"
    ): ScanDeviceEntity {
        return ScanDeviceEntity(
            scanHistoryOwnerId = scanHistoryOwnerId,
            ipAddress = ipAddress,
            latencyMillis = latencyMillis,
            scannedAtEpochMillis = scannedAtEpochMillis,
            detectionMethod = detectionMethod,
            openPortsCsv = openPortsCsv,
            label = label
        )
    }

    private fun countStoredDevices(): Int {
        val cursor = database.openHelper.writableDatabase.query(
            "SELECT COUNT(*) FROM tabela_dispositivos_varredura"
        )

        return try {
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        } finally {
            cursor.close()
        }
    }

    private companion object {
        const val TEMPORARY_SCAN_HISTORY_OWNER_ID = 0L
        const val NETWORK_IDENTIFIER_HOME = "ssid:minha rede"
        const val NETWORK_IDENTIFIER_WORK = "ssid:trabalho"
    }
}