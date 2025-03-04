import TruVideoReactTurboCameraSdk from './NativeTruVideoReactTurboCameraSdk';
import type { CameraConfiguration } from './cameraConfigInterface';
import { LensFacing, FlashMode, Orientation, Mode } from './cameraConfigEnums';

export function initCameraScreen(
  configuration: CameraConfiguration
): Promise<string> {
  return TruVideoReactTurboCameraSdk.initCameraScreen(JSON.stringify(configuration));
}
export { LensFacing, FlashMode, Orientation, Mode };

export * from './cameraConfigInterface';
