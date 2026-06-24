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
import com.example.wifinetworkscanner.ui.text.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

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
            onSaveClick = viewModel::updateScanSettings,
            onInputChanged = viewModel::clearErrorMessage
        )
    }
}

@Composable
private fun SettingsContent(
    paddingValues: PaddingValues,
    uiState: SettingsUiState,
    onSaveClick: (
        maxHostsText: String,
        timeoutMillisText: String,
        parallelismText: String
    ) -> Unit,
    onInputChanged: () -> Unit
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
                        onInputChanged()
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
                        onInputChanged()
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
                        onInputChanged()
                    }
                )
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
                        onInputChanged()
                    },
                    onSaveClick = {
                        onSaveClick(
                            maxHostsText,
                            timeoutMillisText,
                            parallelismText
                        )
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

private fun String.onlyDigits(): String {
    return filter { character ->
        character.isDigit()
    }
}