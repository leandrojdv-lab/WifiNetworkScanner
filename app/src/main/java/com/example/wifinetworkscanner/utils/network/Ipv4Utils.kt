package com.example.wifinetworkscanner.utils.network

import kotlin.math.min

object Ipv4Utils {

    private const val IPV4_BITS = 32
    private const val IPV4_MAX_VALUE = 0xFFFFFFFFL
    private const val OCTET_COUNT = 4
    private const val MIN_PREFIX_LENGTH = 1
    private const val MAX_PREFIX_LENGTH_WITH_HOSTS = 30

    fun calculateUsableHostAddresses(
        ipAddress: String,
        prefixLength: Int,
        maxHosts: Int
    ): List<String> {
        if (prefixLength !in MIN_PREFIX_LENGTH..MAX_PREFIX_LENGTH_WITH_HOSTS) {
            return emptyList()
        }

        if (maxHosts <= 0) {
            return emptyList()
        }

        val ipAsLong = ipv4ToLong(ipAddress) ?: return emptyList()
        val mask = (IPV4_MAX_VALUE shl (IPV4_BITS - prefixLength)) and IPV4_MAX_VALUE
        val networkAddress = ipAsLong and mask
        val broadcastAddress = networkAddress or (mask.inv() and IPV4_MAX_VALUE)
        val firstHost = networkAddress + 1L
        val lastHost = broadcastAddress - 1L

        if (firstHost > lastHost) {
            return emptyList()
        }

        val limitedLastHost = min(lastHost, firstHost + maxHosts - 1L)

        return buildList {
            var currentHost = firstHost

            while (currentHost <= limitedLastHost) {
                add(longToIpv4(currentHost))
                currentHost++
            }
        }
    }

    fun countUsableHosts(
        prefixLength: Int,
        maxHosts: Int
    ): Int {
        if (prefixLength !in MIN_PREFIX_LENGTH..MAX_PREFIX_LENGTH_WITH_HOSTS) {
            return 0
        }

        if (maxHosts <= 0) {
            return 0
        }

        val calculatedHosts = (1L shl (IPV4_BITS - prefixLength)) - 2L
        return min(calculatedHosts, maxHosts.toLong()).toInt()
    }

    fun ipAddressSortValue(ipAddress: String): Long {
        return ipv4ToLong(ipAddress) ?: Long.MAX_VALUE
    }

    private fun ipv4ToLong(ipAddress: String): Long? {
        val parts = ipAddress.split(".")

        if (parts.size != OCTET_COUNT) {
            return null
        }

        var result = 0L

        for (part in parts) {
            val octet = part.toIntOrNull() ?: return null

            if (octet !in 0..255) {
                return null
            }

            result = (result shl 8) or octet.toLong()
        }

        return result and IPV4_MAX_VALUE
    }

    private fun longToIpv4(value: Long): String {
        val firstOctet = (value shr 24) and 0xFF
        val secondOctet = (value shr 16) and 0xFF
        val thirdOctet = (value shr 8) and 0xFF
        val fourthOctet = value and 0xFF

        return "$firstOctet.$secondOctet.$thirdOctet.$fourthOctet"
    }
}