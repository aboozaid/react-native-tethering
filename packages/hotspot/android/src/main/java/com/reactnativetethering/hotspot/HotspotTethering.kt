package com.reactnativetethering.hotspot

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.android.dx.stock.ProxyBuilder
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.reactnativetethering.hotspot.utils.CodedException
import com.reactnativetethering.hotspot.utils.CustomPromise
import com.reactnativetethering.hotspot.utils.HotspotEnabledFailedException
import com.reactnativetethering.hotspot.utils.NoPermissionException
import com.reactnativetethering.hotspot.utils.UnsupportedApiException
import it.alessangiorgi.ipneigh30.ArpNDK
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Proxy
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.Executor

class HotspotTethering(private val context: ReactApplicationContext) {

  private val connectivityManager: ConnectivityManager by lazy {
    context.getSystemService(ReactApplicationContext.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  private val wifiManager: WifiManager by lazy {
    context.applicationContext.getSystemService(ReactApplicationContext.WIFI_SERVICE) as WifiManager
  }

  @get:SuppressLint("WrongConstant")
  private val tetheringManager: Any
    get() = context.getSystemService("tethering")

  private var localOnlyHotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null


  fun isEnabled(promise: Promise) {
    runCatching {
      isHotspotRunning()
    }.onSuccess {status ->
      promise.resolve(status)
    }.onFailure {
      when(it) {
        is SecurityException -> CustomPromise(promise).reject(CodedException(NoPermissionException(it)))
        else -> CustomPromise(promise).reject(CodedException(it.message, it))
      }
    }
  }

  fun setEnabled(state: Boolean, promise: Promise) {
    if (!state) {
      disableHotspot(promise)
    } else {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        enableHotspotInRAndAbove(promise)
      } else {
        enableHotspotBelowR(promise)
      }
    }
  }

  fun setLocalHotspotEnabled(state: Boolean, promise: Promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      when(state) {
        false -> disableLocalHotspot(promise)
        else -> enableLocalHotspot(promise)
      }
    } else {
      CustomPromise(promise).reject(UnsupportedApiException())
    }
  }

  fun getConnectedDevices(promise: Promise) {
    val arpTableString = ArpNDK.getARP().trimIndent()
    val pattern =
      """^(\S+)\s+dev\s+(swlan0|ap0|wlan1|wlan0)\s+lladdr\s+([0-9a-fA-F]{2}(?::[0-9a-fA-F]{2}){5})\s+(\S+)${'$'}""".toRegex(RegexOption.MULTILINE)
    val devices = WritableNativeArray()
    for (matchResult in pattern.findAll(arpTableString)) {
      // this peer uses wifi and we look only for hotspot peers
      if (matchResult.groupValues[2] == "wlan0" && !isWifiInterfaceUsed(matchResult.groupValues[2])) {
        continue
      }

      devices.pushMap(WritableNativeMap().apply {
        putString("ipAddress", matchResult.groupValues[1])
        putString("macAddress", matchResult.groupValues[3])
        putString("status", matchResult.groupValues[4])
      })
    }

    promise.resolve(devices)
  }

  fun getMyIPAddress(promise: Promise) = if (isHotspotRunning()) promise.resolve(null) else promise.resolve(getHotspotIpAddress())

  @RequiresApi(Build.VERSION_CODES.P)
  private fun enableHotspotInRAndAbove(promise: Promise) {
    try {
      val startTetheringInterface = Class.forName("android.net.TetheringManager\$StartTetheringCallback")
      val callback = Proxy.newProxyInstance(
        startTetheringInterface.classLoader,
        arrayOf(startTetheringInterface)
      ) {_, method, args ->
        when (method.name) {
          "onTetheringStarted" -> {
            promise.resolve(null)
            /*nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)*/
          }
          "onTetheringFailed" -> {
            val errorCode = args[0] as Int
            val tetheringError = TetheringErrorCode.fromCode(errorCode)
            CustomPromise(promise).reject(CodedException(tetheringError.name, tetheringError.message, null))
          }
        }
      }

      HiddenApiBypass.getDeclaredMethod(
        tetheringManager.javaClass,
        "startTethering",
        Class.forName("android.net.TetheringManager\$TetheringRequest"),
        Executor::class.java,
        startTetheringInterface
      ).invoke(tetheringManager, createTetheringRequestClass(), Executor(Runnable::run), callback)
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace()
    }
  }

  private fun enableHotspotBelowR(promise: Promise) {
    try {
      val onStartTetheringCallback = Class.forName("android.net.ConnectivityManager\$OnStartTetheringCallback")
      val callback = ProxyBuilder.forClass(onStartTetheringCallback)
        .dexCache(context.codeCacheDir)
        .handler { proxy, method, args ->
          when (method.name) {
            "onTetheringStarted" -> {
              promise.resolve(null)

              /*nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)*/
            }
            "onTetheringFailed" -> {
              CustomPromise(promise).reject(HotspotEnabledFailedException())
            }
            else -> ProxyBuilder.callSuper(proxy, method, args)
          }
          null
        }.build()

      if (isAndroidNineAndAbove()) {
        HiddenApiBypass.getDeclaredMethod(
          connectivityManager.javaClass,
          "startTethering",
          Int::class.java,
          Boolean::class.java,
          onStartTetheringCallback,
          Handler::class.java
        ).invoke(connectivityManager, TETHERING_WIFI, false, callback, null)
      } else {
        connectivityManager.javaClass.getMethod("startTethering", Int::class.java, Boolean::class.java, onStartTetheringCallback, Handler::class.java)
          .invoke(connectivityManager, TETHERING_WIFI, false, callback, null)
      }
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace()
    }
  }

  private fun disableHotspot(promise: Promise) {
    try {
      if (isAndroidNineAndAbove()) {
        HiddenApiBypass.getDeclaredMethod(
          connectivityManager.javaClass,
          "stopTethering",
          Int::class.java
        ).invoke(connectivityManager, TETHERING_WIFI)
      } else {
        connectivityManager.javaClass.getMethod("stopTethering", Int::class.java)
          .invoke(connectivityManager, TETHERING_WIFI)
      }

      promise.resolve(null)
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace()
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun enableLocalHotspot(promise: Promise) {
    try {
      wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
        override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
          super.onStarted(reservation)
          localOnlyHotspotReservation = reservation
          promise.resolve(WritableNativeMap().apply {
            putString("ssid", reservation.wifiConfiguration?.SSID)
            putString("password", reservation.wifiConfiguration?.preSharedKey)
          })
        }

        override fun onFailed(reason: Int) {
          super.onFailed(reason)
          val failureReason = getLocalOnlyHotspotError(reason)
          CustomPromise(promise).reject(CodedException(reason.toString(), failureReason, null))
        }
      }, Handler(context.mainLooper))
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace();
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun disableLocalHotspot(promise: Promise) {
    try {
      if (localOnlyHotspotReservation != null) {
        localOnlyHotspotReservation?.close()
        localOnlyHotspotReservation = null
      }
      promise.resolve(null)
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace();
    }
  }

  private fun isHotspotRunning(): Boolean {
    val interfaces = if (isAndroidNineAndAbove()) {
      HiddenApiBypass.getDeclaredMethod(
        connectivityManager.javaClass,
        "getTetheredIfaces"
      ).invoke(connectivityManager) as Array<String>
    } else {
      connectivityManager.javaClass.getMethod("getTetheredIfaces")
        .invoke(connectivityManager) as Array<String>
    }
    return interfaces.isNotEmpty()
  }

  private fun getHotspotIpAddress(): String? {
    val interfaces = NetworkInterface.getNetworkInterfaces()
    while (interfaces.hasMoreElements()) {
      val ntw = interfaces.nextElement()
      if (
        listOf("swlan0", "ap0", "wlan1").any { it == ntw.displayName } ||
        isWifiInterfaceUsed(ntw.displayName)
      ) {
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

  // some devices use wlan0 as interface for both wifi and hotspot so if it wlan0 we need to be sure that wifi disabled
  private fun isWifiInterfaceUsed(ntwInterface: String) = ntwInterface == "wlan0" && !wifiManager.isWifiEnabled

  private fun isAndroidNineAndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

  private fun createTetheringRequestClass(): Any? {
    val tetheringRequestBuilderClass = Class.forName("android.net.TetheringManager\$TetheringRequest\$Builder")
    val tetheringRequestBuilderConstructor = tetheringRequestBuilderClass.getDeclaredConstructor(Int::class.java)
    tetheringRequestBuilderConstructor.isAccessible = true
    val tetheringRequestBuilder = tetheringRequestBuilderConstructor.newInstance(TETHERING_WIFI) // 0 means hotspot

    tetheringRequestBuilder::class.java.getDeclaredMethod("setShouldShowEntitlementUi", Boolean::class.java)
      .invoke(tetheringRequestBuilder, false)

    return tetheringRequestBuilder::class.java.getDeclaredMethod("build")
      .invoke(tetheringRequestBuilder)
  }

  private fun getLocalOnlyHotspotError(errorCode: Int): String {
    return when (errorCode) {
      WifiManager.LocalOnlyHotspotCallback.ERROR_NO_CHANNEL -> "Failed to start LocalOnlyHotspot because channel was unavailable."
      WifiManager.LocalOnlyHotspotCallback.ERROR_GENERIC -> "Failed to start LocalOnlyHotspot due to an unknown error."
      WifiManager.LocalOnlyHotspotCallback.ERROR_INCOMPATIBLE_MODE -> "Failed to start LocalOnlyHotspot because the current mode is not compatible."
      else -> "Unknown LocalOnlyHotspot error."
    }
  }

  companion object {
    /*const val TAG = "RNHotspotTethering"*/
    const val TETHERING_WIFI = 0
  }

  enum class TetheringErrorCode(val code: Int, val message: String) {
    NO_ERROR(0, "No error"),
    UNKNOWN_IFACE(1, "Unknown interface"),
    SERVICE_UNAVAIL(2, "Tethering service is not available"),
    UNSUPPORTED(3, "Tethering is not supported"),
    UNAVAIL_IFACE(4, "The tethering interface is unavailable"),
    INTERNAL_ERROR(5, "Tethering service encountered an internal error"),
    TETHER_IFACE_ERROR(6, "An error occurred on the tethering interface"),
    UNTETHER_IFACE_ERROR(7, "Tethering interface is invalid"),
    IFACE_CFG_ERROR(10, "An error occurred while configuring the interface"),
    PROVISIONING_FAILED(11, "Provisioning the hotspot configuration failed"),
    DHCPSERVER_ERROR(12, "DHCP server error"),
    ENTITLEMENT_UNKNOWN(13, "The user does not have entitlement to tether"),
    NO_CHANGE_TETHERING_PERMISSION(14, "The app must be granted the write settings permission"),
    NO_ACCESS_TETHERING_PERMISSION(15, "The app must be granted the write settings permission"),
    UNKNOWN_ERROR(16, "Unknown error occurred");

    companion object {
      fun fromCode(code: Int): TetheringErrorCode = values().find { it.code == code } ?: UNKNOWN_ERROR
    }
  }
}
