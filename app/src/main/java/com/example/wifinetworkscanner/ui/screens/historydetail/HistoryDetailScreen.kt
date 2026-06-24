package com.example.wifinetworkscanner.ui.screens.historydetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ShareableTextFile
import com.example.wifinetworkscanner.ui.components.EmptyStateCardComponent
import com.example.wifinetworkscanner.ui.components.NetworkDeviceCardComponent
import com.example.wifinetworkscanner.ui.components.ScanHistorySummaryCardComponent
import com.example.wifinetworkscanner.ui.text.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val shareChooserTitle = stringResource(id = R.string.history_detail_share_chooser_title)

    var isExportMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HistoryDetailUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }

                is HistoryDetailUiEffect.ShareReport -> {
                    context.shareTextReport(
                        shareableTextFile = effect.shareableTextFile,
                        chooserTitle = shareChooserTitle
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.history_detail_title))
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick
                    ) {
                        Text(text = stringResource(id = R.string.common_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            isExportMenuExpanded = true
                        },
                        enabled = uiState.scanHistory != null && !uiState.isExportingReport
                    ) {
                        Text(
                            text = if (uiState.isExportingReport) {
                                stringResource(id = R.string.history_detail_exporting)
                            } else {
                                stringResource(id = R.string.history_detail_export)
                            }
                        )
                    }

                    DropdownMenu(
                        expanded = isExportMenuExpanded,
                        onDismissRequest = {
                            isExportMenuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(text = stringResource(id = R.string.history_detail_export_txt))
                            },
                            onClick = {
                                isExportMenuExpanded = false
                                viewModel.exportTextReport()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(text = stringResource(id = R.string.history_detail_export_csv))
                            },
                            onClick = {
                                isExportMenuExpanded = false
                                viewModel.exportCsvReport()
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        HistoryDetailContent(
            paddingValues = paddingValues,
            uiState = uiState
        )
    }
}

@Composable
private fun HistoryDetailContent(
    paddingValues: PaddingValues,
    uiState: HistoryDetailUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (uiState.isLoading || uiState.isExportingReport) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        val scanHistory = uiState.scanHistory

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (scanHistory == null && !uiState.isLoading) {
                item {
                    EmptyStateCardComponent(
                        title = stringResource(id = R.string.history_detail_not_found_title),
                        message = stringResource(id = R.string.history_detail_not_found_message)
                    )
                }
            }

            if (scanHistory != null) {
                item {
                    ScanHistorySummaryCardComponent(
                        scanHistory = scanHistory
                    )
                }

                item {
                    Text(
                        text = stringResource(
                            id = R.string.history_detail_devices_found,
                            scanHistory.devices.size
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (scanHistory.devices.isEmpty()) {
                    item {
                        EmptyStateCardComponent(
                            title = stringResource(id = R.string.history_detail_no_saved_device_title),
                            message = stringResource(id = R.string.history_detail_no_saved_device_message)
                        )
                    }
                } else {
                    items(
                        items = scanHistory.devices,
                        key = { device -> "${device.ipAddress}-${device.scannedAtEpochMillis}" }
                    ) { device ->
                        NetworkDeviceCardComponent(
                            device = device
                        )
                    }
                }
            }
        }
    }
}

private fun Context.shareTextReport(
    shareableTextFile: ShareableTextFile,
    chooserTitle: String
) {
    val contentUri = Uri.parse(shareableTextFile.contentUri)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = shareableTextFile.mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_SUBJECT, shareableTextFile.fileName)
        putExtra(Intent.EXTRA_TITLE, shareableTextFile.fileName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(
        shareIntent,
        chooserTitle
    )

    startActivity(chooserIntent)
}