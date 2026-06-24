package com.example.wifinetworkscanner.domain.model

data class ShareableTextFile(
    val fileName: String,
    val contentUri: String,
    val mimeType: String
)