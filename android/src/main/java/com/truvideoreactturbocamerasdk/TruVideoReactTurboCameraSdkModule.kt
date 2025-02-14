package com.truvideoreactturbocamerasdk

import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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

  override fun initCameraScreen(configuration:String,promise: Promise){
    Log.d("initCameraScreen","initCameraScreen")
    promise2 = promise
    reactContext = reactApplicationContext
    currentActivity!!.startActivity(Intent(currentActivity, CameraActivity::class.java).putExtra("configuration",configuration))
  }

  companion object {
    const val NAME = "TruVideoReactTurboCameraSdk"
    lateinit var reactContext : ReactApplicationContext
    var promise2 : Promise? = null
  }
}
