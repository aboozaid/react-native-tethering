import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
import type {
  EventsPayload,
  Event,
  Network,
  NetworkConnectionParams,
  NetworkConnectionTimeoutParams,
} from './types';
import { TetheringError } from './types';

const LINKING_ERROR =
  `The package '@react-native-tethering/wifi' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const WifiTethering = NativeModules.WifiTethering
  ? NativeModules.WifiTethering
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const emitter = new NativeEventEmitter();

async function callPromise<T>(method: Promise<T>): Promise<T> {
  try {
    return await method;
  } catch (error: any) {
    throw new TetheringError(error.code, error.message);
  }
}

export function addEventListener<T extends Event>(
  event: T,
  listener: EventsPayload[T] extends never
    ? () => void
    : (event: EventsPayload[T]) => void
) {
  return emitter.addListener(event, listener);
}

export async function isWifiEnabled(): Promise<boolean> {
  return callPromise(WifiTethering.isWifiEnabled());
}

export async function setWifiEnabled(autoScan: boolean = false): Promise<void> {
  return callPromise(WifiTethering.setWifiEnabled(true, autoScan)); // in android 29 this will trigger bottom sheet panel
}

export async function setWifiDisabled(): Promise<void> {
  return callPromise(WifiTethering.setWifiEnabled(false, false)); // in android 29 this will trigger bottom sheet panel
}

export async function connectToLocalNetwork({
  ssid,
  password,
  isHidden = false,
}: NetworkConnectionParams): Promise<void> {
  return callPromise(
    WifiTethering.connectToLocalNetwork(ssid, password, isHidden)
  );
}

export async function connectToNetwork({
  ssid,
  password,
  isHidden = false,
  timeout = 6000,
}: NetworkConnectionTimeoutParams): Promise<void> {
  return callPromise(
    WifiTethering.connectToNetwork(ssid, password, isHidden, timeout)
  );
}

export async function disconnectFromLocalNetwork(): Promise<void> {
  return callPromise(WifiTethering.disconnectFromLocalNetwork());
}

export async function disconnectFromNetwork(): Promise<void> {
  return callPromise(WifiTethering.disconnectFromNetwork());
}

export async function saveNetworkInDevice({
  ssid,
  password,
  isHidden = false,
}: NetworkConnectionParams): Promise<void> {
  return callPromise(
    WifiTethering.saveNetworkInDevice(ssid, password, isHidden)
  );
}

export async function getWifiNetworks(
  rescan: boolean = false
): Promise<Network[]> {
  return callPromise(WifiTethering.getWifiNetworks(rescan));
}

export async function getMaxNumberOfNetworkSuggestions(): Promise<number> {
  return callPromise(WifiTethering.getMaxNumberOfNetworkSuggestions());
}

export async function isDeviceAlreadyConnected(): Promise<boolean> {
  return callPromise(WifiTethering.isDeviceAlreadyConnected());
}

export async function getDeviceIP(): Promise<string | null> {
  return callPromise(WifiTethering.getDeviceIP());
}

export function openWifiSettings(asDialog: boolean = true): void {
  return WifiTethering.openWifiSettings(asDialog);
}
