package com.tethering.utils

internal class NoPermissionException(cause: SecurityException?) : CodedException(cause!!)
internal class UnsupportedApiException(cause: SecurityException? = null) : CodedException("Only android Q and above are supported", cause)
internal class HotspotDisabledException(cause: SecurityException? = null) : CodedException("Hotspot must be running before calling this function", cause)
internal class WifiEnabledFailedException(cause: SecurityException? = null) : CodedException("Unable to toggle wifi state", cause)
internal class AddNetworkException(cause: SecurityException? = null) : CodedException("Unable to add wifi network", cause)

internal class AddNetworkCanceledException(cause: SecurityException? = null) : CodedException("User has canceled the request", cause)

internal class NetworkNotFoundException(cause: SecurityException? = null) : CodedException("Network cannot be found within wifi range", cause)

internal class NetworkScanTimeoutException(cause: SecurityException? = null) : CodedException("Network scan timeout", cause)

internal class NetworkSavedException(cause: SecurityException? = null) : CodedException("Network already saved in user's saved networks", cause)
internal class WifiConnectionFailedException(cause: SecurityException? = null) : CodedException("Unable connecting to the network", cause)
internal class WifiConnectionTimeoutException(cause: SecurityException? = null) : CodedException("Connection timeout", cause)

internal class WifiScanFailedException(cause: SecurityException? = null) : CodedException("Unable to start scanning wifi networks", cause)

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

