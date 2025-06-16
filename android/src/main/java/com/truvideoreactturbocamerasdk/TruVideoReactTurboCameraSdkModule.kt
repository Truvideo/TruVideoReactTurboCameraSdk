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

  override fun videoAndImage(jsonString: String, promise: Promise){
    val jsonObject = JSONObject(jsonString)
    if(jsonObject.has("videoMaxCount") && jsonObject.has("imageMaxCount") && jsonObject.has("durationLimit")){
        val videoMaxCount = jsonObject.getInt("videoMaxCount")
        val imageMaxCount = jsonObject.getInt("imageMaxCount")
        val durationLimit = jsonObject.getInt("durationLimit")
        promise.resolve(TruvideoSdkCameraMode.videoAndImage(videoMaxCount.toInt(),imageMaxCount.toInt(),durationLimit.toInt()).toJson())
    }else if(jsonObject.has("maxCount") && jsonObject.has("durationLimit")){
        val maxCount = jsonObject.getInt("maxCount")
        val durationLimit = jsonObject.getInt("durationLimit")
      promise.resolve(TruvideoSdkCameraMode.videoAndImage(maxCount.toInt(),durationLimit.toInt()).toJson())
    }else if (jsonObject.has("durationLimit")){
      val durationLimit = jsonObject.getInt("durationLimit")
      promise.resolve(TruvideoSdkCameraMode.videoAndImage(durationLimit.toInt()).toJson())
    }else {
      promise.resolve(TruvideoSdkCameraMode.videoAndImage().toJson())
    }
  }

  override fun singleVideo(videoDuration: String?, promise: Promise?) {
    if(videoDuration == ""){
      promise!!.resolve(TruvideoSdkCameraMode.singleVideo().toJson())
    }else{
      promise!!.resolve(TruvideoSdkCameraMode.singleVideo(videoDuration!!.toInt()).toJson())
    }
  }


  override fun singleImage(promise: Promise) {
    promise.resolve(TruvideoSdkCameraMode.singleImage().toJson())
  }

  override fun singleVideoOrImage(durationLimit: String?, promise: Promise?) {
    if(durationLimit == ""){
      promise!!.resolve(TruvideoSdkCameraMode.singleVideoOrImage().toJson())
    }else{
      promise!!.resolve(TruvideoSdkCameraMode.singleVideoOrImage(durationLimit!!.toInt()).toJson())
    }
  }

  override fun video(maxCount: String?, durationLimit: String?, promise: Promise?) {
    if(durationLimit == ""){
      if(maxCount == ""){
        promise!!.resolve(TruvideoSdkCameraMode.video().toJson())
      }else{
        promise!!.resolve(TruvideoSdkCameraMode.video(maxCount = maxCount!!.toInt()).toJson())
      }
    }else if (maxCount == ""){
      promise!!.resolve(TruvideoSdkCameraMode.video(durationLimit = durationLimit!!.toInt()).toJson())
    }else {
      promise!!.resolve(TruvideoSdkCameraMode.video(maxCount = maxCount!!.toInt(),durationLimit = durationLimit!!.toInt()).toJson())
    }
  }

  override fun image(maxCount: String?, promise: Promise?) {
    if(maxCount == ""){
      promise!!.resolve(TruvideoSdkCameraMode.image().toJson())
    }else {
      promise!!.resolve(TruvideoSdkCameraMode.image(maxCount = maxCount!!.toInt()).toJson())
    }
  }


  override fun initCameraScreen(configuration:String,promise: Promise){
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
