package com.thanh0x.coursedeals.core.ui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

open class NetworkUtil(val context: Context? = null) {
    open fun isInternetAvailable(): Boolean {
        val appContext = context ?: return false
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val capabilities =
            connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        return isInternetCapabilityAvailable(capabilities)
    }

    private fun isInternetCapabilityAvailable(capabilities: NetworkCapabilities?): Boolean {
        if (capabilities == null) return false

        val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        return hasCellular || hasWifi || hasEthernet
    }
}
