package com.reactnativetethering.wifi.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.reactnativetethering.wifi.callbacks.WifiStateCallback

class WifiStateReceiver(private val wifiStateCallback: WifiStateCallback): BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
      WifiManager.WIFI_STATE_ENABLED -> wifiStateCallback.onWifiEnabled()
      else -> Unit
    }
  }
}
