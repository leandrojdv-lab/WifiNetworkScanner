package com.example.wifinetworkscanner.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.wifinetworkscanner.domain.model.WifiNetworkInfo
import com.example.wifinetworkscanner.utils.logger.AppLogger
import com.example.wifinetworkscanner.utils.network.Ipv4Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Inet4Address
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class WifiNetworkInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getActiveWifiNetworkInfo(maxHosts: Int): Result<WifiNetworkInfo> {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
            ?: return Result.failure(IllegalStateException("Serviço de conectividade indisponível."))

        val activeNetwork = connectivityManager.activeNetwork
            ?: return Result.failure(IllegalStateException("Nenhuma rede ativa encontrada."))

        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return Result.failure(IllegalStateException("Não foi possível ler as capacidades da rede atual."))

        if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return Result.failure(IllegalStateException("Conecte o celular a uma rede Wi-Fi para iniciar a varredura."))
        }

        val linkProperties = connectivityManager.getLinkProperties(activeNetwork)
            ?: return Result.failure(IllegalStateException("Não foi possível ler os dados da rede Wi-Fi."))

        val interfaceName = linkProperties.interfaceName
            ?: return Result.failure(IllegalStateException("Interface de rede Wi-Fi não identificada."))

        val ipv4LinkAddress = linkProperties.linkAddresses
            .firstOrNull { linkAddress ->
                linkAddress.address is Inet4Address && !linkAddress.address.isLoopbackAddress
            }
            ?: return Result.failure(IllegalStateException("IPv4 local não encontrado na rede Wi-Fi."))

        val localIpAddress = ipv4LinkAddress.address.hostAddress
            ?: return Result.failure(IllegalStateException("Endereço IPv4 inválido."))

        val gatewayIpAddress = linkProperties.routes
            .firstOrNull { routeInfo ->
                routeInfo.isDefaultRoute && routeInfo.gateway is Inet4Address
            }
            ?.gateway
            ?.hostAddress

        val prefixLength = ipv4LinkAddress.prefixLength

        val totalHostCount = Ipv4Utils.countUsableHosts(
            prefixLength = prefixLength,
            maxHosts = maxHosts
        )

        if (totalHostCount <= 0) {
            return Result.failure(IllegalStateException("Faixa IPv4 não suportada para varredura."))
        }

        val networkName = readCurrentWifiNetworkName(
            connectivityManager = connectivityManager,
            activeNetwork = activeNetwork
        ) ?: WifiNetworkInfo.UNKNOWN_NETWORK_NAME

        val networkIdentifier = buildNetworkIdentifier(
            networkName = networkName,
            gatewayIpAddress = gatewayIpAddress,
            localIpAddress = localIpAddress,
            prefixLength = prefixLength
        )

        return Result.success(
            WifiNetworkInfo(
                localIpAddress = localIpAddress,
                prefixLength = prefixLength,
                interfaceName = interfaceName,
                totalHostCount = totalHostCount,
                gatewayIpAddress = gatewayIpAddress,
                networkName = networkName,
                networkIdentifier = networkIdentifier
            )
        )
    }

    private suspend fun readCurrentWifiNetworkName(
        connectivityManager: ConnectivityManager,
        activeNetwork: Network
    ): String? {
        if (!hasWifiNamePermission()) {
            AppLogger.debug(TAG, "Nome da rede não lido: permissão de localização ausente.")
            return null
        }

        if (!isLocationEnabled()) {
            AppLogger.debug(TAG, "Nome da rede não lido: localização do sistema desativada.")
            return null
        }

        val networkCallbackName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            readWifiNameFromNetworkCallback(
                connectivityManager = connectivityManager,
                activeNetwork = activeNetwork
            )
        } else {
            null
        }

        if (!networkCallbackName.isNullOrBlank()) {
            return networkCallbackName
        }

        return readWifiNameFromWifiManager()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun readWifiNameFromNetworkCallback(
        connectivityManager: ConnectivityManager,
        activeNetwork: Network
    ): String? {
        return withTimeoutOrNull(WIFI_INFO_CALLBACK_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val isRegistered = AtomicBoolean(false)

                lateinit var callback: ConnectivityManager.NetworkCallback

                fun unregisterCallbackSafely() {
                    if (isRegistered.compareAndSet(true, false)) {
                        try {
                            connectivityManager.unregisterNetworkCallback(callback)
                        } catch (exception: RuntimeException) {
                            AppLogger.error(TAG, "Falha ao remover callback de rede.", exception)
                        }
                    }
                }

                fun resumeSafely(networkName: String?) {
                    if (continuation.isActive) {
                        continuation.resume(networkName)
                    }

                    unregisterCallbackSafely()
                }

                callback = object : ConnectivityManager.NetworkCallback(
                    FLAG_INCLUDE_LOCATION_INFO
                ) {

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        if (network != activeNetwork) {
                            return
                        }

                        val networkName = networkCapabilities.toWifiNetworkName()

                        if (!networkName.isNullOrBlank()) {
                            resumeSafely(networkName)
                        }
                    }

                    override fun onLost(network: Network) {
                        if (network == activeNetwork) {
                            resumeSafely(null)
                        }
                    }
                }

                try {
                    val networkRequest = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build()

                    connectivityManager.registerNetworkCallback(
                        networkRequest,
                        callback
                    )
                    isRegistered.set(true)
                } catch (exception: SecurityException) {
                    AppLogger.error(TAG, "Permissão insuficiente ao registrar callback com dados de Wi-Fi.", exception)
                    resumeSafely(null)
                } catch (exception: RuntimeException) {
                    AppLogger.error(TAG, "Falha ao registrar callback com dados de Wi-Fi.", exception)
                    resumeSafely(null)
                }

                continuation.invokeOnCancellation {
                    unregisterCallbackSafely()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun readWifiNameFromWifiManager(): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
                ?: return null

            /*
             * Fallback legado intencional.
             *
             * WifiManager.connectionInfo foi depreciado no Android 12/API 31.
             * O caminho preferencial em versões novas é NetworkCapabilities.transportInfo,
             * usado acima via NetworkCallback com FLAG_INCLUDE_LOCATION_INFO.
             *
             * Mantemos este fallback para aparelhos/ROMs em que o callback moderno não
             * retorna o SSID mesmo com permissão e localização habilitadas.
             */
            wifiManager.connectionInfo
                ?.ssid
                ?.toNormalizedSsid()
        } catch (exception: SecurityException) {
            AppLogger.error(TAG, "Permissão insuficiente ao ler nome da rede Wi-Fi.", exception)
            null
        } catch (exception: RuntimeException) {
            AppLogger.error(TAG, "Falha ao ler nome da rede Wi-Fi.", exception)
            null
        }
    }

    private fun NetworkCapabilities.toWifiNetworkName(): String? {
        return (transportInfo as? WifiInfo)
            ?.ssid
            ?.toNormalizedSsid()
    }

    private fun hasWifiNamePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(LocationManager::class.java)
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun String.toNormalizedSsid(): String? {
        val normalizedValue = trim()
            .removeSurrounding("\"")
            .trim()

        if (normalizedValue.isBlank()) {
            return null
        }

        if (normalizedValue.equals(UNKNOWN_SSID_VALUE, ignoreCase = true)) {
            return null
        }

        if (normalizedValue == HEX_EMPTY_SSID_VALUE) {
            return null
        }

        return normalizedValue
    }

    private fun buildNetworkIdentifier(
        networkName: String,
        gatewayIpAddress: String?,
        localIpAddress: String,
        prefixLength: Int
    ): String {
        if (networkName != WifiNetworkInfo.UNKNOWN_NETWORK_NAME) {
            return "ssid:${networkName.trim().lowercase()}"
        }

        val gatewayPart = gatewayIpAddress ?: localIpAddress

        return "network:$gatewayPart/$prefixLength"
    }

    private companion object {
        const val TAG = "WifiNetworkInfoProvider"
        const val UNKNOWN_SSID_VALUE = "<unknown ssid>"
        const val HEX_EMPTY_SSID_VALUE = "0x"
        const val WIFI_INFO_CALLBACK_TIMEOUT_MILLIS = 800L
    }
}