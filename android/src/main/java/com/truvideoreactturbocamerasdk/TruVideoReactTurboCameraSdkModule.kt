package com.truvideoreactturbocamerasdk

import android.content.Intent
import android.util.Log
import java.io.IOException
import java.util.Properties
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
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

  companion object {
    private const val VERSION_CAMERA_ASSET = "version-camera.properties"

    const val NAME = "TruVideoReactTurboCameraSdk"
    lateinit var reactContext : ReactApplicationContext
    var promise2 : Promise? = null
  }
}
