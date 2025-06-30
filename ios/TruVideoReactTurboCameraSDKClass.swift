//
//  TruVideoReactTurboCameraSDKClass.swift
//  truvideo-react-turbo-camera-sdk
//
//  Created by mac on 27/01/2025.
//

import TruvideoSdkCamera
import Foundation
import UIKit
import React
import AVFoundation
import Combine

@objc final public class TruVideoReactCameraSdkClass: NSObject {
  
  var disposeBag = Set<AnyCancellable>()
  @objc public func initCameraScreen(jsonData: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print(jsonData)
  
    guard let data = jsonData.data(using: .utf8) else {
      print("Invalid JSON string")
      reject("Invalid_Data", "Invalid JSON string", NSError(domain: "Invalid_Data", code: 400, userInfo: nil))
      return
    }
    
    do {
      if let configuration = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
        print(configuration)
        self.cameraInitiate(configuration: configuration) { cameraResult in
          do {
            let cameraResultDict = cameraResult.toDictionary()
            if let mediaData = cameraResultDict["media"] as? [[String: Any]] {
              var sanitizedMediaData: [[String: Any]] = []
              
              for item in mediaData {
                var sanitizedItem: [String: Any] = [:]
                for (key, value) in item {
                  if key == "type" {
                    if (value as AnyObject).description == "TruvideoSdkCamera.TruvideoSdkCameraMediaType.photo"  {
                      sanitizedItem["type"] = "PICTURE"
                    } else {
                      sanitizedItem["type"] = "VIDEO"
                    }
                  }
                  if JSONSerialization.isValidJSONObject([key: value]) {
                    sanitizedItem[key] = value
                  } else if let value = value as? CustomStringConvertible {
                    sanitizedItem[key] = value.description
                  } else {
                    print("Skipping invalid JSON value for key: \(key)")
                  }
                }
                sanitizedMediaData.append(sanitizedItem)
              }
              if let jsonData = try? JSONSerialization.data(withJSONObject: sanitizedMediaData, options: []) {
                if let jsonString = String(data: jsonData, encoding: .utf8) {
                  print(jsonString)
                  resolve(jsonString)
                }
              }
            }
          } catch {
            print("Error serializing camera result: \(error.localizedDescription)")
            reject("Serialization_Error", "Error serializing camera result", error)
          }
        }
      } else {
        print("Invalid JSON format")
        reject("Invalid_JSON_Format", "Invalid JSON format", NSError(domain: "Invalid_JSON_Format", code: 400, userInfo: nil))
      }
    } catch {
      print("Error parsing JSON: \(error.localizedDescription)")
      reject("Error_parsing", "Error parsing JSON: \(error.localizedDescription)", error)
    }
  }
  
  
  @objc public func environment(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolve("true")
  }
  
  
  @objc public func isAugmentedRealityInstalled(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock){
    resolve("true")
  }
  
  @objc public func isAugmentedRealitySupported(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock){
    resolve("true")
  }
  
  
  
 
  private func cameraInitiate(configuration: [String:Any], completion: @escaping (_ cameraResult: TruvideoSdkCameraResult) -> Void) {
    DispatchQueue.main.async {
      guard let rootViewController = UIApplication.shared.keyWindow?.rootViewController else {
        print("E_NO_ROOT_VIEW_CONTROLLER", "No root view controller found")
        return
      }
      guard let lensFacingString = configuration["lensFacing"] as? String,
            let flashModeString = configuration["flashMode"] as? String,
            let orientationString = configuration["orientation"] as? String,
            let outputPath = configuration["outputPath"] as? String,
            let modeString = configuration["mode"] as? String else {
        print("Error: Missing or invalid configuration values")
        return
      }
      // Retrieving information about the device's camera functionality.
      let cameraInfo: TruvideoSdkCameraInformation = TruvideoSdkCamera.camera.getTruvideoSdkCameraInformation()
      print("Camera Info:", cameraInfo)
      
      let lensType: TruvideoSdkCameraLensFacing = lensFacingString == "back" ? .back: .front
      
      let flashMode: TruvideoSdkCameraFlashMode = flashModeString == "on" ? .on: .off
      
      let orientation: TruvideoSdkCameraOrientation
      switch orientationString {
      case "portrait":
        orientation = .portrait
      case "portraitReverse":
        orientation = .portraitReverse
      case "landscapeLeft":
        orientation = .landscapeLeft
      case "landscapeRight":
        orientation = .landscapeRight
      default:
        print("Unknown orientation:", orientationString)
        return
      }
      
      var mode: TruvideoSdkCameraMediaMode = .videoAndPicture()
      
      do {
        guard let data = modeString.data(using: .utf8) else { return }
        let modeData = try JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
        let mainMode  = modeData["mode"] as? String;
        switch mainMode {
            case "videoAndImage":
                if let videoDurationLimitStr = modeData["videoDurationLimit"] as? String,
                   let mediaLimitStr = modeData["mediaLimit"] as? String,
                   !videoDurationLimitStr.isEmpty, !mediaLimitStr.isEmpty,
                   let durationLimit = Int(videoDurationLimitStr),
                   let maxCount = Int(mediaLimitStr) {
                  mode = .videoAndPicture(mediaCount: maxCount, videoDuration: durationLimit)
                } else if let videoLimitStr = modeData["videoLimit"] as? String,
                          let imageLimitStr = modeData["imageLimit"] as? String,
                          let videoDurationLimitStr = modeData["videoDurationLimit"] as? String,
                          !videoLimitStr.isEmpty, !imageLimitStr.isEmpty, !videoDurationLimitStr.isEmpty,
                          let videoMaxCount = Int(videoLimitStr),
                          let imageMaxCount = Int(imageLimitStr),
                          let durationLimit = Int(videoDurationLimitStr) {
                    mode = .videoAndPicture(videoCount: videoMaxCount,pictureCount: imageMaxCount,videoDuration: durationLimit)
                } else if let videoDurationLimitStr = modeData["videoDurationLimit"] as? String,
                          !videoDurationLimitStr.isEmpty,
                          let durationLimit = Int(videoDurationLimitStr) {
                  mode = .videoAndPicture(videoDuration: durationLimit)
                } else {
                    mode = .videoAndPicture()
                }

            case "video":
                if let videoLimitStr = modeData["videoLimit"] as? String,
                   let videoDurationLimitStr = modeData["videoDurationLimit"] as? String,
                   !videoLimitStr.isEmpty, !videoDurationLimitStr.isEmpty,
                   let maxCount = Int(videoLimitStr),
                   let durationLimit = Int(videoDurationLimitStr) {
                    mode = .video(videoCount:  maxCount, videoDuration: durationLimit)
                } else if let videoLimitStr = modeData["videoLimit"] as? String,
                          !videoLimitStr.isEmpty,
                          let maxCount = Int(videoLimitStr) {
                    mode = .video(videoCount: maxCount)
                } else {
                    mode = .video()
                }

            case "image":
                if let imageLimitStr = modeData["imageLimit"] as? String,
                   !imageLimitStr.isEmpty,
                   let maxCount = Int(imageLimitStr) {
                    mode = .picture(pictureCount: maxCount)
                } else {
                    mode = .picture()
                }

            case "singleImage":
                mode = .singlePicture()

            case "singleVideo":
                if let videoDurationLimitStr = modeData["videoDurationLimit"] as? String,
                   !videoDurationLimitStr.isEmpty,
                   let durationLimit = Int(videoDurationLimitStr) {
                    mode = .singleVideo(videoDuration: durationLimit)
                } else {
                    mode = .singleVideo()
                }

            case "singleVideoOrImage":
                if let videoDurationLimitStr = modeData["videoDurationLimit"] as? String,
                   !videoDurationLimitStr.isEmpty,
                   let durationLimit = Int(videoDurationLimitStr) {
                    mode = .singleVideoOrPicture(videoDuration: durationLimit)
                } else {
                    mode = .singleVideoOrPicture()
                }

            default:
                break
            }
        
        
        
//        switch modeString {
//        case "picture":
//          
//          mode = .picture()
//        case "video":
//          mode = .video()
//        case "videoAndPicture":
//          mode = .videoAndPicture()
//        default:
//          print("Unknown mode:", modeString)
//          return
//        }
//        

      }catch {
        
      }
      
      // Configuring the camera with various parameters based on specific requirements.
      let configuration = TruvideoSdkCameraConfiguration(
        lensFacing: lensType,
        flashMode: flashMode,
        orientation: orientation,
        outputPath: outputPath,
        frontResolutions: [],
        frontResolution: nil,
        backResolutions: [],
        backResolution: nil,
        mode: mode
      )
      
      self.checkCameraPermissions { [weak self] granted in
        guard self != nil else { return }
        
        if granted {
          DispatchQueue.main.async {
            rootViewController.presentTruvideoSdkCameraView(
              preset: configuration,
              onComplete: { cameraResult in
                print(cameraResult.toDictionary())
                completion(cameraResult)
              }
            )
          }
        } else {
          print("Camera permission not granted")
        }
      }
    }
    
  
  }
  
  func checkCameraPermissions(completion: @escaping (Bool) -> Void) {
    let status = AVCaptureDevice.authorizationStatus(for: .video)
    switch status {
    case .authorized:
      completion(true)
    case .notDetermined:
      AVCaptureDevice.requestAccess(for: .video) { granted in
        DispatchQueue.main.async {
          completion(granted)
        }
      }
    default:
      completion(false)
    }
  }
  private func subscribeToEventsPublisher() {
           TruvideoSdkCamera
               .events
               .sink { cameraEvent in
                 print("cameraEvent: ",cameraEvent)
                 //self.sendEvent(withName: "cameraEvent", body: cameraEvent)
                 self.sendEventToReact(event: cameraEvent)
               }
               .store(in: &disposeBag)
  }
  
  
  private func sendEventToReact(event: TruvideoSdkCameraEvent) {
          // Initialize the data dictionary that will contain all the event data
          var eventData: [String: Any] = [:]
          
          // Switch through the event types and add the corresponding data
          switch event.type {
          case .truvideoSdkCameraEventFlashModeChanged(let flashMode):
              eventData["flashMode"] = convertFlashModeTOString(flashMode: flashMode)
              
          case .truvideoSdkCameraEventCameraFlipped(let lensFacing):
              eventData["lensFacing"] = convertLensTOString(lensFacing: lensFacing)
              
          case .truvideoSdkCameraEventMediaContinue(let media):
              eventData["media"] = convertMediaArrayToDictionary(mediaArray: media)
              
              
          case .truvideoSdkCameraEventMediaDeleted(let media):
              eventData["media"] = convertMediaToDictionary(media: media)
              
          case .truvideoSdkCameraEventMediaDiscard(let media):
              eventData["media"] = convertMediaArrayToDictionary(mediaArray: media)
              
          case .truvideoSdkCameraEventPictureTaken(let media):
              eventData["media"] = convertMediaToDictionary(media: media)
              
          case .truvideoSdkCameraEventRecordingFinished(let media):
              eventData["media"] = convertMediaToDictionary(media: media)
              
          case .truvideoSdkCameraEventRecordingPaused(let resolution, let orientation, let lensFacing):
              eventData["resolution"] = convertResolutionToDictionary(resolution: resolution)
              eventData["orientation"] = convertOrientationToString(orientation: orientation)
              eventData["lensFacing"] = convertLensTOString(lensFacing: lensFacing)
              
          case .truvideoSdkCameraEventRecordingResumed(let resolution, let orientation, let lensFacing):
              eventData["resolution"] = convertResolutionToDictionary(resolution: resolution)
              eventData["orientation"] = convertOrientationToString(orientation: orientation)
              eventData["lensFacing"] = convertLensTOString(lensFacing: lensFacing)
              
          case .truvideoSdkCameraEventRecordingStarted(let resolution, let orientation, let lensFacing):
              eventData["resolution"] = convertResolutionToDictionary(resolution: resolution)
              eventData["orientation"] = convertOrientationToString(orientation: orientation)
              eventData["lensFacing"] = convertLensTOString(lensFacing: lensFacing)
              
          case .truvideoSdkCameraEventResolutionChanged(let resolution):
              eventData["resolution"] = convertResolutionToDictionary(resolution: resolution)
              
          case .truvideoSdkCameraEventZoomChanged(let zoom):
              eventData["zoom"] = zoom
          @unknown default:
              eventData["empty"] = ""
          }
          // print(event)
          let eventToSend: [String: Any] = [
              "type": eventTypeToString(event.type),
              "data": eventData
          ]
          self.sendEvent(withName: "cameraEvent", body: eventToSend)
//          do {
//              if let jsonData = try? JSONSerialization.data(withJSONObject: eventToSend, options: []),
//                 let jsonString = String(data: jsonData, encoding: .utf8) {
//                self.sendEvent(withName: "cameraEvent", body: eventToSend)
//              } else {
//                  print("Failed to serialize event to JSON")
//              }
//          } catch {
//              print("Error converting object to JSON: \(error.localizedDescription)")
//          }
      }
  
  private func eventTypeToString(_ eventType: TruvideoSdkCameraEventType) -> String {
          switch eventType {
          case .truvideoSdkCameraEventFlashModeChanged(_):
              return "FlashModeChanged"
              
          case .truvideoSdkCameraEventCameraFlipped(_):
              return "CameraFlipped"
              
          case .truvideoSdkCameraEventMediaContinue(_):
              return "Continue"
              
          case .truvideoSdkCameraEventMediaDeleted(_):
              return "MediaDeleted"
              
          case .truvideoSdkCameraEventMediaDiscard(_):
              return "MediaDiscard"
              
          case .truvideoSdkCameraEventPictureTaken(_):
              return "PictureTaken"
              
          case .truvideoSdkCameraEventRecordingFinished(_):
              return "RecordingFinished"
              
          case .truvideoSdkCameraEventRecordingPaused(_, _, _):
              return "RecordingPaused"
              
          case .truvideoSdkCameraEventRecordingResumed(_, _, _):
              return "RecordingResumed"
              
          case .truvideoSdkCameraEventRecordingStarted(_, _, _):
              return "RecordingStarted"
              
          case .truvideoSdkCameraEventResolutionChanged(_):
              return "ResolutionChanged"
              
          case .truvideoSdkCameraEventZoomChanged(_):
              return "ZoomChanged"
              
          @unknown default:
              return "ZoomChanged"
          }
      }
      
  func convertLensTOString(lensFacing: TruvideoSdkCameraLensFacing) -> String{
          if lensFacing == .back{
              return "back"
          }else if lensFacing == .front{
              return "front"
          }else {
              return "back"
          }
      }
      
      func convertFlashModeTOString(flashMode: TruvideoSdkCameraFlashMode) -> String{
          if flashMode == .on{
              return "on"
          }else if flashMode == .off{
              return "off"
          }else {
              return "off"
          }
      }
      
      func convertResolutionToDictionary(resolution: TruvideoSdkCameraResolution) -> [String: Int] {
          var resolutionData : [String: Int] = [:]
          if let width = Int(resolution.width) as? Int , let height = Int(resolution.height) as? Int {
              resolutionData["width"] = width
              resolutionData["height"] = height
              return resolutionData
          }
          return resolutionData
      }
      
      func convertOrientationToString(orientation: TruvideoSdkCameraOrientation) -> String{
          switch orientation{
          case .landscapeLeft:
              return "landscapeLeft"
          case .landscapeRight:
              return "landscapeRight"
          case .portrait:
              return "portrait"
          case .portraitReverse:
              return "portraitReverse"
          @unknown default:
              return "portrait"
          }
      }
      
      func convertMediaTypeToString(type :TruvideoSdkCameraMediaType) -> String{
          switch type {
          case .clip:
              return "clip"
          case .photo:
              return "photo"
          @unknown default:
              return "default"
          }
      }
      
      func convertMediaToDictionary(media: TruvideoSdkCameraMedia) -> [String: Any] {
          print(media)
          var mediaData: [String: Any] = [:]
          if let createdAt = media.createdAt as? Double,
             let filePath = media.filePath as? String,
             let type = convertMediaTypeToString(type: media.type) as? String,
             let cameraLensFacing = convertLensTOString(lensFacing: media.cameraLensFacing) as? String,
             let rotation = convertOrientationToString(orientation: media.rotation) as? String,
             let resolution = convertResolutionToDictionary(resolution: media.resolution)as? [String:Int] ,
             let duration = Int(media.duration) as? Int {
              mediaData["createdAt"] = "\(createdAt)"
              mediaData["filePath"] = filePath
              mediaData["type"] = type
              mediaData["cameraLensFacing"] = cameraLensFacing
              mediaData["rotation"] = rotation
              mediaData["resolution"] = resolution
              mediaData["duration"] = duration
          }
          return mediaData
      }
      
      func convertMediaArrayToDictionary(mediaArray: [TruvideoSdkCameraMedia]) -> [[String: Any]] {
          return mediaArray.map { convertMediaToDictionary(media: $0) }
      }
  //self.sendEvent(withName: "onComplete", body: mainResponse)
  private func sendEvent(withName name: String, body: [String: Any]) {
      guard let bridge = RCTBridge.current() else { return }
      bridge.eventDispatcher().sendAppEvent(withName: name, body: body)
  }
  
}
extension TruvideoSdkCameraResult {
  func toDictionary() -> [String: Any] {
    return [
      "media": media.map { $0.toDictionary() }
    ]
  }
}

extension TruvideoSdkCamera.TruvideoSdkCameraMedia {
  func toDictionary() -> [String: Any] {
    return [
      "createdAt": createdAt,
      "filePath": filePath,
      "type": type,
      "cameraLensFacing": cameraLensFacing.rawValue,
      "rotation": rotation.rawValue,
      "resolution": resolution,
      "duration": duration
    ]
  }
  
  
}

extension TruvideoSdkCamera.TruvideoSdkCameraResolution {
  func toDictionary() -> [String: Any] {
    return [:]
  }
  
  func resulDict() -> [String: Any] {
    //width: Int32, height: Int32
    return [
      "width": 0,
      "height": 0
    ]
  }
}

