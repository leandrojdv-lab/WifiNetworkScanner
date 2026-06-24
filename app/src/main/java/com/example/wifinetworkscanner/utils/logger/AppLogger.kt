package com.example.wifinetworkscanner.utils.logger

import android.util.Log
import com.example.wifinetworkscanner.BuildConfig

object AppLogger {

    fun verbose(
        tag: String,
        message: String
    ) {
        if (BuildConfig.DEBUG) {
            safeLog {
                Log.v(tag, message)
            }
        }
    }

    fun debug(
        tag: String,
        message: String
    ) {
        if (BuildConfig.DEBUG) {
            safeLog {
                Log.d(tag, message)
            }
        }
    }

    fun info(
        tag: String,
        message: String
    ) {
        if (BuildConfig.DEBUG) {
            safeLog {
                Log.i(tag, message)
            }
        }
    }

    fun warning(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        if (BuildConfig.DEBUG) {
            safeLog {
                if (throwable == null) {
                    Log.w(tag, message)
                } else {
                    Log.w(tag, message, throwable)
                }
            }
        }
    }

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        if (BuildConfig.DEBUG) {
            safeLog {
                if (throwable == null) {
                    Log.e(tag, message)
                } else {
                    Log.e(tag, message, throwable)
                }
            }
        }
    }

    private fun safeLog(operation: () -> Unit) {
        try {
            operation()
        } catch (exception: RuntimeException) {
            return
        }
    }
}