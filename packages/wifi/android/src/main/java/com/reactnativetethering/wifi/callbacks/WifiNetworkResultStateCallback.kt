package com.reactnativetethering.wifi.callbacks

import com.reactnativetethering.wifi.utils.CodedException

interface WifiNetworkResultStateCallback {
  fun onAlreadyExists()
  fun onError(error: CodedException)
}
