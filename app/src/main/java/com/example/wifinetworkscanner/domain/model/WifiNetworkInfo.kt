package com.example.wifinetworkscanner.domain.model

data class WifiNetworkInfo(
    val localIpAddress: String,
    val prefixLength: Int,
    val interfaceName: String,
    val totalHostCount: Int,
    val gatewayIpAddress: String?,
    val networkName: String,
    val networkIdentifier: String
) {

    companion object {
        const val UNKNOWN_NETWORK_NAME = "Rede desconhecida"
        const val UNKNOWN_NETWORK_IDENTIFIER = "unknown_network"
    }
}