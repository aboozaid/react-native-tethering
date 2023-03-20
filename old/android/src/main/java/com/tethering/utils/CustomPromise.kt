package com.tethering.utils

import com.facebook.react.bridge.Promise

class CustomPromise(private val promise: Promise) {
  fun reject(exception: CodedException) {
    promise.reject(exception.code, exception.localizedMessage, exception)
  }
}
