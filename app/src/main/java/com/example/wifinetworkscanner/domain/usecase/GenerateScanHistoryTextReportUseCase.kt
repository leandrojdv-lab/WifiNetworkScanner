package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.ScanHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Gera um relatório textual de uma varredura salva no histórico.
 */
class GenerateScanHistoryTextReportUseCase @Inject constructor() {

    operator fun invoke(scanHistory: ScanHistory): String {
        return buildString {
            appendLine(REPORT_TITLE)
            appendLine(REPORT_SEPARATOR)
            appendLine()

            appendLine("Rede: ${scanHistory.networkName.ifBlank { UNKNOWN_VALUE }}")
            appendLine("Identificador da rede: ${scanHistory.networkIdentifier.ifBlank { UNKNOWN_VALUE }}")
            appendLine("Início: ${scanHistory.startedAtEpochMillis.toReportDateTime()}")
            appendLine("Conclusão: ${scanHistory.completedAtEpochMillis.toReportDateTime()}")
            appendLine("Duração aproximada: ${scanHistory.durationSeconds()} segundo(s)")
            appendLine()

            appendLine("Resumo da rede")
            appendLine(REPORT_SEPARATOR)
            appendLine("IP local: ${scanHistory.localIpAddress.ifBlank { UNKNOWN_VALUE }}/${scanHistory.prefixLength}")
            appendLine("Gateway: ${scanHistory.gatewayIpAddress ?: UNKNOWN_VALUE}")
            appendLine("Total de hosts na faixa: ${scanHistory.totalHostCount}")
            appendLine("Hosts verificados: ${scanHistory.scannedHostCount}")
            appendLine("Dispositivos encontrados: ${scanHistory.foundDeviceCount}")
            appendLine()

            appendLine("Configuração usada")
            appendLine(REPORT_SEPARATOR)
            appendLine("Máximo de hosts: ${scanHistory.scanSettings.maxHosts}")
            appendLine("Timeout por host: ${scanHistory.scanSettings.timeoutMillis} ms")
            appendLine("Paralelismo: ${scanHistory.scanSettings.parallelism}")
            appendLine()

            appendLine("Dispositivos")
            appendLine(REPORT_SEPARATOR)

            if (scanHistory.devices.isEmpty()) {
                appendLine("Nenhum dispositivo encontrado nesta varredura.")
            } else {
                scanHistory.devices.forEachIndexed { index, device ->
                    appendDevice(
                        index = index,
                        device = device
                    )
                }
            }
        }.trimEnd()
    }

    private fun StringBuilder.appendDevice(
        index: Int,
        device: NetworkDevice
    ) {
        appendLine("${index + 1}. ${device.ipAddress}")

        val label = device.label

        if (!label.isNullOrBlank()) {
            appendLine("   Nome/descrição: $label")
        }

        appendLine("   Método de detecção: ${device.detectionMethod.toReportText()}")
        appendLine("   Latência aproximada: ${device.latencyMillis} ms")

        if (device.openPorts.isEmpty()) {
            appendLine("   Portas abertas: nenhuma identificada")
        } else {
            appendLine("   Portas abertas: ${device.openPorts.sorted().joinToString(separator = ", ")}")
        }

        appendLine("   Verificado em: ${device.scannedAtEpochMillis.toReportDateTime()}")
        appendLine()
    }

    private fun ScanHistory.durationSeconds(): Long {
        val durationMillis = completedAtEpochMillis - startedAtEpochMillis
        return durationMillis.coerceAtLeast(0L) / MILLIS_IN_SECOND
    }

    private fun Long.toReportDateTime(): String {
        val formatter = SimpleDateFormat(REPORT_DATE_TIME_PATTERN, Locale.getDefault())
        return formatter.format(Date(this))
    }

    private fun DeviceDetectionMethod.toReportText(): String {
        return when (this) {
            DeviceDetectionMethod.LOCAL_ADDRESS -> "Endereço local"
            DeviceDetectionMethod.DEFAULT_GATEWAY -> "Gateway padrão"
            DeviceDetectionMethod.REACHABLE -> "Alcançável"
            DeviceDetectionMethod.TCP_PORT -> "Porta TCP"
            DeviceDetectionMethod.REACHABLE_AND_TCP_PORT -> "Alcançável + Porta TCP"
        }
    }

    private companion object {
        const val REPORT_TITLE = "Relatório de varredura Wi-Fi"
        const val REPORT_SEPARATOR = "----------------------------------------"
        const val REPORT_DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm:ss"
        const val UNKNOWN_VALUE = "não identificado"
        const val MILLIS_IN_SECOND = 1_000L
    }
}