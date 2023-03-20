import type { Network } from 'src/types';
import Tethering, { _callPromise } from '../Tethering';

export async function isEnabled(): Promise<boolean> {
  return await _callPromise(Tethering.isHotspotEnabled());
}

export async function setEnabled(state: boolean): Promise<void> {
  return await _callPromise(Tethering.setHotspotEnabled(state));
}

export async function setLocalEnabled(state: boolean): Promise<Network> {
  return await Tethering.setLocalHotspotEnabled(state);
}
