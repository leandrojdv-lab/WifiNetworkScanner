package com.example.wifinetworkscanner.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.validation.ScanSettingsValidationError
import com.example.wifinetworkscanner.domain.validation.ScanSettingsValidationResult
import com.example.wifinetworkscanner.domain.validation.ScanSettingsValidator
import com.example.wifinetworkscanner.ui.text.UiText
import com.example.wifinetworkscanner.ui.text.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SettingsUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings_title))
                },
                navigationIcon = {
                    TextButton(
                        onClick = onMenuClick
                    ) {
                        Text(text = stringResource(id = R.string.common_menu))
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        SettingsContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onSaveClick = viewModel::updateScanSettings
        )
    }
}

@Composable
private fun SettingsContent(
    paddingValues: PaddingValues,
    uiState: SettingsUiState,
    onSaveClick: (ScanSettings) -> Unit
) {
    var maxHostsText by rememberSaveable {
        mutableStateOf(uiState.scanSettings.maxHosts.toString())
    }
    var timeoutMillisText by rememberSaveable {
        mutableStateOf(uiState.scanSettings.timeoutMillis.toString())
    }
    var parallelismText by rememberSaveable {
        mutableStateOf(uiState.scanSettings.parallelism.toString())
    }
    var validationMessage by remember {
        mutableStateOf<UiText?>(null)
    }

    val maxHostsRangeText = stringResource(
        id = R.string.settings_range_hosts,
        ScanSettings.MIN_HOSTS,
        ScanSettings.MAX_HOSTS_LIMIT
    )
    val timeoutRangeText = stringResource(
        id = R.string.settings_range_timeout,
        ScanSettings.MIN_TIMEOUT_MILLIS,
        ScanSettings.MAX_TIMEOUT_MILLIS
    )
    val parallelismRangeText = stringResource(
        id = R.string.settings_range_parallelism,
        ScanSettings.MIN_PARALLELISM,
        ScanSettings.MAX_PARALLELISM
    )

    LaunchedEffect(uiState.scanSettings) {
        maxHostsText = uiState.scanSettings.maxHosts.toString()
        timeoutMillisText = uiState.scanSettings.timeoutMillis.toString()
        parallelismText = uiState.scanSettings.parallelism.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (uiState.isSaving) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsHeaderCard()
            }

            item {
                SettingsNumberFieldCard(
                    title = stringResource(id = R.string.settings_max_hosts_title),
                    description = stringResource(id = R.string.settings_max_hosts_description),
                    value = maxHostsText,
                    rangeText = maxHostsRangeText,
                    enabled = !uiState.isSaving,
                    onValueChange = { value ->
                        maxHostsText = value.onlyDigits()
                        validationMessage = null
                    }
                )
            }

            item {
                SettingsNumberFieldCard(
                    title = stringResource(id = R.string.settings_timeout_title),
                    description = stringResource(id = R.string.settings_timeout_description),
                    value = timeoutMillisText,
                    rangeText = timeoutRangeText,
                    enabled = !uiState.isSaving,
                    onValueChange = { value ->
                        timeoutMillisText = value.onlyDigits()
                        validationMessage = null
                    }
                )
            }

            item {
                SettingsNumberFieldCard(
                    title = stringResource(id = R.string.settings_parallelism_title),
                    description = stringResource(id = R.string.settings_parallelism_description),
                    value = parallelismText,
                    rangeText = parallelismRangeText,
                    enabled = !uiState.isSaving,
                    onValueChange = { value ->
                        parallelismText = value.onlyDigits()
                        validationMessage = null
                    }
                )
            }

            validationMessage?.let { message ->
                item {
                    Text(
                        text = message.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                SettingsActionsCard(
                    isSaving = uiState.isSaving,
                    onRestoreDefaultsClick = {
                        val defaultSettings = ScanSettings.default()
                        maxHostsText = defaultSettings.maxHosts.toString()
                        timeoutMillisText = defaultSettings.timeoutMillis.toString()
                        parallelismText = defaultSettings.parallelism.toString()
                        validationMessage = null
                    },
                    onSaveClick = {
                        when (
                            val result = ScanSettingsValidator.validate(
                                maxHostsText = maxHostsText,
                                timeoutMillisText = timeoutMillisText,
                                parallelismText = parallelismText
                            )
                        ) {
                            is ScanSettingsValidationResult.Success -> {
                                validationMessage = null
                                onSaveClick(result.scanSettings)
                            }

                            is ScanSettingsValidationResult.Error -> {
                                validationMessage = result.error.toUiText()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings_header_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.settings_header_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsNumberFieldCard(
    title: String,
    description: String,
    value: String,
    rangeText: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(text = stringResource(id = R.string.settings_value_label))
                },
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    Text(
                        text = stringResource(
                            id = R.string.settings_allowed_range,
                            rangeText
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsActionsCard(
    isSaving: Boolean,
    onRestoreDefaultsClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Button(
                onClick = onSaveClick,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.settings_save))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onRestoreDefaultsClick,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.settings_restore_defaults))
            }
        }
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

private fun String.onlyDigits(): String {
    return filter { character ->
        character.isDigit()
    }
}