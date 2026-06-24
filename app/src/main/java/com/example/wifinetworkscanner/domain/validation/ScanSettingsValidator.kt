package com.example.wifinetworkscanner.domain.validation

import com.example.wifinetworkscanner.domain.model.ScanSettings

/**
 * Valida entradas textuais usadas para configurar a varredura de rede.
 */
object ScanSettingsValidator {

    /**
     * Converte e valida os campos da tela de configurações.
     */
    fun validate(
        maxHostsText: String,
        timeoutMillisText: String,
        parallelismText: String
    ): ScanSettingsValidationResult {
        val maxHosts = maxHostsText.toIntOrNull()
            ?: return ScanSettingsValidationResult.Error(
                error = ScanSettingsValidationError.InvalidMaxHostsValue
            )

        val timeoutMillis = timeoutMillisText.toIntOrNull()
            ?: return ScanSettingsValidationResult.Error(
                error = ScanSettingsValidationError.InvalidTimeoutMillisValue
            )

        val parallelism = parallelismText.toIntOrNull()
            ?: return ScanSettingsValidationResult.Error(
                error = ScanSettingsValidationError.InvalidParallelismValue
            )

        if (maxHosts !in ScanSettings.MIN_HOSTS..ScanSettings.MAX_HOSTS_LIMIT) {
            return ScanSettingsValidationResult.Error(
                error = ScanSettingsValidationError.MaxHostsOutOfRange
            )
        }

        if (timeoutMillis !in ScanSettings.MIN_TIMEOUT_MILLIS..ScanSettings.MAX_TIMEOUT_MILLIS) {
            return ScanSettingsValidationResult.Error(
                error = ScanSettingsValidationError.TimeoutMillisOutOfRange
            )
        }

        if (parallelism !in ScanSettings.MIN_PARALLELISM..ScanSettings.MAX_PARALLELISM) {
            return ScanSettingsValidationResult.Error(
                error = ScanSettingsValidationError.ParallelismOutOfRange
            )
        }

        return ScanSettingsValidationResult.Success(
            scanSettings = ScanSettings(
                maxHosts = maxHosts,
                timeoutMillis = timeoutMillis,
                parallelism = parallelism
            )
        )
    }
}

/**
 * Resultado tipado da validação das configurações de varredura.
 */
sealed interface ScanSettingsValidationResult {

    data class Success(
        val scanSettings: ScanSettings
    ) : ScanSettingsValidationResult

    data class Error(
        val error: ScanSettingsValidationError
    ) : ScanSettingsValidationResult
}

/**
 * Erros possíveis na validação de configurações.
 *
 * Mantém a camada de domínio sem dependência de Android, Context ou recursos de string.
 */
sealed interface ScanSettingsValidationError {

    data object InvalidMaxHostsValue : ScanSettingsValidationError

    data object InvalidTimeoutMillisValue : ScanSettingsValidationError

    data object InvalidParallelismValue : ScanSettingsValidationError

    data object MaxHostsOutOfRange : ScanSettingsValidationError

    data object TimeoutMillisOutOfRange : ScanSettingsValidationError

    data object ParallelismOutOfRange : ScanSettingsValidationError
}