package com.truvideoreactturbocamerasdk

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import com.truvideo.sdk.camera.TruvideoSdkCamera
import com.truvideo.sdk.camera.model.TruvideoSdkCameraConfiguration
import com.truvideo.sdk.camera.model.TruvideoSdkCameraEvent
import com.truvideo.sdk.camera.model.TruvideoSdkCameraFlashMode
import com.truvideo.sdk.camera.model.TruvideoSdkCameraLensFacing
import com.truvideo.sdk.camera.model.TruvideoSdkCameraMode
import com.truvideo.sdk.camera.model.TruvideoSdkCameraOrientation
import com.truvideo.sdk.camera.model.TruvideoSdkCameraResolution
import com.truvideo.sdk.camera.ui.activities.camera.TruvideoSdkCameraContract
import org.json.JSONObject

class CameraActivity : AppCompatActivity() {
  var configuration = ""
  var lensFacing = TruvideoSdkCameraLensFacing.BACK
  var flashMode = TruvideoSdkCameraFlashMode.OFF
  var orientation: TruvideoSdkCameraOrientation? = null
  var mode = TruvideoSdkCameraMode.videoAndImage()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_camera)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    getEvent()
    getIntentData()
    startCamera()
  }
  fun startCamera(){
    val cameraScreen = registerForActivityResult(TruvideoSdkCameraContract()){
      // result
      val gson = Gson()
      val jsonResult = gson.toJson(it)
      TruVideoReactTurboCameraSdkModule.promise2!!.resolve(jsonResult)
      finish()
    }
    openCamera(this@CameraActivity,cameraScreen)
  }
  fun getEvent(){
    TruvideoSdkCamera.events.observeForever{event : TruvideoSdkCameraEvent ->
      val gson = Gson()
      val jsonResult = gson.toJson(event)
      sendEvent(reactContext = TruVideoReactTurboCameraSdkModule.reactContext,eventName = "cameraEvent",event = jsonResult.toString())
    }
  }
  fun sendEvent(reactContext: ReactApplicationContext, eventName: String, event: String) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, event)
  }
  fun getIntentData(){
    configuration = intent.getStringExtra("configuration")!!
  }
  private fun openCamera(context: Context, cameraScreen: ActivityResultLauncher<TruvideoSdkCameraConfiguration>?) {
    // Start camera with configuration
    // if camera is not available, it will return null
    if (cameraScreen == null) return
    // Get camera information
    val cameraInfo = TruvideoSdkCamera.getInformation()

    var outputPath = context.filesDir.path + "/camera"
    val jsonConfiguration = JSONObject(configuration)
    if(jsonConfiguration.has("outputPath")){
      val newOutputPath = jsonConfiguration.getString("outputPath")
      if(newOutputPath.isNotEmpty()){
        outputPath = newOutputPath
      }
    }
    var frontResolutions: List<TruvideoSdkCameraResolution> = ArrayList()
    if (cameraInfo.frontCamera != null) {
      // if you don't want to decide the list of allowed resolutions, you can s1end all the resolutions or an empty list
      frontResolutions = cameraInfo.frontCamera!!.resolutions
    }


    // You can decide the default resolution for the front camera
    var frontResolution: TruvideoSdkCameraResolution? = null
    if (cameraInfo.frontCamera != null) {
      // Example of how tho pick the first resolution as the default one
      val resolutions = cameraInfo.frontCamera!!.resolutions
      if (resolutions.isNotEmpty()) {
        frontResolution = resolutions[0]
      }
    }
    val backResolutions: List<TruvideoSdkCameraResolution> = ArrayList()
    val backResolution: TruvideoSdkCameraResolution? = null
    checkConfigure()
    val configuration = TruvideoSdkCameraConfiguration(
      lensFacing = lensFacing,
      flashMode = flashMode,
      orientation = orientation,
      outputPath = outputPath,
      frontResolutions = frontResolutions,
      frontResolution = frontResolution,
      backResolutions = backResolutions,
      backResolution = backResolution,
      mode = mode
    )

    cameraScreen.launch(configuration)

  }

  private fun checkConfigure() {
    val jsonConfiguration = JSONObject(configuration)
    if (jsonConfiguration.has("lensFacing")) {
      when (jsonConfiguration.getString("lensFacing")) {
        "back" -> lensFacing = TruvideoSdkCameraLensFacing.BACK
        "front" -> lensFacing = TruvideoSdkCameraLensFacing.FRONT
      }
    }
    if(jsonConfiguration.has("flashMode")) {
      when (jsonConfiguration.getString("flashMode")) {
        "on" -> flashMode = TruvideoSdkCameraFlashMode.ON
        "off" -> flashMode = TruvideoSdkCameraFlashMode.OFF
      }
    }
    if(jsonConfiguration.has("orientation")) {
      when(jsonConfiguration.getString("orientation")){
        "portrait" -> orientation = TruvideoSdkCameraOrientation.PORTRAIT
        "landscapeLeft" -> orientation = TruvideoSdkCameraOrientation.LANDSCAPE_LEFT
        "landscapeRight" -> orientation = TruvideoSdkCameraOrientation.LANDSCAPE_RIGHT
        "portraitReverse" -> orientation = TruvideoSdkCameraOrientation.PORTRAIT_REVERSE
      }
    }
    if(jsonConfiguration.has("mode")){
      val jsonMode = jsonConfiguration.getJSONObject("mode")

      when(jsonMode.getString("mode")) {
        "videoAndImage" -> {
          if(jsonMode.getString("durationLimit") != "" && jsonMode.getString("maxCount") != ""){
            mode = TruvideoSdkCameraMode.videoAndImage(
              durationLimit = jsonMode.getString("durationLimit").toInt(),
              maxCount = jsonMode.getString("maxCount").toInt())
          }else if (jsonMode.getString("videoMaxCount") != "" && jsonMode.getString("imageMaxCount") != "" && jsonMode.getString("durationLimit") != ""){
            mode = TruvideoSdkCameraMode.videoAndImage(
              durationLimit = jsonMode.getString("durationLimit").toInt(),
              imageMaxCount = jsonMode.getString("imageMaxCount").toInt(),
              videoMaxCount = jsonMode.getString("videoMaxCount").toInt())
          }else if (jsonMode.getString("durationLimit") != ""){
            mode = TruvideoSdkCameraMode.videoAndImage(durationLimit = jsonMode.getString("durationLimit").toInt())
          }else{
            mode = TruvideoSdkCameraMode.videoAndImage()
          }
        }
        "video" -> {
          if(jsonMode.getString("videoMaxCount") != "" && jsonMode.getString("durationLimit") != ""){
            mode = TruvideoSdkCameraMode.video(
              maxCount = jsonMode.getString("videoMaxCount").toInt(),
              durationLimit = jsonMode.getString("durationLimit").toInt()
            )
          }else if (jsonMode.getString("videoMaxCount") != ""){
            mode = TruvideoSdkCameraMode.video(
              maxCount = jsonMode.getString("videoMaxCount").toInt()
            )
          }else {
            mode = TruvideoSdkCameraMode.video()
          }

        }
        "image" -> {
          if (jsonMode.getString("imageMaxCount") != ""){
            mode = TruvideoSdkCameraMode.image(
              maxCount = jsonMode.getString("imageMaxCount").toInt()
            )
          }else {
            mode = TruvideoSdkCameraMode.image()
          }
        }
      }
    }
  }

}
