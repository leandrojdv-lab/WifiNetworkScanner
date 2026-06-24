package com.example.wifinetworkscanner.domain.repository

import com.example.wifinetworkscanner.domain.model.ShareableTextFile

interface ScanReportFileRepository {

    suspend fun createTextReportFile(
        fileName: String,
        content: String
    ): Result<ShareableTextFile>

    suspend fun createTextFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<ShareableTextFile>
}