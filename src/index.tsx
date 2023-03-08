import { NativeModules, Platform } from 'react-native';
import type { Network, Peer } from './types';
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

async function setWifiEnabled(state?: boolean): Promise<void> {
  return await _callPromise(Tethering.setWifiEnabled(state || true)); // in android 29 this will trigger bottom sheet panel
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
};

export { TetheringError };
