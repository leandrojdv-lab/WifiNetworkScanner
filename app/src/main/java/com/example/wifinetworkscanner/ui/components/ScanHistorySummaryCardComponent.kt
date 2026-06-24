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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.ui.formatters.toDisplayDateTime
import com.example.wifinetworkscanner.ui.formatters.toDurationSecondsUntil

@Composable
fun ScanHistorySummaryCardComponent(
    scanHistory: ScanHistory,
    modifier: Modifier = Modifier
) {
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
            Text(
                text = scanHistory.networkName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.scan_history_summary_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_summary_started_at,
                    scanHistory.startedAtEpochMillis.toDisplayDateTime()
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_summary_completed_at,
                    scanHistory.completedAtEpochMillis.toDisplayDateTime()
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_summary_duration,
                    scanHistory.startedAtEpochMillis.toDurationSecondsUntil(
                        endEpochMillis = scanHistory.completedAtEpochMillis
                    )
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_summary_local_ip,
                    scanHistory.localIpAddress,
                    scanHistory.prefixLength
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_summary_gateway,
                    gatewayText
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_summary_hosts_checked,
                    scanHistory.scannedHostCount,
                    scanHistory.totalHostCount
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    id = R.string.scan_history_summary_settings,
                    scanHistory.scanSettings.maxHosts,
                    scanHistory.scanSettings.timeoutMillis,
                    scanHistory.scanSettings.parallelism
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}