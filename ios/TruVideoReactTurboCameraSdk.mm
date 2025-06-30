#import "TruVideoReactTurboCameraSdk.h"
#import <React/RCTBridgeModule.h>
#import "truvideo_react_turbo_camera_sdk-Swift.h"

@implementation TruVideoReactTurboCameraSdk
RCT_EXPORT_MODULE()

- (NSNumber *)multiply:(double)a b:(double)b {
    NSNumber *result = @(a * b);

    return result;
}

- (void)initCameraScreen:(NSString *)configuration resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  TruVideoReactCameraSdkClass *truvideo = [[TruVideoReactCameraSdkClass alloc] init];
  [truvideo initCameraScreenWithJsonData:configuration resolve:resolve reject:reject];
}

- (void)environment:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  TruVideoReactCameraSdkClass *truvideo = [[TruVideoReactCameraSdkClass alloc] init];
  [truvideo  environmentWithResolve:resolve reject:reject ];
}

- (void)isAugmentedRealityInstalled:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  TruVideoReactCameraSdkClass *truvideo = [[TruVideoReactCameraSdkClass alloc] init];
  [truvideo  isAugmentedRealityInstalledWithResolve:resolve reject:reject ];
}


- (void)isAugmentedRealitySupported:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  TruVideoReactCameraSdkClass *truvideo = [[TruVideoReactCameraSdkClass alloc] init];
  [truvideo  isAugmentedRealitySupportedWithResolve :resolve reject:reject ];
}


- (void)requestInstallAugmentedReality:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  
}


- (void)version:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  resolve(@"1");
}

- (void)initARCameraScreen:(NSString *)configuration resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  
}


- (void)initScanerScreen:(NSString *)configuration resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  
}





- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeTruVideoReactTurboCameraSdkSpecJSI>(params);
}

@end
