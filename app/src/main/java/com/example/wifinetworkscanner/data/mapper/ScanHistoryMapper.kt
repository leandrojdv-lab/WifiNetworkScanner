package com.example.wifinetworkscanner.data.mapper

import com.example.wifinetworkscanner.data.local.entity.ScanDeviceEntity
import com.example.wifinetworkscanner.data.local.entity.ScanHistoryEntity
import com.example.wifinetworkscanner.data.local.projection.ScanNetworkGroupProjection
import com.example.wifinetworkscanner.data.local.relation.ScanHistoryWithDevices
import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.model.WifiNetworkInfo

fun ScanHistory.toEntity(): ScanHistoryEntity {
    return ScanHistoryEntity(
        scanHistoryId = id,
        startedAtEpochMillis = startedAtEpochMillis,
        completedAtEpochMillis = completedAtEpochMillis,
        networkName = networkName,
        networkIdentifier = networkIdentifier,
        localIpAddress = localIpAddress,
        gatewayIpAddress = gatewayIpAddress,
        networkPrefixLength = prefixLength,
        totalHostCount = totalHostCount,
        scannedHostCount = scannedHostCount,
        foundDeviceCount = foundDeviceCount,
        maxHosts = scanSettings.maxHosts,
        timeoutMillis = scanSettings.timeoutMillis,
        parallelism = scanSettings.parallelism
    )
}

fun NetworkDevice.toEntity(scanHistoryOwnerId: Long = 0L): ScanDeviceEntity {
    return ScanDeviceEntity(
        scanDeviceId = 0L,
        scanHistoryOwnerId = scanHistoryOwnerId,
        ipAddress = ipAddress,
        latencyMillis = latencyMillis,
        scannedAtEpochMillis = scannedAtEpochMillis,
        detectionMethod = detectionMethod.name,
        openPortsCsv = openPorts.joinToString(separator = ","),
        label = label
    )
}

fun ScanHistoryWithDevices.toDomain(): ScanHistory {
    return ScanHistory(
        id = scanHistory.scanHistoryId,
        startedAtEpochMillis = scanHistory.startedAtEpochMillis,
        completedAtEpochMillis = scanHistory.completedAtEpochMillis,
        networkName = scanHistory.networkName,
        networkIdentifier = scanHistory.networkIdentifier,
        localIpAddress = scanHistory.localIpAddress,
        gatewayIpAddress = scanHistory.gatewayIpAddress,
        prefixLength = scanHistory.networkPrefixLength,
        totalHostCount = scanHistory.totalHostCount,
        scannedHostCount = scanHistory.scannedHostCount,
        scanSettings = ScanSettings(
            maxHosts = scanHistory.maxHosts,
            timeoutMillis = scanHistory.timeoutMillis,
            parallelism = scanHistory.parallelism
        ),
        devices = devices.map { scanDeviceEntity ->
            scanDeviceEntity.toDomain()
        }
    )
}

fun ScanNetworkGroupProjection.toDomain(): NetworkScanGroup {
    return NetworkScanGroup(
        networkIdentifier = networkIdentifier,
        networkName = networkName.ifBlank {
            WifiNetworkInfo.UNKNOWN_NETWORK_NAME
        },
        latestGatewayIpAddress = latestGatewayIpAddress,
        scanCount = scanCount,
        latestCompletedAtEpochMillis = latestCompletedAtEpochMillis,
        totalFoundDeviceCount = totalFoundDeviceCount
    )
}

private fun ScanDeviceEntity.toDomain(): NetworkDevice {
    return NetworkDevice(
        ipAddress = ipAddress,
        latencyMillis = latencyMillis,
        scannedAtEpochMillis = scannedAtEpochMillis,
        detectionMethod = detectionMethod.toDeviceDetectionMethod(),
        openPorts = openPortsCsv.toOpenPorts(),
        label = label
    )
}

private fun String.toDeviceDetectionMethod(): DeviceDetectionMethod {
    return DeviceDetectionMethod.entries.firstOrNull { detectionMethod ->
        detectionMethod.name == this
    } ?: FALLBACK_DEVICE_DETECTION_METHOD
}

private fun String.toOpenPorts(): List<Int> {
    if (isBlank()) {
        return emptyList()
    }

    return split(",")
        .mapNotNull { value -> value.trim().toIntOrNull() }
        .filter { port -> port in MIN_TCP_PORT..MAX_TCP_PORT }
}

private val FALLBACK_DEVICE_DETECTION_METHOD = DeviceDetectionMethod.REACHABLE

private const val MIN_TCP_PORT = 1
private const val MAX_TCP_PORT = 65535