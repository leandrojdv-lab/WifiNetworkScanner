package com.example.wifinetworkscanner.ui.screens.search

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanHistorySearchFilter
import com.example.wifinetworkscanner.ui.text.UiText

data class SearchUiState(
    val searchFilter: ScanHistorySearchFilter = ScanHistorySearchFilter(),
    val results: List<ScanHistory> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
)

sealed interface SearchUiEffect {

    data class ShowMessage(
        val message: UiText
    ) : SearchUiEffect
}