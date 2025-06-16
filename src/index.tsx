import TruVideoReactTurboCameraSdk from './NativeTruVideoReactTurboCameraSdk';

export function initCameraScreen(
  configuration: CameraConfiguration
): Promise<string> {
  return TruVideoReactTurboCameraSdk.initCameraScreen(
    JSON.stringify(configuration)
  );
}

export function initARCameraScreen(
  configuration: CameraConfiguration
): Promise<string> {
  return TruVideoReactTurboCameraSdk.initCameraScreen(
    JSON.stringify(configuration)
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

export class CameraMode {
  videoLimit: number | null = null;
  imageLimit: number | null = null;
  mediaLimit: number | null = null;
  mode: String = 'videoAndImage';
  videoDurationLimit: number | null = null;
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
    this.videoLimit = videoLimit;
    this.imageLimit = imageLimit;
    this.mediaLimit = mediaLimit;
    this.videoDurationLimit = videoDurationLimit;
    this.autoClose = autoClose;
  }

  static videoAndImage(): CameraMode;
  static videoAndImage(durationLimit?: number): CameraMode;
  static videoAndImage(mediaCount?: number, durationLimit?: number): CameraMode;
  static videoAndImage(
    videoMaxCount?: number,
    imageMaxCount?: number,
    durationLimit?: number
  ): CameraMode;

  static videoAndImage(
    mediaCount?: number,
    durationLimit?: number,
    videoMaxCount?: number,
    imageMaxCount?: number
  ): CameraMode {
    return new CameraMode(
      'videoAndImage',
      videoMaxCount ?? null,
      imageMaxCount ?? null,
      mediaCount ?? null,
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
  static video(durationLimit?: number, videoMaxCount?: number): CameraMode;

  static video(durationLimit?: number, videoMaxCount?: number): CameraMode {
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
