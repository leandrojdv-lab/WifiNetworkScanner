package com.example.wifinetworkscanner.ui.screens.networkscans

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.WifiNetworkInfo
import com.example.wifinetworkscanner.ui.text.UiText

data class NetworkScansUiState(
    val networkIdentifier: String = "",
    val networkName: String = WifiNetworkInfo.UNKNOWN_NETWORK_NAME,
    val scanHistoryList: List<ScanHistory> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
)

sealed interface NetworkScansUiEffect {

    data class ShowMessage(
        val message: UiText
    ) : NetworkScansUiEffect
}