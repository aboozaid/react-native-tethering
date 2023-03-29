package com.reactnativetethering.wifi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.MacAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.provider.Settings.ADD_WIFI_RESULT_ALREADY_EXISTS
import android.provider.Settings.ADD_WIFI_RESULT_SUCCESS
import android.provider.Settings.EXTRA_WIFI_NETWORK_RESULT_LIST
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativetethering.wifi.callbacks.WifiNetworkResultStateCallback
import com.reactnativetethering.wifi.callbacks.WifiNetworkStateCallback
import com.reactnativetethering.wifi.callbacks.WifiScanCallback
import com.reactnativetethering.wifi.callbacks.WifiStateCallback
import com.reactnativetethering.wifi.receivers.WifiScanReceiver
import com.reactnativetethering.wifi.receivers.WifiStateReceiver
import com.reactnativetethering.wifi.utils.AddNetworkCanceledException
import com.reactnativetethering.wifi.utils.AddNetworkException
import com.reactnativetethering.wifi.utils.CodedException
import com.reactnativetethering.wifi.utils.CustomPromise
import com.reactnativetethering.wifi.utils.NetworkNotFoundException
import com.reactnativetethering.wifi.utils.NetworkSavedException
import com.reactnativetethering.wifi.utils.NetworkScanTimeoutException
import com.reactnativetethering.wifi.utils.NoPermissionException
import com.reactnativetethering.wifi.utils.UnsupportedApiException
import com.reactnativetethering.wifi.utils.WifiConnectionTimeoutException
import com.reactnativetethering.wifi.utils.WifiEnabledFailedException
import com.reactnativetethering.wifi.utils.WifiScanFailedException
import java.net.Inet4Address
import java.net.NetworkInterface


class WifiTethering(private val context: ReactApplicationContext) {

  private val wifiManager: WifiManager by lazy {
    context.applicationContext.getSystemService(ReactApplicationContext.WIFI_SERVICE) as WifiManager
  }

  private val connectivityManager: ConnectivityManager by lazy {
    context.getSystemService(ReactApplicationContext.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  private var wifiNetworkStateCallback: WifiNetworkStateCallback? = null
  private var wifiNetworkResultStateCallback: WifiNetworkResultStateCallback? = null

  private var suggestedNetwork: WifiNetworkSuggestion? = null
  private var currentNetwork: String? = null
  private var isConnected: Boolean = false

  private val connectivityNetworkListener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    object: ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)

        connectivityManager.bindProcessToNetwork(network)
      }

      override fun onLost(network: Network) {
        super.onLost(network)

        Log.d(TAG, "onLost")

        // if device already connected notify the app that network disconnected
        if (isConnected) {
          context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("OnNetworkDisconnected", null)
          isConnected = false
        }
      }

      override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        if (networkCapabilities.transportInfo != null) {
          val info = networkCapabilities.transportInfo as WifiInfo
          if (info.ssid.replace("\"", "") == currentNetwork && wifiNetworkStateCallback != null) {
            wifiNetworkStateCallback?.onConnected()
            wifiNetworkStateCallback = null
            isConnected = true
          }
        }
      }

      override fun onUnavailable() {
        super.onUnavailable()

        // this called when user press cancel to connect or timeout with no found network so we need to handle each case alone
        // we can use scan result to find if the network we're trying to connect within scan list that mean user cancel to connect
        Log.d(TAG, "onUnavailable")
        if (wifiNetworkStateCallback != null) {
          val reason = findAndGetNetwork(currentNetwork!!)?.let {
            AddNetworkCanceledException()
          } ?: NetworkScanTimeoutException()

          wifiNetworkStateCallback?.onConnectionFailed(reason)
          // since connection failed we should stop listening
          stopNetworkListening()
        }

      }
    }
  } else {
    object: ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)

        connectivityManager.bindProcessToNetwork(network)
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        if (isAlreadyConnected(currentNetwork!!) && wifiNetworkStateCallback != null) {
          wifiNetworkStateCallback?.onConnected()
          wifiNetworkStateCallback = null
          isConnected = true
        }
      }

      override fun onLost(network: Network) {
        super.onLost(network)

        Log.d(TAG, "onLost")

        // if device already connected notify the app that network disconnected
        if (isConnected) {
          context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("OnNetworkDisconnected", null)
          isConnected = false
        }
      }


      override fun onUnavailable() {
        super.onUnavailable()

        // this called when user press cancel to connect or timeout with no found network so we need to handle each case alone
        // we can use scan result to find if the network we're trying to connect within scan list that mean user cancel to connect
        Log.d(TAG, "onUnavailable")
        if (wifiNetworkStateCallback != null) {
          val reason = findAndGetNetwork(currentNetwork!!)?.let {
            AddNetworkCanceledException()
          } ?: NetworkScanTimeoutException()

          wifiNetworkStateCallback?.onConnectionFailed(reason)
          // since connection failed we should stop listening
          stopNetworkListening()
        }

        /*context
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("OnNetworkNotFound", null)*/
      }
    }
  }

  private lateinit var wifiStateReceiver: WifiStateReceiver
  private lateinit var wifiScanReceiver: WifiScanReceiver

  private val activityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity?,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
    ) {
      if(resultCode == RESULT_OK) {
        // user agreed to save configurations: still need to check individual results
        if (data != null && data.hasExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)) {
          for (code in data.getIntegerArrayListExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)!!) {
            if (code == ADD_WIFI_RESULT_ALREADY_EXISTS) {
              wifiNetworkResultStateCallback?.onAlreadyExists()
            }

            if (code != ADD_WIFI_RESULT_SUCCESS) {
              wifiNetworkResultStateCallback?.onError(AddNetworkException())
            }
          }
        }
      } else {
        // User refused to save configurations
        wifiNetworkResultStateCallback?.onError(AddNetworkCanceledException())
      }
    }
  }

  /*init {
    context.addActivityEventListener(activityEventListener)
  }*/

  fun isEnabled(promise: Promise) {
    promise.resolve(wifiManager.isWifiEnabled)
  }

  // Manifest.permission.CHANGE_WIFI_STATE
  fun setEnabled(state: Boolean, autoScan: Boolean, promise: Promise) {
    wifiStateReceiver = WifiStateReceiver(object: WifiStateCallback {
      override fun onWifiEnabled() {
        context.unregisterReceiver(wifiStateReceiver)
        if (autoScan) {
          wifiScanReceiver = WifiScanReceiver(object: WifiScanCallback {
            @SuppressLint("MissingPermission")
            override fun onScanResultsReady() {
              context.unregisterReceiver(wifiScanReceiver)
              context
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("OnWifiScanResults", mapScanResultsToJS(wifiManager.scanResults))

              promise.resolve(null)
            }
          })

          if (wifiManager.startScan()) {
            context.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
          } else {
            promise.resolve(null)
          }
        } else {
          promise.resolve(null)
        }
      }
    })

    if (isAndroidTenOrLater()) {
      openWifiSettings(true)
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

  fun connectToLocalNetwork(networkSSID: String, networkPass: String?, isHidden: Boolean, promise: Promise) {
    if (isAndroidTenOrLater()) {

      try {
        val networkToConnectTo = findAndGetNetwork(networkSSID)
          ?: return CustomPromise(promise).reject(NetworkNotFoundException())

        wifiNetworkStateCallback = object: WifiNetworkStateCallback {
          override fun onConnected() {
            promise.resolve(null)
          }

          override fun onConnectionFailed(reason: CodedException) {
            CustomPromise(promise).reject(reason)
          }
        }

        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
          .setSsid(networkToConnectTo.SSID)
          .setBssid(MacAddress.fromString(networkToConnectTo.BSSID))
          .setIsHiddenSsid(isHidden)
          .apply {
            networkPass?.let {
              setWpa2Passphrase(networkPass)
            }
          }.build()

        val networkRequest = NetworkRequest.Builder()
          .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
          .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
          .setNetworkSpecifier(wifiNetworkSpecifier)
          .build()

        currentNetwork = networkSSID

        connectivityManager.requestNetwork(networkRequest, connectivityNetworkListener)
      } catch (e: SecurityException) {
        CustomPromise(promise).reject(NoPermissionException(e))
        e.printStackTrace()
      } catch (e: Exception) {
        CustomPromise(promise).reject(CodedException(e))
        e.printStackTrace()
      }
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }

  fun connectToNetwork(networkSSID: String, networkPass: String?, isHidden: Boolean, timeout: Int, promise: Promise) {
    if (isAndroidTenOrLater()) {
      try {
        val networkToConnectTo = findAndGetNetwork(networkSSID)
          ?: return CustomPromise(promise).reject(NetworkNotFoundException())

        var wifiNetworkSuggestion = WifiNetworkSuggestion.Builder()
          .setSsid(networkToConnectTo.SSID)
          .setBssid(MacAddress.fromString(networkToConnectTo.BSSID))
          /*.setIsAppInteractionRequired(true)*/
          .setIsHiddenSsid(isHidden)
          .setPriority(Int.MAX_VALUE)
          .apply {
            networkPass?.let {
              setWpa2Passphrase(networkPass)
            }
          }.build()

        val suggestions = listOf(wifiNetworkSuggestion)

        wifiNetworkStateCallback = object : WifiNetworkStateCallback {
          override fun onConnected() {
            suggestedNetwork = wifiNetworkSuggestion
            promise.resolve(null)
            /*stopNetworkListening()*/
          }

          override fun onConnectionFailed(reason: CodedException) {
            CustomPromise(promise).reject(reason)
          }
        }

        // always re-suggest the network
        val removeSuggestionsState = wifiManager.removeNetworkSuggestions(suggestions)

        if (removeSuggestionsState != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS &&
          removeSuggestionsState != WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_REMOVE_INVALID) {
          val status = SuggestionStatus.fromCode(removeSuggestionsState)
          throw CodedException(status.name, status.reason, null)
        }

        var addSuggestionState = wifiManager.addNetworkSuggestions(suggestions)

        if (addSuggestionState != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
          val status = SuggestionStatus.fromCode(addSuggestionState)
          throw CodedException(status.name, status.reason, null)
        }

        forceWifiConnect(networkToConnectTo, isHidden, networkPass, timeout.toLong()) {
          if (wifiNetworkStateCallback != null) {
            wifiNetworkStateCallback?.onConnectionFailed(WifiConnectionTimeoutException())
            stopNetworkListening()
          }
        }

        currentNetwork = networkSSID

        startNetworkListening()
      }
      catch(err: Exception) {
        when(err) {
          is SecurityException -> CustomPromise(promise).reject(NoPermissionException(err))
          is CodedException -> CustomPromise(promise).reject(err)
          else -> CustomPromise(promise).reject(CodedException(err))
        }
      }
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }

  fun saveNetworkInDevice(networkSSID: String, networkPass: String?, isHidden: Boolean, promise: Promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      try {
        val networkToConnectTo = findAndGetNetwork(networkSSID)
          ?: return CustomPromise(promise).reject(NetworkNotFoundException())

        val wifiNetworkSuggestion = WifiNetworkSuggestion.Builder()
          .setSsid(networkToConnectTo.SSID)
          .setBssid(MacAddress.fromString(networkToConnectTo.BSSID))
          .setIsHiddenSsid(isHidden)
          .setPriority(Int.MAX_VALUE)
          .apply {
            networkPass?.let {
              setWpa2Passphrase(networkPass)
            }
          }.build()

        wifiNetworkStateCallback = object : WifiNetworkStateCallback {
          override fun onConnected() {
            promise.resolve(null)
            context.removeActivityEventListener(activityEventListener)
            suggestedNetwork = wifiNetworkSuggestion
            /*stopNetworkListening()*/
          }

          override fun onConnectionFailed(reason: CodedException) {
            CustomPromise(promise).reject(reason)
            context.removeActivityEventListener(activityEventListener)
          }
        }

        wifiNetworkResultStateCallback = object : WifiNetworkResultStateCallback {
          override fun onAlreadyExists() {
            CustomPromise(promise).reject(NetworkSavedException())
            clear()
          }

          override fun onError(error: CodedException) {
            CustomPromise(promise).reject(error)
            clear()
          }

          private fun clear() {
            stopNetworkListening()
            context.removeActivityEventListener(activityEventListener)
            wifiNetworkResultStateCallback = null
          }
        }

        val suggestions = ArrayList(listOf(wifiNetworkSuggestion))
        val bundle = Bundle().also {
          it.putParcelableArrayList(Settings.EXTRA_WIFI_NETWORK_LIST, ArrayList(suggestions))
        }
        val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).also {
          it.putExtras(bundle)
        }

        currentNetwork = networkSSID

        context.addActivityEventListener(activityEventListener)

        context.currentActivity?.startActivityForResult(intent, 0)
        startNetworkListening()
      } catch (err: Exception) {
        CustomPromise(promise).reject(CodedException(err))
      }
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }

  fun getWifiNetworks(rescan: Boolean, promise: Promise) {
    try {
      if (rescan) {
        wifiScanReceiver = WifiScanReceiver(object: WifiScanCallback {
          override fun onScanResultsReady() {
            context.unregisterReceiver(wifiScanReceiver)
            val networks = mapScanResultsToJS(wifiManager.scanResults)
            promise.resolve(networks)
          }
        })

        if (!wifiManager.startScan()) {
          return CustomPromise(promise).reject(WifiScanFailedException())
        }
        context.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
      } else {
        val networks = mapScanResultsToJS(wifiManager.scanResults)
        promise.resolve(networks)
      }
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e))
      e.printStackTrace()
    }

  }

  fun disconnectFromLocalNetwork(promise: Promise) {
    if (isAndroidTenOrLater()) {
      connectivityManager.bindProcessToNetwork(null)
      connectivityManager.unregisterNetworkCallback(connectivityNetworkListener)
      promise.resolve(null)
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }

  fun disconnectFromNetwork(promise: Promise) {
    if (isAndroidTenOrLater()) {
      if (suggestedNetwork != null) {
        wifiManager.removeNetworkSuggestions(listOf(suggestedNetwork))
      }
      promise.resolve(null)
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }

  fun getMaxNumberOfNetworkSuggestions(promise: Promise) = if (isAndroidTenOrLater()) {
    promise.resolve(wifiManager.maxNumberOfNetworkSuggestionsPerApp)
  } else {
    CustomPromise(promise).reject(UnsupportedApiException())
  }

  fun isDeviceAlreadyConnected(promise: Promise) = promise.resolve(isAlreadyConnected())

  fun getDeviceIP(promise: Promise) = promise.resolve(getIpAddress())

  fun openWifiSettings(asDialog: Boolean) {
    val intent = if (asDialog) Intent(Settings.Panel.ACTION_WIFI) else Intent(Settings.ACTION_WIFI_SETTINGS)
    intent.flags = FLAG_ACTIVITY_NEW_TASK

    context.startActivity(intent)
  }

  private fun startNetworkListening() {
    val networkRequest = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
      .build()

    connectivityManager.registerNetworkCallback(networkRequest, connectivityNetworkListener)
  }
  private fun stopNetworkListening() {
    connectivityManager.unregisterNetworkCallback(connectivityNetworkListener)
    if (wifiNetworkStateCallback != null) {
      wifiNetworkStateCallback = null
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun forceWifiConnect(networkToConnectTo: ScanResult, isHidden: Boolean, networkPass: String?, timeout: Long, onTimeout: () -> Unit) {
    val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
      .setSsid(networkToConnectTo.SSID)
      .setBssid(MacAddress.fromString(networkToConnectTo.BSSID))
      .setIsHiddenSsid(isHidden)
      .apply {
        networkPass?.let {
          setWpa2Passphrase(networkPass)
        }
      }.build()

    val networkRequest = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
      .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
      .setNetworkSpecifier(wifiNetworkSpecifier)
      .build()

    connectivityManager.requestNetwork(networkRequest, object: ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)

        /*connectivityManager.bindProcessToNetwork(null)*/
        connectivityManager.unregisterNetworkCallback(this)

        Handler(context.mainLooper).postDelayed({
          onTimeout.invoke()
        }, timeout)
      }
    })
  }

  private fun mapScanResultsToJS(scanResults: List<ScanResult>): WritableArray {
    val wifiArray = WritableNativeArray()

    for (result in scanResults) {
      val wifiObject = WritableNativeMap()
      wifiObject.putString("ssid", result.SSID)
      wifiObject.putString("bssid", result.BSSID)
      wifiObject.putString("capabilities", result.capabilities)
      wifiObject.putInt("frequency", result.frequency)
      wifiObject.putInt("level", result.level)
      wifiObject.putDouble("timestamp", result.timestamp.toDouble())
      wifiArray.pushMap(wifiObject)
    }

    return wifiArray
  }

  @SuppressLint("MissingPermission")
  private fun findAndGetNetwork(ssid: String) = wifiManager.scanResults.find { it.SSID == ssid }

  private fun isAndroidTenOrLater(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
  }

  private fun getIpAddress(): String? {
    val interfaces = NetworkInterface.getNetworkInterfaces()
    while (interfaces.hasMoreElements()) {
      val ntw = interfaces.nextElement()
      if (listOf("wlan0", "eth0").any { it == ntw.displayName }) {
        val addresses = ntw.inetAddresses
        while (addresses.hasMoreElements()) {
          val address = addresses.nextElement()
          if (!address.isLinkLocalAddress && address is Inet4Address) {
            return address.hostAddress
          }
        }
      }
    }
    return null
  }

  private fun isAlreadyConnected() = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnected ?: false

  private fun isAlreadyConnected(ssid: String) = isAlreadyConnected() && wifiManager.connectionInfo.ssid.replace("\"", "") == ssid

  enum class SuggestionStatus(val code: Int, val reason: String) {
    INTERNAL_ERROR(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL, "An internal error occurred while processing the suggestion"),
    APP_DISALLOWED(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED, "The app is not allowed to suggest networks"),
    ADD_DUPLICATE(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE, "The suggestion is already in the suggestion list"),
    ADD_EXCEEDS_MAX_PER_APP(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP, "The app has exceeded the maximum number of suggestions allowed"),
    ADD_NOT_ALLOWED(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_NOT_ALLOWED, "The suggestion is not allowed on this device"),
    ADD_INVALID(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_INVALID, "The suggestion is invalid or incomplete"),
    REMOVE_INVALID(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_REMOVE_INVALID, "Unable to remove suggested networks for this app"),
    RESTRICTED_BY_ADMIN(WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_RESTRICTED_BY_ADMIN, "The suggestion is restricted by the device admin"),
    UNKNOWN_ERROR(-1, "Unknown error");
    companion object {
      fun fromCode(code: Int): SuggestionStatus = values().find { it.code == code } ?: UNKNOWN_ERROR
    }
  }

  companion object {
    const val TAG = "RNWifiTethering"
  }
}
