---
sidebar_position: 3
---

# setLocalHotspotEnabled
Turn on your device Local Hotspot and return a promise of [`Network`](/docs/hotspot/api-reference/objects#network) otherwise [`TetheringError`](/docs/hotspot/api-reference/objects#tetheringerror) will be thrown with the error details.


:::caution

You must have `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` permissions granted to your App. and if you target Android 13 and above `NEARBY_WIFI_DEVICES` must be granted as well.

:::

**Returns:** `Promise`

