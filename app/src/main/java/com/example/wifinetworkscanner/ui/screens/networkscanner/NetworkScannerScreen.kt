package com.example.wifinetworkscanner.ui.screens.networkscanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.ui.components.EmptyStateCardComponent
import com.example.wifinetworkscanner.ui.components.NetworkDeviceCardComponent
import com.example.wifinetworkscanner.ui.components.ScannerActionsComponent
import com.example.wifinetworkscanner.ui.components.ScannerNetworkSummaryCardComponent
import com.example.wifinetworkscanner.ui.components.ScannerProgressCardComponent
import com.example.wifinetworkscanner.ui.text.asString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScannerScreen(
    onMenuClick: () -> Unit,
    viewModel: NetworkScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionGrantedMessage = stringResource(id = R.string.network_scanner_permission_granted)
    val permissionGrantedLocationDisabledMessage = stringResource(
        id = R.string.network_scanner_permission_granted_location_disabled
    )
    val permissionDeniedMessage = stringResource(id = R.string.network_scanner_permission_denied)
    val continueWithoutNetworkNameMessage = stringResource(
        id = R.string.network_scanner_continue_without_network_name_message
    )
    val locationDisabledMessage = stringResource(id = R.string.network_scanner_location_disabled_message)

    var isWifiNamePermissionDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val wifiNamePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        coroutineScope.launch {
            val message = if (isGranted) {
                if (context.isLocationEnabledSafely()) {
                    permissionGrantedMessage
                } else {
                    permissionGrantedLocationDisabledMessage
                }
            } else {
                permissionDeniedMessage
            }

            snackbarHostState.showSnackbar(message)
        }

        if (!uiState.isScanning) {
            viewModel.startScan()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is NetworkScannerUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    if (isWifiNamePermissionDialogVisible) {
        WifiNamePermissionDialog(
            onDismissRequest = {
                isWifiNamePermissionDialogVisible = false
            },
            onContinueWithoutPermissionClick = {
                isWifiNamePermissionDialogVisible = false

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = continueWithoutNetworkNameMessage
                    )
                }

                viewModel.startScan()
            },
            onGrantPermissionClick = {
                isWifiNamePermissionDialogVisible = false
                wifiNamePermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.network_scanner_title))
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
        NetworkScannerContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onStartScanClick = {
                when {
                    context.hasWifiNamePermission() && !context.isLocationEnabledSafely() -> {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = locationDisabledMessage
                            )
                        }

                        viewModel.startScan()
                    }

                    context.hasWifiNamePermission() -> {
                        viewModel.startScan()
                    }

                    else -> {
                        isWifiNamePermissionDialogVisible = true
                    }
                }
            },
            onCancelScanClick = viewModel::cancelScan
        )
    }
}

@Composable
private fun NetworkScannerContent(
    paddingValues: PaddingValues,
    uiState: NetworkScannerUiState,
    onStartScanClick: () -> Unit,
    onCancelScanClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScannerNetworkSummaryCardComponent(
                networkName = uiState.networkName,
                localIpAddress = uiState.localIpAddress,
                gatewayIpAddress = uiState.gatewayIpAddress,
                networkPrefixLength = uiState.networkPrefixLength,
                totalHostCount = uiState.totalHostCount
            )
        }

        item {
            ScannerActionsComponent(
                isScanning = uiState.isScanning,
                onStartScanClick = onStartScanClick,
                onCancelScanClick = onCancelScanClick
            )
        }

        if (uiState.isScanning) {
            item {
                ScannerProgressCardComponent(
                    scannedHostCount = uiState.scannedHostCount,
                    totalHostCount = uiState.totalHostCount,
                    progress = uiState.progress
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
            Text(
                text = stringResource(
                    id = R.string.network_scanner_devices_found,
                    uiState.devices.size
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.devices.isEmpty()) {
            item {
                EmptyStateCardComponent(
                    title = if (uiState.isScanning) {
                        stringResource(id = R.string.network_scanner_waiting_responses_title)
                    } else {
                        stringResource(id = R.string.network_scanner_no_device_found_title)
                    },
                    message = if (uiState.isScanning) {
                        stringResource(id = R.string.network_scanner_waiting_responses_message)
                    } else {
                        stringResource(id = R.string.network_scanner_no_device_found_message)
                    }
                )
            }
        } else {
            items(
                items = uiState.devices,
                key = { device -> device.ipAddress }
            ) { device ->
                NetworkDeviceCardComponent(
                    device = device
                )
            }
        }
    }
}

@Composable
private fun WifiNamePermissionDialog(
    onDismissRequest: () -> Unit,
    onContinueWithoutPermissionClick: () -> Unit,
    onGrantPermissionClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.network_scanner_wifi_name_permission_title))
        },
        text = {
            Text(text = stringResource(id = R.string.network_scanner_wifi_name_permission_message))
        },
        confirmButton = {
            TextButton(
                onClick = onGrantPermissionClick
            ) {
                Text(text = stringResource(id = R.string.common_allow))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onContinueWithoutPermissionClick
            ) {
                Text(text = stringResource(id = R.string.network_scanner_continue_without_name))
            }
        }
    )
}

private fun Context.hasWifiNamePermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.isLocationEnabledSafely(): Boolean {
    val locationManager = getSystemService(LocationManager::class.java)
        ?: return false

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        locationManager.isLocationEnabled
    } else {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}