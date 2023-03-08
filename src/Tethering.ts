import { TetheringError } from './utils/TetheringError';
import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-tethering' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export default NativeModules.Tethering
  ? NativeModules.Tethering
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const _callPromise = async <T>(promise: Promise<T>): Promise<T> => {
  try {
    const result = await promise;
    return result;
  } catch (error: any) {
    throw new TetheringError(error.code, error.message);
  }
};
