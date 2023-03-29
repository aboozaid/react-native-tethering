package com.reactnativetethering.hotspot.utils

internal class NoPermissionException(cause: SecurityException) : CodedException(cause)

internal class UnsupportedApiException() : CodedException("Only android Oreo and above are supported", null)

internal class HotspotEnabledFailedException() : CodedException("Unable to toggle hotspot state", null)
