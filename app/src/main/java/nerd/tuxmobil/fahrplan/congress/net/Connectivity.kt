@file:JvmName("Connectivity")

package nerd.tuxmobil.fahrplan.congress.net

import android.content.Context
import android.net.NetworkInfo
import nerd.tuxmobil.fahrplan.congress.extensions.getConnectivityManager

fun Context.networkIsAvailable(): Boolean {
    val manager = getConnectivityManager()
    val networkInfo: NetworkInfo? = manager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}
