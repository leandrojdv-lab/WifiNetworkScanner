package com.example.wifinetworkscanner.data.repository

import com.example.wifinetworkscanner.data.local.WifiNetworkInfoProvider
import com.example.wifinetworkscanner.di.IoDispatcher
import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.NetworkScanEvent
import com.example.wifinetworkscanner.domain.repository.NetworkScannerRepository
import com.example.wifinetworkscanner.utils.logger.AppLogger
import com.example.wifinetworkscanner.utils.network.Ipv4Utils
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Implementação local do scanner de rede.
 *
 * Atenção: InetAddress.isReachable() e Socket.connect() são chamadas bloqueantes da JVM.
 * Por isso esta classe deve receber, via @IoDispatcher, um dispatcher baseado em Dispatchers.IO.
 */
@Singleton
class NetworkScannerRepositoryImpl @Inject constructor(
    private val wifiNetworkInfoProvider: WifiNetworkInfoProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkScannerRepository {

    override fun scanConnectedDevices(
        maxHosts: Int,
        timeoutMillis: Int,
        parallelism: Int
    ): Flow<NetworkScanEvent> = channelFlow {
        val safeTimeoutMillis = timeoutMillis.coerceAtLeast(MIN_TIMEOUT_MILLIS)
        val safeParallelism = parallelism.coerceAtLeast(MIN_PARALLELISM)

        val networkInfoResult = wifiNetworkInfoProvider.getActiveWifiNetworkInfo(maxHosts)

        val networkInfo = networkInfoResult.getOrElse { throwable ->
            AppLogger.error(TAG, "Falha ao obter informações da rede.", throwable)

            send(
                NetworkScanEvent.Failed(
                    message = throwable.message ?: "Não foi possível ler a rede Wi-Fi atual."
                )
            )

            return@channelFlow
        }

        val hostAddresses = Ipv4Utils.calculateUsableHostAddresses(
            ipAddress = networkInfo.localIpAddress,
            prefixLength = networkInfo.prefixLength,
            maxHosts = maxHosts
        )

        if (hostAddresses.isEmpty()) {
            send(
                NetworkScanEvent.Failed(
                    message = "Nenhum endereço IPv4 válido encontrado para varredura."
                )
            )

            return@channelFlow
        }

        send(
            NetworkScanEvent.Started(
                networkInfo = networkInfo.copy(totalHostCount = hostAddresses.size)
            )
        )

        val scannedHostCount = AtomicInteger(0)
        val foundDevicesByIpAddress = ConcurrentHashMap<String, NetworkDevice>()
        val semaphore = Semaphore(permits = safeParallelism)

        val jobs = hostAddresses.map { ipAddress ->
            launch(ioDispatcher) {
                semaphore.withPermit {
                    val device = scanHost(
                        ipAddress = ipAddress,
                        interfaceName = networkInfo.interfaceName,
                        timeoutMillis = safeTimeoutMillis,
                        localIpAddress = networkInfo.localIpAddress,
                        gatewayIpAddress = networkInfo.gatewayIpAddress
                    )

                    if (device != null) {
                        foundDevicesByIpAddress[device.ipAddress] = device
                        send(NetworkScanEvent.DeviceFound(device = device))
                    }

                    val currentProgress = scannedHostCount.incrementAndGet()

                    send(
                        NetworkScanEvent.Progress(
                            scannedHostCount = currentProgress,
                            totalHostCount = hostAddresses.size
                        )
                    )
                }
            }
        }

        jobs.joinAll()

        val orderedDevices = foundDevicesByIpAddress.values.sortedBy { device ->
            Ipv4Utils.ipAddressSortValue(device.ipAddress)
        }

        send(NetworkScanEvent.Completed(devices = orderedDevices))
    }.flowOn(ioDispatcher)

    private suspend fun scanHost(
        ipAddress: String,
        interfaceName: String,
        timeoutMillis: Int,
        localIpAddress: String,
        gatewayIpAddress: String?
    ): NetworkDevice? = withContext(ioDispatcher) {
        if (ipAddress == localIpAddress) {
            return@withContext NetworkDevice(
                ipAddress = ipAddress,
                latencyMillis = 0L,
                scannedAtEpochMillis = System.currentTimeMillis(),
                detectionMethod = DeviceDetectionMethod.LOCAL_ADDRESS,
                openPorts = emptyList(),
                label = "Este celular"
            )
        }

        val startNanos = System.nanoTime()

        val reachableDeferred = async {
            checkReachable(
                ipAddress = ipAddress,
                interfaceName = interfaceName,
                timeoutMillis = timeoutMillis
            )
        }

        val openPortsDeferred = async {
            findOpenTcpPorts(
                ipAddress = ipAddress,
                timeoutMillis = timeoutMillis
            )
        }

        val isReachable = reachableDeferred.await()
        val openPorts = openPortsDeferred.await()

        val isGateway = ipAddress == gatewayIpAddress
        val latencyMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)

        if (!isReachable && openPorts.isEmpty()) {
            if (isGateway) {
                return@withContext NetworkDevice(
                    ipAddress = ipAddress,
                    latencyMillis = latencyMillis,
                    scannedAtEpochMillis = System.currentTimeMillis(),
                    detectionMethod = DeviceDetectionMethod.DEFAULT_GATEWAY,
                    openPorts = emptyList(),
                    label = "Gateway/Roteador provável"
                )
            }

            return@withContext null
        }

        val detectionMethod = when {
            isReachable && openPorts.isNotEmpty() -> DeviceDetectionMethod.REACHABLE_AND_TCP_PORT
            isReachable -> DeviceDetectionMethod.REACHABLE
            else -> DeviceDetectionMethod.TCP_PORT
        }

        NetworkDevice(
            ipAddress = ipAddress,
            latencyMillis = latencyMillis,
            scannedAtEpochMillis = System.currentTimeMillis(),
            detectionMethod = detectionMethod,
            openPorts = openPorts,
            label = if (isGateway) "Gateway/Roteador provável" else null
        )
    }

    private fun checkReachable(
        ipAddress: String,
        interfaceName: String,
        timeoutMillis: Int
    ): Boolean {
        return try {
            val address = InetAddress.getByName(ipAddress)
            val networkInterface = NetworkInterface.getByName(interfaceName)

            if (networkInterface != null) {
                address.isReachable(
                    networkInterface,
                    DEFAULT_TIME_TO_LIVE,
                    timeoutMillis
                )
            } else {
                address.isReachable(timeoutMillis)
            }
        } catch (exception: IOException) {
            AppLogger.debug(
                TAG,
                "Host não respondeu por reachable: $ipAddress. Motivo: ${exception.message}"
            )
            false
        } catch (exception: SecurityException) {
            AppLogger.error(TAG, "Permissão insuficiente ao testar reachable: $ipAddress.", exception)
            false
        } catch (exception: IllegalArgumentException) {
            AppLogger.error(TAG, "Parâmetro inválido ao testar reachable: $ipAddress.", exception)
            false
        }
    }

    private suspend fun findOpenTcpPorts(
        ipAddress: String,
        timeoutMillis: Int
    ): List<Int> = coroutineScope {
        COMMON_TCP_PORTS.map { port ->
            async(ioDispatcher) {
                port.takeIf {
                    isTcpPortOpen(
                        ipAddress = ipAddress,
                        port = port,
                        timeoutMillis = timeoutMillis
                    )
                }
            }
        }
            .awaitAll()
            .filterNotNull()
            .sorted()
    }

    private fun isTcpPortOpen(
        ipAddress: String,
        port: Int,
        timeoutMillis: Int
    ): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(
                    InetSocketAddress(ipAddress, port),
                    timeoutMillis
                )

                true
            }
        } catch (exception: IOException) {
            false
        } catch (exception: SecurityException) {
            AppLogger.error(TAG, "Permissão insuficiente ao testar porta TCP $port em $ipAddress.", exception)
            false
        } catch (exception: IllegalArgumentException) {
            AppLogger.error(TAG, "Parâmetro inválido ao testar porta TCP $port em $ipAddress.", exception)
            false
        }
    }

    private companion object {
        const val TAG = "NetworkScannerRepository"
        const val DEFAULT_TIME_TO_LIVE = 64
        const val MIN_TIMEOUT_MILLIS = 100
        const val MIN_PARALLELISM = 1

        val COMMON_TCP_PORTS = listOf(
            22,
            53,
            80,
            443,
            5000,
            5357,
            8000,
            8080,
            8443
        )
    }
}