package com.example.wifinetworkscanner.utils.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Ipv4UtilsTest {

    @Test
    fun calculateUsableHostAddresses_whenNetworkIs24_shouldReturn254Hosts() {
        val result = Ipv4Utils.calculateUsableHostAddresses(
            ipAddress = "192.168.1.25",
            prefixLength = 24,
            maxHosts = 254
        )

        assertEquals(254, result.size)
        assertEquals("192.168.1.1", result.first())
        assertEquals("192.168.1.254", result.last())
    }

    @Test
    fun calculateUsableHostAddresses_whenMaxHostsIs10_shouldReturnOnly10Hosts() {
        val result = Ipv4Utils.calculateUsableHostAddresses(
            ipAddress = "192.168.1.25",
            prefixLength = 24,
            maxHosts = 10
        )

        assertEquals(10, result.size)
        assertEquals("192.168.1.1", result.first())
        assertEquals("192.168.1.10", result.last())
    }

    @Test
    fun calculateUsableHostAddresses_whenIpIsInvalid_shouldReturnEmptyList() {
        val result = Ipv4Utils.calculateUsableHostAddresses(
            ipAddress = "192.168.1.999",
            prefixLength = 24,
            maxHosts = 254
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun countUsableHosts_whenNetworkIs24_shouldReturn254Hosts() {
        val result = Ipv4Utils.countUsableHosts(
            prefixLength = 24,
            maxHosts = 254
        )

        assertEquals(254, result)
    }

    @Test
    fun ipAddressSortValue_whenComparingAddresses_shouldKeepNumericOrder() {
        val first = Ipv4Utils.ipAddressSortValue("192.168.1.2")
        val second = Ipv4Utils.ipAddressSortValue("192.168.1.10")

        assertTrue(first < second)
    }
}