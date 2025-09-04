package com.truvideoreactturbocamerasdk

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.truvideo.sdk.camera.TruvideoSdkCamera
import com.truvideo.sdk.camera.model.TruvideoSdkCameraConfiguration
import com.truvideo.sdk.camera.model.TruvideoSdkCameraEvent
import com.truvideo.sdk.camera.model.TruvideoSdkCameraFlashMode
import com.truvideo.sdk.camera.model.TruvideoSdkCameraImageFormat
import com.truvideo.sdk.camera.model.TruvideoSdkCameraLensFacing
import com.truvideo.sdk.camera.model.TruvideoSdkCameraMode
import com.truvideo.sdk.camera.model.TruvideoSdkCameraOrientation
import com.truvideo.sdk.camera.model.TruvideoSdkCameraResolution
import com.truvideo.sdk.camera.ui.activities.camera.TruvideoSdkCameraContract
import org.json.JSONArray
import org.json.JSONObject

class CameraActivity : AppCompatActivity() {
  var configuration = ""
  var lensFacing = TruvideoSdkCameraLensFacing.BACK
  var flashMode = TruvideoSdkCameraFlashMode.OFF
  var imageFormat = TruvideoSdkCameraImageFormat.JPEG
  var videoStabilizationEnabled = true
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
      //val jsonArray = Json.encodeToString(ListSerializer(TruvideoSdkCameraMedia.serializer()),it)
      val jsonArray = JSONArray()
      it.forEach { media ->
        val resolutionObj = JSONObject().apply {
          put("width", media.resolution.width)
          put("height", media.resolution.height)
        }
        val obj = JSONObject().apply {
          put("id", media.id)
          put("createdAt", (media.createdAt/1000))
          put("filePath", media.filePath)
          put("type", media.type.name)          // enum as string
          put("lensFacing", media.lensFacing.name)
          put("orientation",media.orientation.name)
          put("resolution", resolutionObj)
          put("duration", (media.duration/1000))
        }
        jsonArray.put(obj)
      }
      TruVideoReactTurboCameraSdkModule.promise2!!.resolve(jsonArray.toString())
      finish()
    }
    try{
      openCamera(this@CameraActivity,cameraScreen)
    }catch (e : Exception){
      TruVideoReactTurboCameraSdkModule.promise2!!.reject("Exception",e.message)
      finish()
    }
  }
  fun getEvent(){
    TruvideoSdkCamera.events.observeForever{event : TruvideoSdkCameraEvent ->
      val obj = JSONObject().apply {
        put("data", event.data)
        put("type",event.type.name)
      }
      sendEvent(reactContext = TruVideoReactTurboCameraSdkModule.reactContext,eventName = "cameraEvent",event = obj.toString())
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
      mode = mode,
      imageFormat = imageFormat,
      videoStabilizationEnabled = videoStabilizationEnabled
    )

    cameraScreen.launch(configuration)

  }

  private fun checkConfigure() {
    val jsonConfiguration = JSONObject(configuration)
    if (jsonConfiguration.has("lensFacing")) {
      when (jsonConfiguration.getString("lensFacing")) {
        "BACK" -> lensFacing = TruvideoSdkCameraLensFacing.BACK
        "FRONT" -> lensFacing = TruvideoSdkCameraLensFacing.FRONT
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
        "PORTRAIT" -> orientation = TruvideoSdkCameraOrientation.PORTRAIT
        "LANDSCAPE_LEFT" -> orientation = TruvideoSdkCameraOrientation.LANDSCAPE_LEFT
        "LANDSCAPE_RIGHT" -> orientation = TruvideoSdkCameraOrientation.LANDSCAPE_RIGHT
        "PORTRAIT_REVERSE" -> orientation = TruvideoSdkCameraOrientation.PORTRAIT_REVERSE
      }
    }

    if(jsonConfiguration.has("imageFormat")) {
      when(jsonConfiguration.getString("imageFormat")){
        "jpeg" -> imageFormat = TruvideoSdkCameraImageFormat.JPEG
        "png" -> imageFormat = TruvideoSdkCameraImageFormat.PNG
      }
    }

    if(jsonConfiguration.has("videoStabilizationEnabled")) {
      when(jsonConfiguration.getString("videoStabilizationEnabled")){
        "true" -> videoStabilizationEnabled = true
        "false" -> videoStabilizationEnabled = false
      }
    }

    if(jsonConfiguration.has("mode")){
      val jsonMode = JSONObject(jsonConfiguration.getString("mode"))
      val videoDurationLimit : String? = if(jsonMode.getString("videoDurationLimit") != "" ) jsonMode.getString("videoDurationLimit") else null
      val mediaLimit : String? = if(jsonMode.getString("mediaLimit") != "" ) jsonMode.getString("mediaLimit") else null
      val videoLimit : String? = if(jsonMode.getString("videoLimit") != "" ) jsonMode.getString("videoLimit") else null
      val imageLimit : String? = if(jsonMode.getString("imageLimit") != "" ) jsonMode.getString("imageLimit") else null
      when(jsonMode.getString("mode")) {
        "videoAndImage" -> {
          if(imageLimit != null || videoLimit != null){
            mode = TruvideoSdkCameraMode.videoAndImage(
              imageMaxCount = imageLimit?.toInt(),
              videoMaxCount = videoLimit?.toInt(),
              durationLimit = videoDurationLimit?.toDouble()
            )
          }else if(mediaLimit != null){
            mode = TruvideoSdkCameraMode.videoAndImage(
              maxCount = mediaLimit.toInt(),
              durationLimit = videoDurationLimit?.toDouble()
            )
          }else {
            mode = TruvideoSdkCameraMode.videoAndImage()
          }
        }
        "video" -> {
          mode = TruvideoSdkCameraMode.video(
            maxCount = videoLimit?.toInt(),
            durationLimit = videoDurationLimit?.toDouble()
          )
        }
        "image" -> {
          mode = TruvideoSdkCameraMode.image(
            maxCount = imageLimit?.toInt()
          )
        }
        "singleImage" ->{
          mode = TruvideoSdkCameraMode.singleImage()
        }
        "singleVideo" ->{
          mode = TruvideoSdkCameraMode.singleVideo(
            durationLimit = videoDurationLimit?.toDouble()
          )
        }
        "singleVideoOrImage" -> {
          mode = TruvideoSdkCameraMode.singleVideoOrImage(
            durationLimit = videoDurationLimit?.toDouble()
          )
        }
      }
    }
  }

}
