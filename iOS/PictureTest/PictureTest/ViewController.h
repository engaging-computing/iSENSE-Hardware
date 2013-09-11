//
//  ViewController.h
//  PictureTest
//
//  Created by Jeremy Poulin on 9/11/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MobileCoreServices/UTCoreTypes.h> 

@interface ViewController : UIViewController <UIImagePickerControllerDelegate, UINavigationControllerDelegate>

-(IBAction)browsePictures:(id)sender;
-(IBAction)upload:(id)sender;

@end
