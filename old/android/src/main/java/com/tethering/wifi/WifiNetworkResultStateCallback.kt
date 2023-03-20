package com.tethering.wifi

import com.tethering.utils.CodedException

interface WifiNetworkResultStateCallback {
  fun onAlreadyExists()
  fun onError(error: CodedException)
}
