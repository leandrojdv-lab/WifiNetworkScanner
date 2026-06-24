package com.example.wifinetworkscanner.data.repository

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanReportFileRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var reportsDirectory: File
    private lateinit var repository: ScanReportFileRepositoryImpl

    @Before
    fun setUp() {
        context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        reportsDirectory = File(context.cacheDir, REPORTS_DIRECTORY_NAME)
        reportsDirectory.deleteRecursively()
        reportsDirectory.mkdirs()

        repository = ScanReportFileRepositoryImpl(
            context = context,
            ioDispatcher = Dispatchers.IO
        )
    }

    @After
    fun tearDown() {
        reportsDirectory.deleteRecursively()
    }

    @Test
    fun createTextFile_whenCsvMimeAndUnsafeName_shouldCreateSafeCsvFileInsideReportsDirectory() = runBlocking {
        val result = repository.createTextFile(
            fileName = "../..\\pasta/Relatório.CSV",
            content = CSV_CONTENT,
            mimeType = TEXT_CSV_MIME_TYPE
        )

        assertTrue(result.isSuccess)

        val shareableTextFile = result.getOrThrow()
        val reportFile = File(reportsDirectory, shareableTextFile.fileName)

        assertEquals("pasta_relatorio.csv", shareableTextFile.fileName)
        assertEquals(TEXT_CSV_MIME_TYPE, shareableTextFile.mimeType)
        assertTrue(shareableTextFile.contentUri.startsWith("content://${context.packageName}.fileprovider/"))
        assertTrue(reportFile.exists())
        assertTrue(reportFile.canonicalPath.startsWith(reportsDirectory.canonicalPath))
        assertEquals(CSV_CONTENT, reportFile.readText(Charsets.UTF_8))
    }

    @Test
    fun createTextFile_whenCsvMimeAndNameWithoutExtension_shouldUseCsvExtension() = runBlocking {
        val result = repository.createTextFile(
            fileName = "relatorio_varredura",
            content = CSV_CONTENT,
            mimeType = TEXT_CSV_MIME_TYPE
        )

        assertTrue(result.isSuccess)

        val shareableTextFile = result.getOrThrow()

        assertEquals("relatorio_varredura.csv", shareableTextFile.fileName)
        assertEquals(TEXT_CSV_MIME_TYPE, shareableTextFile.mimeType)
    }

    @Test
    fun createTextFile_whenTextMimeAndNameWithoutExtension_shouldUseTxtExtension() = runBlocking {
        val result = repository.createTextFile(
            fileName = "relatorio_varredura",
            content = TEXT_CONTENT,
            mimeType = TEXT_PLAIN_MIME_TYPE
        )

        assertTrue(result.isSuccess)

        val shareableTextFile = result.getOrThrow()

        assertEquals("relatorio_varredura.txt", shareableTextFile.fileName)
        assertEquals(TEXT_PLAIN_MIME_TYPE, shareableTextFile.mimeType)
    }

    @Test
    fun createTextFile_whenMimeTypeIsInvalid_shouldReturnFailure() = runBlocking {
        val result = repository.createTextFile(
            fileName = "relatorio.exe",
            content = TEXT_CONTENT,
            mimeType = "application/octet-stream"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun createTextFile_whenOldReportExists_shouldDeleteExpiredReport() = runBlocking {
        val oldReportFile = File(reportsDirectory, "relatorio_antigo.txt")
        oldReportFile.writeText(
            text = TEXT_CONTENT,
            charset = Charsets.UTF_8
        )
        oldReportFile.setLastModified(
            System.currentTimeMillis() - OLD_REPORT_AGE_MILLIS
        )

        val result = repository.createTextFile(
            fileName = "relatorio_novo",
            content = TEXT_CONTENT,
            mimeType = TEXT_PLAIN_MIME_TYPE
        )

        assertTrue(result.isSuccess)
        assertFalse(oldReportFile.exists())
        assertTrue(File(reportsDirectory, "relatorio_novo.txt").exists())
    }

    private companion object {
        const val REPORTS_DIRECTORY_NAME = "reports"
        const val TEXT_PLAIN_MIME_TYPE = "text/plain"
        const val TEXT_CSV_MIME_TYPE = "text/csv"
        const val TEXT_CONTENT = "Relatório de varredura Wi-Fi"
        const val CSV_CONTENT = "section;field;value"
        const val OLD_REPORT_AGE_MILLIS = 2L * 24L * 60L * 60L * 1000L
    }
}