import TruVideoReactTurboCameraSdk from './NativeTruVideoReactTurboCameraSdk';

interface Configuration {
  lensFacing: LensFacing;
  flashMode: FlashMode;
  orientation: Orientation;
  outputPath: string;
  frontResolutions: Resolution[] | null;
  frontResolution: Resolution | null;
  backResolutions: Resolution[] | null;
  backResolution: Resolution | null;
  mode: string;
}
interface ARConfiguration {
  outputPath: string;
  mode: string;
}

export function initCameraScreen(
  configuration: CameraConfiguration
): Promise<string> {
  let data = {
      mode: configuration.mode.mode,
      videoLimit: configuration.mode.videoLimit,
      imageLimit: configuration.mode.imageLimit,
      mediaLimit: configuration.mode.mediaLimit,
      videoDurationLimit: configuration.mode.videoDurationLimit,
      autoClose: configuration.mode.autoClose,
    };
  var cameraConfiguration  :  Configuration = {
            lensFacing: configuration.lensFacing,
            flashMode: configuration.flashMode,
            orientation: configuration.orientation,
            outputPath: configuration.outputPath,
            frontResolutions: configuration.frontResolutions,
            frontResolution: configuration.frontResolution,
            backResolutions: configuration.backResolutions,
            backResolution: configuration.backResolution,
            mode: JSON.stringify(data),
        }
  return TruVideoReactTurboCameraSdk.initCameraScreen(
    JSON.stringify(cameraConfiguration)
  );
}

export function initARCameraScreen(
  configuration: ARCameraConfiguration
): Promise<string> {
  let data = {
      mode: configuration.mode.mode,
      videoLimit: configuration.mode.videoLimit,
      imageLimit: configuration.mode.imageLimit,
      mediaLimit: configuration.mode.mediaLimit,
      videoDurationLimit: configuration.mode.videoDurationLimit,
      autoClose: configuration.mode.autoClose,
    };
  var cameraConfiguration  :  ARConfiguration = {
            outputPath: configuration.outputPath,
            mode: JSON.stringify(data),
        }
  return TruVideoReactTurboCameraSdk.initARCameraScreen(
    JSON.stringify(cameraConfiguration)
  );
}
export function initScanerScreen(): Promise<string> {
  return TruVideoReactTurboCameraSdk.initScanerScreen(
    JSON.stringify("")
  );
}

export function version(): Promise<string> {
  return TruVideoReactTurboCameraSdk.version();
}

export function environment(): Promise<string> {
  return TruVideoReactTurboCameraSdk.environment();
}
export function isAugmentedRealityInstalled(): Promise<string> {
  return TruVideoReactTurboCameraSdk.isAugmentedRealityInstalled();
}
export function isAugmentedRealitySupported(): Promise<string> {
  return TruVideoReactTurboCameraSdk.isAugmentedRealitySupported();
}

export function requestInstallAugmentedReality(): Promise<string> {
  return TruVideoReactTurboCameraSdk.requestInstallAugmentedReality();
}

export enum LensFacing {
  Back = 'back',
  Front = 'front',
}

export enum FlashMode {
  Off = 'off',
  On = 'on',
}

export enum Orientation {
  Portrait = 'portrait',
  LandscapeLeft = 'landscapeLeft',
  LandscapeRight = 'landscapeRight',
  PortraitReverse = 'portraitReverse',
}

export interface Resolution {
  width: number;
  height: number;
}

export interface CameraConfiguration {
  lensFacing: LensFacing;
  flashMode: FlashMode;
  orientation: Orientation;
  outputPath: string;
  frontResolutions: Resolution[] | null;
  frontResolution: Resolution | null;
  backResolutions: Resolution[] | null;
  backResolution: Resolution | null;
  mode: CameraMode;
}
export interface ARCameraConfiguration {
  outputPath: string;
  mode: CameraMode;
}

export class CameraMode {
  videoLimit: string = "";
  imageLimit: string = "";
  mediaLimit: string = "";
  mode: string = 'videoAndImage';
  videoDurationLimit: string = "";
  autoClose: boolean = false;
  private constructor(
    mode: string,
    videoLimit: number | null,
    imageLimit: number | null,
    mediaLimit: number | null,
    videoDurationLimit: number | null,
    autoClose: boolean
  ) {
    this.mode = mode;
    this.videoLimit = videoLimit != null ? videoLimit.toString() : "";
    this.imageLimit = imageLimit != null ? imageLimit.toString() : "";
    this.mediaLimit = mediaLimit != null ? mediaLimit.toString() : "";
    this.videoDurationLimit = videoDurationLimit != null ? videoDurationLimit.toString() : "";
    this.autoClose = autoClose;
  }
  static singleMedia(): CameraMode;
  static singleMedia( durationLimit?: number,mediaCount?: number): CameraMode;
  static singleMedia(
    durationLimit?: number,
    mediaCount?: number,
  ): CameraMode {
    return new CameraMode(
      'singleMedia',
      null,
      null,
      mediaCount ?? null,
      durationLimit ?? null,
      false
    );
  }

  static videoAndImage(): CameraMode;
  static videoAndImage(
    durationLimit?: number,
    videoMaxCount?: number,
    imageMaxCount?: number
  ): CameraMode;

  static videoAndImage(
    durationLimit?: number,
    videoMaxCount?: number,
    imageMaxCount?: number
  ): CameraMode {
    return new CameraMode(
      'videoAndImage',
      videoMaxCount ?? null,
      imageMaxCount ?? null,
      null,
      durationLimit ?? null,
      false
    );
  }

  getJson(): string {
    var data = {
      mode: this.mode,
      videoLimit: this.videoLimit,
      imageLimit: this.imageLimit,
      mediaLimit: this.mediaLimit,
      videoDurationLimit: this.videoDurationLimit,
      autoClose: this.autoClose,
    };
    return JSON.stringify(data);
  }

  static singleVideo(): CameraMode;
  static singleVideo(durationLimit?: number): CameraMode;

  static singleVideo(durationLimit?: number): CameraMode {
    return new CameraMode(
      'singleVideo',
      1,
      0,
      null,
      durationLimit ?? null,
      true
    );
  }

  static singleImage(): CameraMode {
    return new CameraMode('singleImage', 0, 1, null, null, true);
  }
  static singleVideoOrImage(): CameraMode;
  static singleVideoOrImage(durationLimit?: number): CameraMode;
  static singleVideoOrImage(durationLimit?: number): CameraMode {
    return new CameraMode(
      'singleVideoOrImage',
      null,
      null,
      1,
      durationLimit ?? null,
      true
    );
  }

  static video(): CameraMode;
  static video(videoMaxCount?: number): CameraMode;
  static video(videoMaxCount?: number,durationLimit?: number): CameraMode;

  static video(videoMaxCount?: number,durationLimit?: number): CameraMode {
    return new CameraMode(
      'video',
      videoMaxCount ?? null,
      0,
      null,
      durationLimit ?? null,
      false
    );
  }

  static image(): CameraMode;
  static image(imageMaxCount?: number): CameraMode;
  static image(imageMaxCount?: number): CameraMode {
    return new CameraMode('image', 0, imageMaxCount ?? null, null, null, false);
  }
}
