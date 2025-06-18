import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  initCameraScreen(configuration: string): Promise<string>;
  initARCameraScreen(configuration: string): Promise<string>;
  initScanerScreen(configuration: string): Promise<string>;
  version(): Promise<string>;
  environment(): Promise<string>;
  isAugmentedRealityInstalled(): Promise<string>;
  isAugmentedRealitySupported(): Promise<string>;
  requestInstallAugmentedReality(): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'TruVideoReactTurboCameraSdk'
);
