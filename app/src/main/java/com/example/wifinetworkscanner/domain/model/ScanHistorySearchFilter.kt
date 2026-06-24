package com.example.wifinetworkscanner.domain.model

data class ScanHistorySearchFilter(
    val query: String = "",
    val openPortText: String = ""
) {

    val normalizedQuery: String
        get() = query.trim()

    val openPort: Int?
        get() {
            val value = openPortText.trim().toIntOrNull() ?: return null
            return value.takeIf { port ->
                port in MIN_TCP_PORT..MAX_TCP_PORT
            }
        }

    val isOpenPortValid: Boolean
        get() = openPortText.isBlank() || openPort != null

    val hasActiveFilters: Boolean
        get() = normalizedQuery.isNotBlank() || openPortText.isNotBlank()

    companion object {
        const val MIN_TCP_PORT = 1
        const val MAX_TCP_PORT = 65535
    }
}