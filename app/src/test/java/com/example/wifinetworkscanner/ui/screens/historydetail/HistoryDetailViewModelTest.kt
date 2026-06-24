package com.example.wifinetworkscanner.ui.screens.historydetail

import androidx.lifecycle.SavedStateHandle
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.model.ShareableTextFile
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import com.example.wifinetworkscanner.domain.repository.ScanReportFileRepository
import com.example.wifinetworkscanner.domain.usecase.GenerateScanHistoryCsvReportUseCase
import com.example.wifinetworkscanner.domain.usecase.GenerateScanHistoryTextReportUseCase
import com.example.wifinetworkscanner.domain.usecase.ObserveScanHistoryDetailUseCase
import com.example.wifinetworkscanner.test.MainDispatcherRule
import com.example.wifinetworkscanner.ui.text.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_whenScanHistoryExists_shouldLoadHistoryDetail() = runTest {
        val scanHistory = createScanHistory()
        val viewModel = createViewModel(scanHistory = scanHistory)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(scanHistory, viewModel.uiState.value.scanHistory)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun init_whenScanHistoryDoesNotExist_shouldShowNotFoundError() = runTest {
        val viewModel = createViewModel(scanHistory = null)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            UiText.StringResource(resId = R.string.history_detail_not_found_error_message),
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun exportTextReport_whenScanHistoryLoaded_shouldCreateTextReportContent() = runTest {
        val fileRepository = FakeScanReportFileRepository()
        val viewModel = createViewModel(
            scanHistory = createScanHistory(),
            fileRepository = fileRepository
        )

        advanceUntilIdle()

        viewModel.exportTextReport()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isExportingReport)
        assertEquals("text/plain", fileRepository.lastMimeType)
        assertTrue(fileRepository.lastFileName.orEmpty().endsWith(".txt"))
        assertTrue(fileRepository.lastContent.orEmpty().contains("varredura Wi-Fi"))
    }

    @Test
    fun exportCsvReport_whenScanHistoryLoaded_shouldCreateCsvReportContent() = runTest {
        val fileRepository = FakeScanReportFileRepository()
        val viewModel = createViewModel(
            scanHistory = createScanHistory(),
            fileRepository = fileRepository
        )

        advanceUntilIdle()

        viewModel.exportCsvReport()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isExportingReport)
        assertEquals("text/csv", fileRepository.lastMimeType)
        assertTrue(fileRepository.lastFileName.orEmpty().endsWith(".csv"))
        assertTrue(fileRepository.lastContent.orEmpty().contains("section;field;value"))
        assertTrue(fileRepository.lastContent.orEmpty().contains("192.168.1.20"))
    }

    @Test
    fun exportTextReport_whenFileCreationFails_shouldExposeErrorMessage() = runTest {
        val fileRepository = FakeScanReportFileRepository(shouldFail = true)
        val viewModel = createViewModel(
            scanHistory = createScanHistory(),
            fileRepository = fileRepository
        )

        advanceUntilIdle()

        viewModel.exportTextReport()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isExportingReport)
        assertEquals(
            UiText.StringResource(resId = R.string.history_detail_export_failed_message),
            viewModel.uiState.value.errorMessage
        )
    }

    private fun createViewModel(
        scanHistory: ScanHistory?,
        fileRepository: FakeScanReportFileRepository = FakeScanReportFileRepository()
    ): HistoryDetailViewModel {
        val scanHistoryRepository = FakeScanHistoryRepository(scanHistory = scanHistory)

        return HistoryDetailViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(SCAN_HISTORY_ID_ARGUMENT to 1L)
            ),
            observeScanHistoryDetailUseCase = ObserveScanHistoryDetailUseCase(
                scanHistoryRepository = scanHistoryRepository
            ),
            generateScanHistoryTextReportUseCase = GenerateScanHistoryTextReportUseCase(),
            generateScanHistoryCsvReportUseCase = GenerateScanHistoryCsvReportUseCase(),
            scanReportFileRepository = fileRepository
        )
    }

    private fun createScanHistory(): ScanHistory {
        return ScanHistory(
            id = 1L,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 4_500L,
            networkName = "Minha Rede",
            networkIdentifier = "ssid:minha rede",
            localIpAddress = "192.168.1.10",
            gatewayIpAddress = "192.168.1.1",
            prefixLength = 24,
            totalHostCount = 254,
            scannedHostCount = 254,
            scanSettings = ScanSettings.default(),
            devices = listOf(
                NetworkDevice(
                    ipAddress = "192.168.1.20",
                    latencyMillis = 12L,
                    scannedAtEpochMillis = 3_000L,
                    detectionMethod = DeviceDetectionMethod.REACHABLE_AND_TCP_PORT,
                    openPorts = listOf(80, 443),
                    label = "Notebook"
                )
            )
        )
    }

    private class FakeScanHistoryRepository(
        private val scanHistory: ScanHistory?
    ) : ScanHistoryRepository {

        override fun observeNetworkScanGroups(): Flow<List<NetworkScanGroup>> {
            return flowOf(emptyList())
        }

        override fun observeAllScanHistory(): Flow<List<ScanHistory>> {
            return flowOf(emptyList())
        }

        override fun observeRecentScanHistory(limit: Int): Flow<List<ScanHistory>> {
            return flowOf(emptyList())
        }

        override fun observeScanHistoryById(scanHistoryId: Long): Flow<ScanHistory?> {
            return flowOf(scanHistory)
        }

        override fun observeScanHistoryByNetworkIdentifier(
            networkIdentifier: String
        ): Flow<List<ScanHistory>> {
            return flowOf(emptyList())
        }

        override suspend fun saveScanHistory(scanHistory: ScanHistory): Long {
            return 1L
        }

        override suspend fun deleteScanHistory(scanHistoryId: Long) {
            return
        }

        override suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String) {
            return
        }

        override suspend fun deleteAllScanHistory() {
            return
        }
    }

    private class FakeScanReportFileRepository(
        private val shouldFail: Boolean = false
    ) : ScanReportFileRepository {

        var lastFileName: String? = null
            private set

        var lastContent: String? = null
            private set

        var lastMimeType: String? = null
            private set

        override suspend fun createTextReportFile(
            fileName: String,
            content: String
        ): Result<ShareableTextFile> {
            return createTextFile(
                fileName = fileName,
                content = content,
                mimeType = "text/plain"
            )
        }

        override suspend fun createTextFile(
            fileName: String,
            content: String,
            mimeType: String
        ): Result<ShareableTextFile> {
            if (shouldFail) {
                return Result.failure(IllegalStateException("Falha simulada."))
            }

            lastFileName = fileName
            lastContent = content
            lastMimeType = mimeType

            return Result.success(
                ShareableTextFile(
                    fileName = fileName,
                    contentUri = "content://test/$fileName",
                    mimeType = mimeType
                )
            )
        }
    }

    private companion object {
        const val SCAN_HISTORY_ID_ARGUMENT = "scanHistoryId"
    }
}