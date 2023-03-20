package com.tethering.wifi

import android.net.wifi.WifiConfiguration

enum class WifiNetworkStatus(val status: Int) {
  CONNECTED(WifiConfiguration.Status.CURRENT),
  AVAILABLE(WifiConfiguration.Status.ENABLED),
  UNAVAILABLE(WifiConfiguration.Status.DISABLED);

  companion object {
    fun getStatus(status: Int): WifiNetworkStatus = values().find { it.status == status }!!
  }
}
