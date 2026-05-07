package com.mm.astraisandroid.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observador del estado de conectividad de red mediante Flow reactivo.
 *
 * Emite `true` cuando hay conexión disponible y `false` cuando se pierde.
 * Usa `ConnectivityManager.NetworkCallback` para detectar cambios en tiempo real.
 *
 * @param context Contexto de la aplicación para acceder al ConnectivityManager.
 */
class ConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Flow que emite el estado de conectividad (`true` = conectado, `false` = desconectado).
     * Usa `distinctUntilChanged` para evitar emisiones duplicadas.
     */
    val status: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}