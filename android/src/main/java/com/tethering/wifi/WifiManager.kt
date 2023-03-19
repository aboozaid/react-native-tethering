package com.tethering.wifi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
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
import android.os.Looper
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
import com.tethering.receivers.WifiScanReceiver
import com.tethering.receivers.WifiStateReceiver
import com.tethering.utils.AddNetworkCanceledException
import com.tethering.utils.AddNetworkException
import com.tethering.utils.CodedException
import com.tethering.utils.CustomPromise
import com.tethering.utils.NetworkNotFoundException
import com.tethering.utils.NetworkSavedException
import com.tethering.utils.NetworkScanTimeoutException
import com.tethering.utils.NoPermissionException
import com.tethering.utils.UnsupportedApiException
import com.tethering.utils.WifiConnectionFailedException
import com.tethering.utils.WifiConnectionTimeoutException
import com.tethering.utils.WifiEnabledFailedException
import com.tethering.utils.WifiScanFailedException

@SuppressLint("NewApi")
class WifiManager(private val context: ReactApplicationContext) {

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

  init {
    context.addActivityEventListener(activityEventListener)
  }
  fun isEnabled(promise: Promise) {
    promise.resolve(wifiManager.isWifiEnabled)
  }

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

  fun connectToLocalNetwork(networkSSID: String, networkPass: String?, isHidden: Boolean, promise: Promise) {
    if (isAndroidTenOrLater()) {

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

      try {
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

      try {
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

  fun saveNetworkInDevice(networkSSID: String, networkPass: String?, isHidden: Boolean, promise: Promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

  fun disconnectFromLocalNetwork(promise: Promise) {
    if (isAndroidTenOrLater()) {
      disconnectFromLocalNetwork()
      promise.resolve(null)
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }

  fun disconnectFromNetwork(promise: Promise) {
    if (isAndroidTenOrLater()) {
      wifiManager.removeNetworkSuggestions(listOf(suggestedNetwork))
      promise.resolve(null)
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }
  /*private fun formatWithBackslashes(value: String): String {
    return buildString {
      append('"')
      append(value)
      append('"')
    }
  }

  @SuppressLint("MissingPermission")
  private fun getMaxPriority(): Int {
    val configurations = wifiManager.configuredNetworks
    var pri = 0
    configurations.forEach { config ->
      if (config.priority > pri) {
        pri = config.priority
      }
    }
    return pri
  }

  @SuppressLint("MissingPermission")
  private fun shiftPriorityAndSave(): Int {
    val configurations = wifiManager.configuredNetworks
    sortByPriority(configurations)
    val size = configurations.size

    configurations.forEachIndexed { index, config ->
      config.priority = index
      wifiManager.updateNetwork(config)
    }

    return size
  }*/
  /*private fun sortByPriority(configurations: List<WifiConfiguration>) {
    configurations.sortedWith(compareByDescending<WifiConfiguration> { it.priority })
  }*/
  private fun isAndroidTenOrLater(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
  }

  private fun isAlreadyConnected() = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnected ?: false

  private fun isAlreadyConnected(ssid: String) = isAlreadyConnected() && wifiManager.connectionInfo.ssid.replace("\"", "") == ssid
  @RequiresApi(Build.VERSION_CODES.M)
  private fun disconnectFromLocalNetwork() {
    connectivityManager.bindProcessToNetwork(null)
    connectivityManager.unregisterNetworkCallback(connectivityNetworkListener)
  }

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

  /*@SuppressLint("MissingPermission")
  private fun getWifiConfiguration(networkId: Int) = wifiManager.configuredNetworks.find { it.networkId == networkId }*/
  companion object {
    const val TAG = "RNWifiManager"
  }
}
