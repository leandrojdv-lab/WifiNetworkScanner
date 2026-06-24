package com.example.wifinetworkscanner.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import com.example.wifinetworkscanner.di.IoDispatcher
import com.example.wifinetworkscanner.domain.model.ShareableTextFile
import com.example.wifinetworkscanner.domain.repository.ScanReportFileRepository
import com.example.wifinetworkscanner.utils.files.SafeFileNameFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class ScanReportFileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ScanReportFileRepository {

    override suspend fun createTextReportFile(
        fileName: String,
        content: String
    ): Result<ShareableTextFile> {
        return createTextFile(
            fileName = fileName,
            content = content,
            mimeType = TEXT_PLAIN_MIME_TYPE
        )
    }

    override suspend fun createTextFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<ShareableTextFile> {
        return withContext(ioDispatcher) {
            runCatching {
                validateReportInput(
                    fileName = fileName,
                    content = content,
                    mimeType = mimeType
                )

                val reportsDirectory = getReportsDirectory()
                ensureReportsDirectoryExists(reportsDirectory)
                deleteExpiredReportFiles(reportsDirectory)

                val safeFileName = SafeFileNameFormatter.formatFileName(
                    rawFileName = fileName,
                    fallbackExtension = resolveFallbackExtension(mimeType = mimeType)
                ).trim()

                require(safeFileName.isNotBlank()) {
                    "O nome seguro do arquivo não pode ser vazio."
                }

                val reportFile = File(reportsDirectory, safeFileName)
                validateReportFileLocation(
                    reportsDirectory = reportsDirectory,
                    reportFile = reportFile
                )

                reportFile.writeText(
                    text = content,
                    charset = Charsets.UTF_8
                )

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    reportFile
                )

                ShareableTextFile(
                    fileName = safeFileName,
                    contentUri = contentUri.toString(),
                    mimeType = mimeType
                )
            }
        }
    }

    private fun validateReportInput(
        fileName: String,
        content: String,
        mimeType: String
    ) {
        require(fileName.isNotBlank()) {
            "O nome do arquivo não pode ser vazio."
        }

        require(content.isNotBlank()) {
            "O conteúdo do relatório não pode ser vazio."
        }

        require(mimeType in ALLOWED_MIME_TYPES) {
            "Tipo de arquivo não permitido para exportação."
        }
    }

    private fun getReportsDirectory(): File {
        return File(context.cacheDir, REPORTS_DIRECTORY_NAME)
    }

    private fun ensureReportsDirectoryExists(reportsDirectory: File) {
        if (reportsDirectory.exists()) {
            require(reportsDirectory.isDirectory) {
                "O caminho de relatórios não é um diretório válido."
            }
            return
        }

        require(reportsDirectory.mkdirs()) {
            "Não foi possível criar o diretório de relatórios."
        }
    }

    private fun validateReportFileLocation(
        reportsDirectory: File,
        reportFile: File
    ) {
        val reportsDirectoryPath = reportsDirectory.canonicalPath
        val reportFilePath = reportFile.canonicalPath

        require(reportFilePath.startsWith(reportsDirectoryPath)) {
            "O arquivo de relatório precisa estar dentro do diretório permitido."
        }
    }

    private fun deleteExpiredReportFiles(reportsDirectory: File) {
        val expirationThresholdMillis = System.currentTimeMillis() - REPORT_EXPIRATION_MILLIS

        reportsDirectory
            .listFiles()
            .orEmpty()
            .filter { file ->
                file.isFile && file.lastModified() < expirationThresholdMillis
            }
            .forEach { file ->
                runCatching {
                    file.delete()
                }
            }
    }

    private fun resolveFallbackExtension(mimeType: String): String {
        return when (mimeType) {
            TEXT_CSV_MIME_TYPE -> CSV_EXTENSION
            else -> TXT_EXTENSION
        }
    }

    private companion object {
        const val REPORTS_DIRECTORY_NAME = "reports"
        const val TEXT_PLAIN_MIME_TYPE = "text/plain"
        const val TEXT_CSV_MIME_TYPE = "text/csv"
        const val TXT_EXTENSION = ".txt"
        const val CSV_EXTENSION = ".csv"
        const val REPORT_EXPIRATION_MILLIS = 24L * 60L * 60L * 1000L

        val ALLOWED_MIME_TYPES = setOf(
            TEXT_PLAIN_MIME_TYPE,
            TEXT_CSV_MIME_TYPE
        )
    }
}