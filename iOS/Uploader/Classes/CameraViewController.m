//
//  CameraUsage.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "CameraViewController.h"


@implementation CameraViewController

// For responding to the user tapping Cancel.
- (void) imagePickerControllerDidCancel: (UIImagePickerController *) picker {
    
    NSLog(@"Got into camera delegate method cancel");
    
    [self dismissModalViewControllerAnimated: YES];
    [picker release];
    
}

// For responding to the user accepting a newly-captured picture or movie
- (void) imagePickerController: (UIImagePickerController *) picker didFinishPickingMediaWithInfo:(NSDictionary *) info {
    NSLog(@"Image picker controller method");
    
    NSString *mediaType = [info objectForKey: UIImagePickerControllerMediaType];
    UIImage *originalImage, *editedImage, *imageToSave;
    
    // Handle a still image capture
    if (CFStringCompare ((CFStringRef) mediaType, kUTTypeImage, 0) == kCFCompareEqualTo) {

        editedImage = (UIImage *) [info objectForKey:UIImagePickerControllerEditedImage];
        originalImage = (UIImage *) [info objectForKey:UIImagePickerControllerOriginalImage];
        
        if (editedImage) {
            imageToSave = editedImage;
        } else {
            imageToSave = originalImage;
        }
        
        NSLog(@"About to save picture!");
        
        // Save the new image (original or edited) to the Camera Roll (then send the image to the caller's image:didFinishSavingWithError: method);
        UIImageWriteToSavedPhotosAlbum (imageToSave, self.delegate, @selector(image:didFinishSavingWithError:), nil);
        
    }
    
    [[picker parentViewController] dismissModalViewControllerAnimated: YES];
    [picker release];
    
}


@end
