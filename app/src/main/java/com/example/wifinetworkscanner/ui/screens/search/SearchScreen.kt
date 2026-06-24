package com.example.wifinetworkscanner.ui.screens.search

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanHistorySearchFilter
import com.example.wifinetworkscanner.ui.components.EmptyStateCardComponent
import com.example.wifinetworkscanner.ui.components.ScanHistoryListCardComponent
import com.example.wifinetworkscanner.ui.text.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMenuClick: () -> Unit,
    onScanHistoryClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SearchUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.search_title))
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
        SearchContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onQueryChange = viewModel::updateQuery,
            onOpenPortChange = viewModel::updateOpenPortText,
            onClearFiltersClick = viewModel::clearFilters,
            onScanHistoryClick = onScanHistoryClick
        )
    }
}

@Composable
private fun SearchContent(
    paddingValues: PaddingValues,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onOpenPortChange: (String) -> Unit,
    onClearFiltersClick: () -> Unit,
    onScanHistoryClick: (Long) -> Unit
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
                SearchFilterCard(
                    searchFilter = uiState.searchFilter,
                    onQueryChange = onQueryChange,
                    onOpenPortChange = onOpenPortChange,
                    onClearFiltersClick = onClearFiltersClick
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
                Text(
                    text = stringResource(
                        id = R.string.search_results_count,
                        uiState.results.size
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.results.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateCardComponent(
                        title = if (uiState.searchFilter.hasActiveFilters) {
                            stringResource(id = R.string.search_no_result_title)
                        } else {
                            stringResource(id = R.string.search_empty_history_title)
                        },
                        message = if (uiState.searchFilter.hasActiveFilters) {
                            stringResource(id = R.string.search_no_result_message)
                        } else {
                            stringResource(id = R.string.search_empty_history_message)
                        }
                    )
                }
            } else {
                items(
                    items = uiState.results,
                    key = { scanHistory -> scanHistory.id }
                ) { scanHistory ->
                    ScanHistoryListCardComponent(
                        scanHistory = scanHistory,
                        showNetworkName = true,
                        primaryActionText = stringResource(id = R.string.common_view_details),
                        onPrimaryActionClick = {
                            onScanHistoryClick(scanHistory.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchFilterCard(
    searchFilter: ScanHistorySearchFilter,
    onQueryChange: (String) -> Unit,
    onOpenPortChange: (String) -> Unit,
    onClearFiltersClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.search_filters_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchFilter.query,
                onValueChange = onQueryChange,
                label = {
                    Text(text = stringResource(id = R.string.search_query_label))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchFilter.openPortText,
                onValueChange = onOpenPortChange,
                label = {
                    Text(text = stringResource(id = R.string.search_open_port_label))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = !searchFilter.isOpenPortValid,
                supportingText = {
                    if (!searchFilter.isOpenPortValid) {
                        Text(
                            text = stringResource(
                                id = R.string.search_invalid_port_message,
                                ScanHistorySearchFilter.MIN_TCP_PORT,
                                ScanHistorySearchFilter.MAX_TCP_PORT
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClearFiltersClick,
                enabled = searchFilter.hasActiveFilters,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.search_clear_filters))
            }
        }
    }
}