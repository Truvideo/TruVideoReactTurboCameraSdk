import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  initCameraScreen(configuration:string): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('TruVideoReactTurboCameraSdk');
