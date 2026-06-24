package com.example.wifinetworkscanner.domain.model

enum class DeviceDetectionMethod {
    LOCAL_ADDRESS,
    DEFAULT_GATEWAY,
    REACHABLE,
    TCP_PORT,
    REACHABLE_AND_TCP_PORT
}