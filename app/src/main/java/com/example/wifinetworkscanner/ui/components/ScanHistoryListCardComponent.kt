package com.example.wifinetworkscanner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.ui.formatters.toDisplayDateTime

@Composable
fun ScanHistoryListCardComponent(
    scanHistory: ScanHistory,
    primaryActionText: String,
    onPrimaryActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    showNetworkName: Boolean = true,
    secondaryActionText: String? = null,
    onSecondaryActionClick: (() -> Unit)? = null
) {
    val openPorts = scanHistory.devices
        .flatMap { device -> device.openPorts }
        .distinct()
        .sorted()
        .take(OPEN_PORT_PREVIEW_LIMIT)

    val gatewayText = scanHistory.gatewayIpAddress
        ?: stringResource(id = R.string.common_not_identified)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (showNetworkName) {
                Text(
                    text = scanHistory.networkName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = scanHistory.completedAtEpochMillis.toDisplayDateTime(),
                style = if (showNetworkName) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = if (showNetworkName) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (showNetworkName) {
                    FontWeight.Normal
                } else {
                    FontWeight.SemiBold
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_list_local_ip,
                    scanHistory.localIpAddress,
                    scanHistory.prefixLength
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_list_gateway,
                    gatewayText
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_list_found,
                    scanHistory.foundDeviceCount,
                    scanHistory.totalHostCount
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            if (openPorts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(
                        id = R.string.scan_history_list_open_ports,
                        openPorts.joinToString(separator = ", ")
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_list_settings,
                    scanHistory.scanSettings.maxHosts,
                    scanHistory.scanSettings.timeoutMillis,
                    scanHistory.scanSettings.parallelism
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onPrimaryActionClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = primaryActionText)
            }

            if (secondaryActionText != null && onSecondaryActionClick != null) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSecondaryActionClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = secondaryActionText)
                }
            }
        }
    }
}

private const val OPEN_PORT_PREVIEW_LIMIT = 10