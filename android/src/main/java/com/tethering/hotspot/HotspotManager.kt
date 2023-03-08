package com.tethering.hotspot

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.dx.stock.ProxyBuilder
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.tethering.utils.CodedException
import com.tethering.utils.CustomPromise
import com.tethering.utils.HotspotDisabledException
import com.tethering.utils.NoPermissionException
import com.tethering.utils.TetheringErrorCode
import com.tethering.utils.UnsupportedApiException
import it.alessangiorgi.ipneigh30.ArpNDK
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Proxy
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.Executor

class HotspotManager(private val context: ReactApplicationContext) {

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

  @RequiresApi(Build.VERSION_CODES.O)
  fun setLocalHotspotEnabled(state: Boolean, promise: Promise) {
    if (!state) {
      disableLocalHotspot(promise)
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        enableLocalHotspot(promise)
      } else {
        CustomPromise(promise).reject(UnsupportedApiException(null))
      }
    }
  }

  fun getMyCurrentAddress(promise: Promise) {
    if (!isHotspotRunning()) {
      return CustomPromise(promise).reject(CodedException(HotspotDisabledException(null)))
    } else {
      val ipAddress = getHotspotIpAddress()
      promise.resolve(ipAddress)
    }
  }

  fun getPeersAddresses(promise: Promise) {
    if (!isHotspotRunning()) {
      return CustomPromise(promise).reject(CodedException(HotspotDisabledException(null)))
    } else {
      // In android 13 and above this will return an empty array
      val arpTableString = ArpNDK.getARP().trimIndent()
      val pattern =
        """^(\S+)\s+dev\s+(swlan0|ap0|wlan0)\s+lladdr\s+([0-9a-fA-F]{2}(?::[0-9a-fA-F]{2}){5})\s+(\S+)${'$'}""".toRegex(RegexOption.MULTILINE)
      val peers = Arguments.createArray()
      for (matchResult in pattern.findAll(arpTableString)) {
        val ipAddress = matchResult.groupValues[1]
        peers.pushMap(Arguments.createMap().apply { putString("ipAddress", ipAddress) })
      }
      promise.resolve(peers)
    }
  }

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
           }
           "onTetheringFailed" -> {
             val errorCode = args[0] as Int
             val tetheringError = TetheringErrorCode.fromCode(errorCode)
             CustomPromise(promise).reject(CodedException(tetheringError.name, tetheringError.message, null))
           }
         }
       }
      val executor = Executor(Runnable::run)
      val tetheringRequest = createTetheringRequestClass()

      HiddenApiBypass.getDeclaredMethod(
        tetheringManager.javaClass,
        "startTethering",
        Class.forName("android.net.TetheringManager\$TetheringRequest"),
        Executor::class.java,
        startTetheringInterface
      ).invoke(tetheringManager, tetheringRequest, executor, callback)
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace()
    }
  }

  @SuppressLint("NewApi")
  private fun enableHotspotBelowR(promise: Promise) {
    try {
      val onStartTetheringCallback = Class.forName("android.net.ConnectivityManager\$OnStartTetheringCallback")
      val callback = ProxyBuilder.forClass(onStartTetheringCallback)
        .dexCache(context.codeCacheDir)
        .handler { proxy, method, args ->
          when (method.name) {
            "onTetheringStarted" -> {
              promise.resolve(null)
            }
            "onTetheringFailed" -> {
              CustomPromise(promise).reject(CodedException("onTetheringFailed", "Unknown error occurred", null))
            }
            else -> ProxyBuilder.callSuper(proxy, method, args)
          }
          null
        }.build()

      HiddenApiBypass.getDeclaredMethod(
        connectivityManager.javaClass,
        "startTethering",
        Int::class.java,
        Boolean::class.java,
        onStartTetheringCallback,
        Handler::class.java
      ).invoke(connectivityManager, TETHERING_WIFI, false, callback, null)
    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace()
    }
  }

  @SuppressLint("NewApi")
  private fun disableHotspot(promise: Promise) {
    try {
      HiddenApiBypass.getDeclaredMethod(
        connectivityManager.javaClass,
        "stopTethering",
        Int::class.java
      ).invoke(connectivityManager, TETHERING_WIFI)

      promise.resolve(null)

    } catch (e: SecurityException) {
      CustomPromise(promise).reject(NoPermissionException(e))
      e.printStackTrace()
    } catch (e: Exception) {
      CustomPromise(promise).reject(CodedException(e.message, e))
      e.printStackTrace();
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun enableLocalHotspot(promise: Promise) {
    try {
      wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
        override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
          super.onStarted(reservation)
          localOnlyHotspotReservation = reservation
          promise.resolve(Arguments.createMap().apply {
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

  private fun isHotspotRunning(): Boolean {
    val interfaces = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
      if (listOf("swlan0", "ap0", "wlan0").any { it == ntw.displayName }) {
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

  fun getLocalOnlyHotspotError(errorCode: Int): String {
    return when (errorCode) {
      WifiManager.LocalOnlyHotspotCallback.ERROR_NO_CHANNEL -> "Failed to start LocalOnlyHotspot because channel was unavailable."
      WifiManager.LocalOnlyHotspotCallback.ERROR_GENERIC -> "Failed to start LocalOnlyHotspot due to an unknown error."
      WifiManager.LocalOnlyHotspotCallback.ERROR_INCOMPATIBLE_MODE -> "Failed to start LocalOnlyHotspot because the current mode is not compatible."
      else -> "Unknown LocalOnlyHotspot error."
    }
  }


  companion object {
    const val TAG = "HotspotManager"
    const val TETHERING_WIFI = 0
  }
}
