import { TetheringError } from './TetheringError';

export default async <T>(promise: Promise<T>): Promise<T> => {
  try {
    const result = await promise;
    return result;
  } catch (error: any) {
    throw new TetheringError(error.code, error.message);
  }
};
