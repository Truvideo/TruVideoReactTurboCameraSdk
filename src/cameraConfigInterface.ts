import { LensFacing, FlashMode, Orientation, Mode } from './cameraConfigEnums';

export interface CameraConfiguration {
  lensFacing: LensFacing;
  flashMode: FlashMode;
  orientation: Orientation;
  outputPath: string;
  frontResolutions: string[];
  frontResolution: string;
  backResolutions: string[];
  backResolution: string;
  mode: Mode;
}
