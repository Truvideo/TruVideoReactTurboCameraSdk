package com.truvideoreactturbocamerasdk

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.truvideo.sdk.camera.TruvideoSdkCamera
//import com.truvideo.sdk.camera.model.TruvideoSdkCameraConfiguration
//import com.truvideo.sdk.camera.model.TruvideoSdkCameraEvent
import com.truvideo.sdk.camera.model.TruvideoSdkCameraFlashMode
import com.truvideo.sdk.camera.model.TruvideoSdkCameraImageFormat
import com.truvideo.sdk.camera.model.TruvideoSdkCameraLensFacing
//import com.truvideo.sdk.camera.model.TruvideoSdkCameraMode
import com.truvideo.sdk.camera.model.TruvideoSdkCameraOrientation
import com.truvideo.sdk.camera.model.TruvideoSdkCameraResolution
import com.truvideo.sdk.camera.model.external.TruvideoSdkCameraConfiguration
import com.truvideo.sdk.camera.model.external.TruvideoSdkCameraMode
import com.truvideo.sdk.camera.ui.activities.camera.TruvideoSdkCameraContract
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CameraActivity : AppCompatActivity() {
  var configuration = ""
  var lensFacing = TruvideoSdkCameraLensFacing.BACK
  var flashMode = TruvideoSdkCameraFlashMode.OFF
  var imageFormat = TruvideoSdkCameraImageFormat.JPEG
  var videoStabilizationEnabled = true
  var orientation: TruvideoSdkCameraOrientation? = null
  var mode = TruvideoSdkCameraMode.VideoAndImage()
  var frontResolutions : List<TruvideoSdkCameraResolution> = listOf()
  var frontResolution : TruvideoSdkCameraResolution? = null
  var backResolutions : List<TruvideoSdkCameraResolution> = listOf()
  var backResolution : TruvideoSdkCameraResolution? = null
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
          put("createdAt", media.createdAt)
          put("filePath", media.filePath)
          put("type", media.type.name)          // enum as string
          put("lensFacing", media.lensFacing.name)
          put("orientation",media.orientation.name)
          put("resolution", resolutionObj)
          put("duration", media.duration)
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
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        TruvideoSdkCamera.events.collect { event ->
          val gson = Gson()
          val eventData = mapOf(
            "type" to event.eventType.name,
            "data" to event.data,
          )
          val jsonResult = gson.toJson(eventData)
          sendEvent(reactContext = TruVideoReactTurboCameraSdkModule.reactContext,eventName = "cameraEvent",event = jsonResult.toString())
        }
      }
    }

//    TruvideoSdkCamera.events.observeForever{event : TruvideoSdkCameraEvent ->
//      val obj = JSONObject().apply {
//        put("data", event.data)
//        put("type",event.type.name)
//      }
//      sendEvent(reactContext = TruVideoReactTurboCameraSdkModule.reactContext,eventName = "cameraEvent",event = obj.toString())
//    }
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
    var outputPath = context.filesDir.path + "/camera"
    val jsonConfiguration = JSONObject(configuration)
    if(jsonConfiguration.has("outputPath")){
      val newOutputPath = jsonConfiguration.getString("outputPath")
      if(newOutputPath.isNotEmpty()){
        outputPath = context.filesDir.path + newOutputPath
      }
    }
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

  // Single Resolution Parser
  fun parseResolution(obj: JSONObject): TruvideoSdkCameraResolution {
    val width = obj.optInt("width", 0)
    val height = obj.optInt("height", 0)
    return TruvideoSdkCameraResolution(width, height) // Assume Resolution(width, height) is your model
  }

  // Array of Resolutions
  fun parseResolutions(array: JSONArray): List<TruvideoSdkCameraResolution> {
    val list = mutableListOf<TruvideoSdkCameraResolution>()
    for (i in 0 until array.length()) {
      val resObj = array.getJSONObject(i)
      list.add(parseResolution(resObj))
    }
    return list
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

  // Front Resolutions
    if (jsonConfiguration.has("frontResolutions") && jsonConfiguration.getString("frontResolutions") != "") {
      frontResolutions = parseResolutions(jsonConfiguration.getJSONArray("frontResolutions"))
    }
    if (jsonConfiguration.has("frontResolution") && jsonConfiguration.getString("frontResolution") != "") {
      frontResolution = parseResolution(jsonConfiguration.getJSONObject("frontResolution"))
    }

  // Back Resolutions
    if (jsonConfiguration.has("backResolutions") && jsonConfiguration.getString("backResolutions") != "") {
      backResolutions = parseResolutions(jsonConfiguration.getJSONArray("backResolutions"))
    }
    if (jsonConfiguration.has("backResolution") && jsonConfiguration.getString("backResolution") != "") {
      backResolution = parseResolution(jsonConfiguration.getJSONObject("backResolution"))
    }


    if(jsonConfiguration.has("mode")){
      fun String?.intOrNull() = this?.takeIf { it.isNotEmpty() }?.toInt()
      fun String?.longOrNull() = this?.takeIf { it.isNotEmpty() }?.toLong()

      val jsonMode = JSONObject(jsonConfiguration.getString("mode"))
      val videoDurationLimit : Long? = if(jsonMode.getString("videoDurationLimit") != "" ) jsonMode.getString("videoDurationLimit").toLongOrNull() else null
      val mediaLimit : Int? = if(jsonMode.getString("mediaLimit") != "" ) jsonMode.getString("mediaLimit").intOrNull() else null
      val videoLimit : Int? = if(jsonMode.getString("videoLimit") != "" ) jsonMode.getString("videoLimit").intOrNull() else null
      val imageLimit : Int? = if(jsonMode.getString("imageLimit") != "" ) jsonMode.getString("imageLimit").intOrNull() else null

      when(jsonMode.getString("mode")) {

        "videoAndImage" -> when {
          videoDurationLimit != null && mediaLimit != null ->
            TruvideoSdkCameraMode.VideoAndImage(
              limit = TruvideoSdkCameraMode.VideoAndImage.Limit.ByTotal(
                maxMediaCount = mediaLimit
              ),
              videoDurationLimit = videoDurationLimit
            )

          videoDurationLimit != null && videoLimit != null && imageLimit != null ->
            TruvideoSdkCameraMode.VideoAndImage(
              limit = TruvideoSdkCameraMode.VideoAndImage.Limit.ByType(
                maxImageCount = imageLimit,
                maxVideoCount = videoLimit
              ),
              videoDurationLimit = videoDurationLimit
            )

          videoDurationLimit != null ->
            TruvideoSdkCameraMode.VideoAndImage(
              videoDurationLimit = videoDurationLimit
            )

          else -> TruvideoSdkCameraMode.VideoAndImage()
        }

        "video" -> TruvideoSdkCameraMode.Video(
          maxCount = videoLimit,
          durationLimit = videoDurationLimit
        )

        "image" -> TruvideoSdkCameraMode.Image(
          maxCount = imageLimit
        )

        "singleImage" ->
          TruvideoSdkCameraMode.SingleImage(autoClose = true)

        "singleVideo" ->
          TruvideoSdkCameraMode.SingleVideo(
            durationLimit = videoDurationLimit,
            autoClose = true
          )

        "singleVideoOrImage" ->
          TruvideoSdkCameraMode.SingleVideoOrImage(
            videoDurationLimit = videoDurationLimit,
            autoClose = true
          )

        else -> mode
      }

//      when(jsonMode.getString("mode")) {
//        "videoAndImage" -> {
//          if(imageLimit != null || videoLimit != null){
//            mode = TruvideoSdkCameraMode.videoAndImage(
//              imageMaxCount = imageLimit?.toInt(),
//              videoMaxCount = videoLimit?.toInt(),
//              durationLimit = videoDurationLimit?.toInt()
//            )
//          }else if(mediaLimit != null){
//            mode = TruvideoSdkCameraMode.videoAndImage(
//              maxCount = mediaLimit.toInt(),
//              durationLimit = videoDurationLimit?.toInt()
//            )
//          }else {
//            mode = TruvideoSdkCameraMode.videoAndImage()
//          }
//        }
//        "video" -> {
//          mode = TruvideoSdkCameraMode.video(
//            maxCount = videoLimit?.toInt(),
//            durationLimit = videoDurationLimit?.toInt()
//          )
//        }
//        "image" -> {
//          mode = TruvideoSdkCameraMode.image(
//            maxCount = imageLimit?.toInt()
//          )
//        }
//        "singleImage" ->{
//          mode = TruvideoSdkCameraMode.singleImage()
//        }
//        "singleVideo" ->{
//          mode = TruvideoSdkCameraMode.singleVideo(
//            durationLimit = videoDurationLimit?.toInt()
//          )
//        }
//        "singleVideoOrImage" -> {
//          mode = TruvideoSdkCameraMode.singleVideoOrImage(
//            durationLimit = videoDurationLimit?.toInt()
//          )
//        }
//      }
    }
  }

}
