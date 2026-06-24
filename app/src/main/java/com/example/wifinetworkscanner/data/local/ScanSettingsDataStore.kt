package com.example.wifinetworkscanner.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.wifinetworkscanner.di.IoDispatcher
import com.example.wifinetworkscanner.domain.model.ScanSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class ScanSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(SupervisorJob() + ioDispatcher),
        produceFile = {
            context.preferencesDataStoreFile(SCAN_SETTINGS_FILE_NAME)
        }
    )

    fun observeScanSettings(): Flow<ScanSettings> {
        return dataStore.data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                ScanSettings(
                    maxHosts = preferences[MAX_HOSTS_KEY] ?: ScanSettings.DEFAULT_MAX_HOSTS,
                    timeoutMillis = preferences[TIMEOUT_MILLIS_KEY] ?: ScanSettings.DEFAULT_TIMEOUT_MILLIS,
                    parallelism = preferences[PARALLELISM_KEY] ?: ScanSettings.DEFAULT_PARALLELISM
                )
            }
    }

    suspend fun updateScanSettings(scanSettings: ScanSettings) {
        dataStore.edit { preferences ->
            preferences[MAX_HOSTS_KEY] = scanSettings.maxHosts
            preferences[TIMEOUT_MILLIS_KEY] = scanSettings.timeoutMillis
            preferences[PARALLELISM_KEY] = scanSettings.parallelism
        }
    }

    private companion object {
        const val SCAN_SETTINGS_FILE_NAME = "scan_settings.preferences_pb"

        val MAX_HOSTS_KEY = intPreferencesKey("max_hosts")
        val TIMEOUT_MILLIS_KEY = intPreferencesKey("timeout_millis")
        val PARALLELISM_KEY = intPreferencesKey("parallelism")
    }
}