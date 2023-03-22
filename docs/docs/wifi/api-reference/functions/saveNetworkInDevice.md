---
sidebar_position: 4
---

# saveNetworkInDevice
Add a network to saved networks of a device then connect to it and return a promise of success otherwise `TetheringError` will be thrown with the error details.

:::info

Only Android 11 and above can use this function otherwise you will get an error of `ERR_UNSUPPORTED_API`. Also note if the network already saved you would get an error of `ERR_NETWORK_SAVED` and connection won't be established.

:::

:::caution

You must have 

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