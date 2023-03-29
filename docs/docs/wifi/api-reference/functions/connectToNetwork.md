---
sidebar_position: 4
---

# connectToNetwork
Connect to a network with internet connectivity and return a promise of success otherwise [`TetheringError`](/docs/wifi/api-reference/objects#tetheringerror) will be thrown with the error details.

:::info

Due to the limitation of newly Android versions please read [Selection Criteria](https://source.android.com/docs/core/connect/wifi-network-selection) and [Network Suggestions](https://developer.android.com/guide/topics/connectivity/wifi-suggest) although the system only decides which network to connect to we use a workaround to force it to connect but we cannot guarantee that would work always so in case of failure you should use `saveNetworkInDevice` or `openWifiSettings` as fallbacks.

:::

:::caution

You must have 

* `CHANGE_NETWORK_STATE`
* `CHANGE_WIFI_STATE`
* `ACCESS_COARSE_LOCATION` 
* `ACCESS_FINE_LOCATION`

permissions granted to your App

:::

**Returns:** `Promise`

| Params | Type | Description | Default
| ------- | :-----: | :-----: | :-----: |
| ssid | String | network name | required |
| password | String | network password | none |
| isHidden | Boolean | whether a network is hidden or not | false |
| timeout | number | in case the connection failed within that time you will get a timeout error which you can use for fallbacks | 6 sec |