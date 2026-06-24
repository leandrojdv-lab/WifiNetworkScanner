package com.example.wifinetworkscanner.ui.screens.networkscans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.WifiNetworkInfo
import com.example.wifinetworkscanner.domain.usecase.DeleteScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.ObserveScansByNetworkUseCase
import com.example.wifinetworkscanner.ui.text.UiText
import com.example.wifinetworkscanner.utils.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import javax.inject.Inject
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
class NetworkScansViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeScansByNetworkUseCase: ObserveScansByNetworkUseCase,
    private val deleteScanHistoryUseCase: DeleteScanHistoryUseCase
) : ViewModel() {

    private val encodedNetworkIdentifier: String = savedStateHandle
        .get<String>(NETWORK_IDENTIFIER_ARGUMENT)
        .orEmpty()

    private val networkIdentifier: String = URLDecoder.decode(
        encodedNetworkIdentifier,
        CHARSET_NAME
    )

    private val _uiState = MutableStateFlow(
        NetworkScansUiState(
            networkIdentifier = networkIdentifier
        )
    )
    val uiState: StateFlow<NetworkScansUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NetworkScansUiEffect>()
    val uiEffect: SharedFlow<NetworkScansUiEffect> = _uiEffect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.error(TAG, "Erro inesperado ao carregar varreduras da rede.", throwable)

        val message = UiText.StringResource(
            resId = R.string.network_scans_load_failed_message
        )

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = message
            )
        }

        viewModelScope.launch {
            _uiEffect.emit(
                NetworkScansUiEffect.ShowMessage(
                    message = message
                )
            )
        }
    }

    init {
        observeNetworkScans()
    }

    fun deleteScanHistory(scanHistoryId: Long) {
        viewModelScope.launch(exceptionHandler) {
            deleteScanHistoryUseCase(scanHistoryId)

            _uiEffect.emit(
                NetworkScansUiEffect.ShowMessage(
                    message = UiText.StringResource(
                        resId = R.string.network_scans_removed_message
                    )
                )
            )
        }
    }

    private fun observeNetworkScans() {
        if (networkIdentifier.isBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = UiText.StringResource(
                        resId = R.string.network_scans_invalid_network_message
                    )
                )
            }

            return
        }

        viewModelScope.launch(exceptionHandler) {
            observeScansByNetworkUseCase(
                networkIdentifier = networkIdentifier
            ).collect { scanHistoryList ->
                val networkName = scanHistoryList.firstOrNull()?.networkName
                    ?: WifiNetworkInfo.UNKNOWN_NETWORK_NAME

                _uiState.update { currentState ->
                    currentState.copy(
                        networkName = networkName,
                        scanHistoryList = scanHistoryList,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "NetworkScansViewModel"
        const val NETWORK_IDENTIFIER_ARGUMENT = "networkIdentifier"
        const val CHARSET_NAME = "UTF-8"
    }
}