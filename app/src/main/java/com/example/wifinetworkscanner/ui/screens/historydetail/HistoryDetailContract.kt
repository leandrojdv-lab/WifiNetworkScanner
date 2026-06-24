package com.example.wifinetworkscanner.ui.screens.historydetail

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ShareableTextFile
import com.example.wifinetworkscanner.ui.text.UiText

data class HistoryDetailUiState(
    val scanHistory: ScanHistory? = null,
    val isLoading: Boolean = true,
    val isExportingReport: Boolean = false,
    val errorMessage: UiText? = null
)

sealed interface HistoryDetailUiEffect {

    data class ShowMessage(
        val message: UiText
    ) : HistoryDetailUiEffect

    data class ShareReport(
        val shareableTextFile: ShareableTextFile
    ) : HistoryDetailUiEffect
}