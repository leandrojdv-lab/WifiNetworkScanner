package com.example.wifinetworkscanner.ui.screens.history

import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.ui.text.UiText

data class HistoryUiState(
    val networkScanGroups: List<NetworkScanGroup> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
)

sealed interface HistoryUiEffect {

    data class ShowMessage(
        val message: UiText
    ) : HistoryUiEffect
}