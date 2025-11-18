package com.raytonc.queuecord.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitors WiFi connectivity status.
 *
 * Provides a Flow that emits true when WiFi is connected, false otherwise.
 */
class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isWifiConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    networks.add(network)
                    trySend(true)
                }
            }

            override fun onLost(network: Network) {
                networks.remove(network)
                val hasWifi = networks.any { net ->
                    connectivityManager.getNetworkCapabilities(net)
                        ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                }
                trySend(hasWifi)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                if (hasWifi) {
                    networks.add(network)
                } else {
                    networks.remove(network)
                }
                trySend(networks.isNotEmpty())
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Send initial state
        val initialState = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } ?: false
        trySend(initialState)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
