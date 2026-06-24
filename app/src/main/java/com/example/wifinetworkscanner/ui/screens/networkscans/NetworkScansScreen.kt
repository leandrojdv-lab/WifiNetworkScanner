package com.example.wifinetworkscanner.ui.screens.networkscans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.wifinetworkscanner.ui.components.ConfirmActionDialogComponent
import com.example.wifinetworkscanner.ui.components.EmptyStateCardComponent
import com.example.wifinetworkscanner.ui.components.ScanHistoryListCardComponent
import com.example.wifinetworkscanner.ui.text.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScansScreen(
    onBackClick: () -> Unit,
    onScanHistoryClick: (Long) -> Unit,
    viewModel: NetworkScansViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingDeleteHistoryId by rememberSaveable {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is NetworkScansUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    if (pendingDeleteHistoryId > 0L) {
        ConfirmActionDialogComponent(
            title = stringResource(id = R.string.network_scans_delete_title),
            message = stringResource(id = R.string.network_scans_delete_message),
            confirmText = stringResource(id = R.string.common_remove),
            onDismissRequest = {
                pendingDeleteHistoryId = 0L
            },
            onConfirmClick = {
                viewModel.deleteScanHistory(pendingDeleteHistoryId)
                pendingDeleteHistoryId = 0L
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.network_scans_title))
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick
                    ) {
                        Text(text = stringResource(id = R.string.common_back))
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        NetworkScansContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onScanHistoryClick = onScanHistoryClick,
            onDeleteHistoryClick = { scanHistoryId ->
                pendingDeleteHistoryId = scanHistoryId
            }
        )
    }
}

@Composable
private fun NetworkScansContent(
    paddingValues: PaddingValues,
    uiState: NetworkScansUiState,
    onScanHistoryClick: (Long) -> Unit,
    onDeleteHistoryClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NetworkHeaderCard(uiState = uiState)
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

            if (uiState.scanHistoryList.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateCardComponent(
                        title = stringResource(id = R.string.network_scans_empty_title),
                        message = stringResource(id = R.string.network_scans_empty_message)
                    )
                }
            } else {
                items(
                    items = uiState.scanHistoryList,
                    key = { scanHistory -> scanHistory.id }
                ) { scanHistory ->
                    ScanHistoryListCardComponent(
                        scanHistory = scanHistory,
                        showNetworkName = false,
                        primaryActionText = stringResource(id = R.string.common_view_details),
                        onPrimaryActionClick = {
                            onScanHistoryClick(scanHistory.id)
                        },
                        secondaryActionText = stringResource(id = R.string.network_scans_delete_action),
                        onSecondaryActionClick = {
                            onDeleteHistoryClick(scanHistory.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkHeaderCard(
    uiState: NetworkScansUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = uiState.networkName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.network_scans_saved_count,
                    uiState.scanHistoryList.size
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}