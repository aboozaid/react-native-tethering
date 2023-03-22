---
sidebar_position: 2
---

# setWifiEnabled
Turn on your device WiFi and return a promise of success otherwise `TetheringError` will be thrown with the error details.

:::info

In Android 10 and above you cannot enable WiFi programmatically so instead a popup dialog will be shown without navigating the user from your App otherwise in Android 9 and below it works as expected.

:::

:::caution

You must have `CHANGE_WIFI_STATE` permission granted to your App

:::

**Returns:** `Promise`

| Params | Type | Description | Default
| ------- | :-----: | :-----: | :-----: |
| autoScan | Boolean | Start scanning nearby networks once WiFi get enabled | false |

- if you set `autoScan` to true, you can get networks list by listening to `onWifiScanResults` event.


