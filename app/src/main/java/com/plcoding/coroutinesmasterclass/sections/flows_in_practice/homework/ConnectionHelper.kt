package com.plcoding.coroutinesmasterclass.sections.flows_in_practice.homework

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.text.style.LocaleSpan
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ConnectionStatus(
    val connectionDate: String,
    val wasConnected: Boolean
)

class ConnectionHelper(
    private val ctx: Context
) {
    private val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    fun userHasConnection(): Flow<ConnectionStatus> {
        return callbackFlow {
            val networkCallback = getNetworkCallback(this)
            val manager = getSystemService(
                ctx,
                ConnectivityManager::class.java
            ) as ConnectivityManager
            manager.requestNetwork(request, networkCallback)

            awaitClose {
                manager.unregisterNetworkCallback(networkCallback)
            }
        }
    }

    private fun getCurrentDate(): String {
        val zoneId = ZoneId.of("America/Argentina/Buenos_Aires")
        val dateNow = LocalDateTime
            .now()
            .atZone(zoneId)
        val formatter = DateTimeFormatter.ofPattern(
            "dd/MM/yyyy",
            Locale("es", "AR")
        )
        return dateNow.format(formatter)
    }

    private fun getNetworkCallback(
        flowScope: ProducerScope<ConnectionStatus>
    ): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                flowScope.trySend(
                    ConnectionStatus(
                        connectionDate = getCurrentDate(),
                        wasConnected = true
                    )
                )
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                flowScope.trySend(
                    ConnectionStatus(
                        connectionDate = getCurrentDate(),
                        wasConnected = false
                    )
                )
            }
        }
    }
}