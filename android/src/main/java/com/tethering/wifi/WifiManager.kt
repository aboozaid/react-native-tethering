package com.tethering.wifi

import android.net.wifi.WifiManager
import android.os.Build
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.tethering.utils.CodedException
import com.tethering.utils.CustomPromise
import com.tethering.utils.NoPermissionException

class WifiManager(private val context: ReactApplicationContext) {

  private val wifiManager: WifiManager by lazy {
    context.applicationContext.getSystemService(ReactApplicationContext.WIFI_SERVICE) as WifiManager
  }

  fun isEnabled(promise: Promise) {
    promise.resolve(wifiManager.isWifiEnabled)
  }

  fun setEnabled(state: Boolean, promise: Promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

    } else {
      kotlin.runCatching {
        wifiManager.setWifiEnabled(state)
      }.onSuccess { state ->
        if (state) {
          context.registerReceiver(WifiStateReceiver())
        }
        promise.resolve(state)
      }.onFailure {
        when(it) {
          is SecurityException -> CustomPromise(promise).reject(NoPermissionException(it))
          else -> CustomPromise(promise).reject(CodedException(it))
        }
      }
    }
  }
}
