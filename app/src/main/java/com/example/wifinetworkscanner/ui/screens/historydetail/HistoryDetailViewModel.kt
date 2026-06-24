package com.example.wifinetworkscanner.ui.screens.historydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanReportFileRepository
import com.example.wifinetworkscanner.domain.usecase.GenerateScanHistoryCsvReportUseCase
import com.example.wifinetworkscanner.domain.usecase.GenerateScanHistoryTextReportUseCase
import com.example.wifinetworkscanner.domain.usecase.ObserveScanHistoryDetailUseCase
import com.example.wifinetworkscanner.ui.text.UiText
import com.example.wifinetworkscanner.utils.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeScanHistoryDetailUseCase: ObserveScanHistoryDetailUseCase,
    private val generateScanHistoryTextReportUseCase: GenerateScanHistoryTextReportUseCase,
    private val generateScanHistoryCsvReportUseCase: GenerateScanHistoryCsvReportUseCase,
    private val scanReportFileRepository: ScanReportFileRepository
) : ViewModel() {

    private val scanHistoryId: Long = savedStateHandle[SCAN_HISTORY_ID_ARGUMENT] ?: 0L

    private val _uiState = MutableStateFlow(HistoryDetailUiState())
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HistoryDetailUiEffect>(
        extraBufferCapacity = UI_EFFECT_BUFFER_CAPACITY
    )
    val uiEffect: SharedFlow<HistoryDetailUiEffect> = _uiEffect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.error(TAG, "Erro inesperado ao carregar detalhes do histÃ³rico.", throwable)

        val message = UiText.StringResource(
            resId = R.string.history_detail_load_failed_message
        )

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                isExportingReport = false,
                errorMessage = message
            )
        }

        emitEffect(HistoryDetailUiEffect.ShowMessage(message = message))
    }

    init {
        observeHistoryDetail()
    }

    fun exportTextReport() {
        val scanHistory = _uiState.value.scanHistory ?: run {
            emitMissingScanHistoryMessage()
            return
        }

        exportReport(
            scanHistory = scanHistory,
            fileName = buildReportFileName(
                scanHistory = scanHistory,
                extension = TXT_EXTENSION
            ),
            mimeType = TEXT_PLAIN_MIME_TYPE,
            contentProvider = {
                generateScanHistoryTextReportUseCase(scanHistory)
            }
        )
    }

    fun exportCsvReport() {
        val scanHistory = _uiState.value.scanHistory ?: run {
            emitMissingScanHistoryMessage()
            return
        }

        exportReport(
            scanHistory = scanHistory,
            fileName = buildReportFileName(
                scanHistory = scanHistory,
                extension = CSV_EXTENSION
            ),
            mimeType = TEXT_CSV_MIME_TYPE,
            contentProvider = {
                generateScanHistoryCsvReportUseCase(scanHistory)
            }
        )
    }

    private fun exportReport(
        scanHistory: ScanHistory,
        fileName: String,
        mimeType: String,
        contentProvider: () -> String
    ) {
        viewModelScope.launch(exceptionHandler) {
            _uiState.update { currentState ->
                currentState.copy(
                    isExportingReport = true,
                    errorMessage = null
                )
            }

            try {
                scanReportFileRepository.createTextFile(
                    fileName = fileName,
                    content = contentProvider(),
                    mimeType = mimeType
                ).fold(
                    onSuccess = { shareableTextFile ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                isExportingReport = false,
                                errorMessage = null
                            )
                        }

                        emitEffect(
                            HistoryDetailUiEffect.ShareReport(
                                shareableTextFile = shareableTextFile
                            )
                        )
                    },
                    onFailure = { throwable ->
                        AppLogger.error(
                            TAG,
                            "Erro ao exportar relatÃ³rio da varredura ${scanHistory.id}.",
                            throwable
                        )

                        val message = UiText.StringResource(
                            resId = R.string.history_detail_export_failed_message
                        )

                        _uiState.update { currentState ->
                            currentState.copy(
                                isExportingReport = false,
                                errorMessage = message
                            )
                        }

                        emitEffect(HistoryDetailUiEffect.ShowMessage(message = message))
                    }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                AppLogger.error(
                    TAG,
                    "Erro inesperado ao exportar relatÃ³rio da varredura ${scanHistory.id}.",
                    exception
                )

                val message = UiText.StringResource(
                    resId = R.string.history_detail_export_failed_message
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isExportingReport = false,
                        errorMessage = message
                    )
                }

                emitEffect(HistoryDetailUiEffect.ShowMessage(message = message))
            }
        }
    }

    private fun observeHistoryDetail() {
        if (scanHistoryId <= 0L) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = UiText.StringResource(
                        resId = R.string.history_detail_invalid_id_message
                    )
                )
            }
            return
        }

        viewModelScope.launch(exceptionHandler) {
            observeScanHistoryDetailUseCase(scanHistoryId = scanHistoryId).collect { scanHistory ->
                _uiState.update { currentState ->
                    currentState.copy(
                        scanHistory = scanHistory,
                        isLoading = false,
                        errorMessage = if (scanHistory == null) {
                            UiText.StringResource(
                                resId = R.string.history_detail_not_found_error_message
                            )
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    private fun emitMissingScanHistoryMessage() {
        val message = UiText.StringResource(
            resId = R.string.history_detail_missing_export_message
        )

        emitEffect(HistoryDetailUiEffect.ShowMessage(message = message))
    }

    private fun emitEffect(effect: HistoryDetailUiEffect) {
        _uiEffect.tryEmit(effect)
    }

    private fun buildReportFileName(
        scanHistory: ScanHistory,
        extension: String
    ): String {
        return "relatorio_varredura_${scanHistory.id}_${scanHistory.completedAtEpochMillis}$extension"
    }

    private companion object {
        const val TAG = "HistoryDetailViewModel"
        const val SCAN_HISTORY_ID_ARGUMENT = "scanHistoryId"
        const val TXT_EXTENSION = ".txt"
        const val CSV_EXTENSION = ".csv"
        const val TEXT_PLAIN_MIME_TYPE = "text/plain"
        const val TEXT_CSV_MIME_TYPE = "text/csv"
        const val UI_EFFECT_BUFFER_CAPACITY = 1
    }
}



