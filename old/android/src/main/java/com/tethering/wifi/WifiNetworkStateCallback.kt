package com.tethering.wifi

import android.net.Network
import com.tethering.utils.CodedException

interface WifiNetworkStateCallback {
  fun onConnected() {}
  fun onConnectionFailed(reason: CodedException) {}
}
