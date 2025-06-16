import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  initCameraScreen(configuration: string): Promise<string>;
  version(): Promise<string>;
  environment(): Promise<string>;
  isAugmentedRealityInstalled(): Promise<string>;
  isAugmentedRealitySupported(): Promise<string>;
  requestInstallAugmentedReality(): Promise<string>;
  videoAndImage(jsonString: string): Promise<string>;
  singleVideo(videoDuration: string): Promise<string>;
  singleImage(): Promise<string>;
  singleVideoOrImage(durationLimit: string): Promise<string>;
  video(maxCount: string, durationLimit: string): Promise<string>;
  image(maxCount: string): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'TruVideoReactTurboCameraSdk'
);
