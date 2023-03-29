---
sidebar_position: 4
---

# getConnectedDevices
Gets a list of connected devices to your hotspot on an Android device and return a promise of [`Device`](/docs/hotspot/api-reference/objects#device) otherwise [`TetheringError`](/docs/hotspot/api-reference/objects#tetheringerror) will be thrown with the error details.

:::info
  This method won't work in Android 13 due to the new security roles in that version.
:::

**Returns:** `Promise`