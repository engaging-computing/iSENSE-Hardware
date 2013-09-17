//
//  ViewController.h
//  PictureTest
//
//  Created by Jeremy Poulin on 9/11/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <iSENSE_API/API.h>
#import <MobileCoreServices/UTCoreTypes.h> 
#import <AssetsLibrary/AssetsLibrary.h>


@interface ViewController : UIViewController <UIImagePickerControllerDelegate, UINavigationControllerDelegate>

@property (strong) API *api;

-(IBAction)browsePictures:(id)sender;
-(IBAction)upload:(id)sender;

@end
