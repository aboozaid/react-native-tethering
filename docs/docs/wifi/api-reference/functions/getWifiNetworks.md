---
sidebar_position: 7
---

# getWifiNetworks
Get list of nearby networks and return a promise of [`Network`](#) array otherwise `TetheringError` will be thrown with the error details.

:::caution

You must have 

* `ACCESS_COARSE_LOCATION` 
* `ACCESS_FINE_LOCATION`

permissions granted to your App

:::

**Returns:** `Promise`

| Params | Type | Description | Default
| ------- | :-----: | :-----: | :-----: |
| rescan | Boolean | force device to do another scan | false |