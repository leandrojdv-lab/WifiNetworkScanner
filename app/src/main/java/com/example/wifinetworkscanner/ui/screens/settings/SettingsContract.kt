package com.example.wifinetworkscanner.ui.screens.settings

import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.ui.text.UiText

data class SettingsUiState(
    val scanSettings: ScanSettings = ScanSettings.default(),
    val isSaving: Boolean = false,
    val errorMessage: UiText? = null
)

sealed interface SettingsUiEffect {

    data class ShowMessage(
        val message: UiText
    ) : SettingsUiEffect
}