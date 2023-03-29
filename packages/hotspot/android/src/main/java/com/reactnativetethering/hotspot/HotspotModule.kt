package com.reactnativetethering.hotspot

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.reactnativetethering.hotspot.utils.CodedException
import com.reactnativetethering.hotspot.utils.CustomPromise

class HotspotModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val hotspotTethering: HotspotTethering
    get() = HotspotTethering(reactApplicationContext)

  @ReactMethod
  fun isHotspotEnabled(promise: Promise) {
    hotspotTethering.isEnabled(promise)
  }

  @ReactMethod
  fun setHotspotEnabled(state: Boolean, promise: Promise) {
    hotspotTethering.setEnabled(state, promise)
  }

  @ReactMethod
  fun setLocalHotspotEnabled(state: Boolean, promise: Promise) {
    hotspotTethering.setLocalHotspotEnabled(state, promise)
  }

  @ReactMethod
  fun isWriteSettingsGranted(promise: Promise) {
    promise.resolve(Settings.System.canWrite(reactApplicationContext))
  }

  @ReactMethod
  fun openWriteSettings(promise: Promise) {
    try {
      val reactContext = reactApplicationContext
      val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).also {
        it.data = Uri.parse("package:" + reactContext.packageName)
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      reactContext.startActivity(intent)
      promise.resolve(null)
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e))
    }
  }

  @ReactMethod
  fun navigateToTethering(promise: Promise) {
    try {
      val reactContext = reactApplicationContext
      val intent = Intent(Intent.ACTION_MAIN).also {
        it.addCategory(Intent.CATEGORY_LAUNCHER)
        it.component = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      reactContext.startActivity(intent)
      promise.resolve(null)
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e))
    }
  }

  @ReactMethod
  fun getMyDeviceIp(promise: Promise) {
    hotspotTethering.getMyIPAddress(promise)
  }

  @ReactMethod
  fun getConnectedDevices(promise: Promise) {
    hotspotTethering.getConnectedDevices(promise)
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Hotspot"
  }
}
