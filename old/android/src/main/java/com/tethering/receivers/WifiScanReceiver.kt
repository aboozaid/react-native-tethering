package com.tethering.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tethering.wifi.WifiScanCallback

class WifiScanReceiver(private val wifiScanCallback: WifiScanCallback): BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    wifiScanCallback.onScanResultsReady()
  }
}
