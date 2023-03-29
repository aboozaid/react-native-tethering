package com.reactnativetethering.wifi.callbacks

import com.reactnativetethering.wifi.utils.CodedException

interface WifiNetworkStateCallback {
  fun onConnected() {}
  fun onConnectionFailed(reason: CodedException) {}
}
