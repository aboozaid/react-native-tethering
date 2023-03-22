---
sidebar_position: 3
---

# Objects

All types are exported and can be accessed via:

```ts
import {  } from '@react-native-tethering/wifi';
```

## Types

### `Network`

| Param | Type | Description |
| ------- | :-----: | :-----: | :-----: |
| ssid | `string` | network name |
| bssid | `string` | represents the unique identifier of the network |
| capabilities | `string` | represents the capabilities of the network, such as its encryption, key management and security |
| frequency | `number` | represents the frequency (in megahertz) on which the network operates |
| level | `number` | represents the received signal strength indicator (RSSI) of the network |
| timestamp | `number` | represents the time at which the information about the network was obtained |

### `TetheringError`
An error class used to handle errors between native and js sides. An example of how to use it
```
try {
  await getWifiNetworks()
} catch(err: any) {
  if (err instanceof TetheringError) {
    // handle the error
  }
}
```

| Param | Type | Description |
| ------- | :-----: | :-----: |
| code | `string` | you must use this code to handle every error |
| message | `string` | an error message of failure |