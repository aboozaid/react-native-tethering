package com.reactnativetethering.wifi.utils

internal class NoPermissionException(cause: SecurityException) : CodedException(cause)

internal class UnsupportedApiException() : CodedException("Only android Q and above are supported", null)

internal class WifiEnabledFailedException() : CodedException("Unable to toggle wifi state", null)

internal class AddNetworkException() : CodedException("Unable to add wifi network", null)

internal class AddNetworkCanceledException() : CodedException("User has canceled the request", null)

internal class NetworkNotFoundException() : CodedException("Network cannot be found within wifi range", null)

internal class NetworkScanTimeoutException() : CodedException("Network scan timeout", null)

internal class NetworkSavedException() : CodedException("Network already saved in user's saved networks", null)

internal class WifiConnectionTimeoutException() : CodedException("Connection timeout", null)

internal class WifiScanFailedException() : CodedException("Unable to start scanning wifi networks", null)

