---
sidebar_position: 6
---

# disconnectFromNetwork
Disconnect from a connected network and return a promise of success otherwise [`TetheringError`](/docs/wifi/api-reference/objects#tetheringerror) will be thrown with the error details.

:::info

If you try to disconnect from a saved network this function won't have any effect.

:::

:::caution

You must have `CHANGE_WIFI_STATE` permissions granted to your App

:::

**Returns:** `Promise`