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
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.ui.formatters.toDisplayDateTime

@Composable
fun NetworkScanGroupCardComponent(
    networkGroup: NetworkScanGroup,
    onOpenClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gatewayText = networkGroup.latestGatewayIpAddress
        ?: stringResource(id = R.string.common_not_identified)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = networkGroup.networkName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.network_scan_group_latest_scan,
                    networkGroup.latestCompletedAtEpochMillis.toDisplayDateTime()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    id = R.string.network_scan_group_latest_gateway,
                    gatewayText
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.network_scan_group_scan_count,
                    networkGroup.scanCount
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.network_scan_group_total_found,
                    networkGroup.totalFoundDeviceCount
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.network_scan_group_open_scans))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.network_scan_group_delete_network))
            }
        }
    }
}