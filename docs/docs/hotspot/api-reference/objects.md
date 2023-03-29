---
sidebar_position: 3
---

# Objects

All types are exported and can be accessed via:

```ts
import {  } from '@react-native-tethering/hotspot';
```

## Types

### `Network`

| Param | Type | Description |
| ------- | :-----: | :-----: |
| ssid | `string` | network name |
| password | `string` | represents the secret of the network |

### `Device`

| Param | Type | Description |
| ------- | :-----: | :-----: |
| ipAddress | `string` | represents current ip address of a device |
| macAddress | `string` | represents current mac address of a device |
| status | `string` | represents current status of a device in the network |

### `TetheringError`
An error class used to handle errors between native and js sides. An example of how to use it
```
try {
  await setHotspotEnabled()
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