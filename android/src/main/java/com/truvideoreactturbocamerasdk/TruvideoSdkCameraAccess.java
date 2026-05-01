package com.truvideoreactturbocamerasdk;

import androidx.annotation.NonNull;
import com.truvideo.sdk.camera.TruvideoSdkCamera;
import com.truvideo.sdk.camera.interfaces.TruvideoSdkCameraInterface;

/**
 * Kotlin cannot resolve {@link TruvideoSdkCamera#getInstance()} due to conflicting Kotlin metadata /
 * facade naming on the SDK artifact; delegating through Java restores normal static interop.
 */
public final class TruvideoSdkCameraAccess {
  private TruvideoSdkCameraAccess() {}

  @NonNull
  public static TruvideoSdkCameraInterface sdk() {
    return TruvideoSdkCamera.getInstance();
  }
}
