---
sidebar_position: 1
---

# Events

All event types are made available through the named export `Event`:

```ts
import { Event } from '@react-native-tethering/wifi';
```

## Connection

### `OnNetworkDisconnected`
Fired when a connected network lost whether WiFi turned off or any other case

## Scanning

### `OnWifiScanResults`
Fired when enabling autoScan in [`setWifiEnabled`](/docs/wifi/api-reference/functions/setWifiEnabled)

| Param | Type | Description |
| ------- | :-----: | :-----: |
| networks | array of [`Network`](/docs/wifi/api-reference/objects#network) | List of networks details ssid, bssid and more |