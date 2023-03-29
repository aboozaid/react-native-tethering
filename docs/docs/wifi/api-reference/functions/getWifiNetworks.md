---
sidebar_position: 7
---

# getWifiNetworks
Get list of nearby networks and return a promise of [`Network`](/docs/wifi/api-reference/objects#network) array otherwise [`TetheringError`](/docs/wifi/api-reference/objects#tetheringerror) will be thrown with the error details.

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