package com.example.wifinetworkscanner.utils.logger

import org.junit.Test

class AppLoggerTest {

    @Test
    fun loggerMethods_whenCalledInJvmUnitTest_shouldNotThrowException() {
        val throwable = IllegalStateException("Erro de teste")

        AppLogger.verbose(
            tag = TAG,
            message = "Mensagem verbose"
        )

        AppLogger.debug(
            tag = TAG,
            message = "Mensagem debug"
        )

        AppLogger.info(
            tag = TAG,
            message = "Mensagem info"
        )

        AppLogger.warning(
            tag = TAG,
            message = "Mensagem warning"
        )

        AppLogger.warning(
            tag = TAG,
            message = "Mensagem warning com throwable",
            throwable = throwable
        )

        AppLogger.error(
            tag = TAG,
            message = "Mensagem error"
        )

        AppLogger.error(
            tag = TAG,
            message = "Mensagem error com throwable",
            throwable = throwable
        )
    }

    private companion object {
        const val TAG = "AppLoggerTest"
    }
}