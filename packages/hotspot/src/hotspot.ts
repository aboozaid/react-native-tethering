import { NativeModules, Platform } from 'react-native';
import { Device, Network, TetheringError } from './types';

const LINKING_ERROR =
  `The package '@react-native-tethering/hotspot' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Hotspot = NativeModules.Hotspot
  ? NativeModules.Hotspot
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

async function callPromise<T>(method: Promise<T>): Promise<T> {
  try {
    return await method;
  } catch (error: any) {
    throw new TetheringError(error.code, error.message);
  }
}
/**
 * Check if hotspot already turned on or off returning a boolean value indicating the status of active/inactive.
 *
 * @returns {Promise<Boolean>}
 */
export async function isHotspotEnabled(): Promise<boolean> {
  return callPromise(Hotspot.isHotspotEnabled());
}

/**
 * Turn on hotspot on an Android device runs Oreo and above or rejects with an error message string if the hotspot cannot be turned on.
 *
 * `Required Permission: android.permission.WRITE_SETTINGS`
 *
 * @returns {Promise<void>} A Promise or rejects with an error message string.
 */
export async function setHotspotEnabled(state: boolean): Promise<void> {
  return callPromise(Hotspot.setHotspotEnabled(state));
}

export async function setLocalHotspotEnabled(state: boolean): Promise<Network> {
  return callPromise(Hotspot.setLocalHotspotEnabled(state));
}

/**
 * Turn off hotspot on an Android device runs Oreo and above or rejects with an error message string if the hotspot cannot be turned off.
 *
 * `Required Permission: android.permission.WRITE_SETTINGS`
 *
 * @returns {Promise<void>} A Promise or rejects with an error message string.
 */
// export async function turnOffHotspot(): Promise<void> {
//   return await Hotspot.turnOffHotspot();
// }

/**
 * Check whether `android.permission.WRITE_SETTINGS` permission is granted or not.
 *
 *
 * @returns {Boolean} A boolean value indicating permission is granted or not.
 */
export async function isWriteSettingsGranted(): Promise<boolean> {
  return callPromise(Hotspot.isWriteSettingsGranted());
}

/**
 * Navigate your user to app's write settings permission screen to allow it, This is required to use turn on/off hotspot functions otherwise you won't be able to use them.
 *
 * Also you can check whether the permission already granted or not by using `checkIfWriteSettingsGranted` function
 *
 * @returns {Promise<void>} A Promise.
 */
export async function openWriteSettings(): Promise<void> {
  return callPromise(Hotspot.openWriteSettings());
}

/**
 * Navigate user to tethering screen to open hotspot manually, Android does not provide an API to automatically open hotspot with an internet access.
 *
 * @returns {Promise<void>}
 */
export async function navigateToTethering(): Promise<void> {
  return callPromise(Hotspot.navigateToTethering());
}

/**
 * Retrieves your current device which contains your IP address over a hotspot please note if you're over a wifi it will return a null IP address instead.
 *
 * @returns {Promise<Device>}
 */
export async function getMyDeviceIp(): Promise<string | null> {
  return callPromise(Hotspot.getMyDeviceIp());
}
/**
 * Gets a list of devices connected to the local hotspot on an Android device and returns a Promise that resolves with an array of device objects containing the IP address, MAC address, and status of each device, or resolves with a single device object if the Android API version is 33 or greater, or rejects with an error message string if the devices cannot be retrieved.
 * Only devices connected to your hotspot
 *
 * @returns {Promise<Device[]>} A Promise that resolves with an array of device objects or a single device object if the API version is 33 or greater, or rejects with an error message string.
 */
export async function getConnectedDevices(): Promise<Device[]> {
  return callPromise(Hotspot.getConnectedDevices());
}
