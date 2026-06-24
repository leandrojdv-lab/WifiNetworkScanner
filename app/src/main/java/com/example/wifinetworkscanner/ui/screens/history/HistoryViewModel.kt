package com.example.wifinetworkscanner.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.usecase.DeleteAllScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.DeleteScansByNetworkUseCase
import com.example.wifinetworkscanner.domain.usecase.ObserveNetworkScanGroupsUseCase
import com.example.wifinetworkscanner.ui.text.UiText
import com.example.wifinetworkscanner.utils.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
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
class HistoryViewModel @Inject constructor(
    private val observeNetworkScanGroupsUseCase: ObserveNetworkScanGroupsUseCase,
    private val deleteScansByNetworkUseCase: DeleteScansByNetworkUseCase,
    private val deleteAllScanHistoryUseCase: DeleteAllScanHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HistoryUiEffect>()
    val uiEffect: SharedFlow<HistoryUiEffect> = _uiEffect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.error(TAG, "Erro inesperado no histórico.", throwable)

        val message = UiText.StringResource(
            resId = R.string.history_load_failed_message
        )

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = message
            )
        }

        viewModelScope.launch {
            _uiEffect.emit(
                HistoryUiEffect.ShowMessage(message = message)
            )
        }
    }

    init {
        observeNetworkGroups()
    }

    fun deleteNetworkScanHistory(networkIdentifier: String) {
        viewModelScope.launch(exceptionHandler) {
            deleteScansByNetworkUseCase(networkIdentifier = networkIdentifier)

            _uiEffect.emit(
                HistoryUiEffect.ShowMessage(
                    message = UiText.StringResource(
                        resId = R.string.history_network_removed_message
                    )
                )
            )
        }
    }

    fun deleteAllScanHistory() {
        viewModelScope.launch(exceptionHandler) {
            deleteAllScanHistoryUseCase()

            _uiEffect.emit(
                HistoryUiEffect.ShowMessage(
                    message = UiText.StringResource(
                        resId = R.string.history_cleared_message
                    )
                )
            )
        }
    }

    private fun observeNetworkGroups() {
        viewModelScope.launch(exceptionHandler) {
            observeNetworkScanGroupsUseCase().collect { networkScanGroups ->
                _uiState.update { currentState ->
                    currentState.copy(
                        networkScanGroups = networkScanGroups,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "HistoryViewModel"
    }
}