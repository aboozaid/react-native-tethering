package com.tethering

import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.tethering.hotspot.HotspotManager

class TetheringModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var hotspotManager: HotspotManager

  init {
    hotspotManager = HotspotManager(reactContext)
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

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Tethering"
  }
}
