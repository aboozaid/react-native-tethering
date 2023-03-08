package com.tethering.wifi

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.tethering.receivers.WifiStateReceiver
import com.tethering.utils.CodedException
import com.tethering.utils.CustomPromise
import com.tethering.utils.NoPermissionException
import com.tethering.utils.WifiEnabledFailedException

class WifiManager(private val context: ReactApplicationContext) {

  private val wifiManager: WifiManager by lazy {
    context.applicationContext.getSystemService(ReactApplicationContext.WIFI_SERVICE) as WifiManager
  }

  private lateinit var wifiStateReceiver: WifiStateReceiver

  fun isEnabled(promise: Promise) {
    promise.resolve(wifiManager.isWifiEnabled)
  }

  fun setEnabled(state: Boolean, promise: Promise) {
    wifiStateReceiver = WifiStateReceiver(object: WifiStateCallback {
      override fun onWifiEnabled() {
        context.unregisterReceiver(wifiStateReceiver)
        promise.resolve(null)
      }
    })

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val intent = Intent(Settings.Panel.ACTION_WIFI).also {
        it.flags = FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      context.registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
    } else {
      kotlin.runCatching {
        wifiManager.setWifiEnabled(state)
      }.onSuccess { state ->
        if (state) {
          context.registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        } else {
          CustomPromise(promise).reject(CodedException(WifiEnabledFailedException()))
        }
      }.onFailure {
        when(it) {
          is SecurityException -> CustomPromise(promise).reject(NoPermissionException(it))
          else -> CustomPromise(promise).reject(CodedException(it))
        }
      }
    }
  }
}
