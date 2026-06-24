package com.example.wifinetworkscanner.utils.files

import java.text.Normalizer
import java.util.Locale

object SafeFileNameFormatter {

    fun formatFileName(
        rawFileName: String,
        fallbackExtension: String = TXT_EXTENSION
    ): String {
        val normalizedExtension = normalizeExtension(extension = fallbackExtension)
        val detectedExtension = detectSupportedExtension(rawFileName = rawFileName)
        val extension = detectedExtension ?: normalizedExtension
        val rawNameWithoutExtension = removeSupportedExtension(rawFileName = rawFileName)

        val normalizedName = Normalizer
            .normalize(rawNameWithoutExtension, Normalizer.Form.NFD)
            .replace(DIACRITICS_REGEX, "")

        val sanitizedName = normalizedName
            .trim()
            .lowercase(Locale.ROOT)
            .replace(PATH_SEPARATOR_REGEX, "_")
            .replace(INVALID_FILE_NAME_REGEX, "_")
            .replace(MULTIPLE_UNDERSCORES_REGEX, "_")
            .trim('_', '-', '.')
            .take(MAX_FILE_NAME_LENGTH_WITHOUT_EXTENSION)

        val safeName = sanitizedName.ifBlank {
            DEFAULT_FILE_NAME_WITHOUT_EXTENSION
        }

        return "$safeName$extension"
    }

    fun formatTxtFileName(rawFileName: String): String {
        return formatFileName(
            rawFileName = rawFileName,
            fallbackExtension = TXT_EXTENSION
        )
    }

    fun formatCsvFileName(rawFileName: String): String {
        return formatFileName(
            rawFileName = rawFileName,
            fallbackExtension = CSV_EXTENSION
        )
    }

    private fun normalizeExtension(extension: String): String {
        val sanitizedExtension = extension
            .trim()
            .lowercase(Locale.ROOT)
            .let { value ->
                if (value.startsWith(EXTENSION_SEPARATOR)) {
                    value
                } else {
                    "$EXTENSION_SEPARATOR$value"
                }
            }

        return when (sanitizedExtension) {
            TXT_EXTENSION -> TXT_EXTENSION
            CSV_EXTENSION -> CSV_EXTENSION
            else -> TXT_EXTENSION
        }
    }

    private fun detectSupportedExtension(rawFileName: String): String? {
        val lowerCaseFileName = rawFileName.trim().lowercase(Locale.ROOT)

        return when {
            lowerCaseFileName.endsWith(TXT_EXTENSION) -> TXT_EXTENSION
            lowerCaseFileName.endsWith(CSV_EXTENSION) -> CSV_EXTENSION
            else -> null
        }
    }

    private fun removeSupportedExtension(rawFileName: String): String {
        val trimmedFileName = rawFileName.trim()
        val lowerCaseFileName = trimmedFileName.lowercase(Locale.ROOT)

        return when {
            lowerCaseFileName.endsWith(TXT_EXTENSION) -> {
                trimmedFileName.dropLast(TXT_EXTENSION.length)
            }

            lowerCaseFileName.endsWith(CSV_EXTENSION) -> {
                trimmedFileName.dropLast(CSV_EXTENSION.length)
            }

            else -> trimmedFileName
        }
    }

    private const val TXT_EXTENSION = ".txt"
    private const val CSV_EXTENSION = ".csv"
    private const val EXTENSION_SEPARATOR = "."
    private const val DEFAULT_FILE_NAME_WITHOUT_EXTENSION = "relatorio_varredura"
    private const val MAX_FILE_NAME_LENGTH_WITHOUT_EXTENSION = 120

    private val DIACRITICS_REGEX = Regex("\\p{Mn}+")
    private val PATH_SEPARATOR_REGEX = Regex("[/\\\\]+")
    private val INVALID_FILE_NAME_REGEX = Regex("[^a-z0-9_-]")
    private val MULTIPLE_UNDERSCORES_REGEX = Regex("_+")
}