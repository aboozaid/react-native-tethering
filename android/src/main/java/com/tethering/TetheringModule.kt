package com.tethering

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
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
  fun setWifiEnabled(state:Boolean, autoScan: Boolean, promise: Promise) {
    wifiManager.setEnabled(state, autoScan, promise)
  }

  /*@ReactMethod
  fun setWifiNetwork(ssid: String, password: String?, promise: Promise) {
    wifiManager.addNetwork(ssid, password, promise)
  }

  @ReactMethod
  fun getSavedNetworks(networkIds: ReadableArray?, promise: Promise) {
    wifiManager.getConfiguredNetworks(networkIds, promise)
  }*/

  @ReactMethod
  fun joinWifiLocalNetwork(ssid: String, password: String?, isHidden: Boolean, promise: Promise) {
    wifiManager.connectToLocalNetwork(ssid, password, isHidden, promise)
  }

  @RequiresApi(Build.VERSION_CODES.R)
  @ReactMethod
  fun joinWifiNetwork(ssid: String, password: String?, isHidden: Boolean, timeout: Int, promise: Promise) {
    wifiManager.connectToNetwork(ssid, password, isHidden, timeout, promise)
  }

  @ReactMethod
  fun saveNetworkInDevice(ssid: String, password: String?, isHidden: Boolean, promise: Promise) {
    wifiManager.saveNetworkInDevice(ssid, password, isHidden, promise)
  }
  @ReactMethod
  fun unjoinCurrentWifiLocalNetwork(promise: Promise) {
    wifiManager.disconnectFromLocalNetwork(promise)
  }

  @ReactMethod
  fun unjoinCurrentWifiNetwork(promise: Promise) {
    wifiManager.disconnectFromNetwork(promise)
  }

  @ReactMethod
  fun getWifiNetworks(rescan: Boolean, promise: Promise) {
    wifiManager.getWifiNetworks(rescan, promise)
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Tethering"
  }
}
