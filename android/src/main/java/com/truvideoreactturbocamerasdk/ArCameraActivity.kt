package com.truvideoreactturbocamerasdk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.truvideo.sdk.camera.TruvideoSdkCamera
import com.truvideo.sdk.camera.model.TruvideoSdkArCameraConfiguration
import com.truvideo.sdk.camera.model.TruvideoSdkCameraMode
import com.truvideo.sdk.camera.ui.activities.arcamera.TruvideoSdkArCameraContract
import org.json.JSONObject

class ArCameraActivity : AppCompatActivity() {
    lateinit var launcher : ActivityResultLauncher<TruvideoSdkArCameraConfiguration>
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
          val gson = Gson()
          val jsonResult = gson.toJson(it)
          TruVideoReactTurboCameraSdkModule.promise2!!.resolve(jsonResult)
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
    if(jsonConfiguration.has("mode")){
      //val jsonMode = jsonConfiguration.getString("mode")

      val jsonMode = JSONObject(jsonConfiguration.getString("mode"))
      when(jsonMode.getString("mode")) {
        "videoAndImage" -> {
          if(jsonMode.getString("videoDurationLimit") != "" && jsonMode.getString("mediaLimit") != ""){
            mode = TruvideoSdkCameraMode.videoAndImage(
              durationLimit = jsonMode.getString("videoDurationLimit").toInt(),
              maxCount = jsonMode.getString("mediaLimit").toInt())
          }else if (jsonMode.getString("videoLimit") != "" && jsonMode.getString("imageLimit") != "" && jsonMode.getString("videoDurationLimit") != ""){
            mode = TruvideoSdkCameraMode.videoAndImage(
              durationLimit = jsonMode.getString("videoDurationLimit").toInt(),
              imageMaxCount = jsonMode.getString("imageLimit").toInt(),
              videoMaxCount = jsonMode.getString("videoLimit").toInt())
          }else if (jsonMode.getString("videoDurationLimit") != ""){
            mode = TruvideoSdkCameraMode.videoAndImage(durationLimit = jsonMode.getString("videoDurationLimit").toInt())
          }else{
            mode = TruvideoSdkCameraMode.videoAndImage()
          }
        }
        "video" -> {
          if(jsonMode.getString("videoLimit") != "" && jsonMode.getString("videoDurationLimit") != ""){
            mode = TruvideoSdkCameraMode.video(
              maxCount = jsonMode.getString("videoLimit").toInt(),
              durationLimit = jsonMode.getString("videoDurationLimit").toInt()
            )
          }else if (jsonMode.getString("videoLimit") != ""){
            mode = TruvideoSdkCameraMode.video(
              maxCount = jsonMode.getString("videoLimit").toInt()
            )
          }else {
            mode = TruvideoSdkCameraMode.video()
          }

        }
        "image" -> {
          if (jsonMode.getString("imageLimit") != ""){
            mode = TruvideoSdkCameraMode.image(
              maxCount = jsonMode.getString("imageLimit").toInt()
            )
          }else {
            mode = TruvideoSdkCameraMode.image()
          }
        }
        "singleImage" ->{
          mode = TruvideoSdkCameraMode.singleImage()
        }
        "singleVideo" ->{
          if (jsonMode.getString("videoDurationLimit") != ""){
            mode = TruvideoSdkCameraMode.singleVideo(durationLimit = jsonMode.getString("videoDurationLimit").toInt())
          }else {
            mode = TruvideoSdkCameraMode.singleVideo()
          }
        }
        "singleVideoOrImage" -> {
          if (jsonMode.getString("videoDurationLimit") != ""){
            mode = TruvideoSdkCameraMode.singleVideoOrImage(durationLimit = jsonMode.getString("videoDurationLimit").toInt())
          }else {
            mode = TruvideoSdkCameraMode.singleVideoOrImage()
          }
        }
      }
    }
    var configuration = TruvideoSdkArCameraConfiguration(
      outputPath = outputPath,
      mode = mode
    )
    launcher.launch(configuration)
  }
}
