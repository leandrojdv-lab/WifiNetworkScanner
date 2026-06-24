package com.example.wifinetworkscanner.ui.screens.networkscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.NetworkScanEvent
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.usecase.ObserveScanSettingsUseCase
import com.example.wifinetworkscanner.domain.usecase.SaveScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.ScanWifiDevicesUseCase
import com.example.wifinetworkscanner.domain.usecase.UpdateScanSettingsUseCase
import com.example.wifinetworkscanner.ui.text.UiText
import com.example.wifinetworkscanner.utils.logger.AppLogger
import com.example.wifinetworkscanner.utils.network.Ipv4Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.TreeMap
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NetworkScannerViewModel @Inject constructor(
    private val scanWifiDevicesUseCase: ScanWifiDevicesUseCase,
    private val observeScanSettingsUseCase: ObserveScanSettingsUseCase,
    private val updateScanSettingsUseCase: UpdateScanSettingsUseCase,
    private val saveScanHistoryUseCase: SaveScanHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkScannerUiState())
    val uiState: StateFlow<NetworkScannerUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NetworkScannerUiEffect>()
    val uiEffect: SharedFlow<NetworkScannerUiEffect> = _uiEffect.asSharedFlow()

    private val devicesByIpAddress = TreeMap<String, NetworkDevice> { firstIpAddress, secondIpAddress ->
        Ipv4Utils.ipAddressSortValue(firstIpAddress)
            .compareTo(Ipv4Utils.ipAddressSortValue(secondIpAddress))
    }

    private var scanJob: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.error(TAG, "Erro inesperado durante a operação.", throwable)

        val message = UiText.StringResource(resId = R.string.message_unexpected_error)

        _uiState.update { currentState ->
            currentState.copy(
                isScanning = false,
                errorMessage = message
            )
        }

        viewModelScope.launch {
            _uiEffect.emit(
                NetworkScannerUiEffect.ShowMessage(message = message)
            )
        }
    }

    init {
        observeScanSettings()
    }

    fun startScan() {
        if (_uiState.value.isScanning) {
            return
        }

        scanJob?.cancel()
        devicesByIpAddress.clear()

        val scanStartedAtEpochMillis = System.currentTimeMillis()

        _uiState.update { currentState ->
            currentState.copy(
                isScanning = true,
                scannedHostCount = 0,
                totalHostCount = 0,
                devices = emptyList(),
                currentScanStartedAtEpochMillis = scanStartedAtEpochMillis,
                errorMessage = null
            )
        }

        val currentSettings = _uiState.value.scanSettings

        scanJob = viewModelScope.launch(exceptionHandler) {
            scanWifiDevicesUseCase(currentSettings).collect { event ->
                handleScanEvent(event)
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        scanJob = null

        _uiState.update { currentState ->
            currentState.copy(isScanning = false)
        }

        viewModelScope.launch {
            _uiEffect.emit(
                NetworkScannerUiEffect.ShowMessage(
                    message = UiText.StringResource(
                        resId = R.string.network_scanner_scan_cancelled_message
                    )
                )
            )
        }
    }

    fun updateScanSettings(scanSettings: ScanSettings) {
        viewModelScope.launch(exceptionHandler) {
            updateScanSettingsUseCase(scanSettings)

            _uiEffect.emit(
                NetworkScannerUiEffect.ShowMessage(
                    message = UiText.StringResource(
                        resId = R.string.network_scanner_settings_saved_message
                    )
                )
            )
        }
    }

    private fun observeScanSettings() {
        viewModelScope.launch(exceptionHandler) {
            observeScanSettingsUseCase().collect { scanSettings ->
                _uiState.update { currentState ->
                    currentState.copy(scanSettings = scanSettings)
                }
            }
        }
    }

    private suspend fun handleScanEvent(event: NetworkScanEvent) {
        when (event) {
            is NetworkScanEvent.Started -> {
                devicesByIpAddress.clear()

                _uiState.update { currentState ->
                    currentState.copy(
                        isScanning = true,
                        networkName = event.networkInfo.networkName,
                        networkIdentifier = event.networkInfo.networkIdentifier,
                        localIpAddress = event.networkInfo.localIpAddress,
                        gatewayIpAddress = event.networkInfo.gatewayIpAddress.orEmpty(),
                        networkPrefixLength = event.networkInfo.prefixLength,
                        scannedHostCount = 0,
                        totalHostCount = event.networkInfo.totalHostCount,
                        devices = emptyList(),
                        errorMessage = null
                    )
                }
            }

            is NetworkScanEvent.DeviceFound -> {
                devicesByIpAddress[event.device.ipAddress] = event.device

                _uiState.update { currentState ->
                    currentState.copy(
                        devices = devicesByIpAddress.values.toList()
                    )
                }
            }

            is NetworkScanEvent.Progress -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        scannedHostCount = event.scannedHostCount,
                        totalHostCount = event.totalHostCount
                    )
                }
            }

            is NetworkScanEvent.Completed -> {
                val completedAtEpochMillis = System.currentTimeMillis()
                val stateBeforeCompletion = _uiState.value
                val completedDevices = event.devices.sortedBy { device ->
                    Ipv4Utils.ipAddressSortValue(device.ipAddress)
                }
                val scanHistory = stateBeforeCompletion.toScanHistory(
                    completedAtEpochMillis = completedAtEpochMillis,
                    devices = completedDevices
                )

                devicesByIpAddress.clear()
                completedDevices.forEach { device ->
                    devicesByIpAddress[device.ipAddress] = device
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        isScanning = false,
                        scannedHostCount = currentState.totalHostCount,
                        devices = devicesByIpAddress.values.toList()
                    )
                }

                saveScanHistorySafely(scanHistory)

                _uiEffect.emit(
                    NetworkScannerUiEffect.ShowMessage(
                        message = UiText.StringResource(
                            resId = R.string.network_scanner_scan_finished_message,
                            args = listOf(completedDevices.size)
                        )
                    )
                )
            }

            is NetworkScanEvent.Failed -> {
                val message = UiText.DynamicString(value = event.message)

                _uiState.update { currentState ->
                    currentState.copy(
                        isScanning = false,
                        errorMessage = message
                    )
                }

                _uiEffect.emit(
                    NetworkScannerUiEffect.ShowMessage(message = message)
                )
            }
        }
    }

    private suspend fun saveScanHistorySafely(scanHistory: ScanHistory) {
        try {
            saveScanHistoryUseCase(scanHistory)
        } catch (exception: IllegalArgumentException) {
            AppLogger.error(TAG, "Histórico de varredura inválido.", exception)

            _uiEffect.emit(
                NetworkScannerUiEffect.ShowMessage(
                    message = UiText.StringResource(
                        resId = R.string.network_scanner_history_invalid_message
                    )
                )
            )
        } catch (exception: Exception) {
            AppLogger.error(TAG, "Falha ao salvar histórico da varredura.", exception)

            _uiEffect.emit(
                NetworkScannerUiEffect.ShowMessage(
                    message = UiText.StringResource(
                        resId = R.string.network_scanner_history_save_failed_message
                    )
                )
            )
        }
    }

    private fun NetworkScannerUiState.toScanHistory(
        completedAtEpochMillis: Long,
        devices: List<NetworkDevice>
    ): ScanHistory {
        val safeStartedAtEpochMillis = if (currentScanStartedAtEpochMillis > 0L) {
            currentScanStartedAtEpochMillis
        } else {
            completedAtEpochMillis
        }

        return ScanHistory(
            id = 0L,
            startedAtEpochMillis = safeStartedAtEpochMillis,
            completedAtEpochMillis = completedAtEpochMillis,
            networkName = networkName,
            networkIdentifier = networkIdentifier,
            localIpAddress = localIpAddress,
            gatewayIpAddress = gatewayIpAddress.takeIf { value -> value.isNotBlank() },
            prefixLength = networkPrefixLength,
            totalHostCount = totalHostCount,
            scannedHostCount = totalHostCount,
            scanSettings = scanSettings,
            devices = devices
        )
    }

    override fun onCleared() {
        scanJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TAG = "NetworkScannerViewModel"
    }
}