package com.codedev.videoapplication.ui.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "ConnectionFlow"

class ConnectionFlow(context: Context, val scope: CoroutineScope) {

    private val _connectionFlow = MutableSharedFlow<Boolean>(0)
    val connectionFlow = _connectionFlow.asSharedFlow()

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    init {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun createNetworkCallback(): ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "onAvailable: ${network}")
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                val isInternet = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
                Log.d(TAG, "onAvailable: ${network} ${isInternet}")
                if (isInternet == true) {
                    validNetworks.add(network)
                }
                checkValidNetwork()
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "onLost: $network")
                validNetworks.remove(network)
                checkValidNetwork()
            }
        }

    private fun checkValidNetwork() {
        scope.launch {
            _connectionFlow.emit(validNetworks.size > 0)
        }
    }

}

