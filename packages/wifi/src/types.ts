export type NetworkConnectionParams = {
  ssid: string;
  password?: string;
  isHidden?: boolean;
};

export type NetworkConnectionTimeoutParams = NetworkConnectionParams & {
  timeout?: number;
};

export type Network = {
  ssid: string;
  bssid: string;
  capabilities: string;
  frequency: number;
  level: number;
  timestamp: number;
};

export enum Event {
  OnNetworkDisconnected = 'onNetworkDisconnected',
  OnWifiScanResults = 'onWifiScanResults',
}

type WifiScanResults = Network[];

export interface EventsPayload {
  [Event.OnNetworkDisconnected]: never;
  [Event.OnWifiScanResults]: WifiScanResults;
}

export class TetheringError extends Error {
  code: string;

  constructor(code: string, message: string) {
    super(message);
    this.code = code;
  }
}
