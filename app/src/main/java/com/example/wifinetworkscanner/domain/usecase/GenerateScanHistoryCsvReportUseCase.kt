package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanHistory
import javax.inject.Inject

/**
 * Gera um relatório CSV de uma varredura salva no histórico.
 */
class GenerateScanHistoryCsvReportUseCase @Inject constructor() {

    operator fun invoke(scanHistory: ScanHistory): String {
        return buildString {
            appendRow("section", "field", "value")
            appendRow("network", "network_name", scanHistory.networkName)
            appendRow("network", "network_identifier", scanHistory.networkIdentifier)
            appendRow("network", "started_at_epoch_millis", scanHistory.startedAtEpochMillis.toString())
            appendRow("network", "completed_at_epoch_millis", scanHistory.completedAtEpochMillis.toString())
            appendRow("network", "local_ip_address", scanHistory.localIpAddress)
            appendRow("network", "gateway_ip_address", scanHistory.gatewayIpAddress.orEmpty())
            appendRow("network", "prefix_length", scanHistory.prefixLength.toString())
            appendRow("network", "total_host_count", scanHistory.totalHostCount.toString())
            appendRow("network", "scanned_host_count", scanHistory.scannedHostCount.toString())
            appendRow("network", "found_device_count", scanHistory.foundDeviceCount.toString())
            appendRow("settings", "max_hosts", scanHistory.scanSettings.maxHosts.toString())
            appendRow("settings", "timeout_millis", scanHistory.scanSettings.timeoutMillis.toString())
            appendRow("settings", "parallelism", scanHistory.scanSettings.parallelism.toString())
            appendLine()
            appendRow(
                "device_ip_address",
                "device_label",
                "detection_method",
                "latency_millis",
                "open_ports",
                "scanned_at_epoch_millis"
            )

            scanHistory.devices.forEach { device ->
                appendRow(
                    device.ipAddress,
                    device.label.orEmpty(),
                    device.detectionMethod.name,
                    device.latencyMillis.toString(),
                    device.openPorts.sorted().joinToString(separator = "|"),
                    device.scannedAtEpochMillis.toString()
                )
            }
        }.trimEnd()
    }

    private fun StringBuilder.appendRow(
        vararg values: String
    ) {
        appendLine(
            values.joinToString(separator = CSV_SEPARATOR) { value ->
                value.toCsvCell()
            }
        )
    }

    private fun String.toCsvCell(): String {
        val shouldQuote = contains(CSV_SEPARATOR) ||
                contains(QUOTE) ||
                contains(LINE_BREAK) ||
                contains(CARRIAGE_RETURN)

        val escapedValue = replace(QUOTE, DOUBLE_QUOTE)

        return if (shouldQuote) {
            "$QUOTE$escapedValue$QUOTE"
        } else {
            escapedValue
        }
    }

    private companion object {
        const val CSV_SEPARATOR = ";"
        const val QUOTE = "\""
        const val DOUBLE_QUOTE = "\"\""
        const val LINE_BREAK = "\n"
        const val CARRIAGE_RETURN = "\r"
    }
}