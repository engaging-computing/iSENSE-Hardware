//
//  CameraUsage.h
//  Data_Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//


#import <Foundation/Foundation.h>

@interface CameraUsage : UIViewController {}

+ (void) useCamera;
- (void) imagePickerController:(UIImagePickerController *)picker
	didFinishPickingMediaWithInfo:(NSDictionary *)info;
- (void) image:(UIImage *)image
	finishedSavingWithError:(NSError *)error 
	contextInfo:(void *)contextInfo;

@end
