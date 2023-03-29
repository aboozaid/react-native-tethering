/**
 * REACHABLE: This means that the ARP cache entry is valid and can be used to send packets to the associated IP address.
 * STALE: This means that the ARP cache entry is still valid, but it has not been used recently. When a packet is sent to the associated IP address, the entry will be refreshed and become reachable again.
 * DELAY: This means that an ARP request has been sent for the associated IP address, but a reply has not yet been received. The entry will remain in this state for a short period of time before it times out.
 * PROBE: This means that an ARP request has been sent for the associated IP address, but the MAC address in the ARP request is not yet confirmed to be correct. The entry will remain in this state until an ARP reply is received that confirms the MAC address.
 * FAILED: This means that an ARP request has been sent for the associated IP address, but no reply has been received within a certain amount of time. The entry is considered to be invalid and cannot be used to send packets.
 */
type Status = 'REACHABLE' | 'STALE' | 'DELAY' | 'PROBE' | 'FAILED';

export type Network = {
  ssid: string;
  password: string;
};

export type Device = {
  ipAddress: string;
  macAddress: string;
  status: Status;
};

// export enum Event {
//   OnNetworkDisconnected = 'onNetworkDisconnected',
//   OnWifiScanResults = 'onWifiScanResults',
// }

// type WifiScanResults = Network[];

// export interface EventsPayload {
//   [Event.OnNetworkDisconnected]: never;
//   [Event.OnWifiScanResults]: WifiScanResults;
// }

export class TetheringError extends Error {
  code: string;

  constructor(code: string, message: string) {
    super(message);
    this.code = code;
  }
}
