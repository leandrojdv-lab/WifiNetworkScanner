package com.example.wifinetworkscanner.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.ui.components.ConfirmActionDialogComponent
import com.example.wifinetworkscanner.ui.components.EmptyStateCardComponent
import com.example.wifinetworkscanner.ui.components.NetworkScanGroupCardComponent
import com.example.wifinetworkscanner.ui.text.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onMenuClick: () -> Unit,
    onNetworkGroupClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var isClearHistoryDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var pendingDeleteNetworkIdentifier by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HistoryUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    if (isClearHistoryDialogVisible) {
        ConfirmActionDialogComponent(
            title = stringResource(id = R.string.history_clear_title),
            message = stringResource(id = R.string.history_clear_message),
            confirmText = stringResource(id = R.string.common_clear),
            onDismissRequest = {
                isClearHistoryDialogVisible = false
            },
            onConfirmClick = {
                viewModel.deleteAllScanHistory()
                isClearHistoryDialogVisible = false
            }
        )
    }

    pendingDeleteNetworkIdentifier?.let { networkIdentifier ->
        ConfirmActionDialogComponent(
            title = stringResource(id = R.string.history_delete_network_title),
            message = stringResource(id = R.string.history_delete_network_message),
            confirmText = stringResource(id = R.string.common_remove),
            onDismissRequest = {
                pendingDeleteNetworkIdentifier = null
            },
            onConfirmClick = {
                viewModel.deleteNetworkScanHistory(networkIdentifier = networkIdentifier)
                pendingDeleteNetworkIdentifier = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.history_title))
                },
                navigationIcon = {
                    TextButton(
                        onClick = onMenuClick
                    ) {
                        Text(text = stringResource(id = R.string.common_menu))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            isClearHistoryDialogVisible = true
                        },
                        enabled = uiState.networkScanGroups.isNotEmpty()
                    ) {
                        Text(text = stringResource(id = R.string.common_clear))
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        HistoryContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onNetworkGroupClick = onNetworkGroupClick,
            onDeleteNetworkClick = { networkIdentifier ->
                pendingDeleteNetworkIdentifier = networkIdentifier
            }
        )
    }
}

@Composable
private fun HistoryContent(
    paddingValues: PaddingValues,
    uiState: HistoryUiState,
    onNetworkGroupClick: (String) -> Unit,
    onDeleteNetworkClick: (String) -> Unit
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
            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.networkScanGroups.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateCardComponent(
                        title = stringResource(id = R.string.history_empty_title),
                        message = stringResource(id = R.string.history_empty_message)
                    )
                }
            } else {
                items(
                    items = uiState.networkScanGroups,
                    key = { networkGroup -> networkGroup.networkIdentifier }
                ) { networkGroup ->
                    NetworkScanGroupCardComponent(
                        networkGroup = networkGroup,
                        onOpenClick = {
                            onNetworkGroupClick(networkGroup.networkIdentifier)
                        },
                        onDeleteClick = {
                            onDeleteNetworkClick(networkGroup.networkIdentifier)
                        }
                    )
                }
            }
        }
    }
}