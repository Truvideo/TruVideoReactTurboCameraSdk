package com.truvideoreactturbocamerasdk

import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.truvideo.sdk.camera.TruvideoSdkCamera
import com.truvideo.sdk.camera.model.TruvideoSdkCameraMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.json.JSONObject

@ReactModule(name = TruVideoReactTurboCameraSdkModule.NAME)
class TruVideoReactTurboCameraSdkModule(reactContext: ReactApplicationContext) :
  NativeTruVideoReactTurboCameraSdkSpec(reactContext) {
  private val scope = CoroutineScope(Dispatchers.Main)

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  override fun version(promise: Promise){
     promise.resolve(TruvideoSdkCamera.version)
  }

  override fun environment(promise: Promise){
    promise.resolve(TruvideoSdkCamera.environment)
  }

  override fun isAugmentedRealityInstalled(promise: Promise){
    promise.resolve(TruvideoSdkCamera.isAugmentedRealityInstalled)
  }

  override fun isAugmentedRealitySupported(promise: Promise){
    promise.resolve(TruvideoSdkCamera.isAugmentedRealitySupported)
  }

  override fun requestInstallAugmentedReality(promise: Promise?) {
    TruvideoSdkCamera.requestInstallAugmentedReality(reactContext.currentActivity!!)
    promise?.resolve(true)
  }


  override fun initCameraScreen(configuration:String,promise: Promise){
    Log.d("initCameraScreen","initCameraScreen")
    promise2 = promise
    reactContext = reactApplicationContext
    Log.d("initCameraScreen","$configuration")
    currentActivity!!.startActivity(Intent(currentActivity, CameraActivity::class.java).putExtra("configuration",configuration))
  }

  override fun initARCameraScreen(configuration:String,promise: Promise){
    Log.d("initCameraScreen","initCameraScreen")
    promise2 = promise
    reactContext = reactApplicationContext
    Log.d("initCameraScreen","$configuration")
    currentActivity!!.startActivity(Intent(currentActivity, CameraActivity::class.java).putExtra("configuration",configuration))
  }

  override fun initScanerScreen(configuration:String,promise: Promise){
    Log.d("initCameraScreen","initCameraScreen")
    promise2 = promise
    reactContext = reactApplicationContext
    Log.d("initCameraScreen","$configuration")
    currentActivity!!.startActivity(Intent(currentActivity, CameraActivity::class.java).putExtra("configuration",configuration))
  }

  companion object {
    const val NAME = "TruVideoReactTurboCameraSdk"
    lateinit var reactContext : ReactApplicationContext
    var promise2 : Promise? = null
    var type : TruvideoSdkCameraMode? = null
  }
}
