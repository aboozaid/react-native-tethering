package com.reactnativetethering.wifi

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.reactnativetethering.wifi.WifiTethering

class WifiModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val wifiTethering: WifiTethering
    get() = WifiTethering(reactApplicationContext)

  @ReactMethod
  fun isWifiEnabled(promise: Promise) {
    wifiTethering.isEnabled(promise)
  }

  @ReactMethod
  fun setWifiEnabled(state:Boolean, autoScan: Boolean, promise: Promise) {
    wifiTethering.setEnabled(state, autoScan, promise)
  }

  @ReactMethod
  fun connectToLocalNetwork(ssid: String, password: String?, isHidden: Boolean, promise: Promise) {
    wifiTethering.connectToLocalNetwork(ssid, password, isHidden, promise)
  }

  @ReactMethod
  fun connectToNetwork(ssid: String, password: String?, isHidden: Boolean, timeout: Int, promise: Promise) {
    wifiTethering.connectToNetwork(ssid, password, isHidden, timeout, promise)
  }

  @ReactMethod
  fun saveNetworkInDevice(ssid: String, password: String?, isHidden: Boolean, promise: Promise) {
    wifiTethering.saveNetworkInDevice(ssid, password, isHidden, promise)
  }
  @ReactMethod
  fun disconnectFromLocalNetwork(promise: Promise) {
    wifiTethering.disconnectFromLocalNetwork(promise)
  }

  @ReactMethod
  fun disconnectFromNetwork(promise: Promise) {
    wifiTethering.disconnectFromNetwork(promise)
  }

  @ReactMethod
  fun getWifiNetworks(rescan: Boolean, promise: Promise) {
    wifiTethering.getWifiNetworks(rescan, promise)
  }

  @ReactMethod
  fun getMaxNumberOfNetworkSuggestions(promise: Promise) {
    wifiTethering.getMaxNumberOfNetworkSuggestions(promise)
  }

  @ReactMethod
  fun isDeviceAlreadyConnected(promise: Promise) {
    wifiTethering.isDeviceAlreadyConnected(promise)
  }

  @ReactMethod
  fun getDeviceIP(promise: Promise) {
    wifiTethering.getDeviceIP(promise)
  }

  @ReactMethod
  fun openWifiSettings(asDialog: Boolean) {
    wifiTethering.openWifiSettings(asDialog)
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "WifiTethering"
  }
}
