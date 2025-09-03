package com.truvideoreactturbocamerasdk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.truvideo.sdk.camera.TruvideoSdkCamera
import com.truvideo.sdk.camera.model.TruvideoSdkArCameraConfiguration
import com.truvideo.sdk.camera.model.TruvideoSdkCameraMode
import com.truvideo.sdk.camera.model.TruvideoSdkCameraOrientation
import com.truvideo.sdk.camera.ui.activities.arcamera.TruvideoSdkArCameraContract
import org.json.JSONArray
import org.json.JSONObject

class ArCameraActivity : AppCompatActivity() {
    lateinit var launcher : ActivityResultLauncher<TruvideoSdkArCameraConfiguration>
    var orientation: TruvideoSdkCameraOrientation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val configuration = intent.getStringExtra("configuration")!!
        launcher = registerForActivityResult(TruvideoSdkArCameraContract()){
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
              put("lensFacing", media.lensFacing.name.lowercase())
              put("orientation", when(media.orientation){
                TruvideoSdkCameraOrientation.PORTRAIT -> "portrait"
                TruvideoSdkCameraOrientation.LANDSCAPE_LEFT -> "landscapeLeft"
                TruvideoSdkCameraOrientation.LANDSCAPE_RIGHT -> "landscapeRight"
                TruvideoSdkCameraOrientation.PORTRAIT_REVERSE -> "portraitReverse"
              })
              put("resolution", resolutionObj)
              put("duration", media.duration)
            }
            jsonArray.put(obj)
          }
          TruVideoReactTurboCameraSdkModule.promise2!!.resolve(jsonArray.toString())
          finish()
        }
        try {
            if(TruvideoSdkCamera.isAugmentedRealityInstalled && TruvideoSdkCamera.isAugmentedRealitySupported){
                openArCamera(configuration)
            }else if(TruvideoSdkCamera.isAugmentedRealitySupported){
              TruVideoReactTurboCameraSdkModule.promise2!!.reject("Exception","Ar Not Supported in Device")
            }else{
              TruVideoReactTurboCameraSdkModule.promise2!!.reject("Exception","Ar Core App not Installed")
            }
        }catch (e : Exception){
          TruVideoReactTurboCameraSdkModule.promise2!!.reject("Exception",e.message)
          finish()
        }

    }
  fun openArCamera(configuration: String){
    val jsonConfiguration = JSONObject(configuration)
    var mode = TruvideoSdkCameraMode.videoAndImage()
    var outputPath = filesDir.path + "/camera"
    if(jsonConfiguration.has("outputPath")){
      val newOutputPath = jsonConfiguration.getString("outputPath")
      if(newOutputPath.isNotEmpty()){
        outputPath = newOutputPath
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
    val configuration = TruvideoSdkArCameraConfiguration(
      orientation = orientation,
      outputPath = outputPath,
      mode = mode
    )
    launcher.launch(configuration)
  }
}
