package com.example.wifinetworkscanner.domain.model

data class ScanSettings(
    val maxHosts: Int,
    val timeoutMillis: Int,
    val parallelism: Int
) {

    companion object {
        const val DEFAULT_MAX_HOSTS = 254
        const val DEFAULT_TIMEOUT_MILLIS = 700
        const val DEFAULT_PARALLELISM = 32

        const val MIN_HOSTS = 1
        const val MAX_HOSTS_LIMIT = 4096

        const val MIN_TIMEOUT_MILLIS = 100
        const val MAX_TIMEOUT_MILLIS = 5000

        const val MIN_PARALLELISM = 1
        const val MAX_PARALLELISM = 128

        fun default(): ScanSettings {
            return ScanSettings(
                maxHosts = DEFAULT_MAX_HOSTS,
                timeoutMillis = DEFAULT_TIMEOUT_MILLIS,
                parallelism = DEFAULT_PARALLELISM
            )
        }
    }
}