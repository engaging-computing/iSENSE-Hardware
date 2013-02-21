//
//  CameraUsage.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "CameraUsage.h"
#import "Data_CollectorAppDelegate.h"

#define kUTTypeImage @"image"
#define kUTTypeMovie @"video"

@implementation CameraUsage

+ (void) useCamera {
/*	if ([UIImagePickerController isSourceTypeAvailable:
		 UIImagePickerControllerSourceTypeCamera]) {
		
		UIImagePickerController *imagePicker =
			[[UIImagePickerController alloc] init];
		imagePicker.delegate = self;
		imagePicker.sourceType = UIImagePickerControllerSourceTypeCamera;
		imagePicker.mediaTypes = [NSArray arrayWithObjects:
								  (NSString *) kUTTypeImage,
								  nil];
		imagePicker.allowsEditing = NO;
		Data_CollectorAppDelegate *appDelegate = (Data_CollectorAppDelegate *) [[UIApplication sharedApplication] delegate];
		[[appDelegate navControl] presentModalViewController:imagePicker animated:YES];
		[imagePicker release];
	}*/
}

- (void)imagePickerController:(UIImagePickerController *)picker
didFinishPickingMediaWithInfo:(NSDictionary *)info {
	
	/*NSString *mediaType = [info objectForKey:UIImagePickerControllerMediaType];
	
	[self dismissModalViewControllerAnimated:YES];
	
	if ([mediaType isEqualToString:(NSString *)kUTTypeImage]) {
		UIImage *image = [info 
						  objectForKey:UIImagePickerControllerOriginalImage];
		
		UIImageWriteToSavedPhotosAlbum(image, 
										self,
										@selector(image:finishedSavingWithError:contextInfo:),
										nil);
	}
	else if ([mediaType isEqualToString:(NSString *)kUTTypeMovie]) {
		// Code here to support video if enabled
	}*/
}

- (void)image:(UIImage *)image
finishedSavingWithError:(NSError *)error 
contextInfo:(void *)contextInfo {
	
	/*if (error) {
		UIAlertView *alert = [[UIAlertView alloc]
							  initWithTitle: @"Save failed"
							  message: @"Failed to save image"
							  delegate: nil
							  cancelButtonTitle:@"OK"
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
	}*/
}

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
	//[self dismissModalViewControllerAnimated:YES];
}


@end
