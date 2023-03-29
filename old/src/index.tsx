import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
import type { Network, Peer, WifiNetwork } from './types';
import { TetheringError } from './utils/TetheringError';
import _callPromise from './utils/callPromise';

const LINKING_ERROR =
  `The package 'react-native-tethering' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Tethering = NativeModules.Tethering
  ? NativeModules.Tethering
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

async function isHotspotEnabled(): Promise<boolean> {
  return await _callPromise(Tethering.isHotspotEnabled());
}

async function setHotspotEnabled(state: boolean): Promise<void> {
  return await _callPromise(Tethering.setHotspotEnabled(state));
}

async function setLocalHotspotEnabled(): Promise<Network> {
  return await _callPromise(Tethering.setLocalHotspotEnabled());
}

async function setLocalHotspotDisabled(): Promise<void> {
  return await _callPromise(Tethering.setLocalHotspotDisabled());
}

async function getHotspotDeviceIp(): Promise<string> {
  return await _callPromise(Tethering.getHotspotDeviceIp());
}

async function getHotspotPeersAddresses(): Promise<Peer[]> {
  return await _callPromise(Tethering.getHotspotPeersAddresses());
}

async function isWifiEnabled(): Promise<boolean> {
  return await _callPromise(Tethering.isWifiEnabled());
}

async function setWifiEnabled(autoScan: boolean = false): Promise<void> {
  return await _callPromise(Tethering.setWifiEnabled(true, autoScan)); // in android 29 this will trigger bottom sheet panel
}

async function setWifiDisabled(): Promise<void> {
  return await _callPromise(Tethering.setWifiEnabled(false, false)); // in android 29 this will trigger bottom sheet panel
}

async function connectToLocalNetwork(
  ssid: string,
  password?: string,
  isHidden: boolean = false
): Promise<void> {
  return await _callPromise(
    Tethering.connectToLocalNetwork(ssid, password, isHidden)
  );
}

async function connectToNetwork(
  ssid: string,
  password?: string,
  isHidden: boolean = false,
  timeout: number = 6000
): Promise<void> {
  return await _callPromise(
    Tethering.connectToNetwork(ssid, password, isHidden, timeout)
  );
}

async function disconnectFromLocalNetwork(): Promise<void> {
  return await _callPromise(Tethering.disconnectFromLocalNetwork());
}

async function disconnectFromNetwork(): Promise<void> {
  return await _callPromise(Tethering.disconnectFromNetwork());
}

async function saveNetworkInDevice(
  ssid: string,
  password?: string,
  isHidden: boolean = false
): Promise<void> {
  return await _callPromise(
    Tethering.saveNetworkInDevice(ssid, password, isHidden)
  );
}

async function getWifiNetworks(
  rescan: boolean = false
): Promise<WifiNetwork[]> {
  return await _callPromise(Tethering.getWifiNetworks(rescan));
}

function onNetworkDisconnected(callback: () => void) {
  const emitter = new NativeEventEmitter(Tethering);
  const listener = emitter.addListener('OnNetworkDisconnected', callback);

  return () => listener.remove();
}

export default {
  isHotspotEnabled,
  setHotspotEnabled,
  setLocalHotspotEnabled,
  setLocalHotspotDisabled,
  getHotspotDeviceIp,
  getHotspotPeersAddresses,

  isWifiEnabled,
  setWifiEnabled,
  setWifiDisabled,
  connectToLocalNetwork,
  disconnectFromLocalNetwork,
  connectToNetwork,
  disconnectFromNetwork,
  saveNetworkInDevice,
  getWifiNetworks,
  onNetworkDisconnected,
};

export { TetheringError };