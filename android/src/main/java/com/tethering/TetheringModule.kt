package com.tethering

import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.tethering.hotspot.HotspotManager
import com.tethering.wifi.WifiManager

class TetheringModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var hotspotManager: HotspotManager
  private var wifiManager: WifiManager

  init {
    hotspotManager = HotspotManager(reactContext)
    wifiManager = WifiManager(reactContext)
  }

  @ReactMethod
  fun isHotspotEnabled(promise: Promise) {
    hotspotManager.isEnabled(promise)
  }

  @ReactMethod
  fun setHotspotEnabled(state: Boolean, promise: Promise) {
    hotspotManager.setEnabled(state, promise)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun setLocalHotspotEnabled(promise: Promise) {
    hotspotManager.setLocalHotspotEnabled(true, promise)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun setLocalHotspotDisabled(promise: Promise) {
    hotspotManager.setLocalHotspotEnabled(false, promise)
  }

  @ReactMethod
  fun getHotspotDeviceIp(promise: Promise) {
    hotspotManager.getMyCurrentAddress(promise)
  }

  @ReactMethod
  fun getHotspotPeersAddresses(promise: Promise) {
    hotspotManager.getPeersAddresses(promise)
  }

  @ReactMethod
  fun isWifiEnabled(promise: Promise) {
    wifiManager.isEnabled(promise)
  }

  @ReactMethod
  fun setWifiEnabled(state:Boolean, promise: Promise) {
    wifiManager.setEnabled(state, promise)
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Tethering"
  }
}
