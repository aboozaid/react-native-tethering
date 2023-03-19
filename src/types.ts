export type Network = {
  ssid: string;
  password: string;
};

export type Peer = {
  ipAddress: string;
};

export type WifiNetwork = {
  ssid: string;
  bssid: string;
  capabilities: string;
  frequency: number;
  level: number;
  timestamp: number;
};
