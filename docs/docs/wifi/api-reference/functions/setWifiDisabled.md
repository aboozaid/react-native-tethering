---
sidebar_position: 3
---

# setWifiDisabled
Turn off your device WiFi and return a promise of success otherwise [`TetheringError`](/docs/wifi/api-reference/objects#tetheringerror) will be thrown with the error details.

:::info

In Android 10 and above you cannot disable WiFi programmatically so instead a popup dialog will be shown without navigating the user from your App otherwise in Android 9 and below it works as expected.

:::
:::caution

You must have `CHANGE_WIFI_STATE` permission granted to your App

:::

**Returns:** `Promise`


