package com.example.wifinetworkscanner.ui.formatters

import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod

fun DeviceDetectionMethod.toDisplayText(): String {
    return when (this) {
        DeviceDetectionMethod.LOCAL_ADDRESS -> "Endereço local"
        DeviceDetectionMethod.DEFAULT_GATEWAY -> "Gateway padrão"
        DeviceDetectionMethod.REACHABLE -> "Alcançável"
        DeviceDetectionMethod.TCP_PORT -> "Porta TCP"
        DeviceDetectionMethod.REACHABLE_AND_TCP_PORT -> "Alcançável + Porta TCP"
    }
}