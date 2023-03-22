---
sidebar_position: 4
---

# connectToLocalNetwork
Connect to a network without internet connectivity such as your printer, IoT device or p2p with another device and return a promise of success otherwise `TetheringError` will be thrown with the error details.

:::info

Once your app get terminated you will lost your connection with the local network

:::

:::caution

You must have 

* `CHANGE_NETWORK_STATE`
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