package com.example.wifinetworkscanner.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.usecase.ObserveScanSettingsUseCase
import com.example.wifinetworkscanner.domain.usecase.UpdateScanSettingsUseCase
import com.example.wifinetworkscanner.domain.validation.ScanSettingsValidationError
import com.example.wifinetworkscanner.domain.validation.ScanSettingsValidationResult
import com.example.wifinetworkscanner.domain.validation.ScanSettingsValidator
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
class SettingsViewModel @Inject constructor(
    private val observeScanSettingsUseCase: ObserveScanSettingsUseCase,
    private val updateScanSettingsUseCase: UpdateScanSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SettingsUiEffect>(
        extraBufferCapacity = UI_EFFECT_BUFFER_CAPACITY
    )
    val uiEffect: SharedFlow<SettingsUiEffect> = _uiEffect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.error(TAG, "Erro inesperado nas configurações.", throwable)

        val message = buildOperationFailureMessage()
        applyOperationFailureState(message)
        emitEffect(SettingsUiEffect.ShowMessage(message = message))
    }

    init {
        observeSettings()
    }

    fun updateScanSettings(
        maxHostsText: String,
        timeoutMillisText: String,
        parallelismText: String
    ) {
        when (
            val validationResult = ScanSettingsValidator.validate(
                maxHostsText = maxHostsText,
                timeoutMillisText = timeoutMillisText,
                parallelismText = parallelismText
            )
        ) {
            is ScanSettingsValidationResult.Success -> {
                saveScanSettings(scanSettings = validationResult.scanSettings)
            }

            is ScanSettingsValidationResult.Error -> {
                applyValidationFailureState(validationError = validationResult.error)
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }

    private fun saveScanSettings(scanSettings: ScanSettings) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            try {
                updateScanSettingsUseCase(scanSettings)

                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        errorMessage = null
                    )
                }

                emitEffect(
                    SettingsUiEffect.ShowMessage(
                        message = UiText.StringResource(
                            resId = R.string.settings_saved_message
                        )
                    )
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                AppLogger.error(TAG, "Falha ao salvar configurações.", exception)

                val message = buildOperationFailureMessage()
                applyOperationFailureState(message)
                emitEffect(SettingsUiEffect.ShowMessage(message = message))
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch(exceptionHandler) {
            observeScanSettingsUseCase().collect { scanSettings ->
                _uiState.update { currentState ->
                    currentState.copy(scanSettings = scanSettings)
                }
            }
        }
    }

    private fun applyValidationFailureState(validationError: ScanSettingsValidationError) {
        val message = validationError.toUiText()

        _uiState.update { currentState ->
            currentState.copy(
                isSaving = false,
                errorMessage = message
            )
        }

        emitEffect(SettingsUiEffect.ShowMessage(message = message))
    }

    private fun buildOperationFailureMessage(): UiText {
        return UiText.StringResource(
            resId = R.string.settings_operation_failed_message
        )
    }

    private fun applyOperationFailureState(message: UiText) {
        _uiState.update { currentState ->
            currentState.copy(
                isSaving = false,
                errorMessage = message
            )
        }
    }

    private fun ScanSettingsValidationError.toUiText(): UiText {
        return when (this) {
            ScanSettingsValidationError.InvalidMaxHostsValue -> {
                UiText.StringResource(
                    resId = R.string.settings_validation_max_hosts_required
                )
            }

            ScanSettingsValidationError.InvalidTimeoutMillisValue -> {
                UiText.StringResource(
                    resId = R.string.settings_validation_timeout_required
                )
            }

            ScanSettingsValidationError.InvalidParallelismValue -> {
                UiText.StringResource(
                    resId = R.string.settings_validation_parallelism_required
                )
            }

            ScanSettingsValidationError.MaxHostsOutOfRange -> {
                UiText.StringResource(
                    resId = R.string.settings_validation_max_hosts_range,
                    args = listOf(
                        ScanSettings.MIN_HOSTS,
                        ScanSettings.MAX_HOSTS_LIMIT
                    )
                )
            }

            ScanSettingsValidationError.TimeoutMillisOutOfRange -> {
                UiText.StringResource(
                    resId = R.string.settings_validation_timeout_range,
                    args = listOf(
                        ScanSettings.MIN_TIMEOUT_MILLIS,
                        ScanSettings.MAX_TIMEOUT_MILLIS
                    )
                )
            }

            ScanSettingsValidationError.ParallelismOutOfRange -> {
                UiText.StringResource(
                    resId = R.string.settings_validation_parallelism_range,
                    args = listOf(
                        ScanSettings.MIN_PARALLELISM,
                        ScanSettings.MAX_PARALLELISM
                    )
                )
            }
        }
    }

    private fun emitEffect(effect: SettingsUiEffect) {
        _uiEffect.tryEmit(effect)
    }

    private companion object {
        const val TAG = "SettingsViewModel"
        const val UI_EFFECT_BUFFER_CAPACITY = 1
    }
}