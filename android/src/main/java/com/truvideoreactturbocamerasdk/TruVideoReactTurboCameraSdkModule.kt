package com.truvideoreactturbocamerasdk

import android.content.Intent
import android.util.Log
import java.io.IOException
import java.util.Properties
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.truvideo.sdk.camera.model.TruvideoSdkCameraInformation
@ReactModule(name = TruVideoReactTurboCameraSdkModule.NAME)
class TruVideoReactTurboCameraSdkModule(reactContext: ReactApplicationContext) :
  NativeTruVideoReactTurboCameraSdkSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  override fun version(promise: Promise) {
    promise.resolve(readSdkCameraManifestProperty("versionName").orEmpty())
  }

  override fun environment(promise: Promise) {
    promise.resolve(readSdkCameraManifestProperty("environment").orEmpty())
  }

  override fun isAugmentedRealityInstalled(promise: Promise) {
    promise.resolve(TruvideoSdkCameraAccess.sdk().isAugmentedRealityInstalled)
  }

  override fun isAugmentedRealitySupported(promise: Promise) {
    promise.resolve(TruvideoSdkCameraAccess.sdk().isAugmentedRealitySupported)
  }

  override fun requestInstallAugmentedReality(promise: Promise?) {
    val activity = reactApplicationContext.currentActivity
    if (activity == null) {
      promise?.reject("E_ACTIVITY_NULL", "Current activity is null")
      return
    }
    TruvideoSdkCameraAccess.sdk().requestInstallAugmentedReality(activity)
    promise?.resolve(true)
  }

  private fun readSdkCameraManifestProperty(key: String): String? =
    try {
      Properties().apply {
        reactApplicationContext.assets.open(VERSION_CAMERA_ASSET).use { load(it) }
      }.getProperty(key)
    } catch (_: IOException) {
      null
    }


  override fun initCameraScreen(configuration:String,promise: Promise){
    Log.d("initCameraScreen","initCameraScreen")
    promise2 = promise
    reactContext = reactApplicationContext
    Log.d("initCameraScreen", configuration)
    val activity = reactContext.currentActivity
    if (activity == null) {
      promise.reject("E_ACTIVITY_NULL", "Current activity is null")
      return
    }
    activity.startActivity(
      Intent(activity, CameraActivity::class.java).putExtra("configuration",configuration)
    )
  }

  override fun initARCameraScreen(configuration:String,promise: Promise){
    Log.d("initCameraScreen","initCameraScreen")
    promise2 = promise
    reactContext = reactApplicationContext
    Log.d("initCameraScreen", configuration)
    val activity = reactContext.currentActivity
    if (activity == null) {
      promise.reject("E_ACTIVITY_NULL", "Current activity is null")
      return
    }
    activity.startActivity(
      Intent(activity, ArCameraActivity::class.java).putExtra("configuration",configuration)
    )
  }

  override fun initScanerScreen(configuration:String,promise: Promise){
    Log.d("initCameraScreen","initCameraScreen")
    promise2 = promise
    reactContext = reactApplicationContext
    val activity = reactContext.currentActivity
    if (activity == null) {
      promise.reject("E_ACTIVITY_NULL", "Current activity is null")
      return
    }
    activity.startActivity(Intent(activity, ScannerActivity::class.java))
  }

  override fun getCameraInformation(promise: Promise) {
    try {
      val payload = readCameraInformationJson()
      promise.resolve(payload)
    } catch (e: Exception) {
      promise.reject(
        "CAMERA_ERROR",
        e.message ?: "Failed to get camera information",
        e
      )
    }
  }

  /**
   * Kotlin SDK models expose [TruvideoSdkCameraInformation.toJson]; [JSONObject.wrap] returns empty objects.
   */
  private fun readCameraInformationJson(): String {
    val info = readCameraInformationCompat() ?: return "{}"
    if (info is String) {
      return info.ifBlank { "{}" }
    }
    if (info is TruvideoSdkCameraInformation) {
      return info.toJson()
    }
    runCatching {
      val toJson = info.javaClass.methods.firstOrNull { it.name == "toJson" && it.parameterCount == 0 }
      val json = toJson?.invoke(info) as? String
      if (!json.isNullOrBlank()) {
        return json
      }
    }
    throw IllegalStateException("Camera information could not be serialized to JSON")
  }

  private fun readCameraInformationCompat(): Any? {
    val sdk = TruvideoSdkCameraAccess.sdk()

    runCatching {
      return sdk.getInformation()
    }

    val methodCandidates = listOf(
      "getInformation",
      "information",
      "getCameraInformation",
      "cameraInformation"
    )
    for (methodName in methodCandidates) {
      runCatching {
        val method = sdk.javaClass.methods.firstOrNull { it.name == methodName && it.parameterCount == 0 }
        method?.invoke(sdk)
      }.getOrNull()?.let { return it }
    }

    val fieldCandidates = listOf("information", "cameraInformation")
    for (fieldName in fieldCandidates) {
      runCatching {
        val field = sdk.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.get(sdk)
      }.getOrNull()?.let { return it }
    }

    throw IllegalStateException("Camera information API is not available in current SDK version")
  }

  companion object {
    private const val VERSION_CAMERA_ASSET = "version-camera.properties"

    const val NAME = "TruVideoReactTurboCameraSdk"
    lateinit var reactContext : ReactApplicationContext
    var promise2 : Promise? = null
  }
}
