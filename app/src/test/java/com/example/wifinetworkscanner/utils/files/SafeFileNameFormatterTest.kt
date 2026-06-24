package com.example.wifinetworkscanner.utils.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeFileNameFormatterTest {

    @Test
    fun formatTxtFileName_whenNameHasAccentAndSpaces_shouldReturnSafeTxtName() {
        val result = SafeFileNameFormatter.formatTxtFileName(
            rawFileName = "Relatório de Varredura Wi-Fi"
        )

        assertEquals(
            "relatorio_de_varredura_wi-fi.txt",
            result
        )
    }

    @Test
    fun formatCsvFileName_whenNameHasAccentAndSpaces_shouldReturnSafeCsvName() {
        val result = SafeFileNameFormatter.formatCsvFileName(
            rawFileName = "Relatório de Varredura Wi-Fi"
        )

        assertEquals(
            "relatorio_de_varredura_wi-fi.csv",
            result
        )
    }

    @Test
    fun formatFileName_whenRawFileNameHasSupportedExtension_shouldPreserveDetectedExtension() {
        val result = SafeFileNameFormatter.formatFileName(
            rawFileName = "Minha Exportação.CSV",
            fallbackExtension = ".txt"
        )

        assertEquals(
            "minha_exportacao.csv",
            result
        )
    }

    @Test
    fun formatFileName_whenRawFileNameHasUnsupportedExtension_shouldUseFallbackExtension() {
        val result = SafeFileNameFormatter.formatFileName(
            rawFileName = "relatorio.exe",
            fallbackExtension = ".csv"
        )

        assertEquals(
            "relatorio_exe.csv",
            result
        )
    }

    @Test
    fun formatFileName_whenPathTraversalIsProvided_shouldRemoveUnsafeCharacters() {
        val result = SafeFileNameFormatter.formatCsvFileName(
            rawFileName = "../..\\pasta/relatorio.csv"
        )

        assertEquals(
            "pasta_relatorio.csv",
            result
        )

        assertFalse(result.contains(".."))
        assertFalse(result.contains("/"))
        assertFalse(result.contains("\\"))
    }

    @Test
    fun formatFileName_whenNameHasOnlyInvalidCharacters_shouldUseDefaultName() {
        val result = SafeFileNameFormatter.formatTxtFileName(
            rawFileName = "///:::***"
        )

        assertEquals(
            "relatorio_varredura.txt",
            result
        )
    }

    @Test
    fun formatFileName_whenFallbackExtensionIsInvalid_shouldUseTxtExtension() {
        val result = SafeFileNameFormatter.formatFileName(
            rawFileName = "relatorio",
            fallbackExtension = ".exe"
        )

        assertEquals(
            "relatorio.txt",
            result
        )
    }

    @Test
    fun formatFileName_whenNameIsTooLong_shouldLimitNameLengthAndKeepExtension() {
        val longName = "a".repeat(200)

        val result = SafeFileNameFormatter.formatCsvFileName(
            rawFileName = longName
        )

        assertTrue(result.endsWith(".csv"))
        assertEquals(124, result.length)
    }
}