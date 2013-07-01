//
//  CarRampPhysicsViewController.h
//  CarRampPhysics
//
//  Created by Virinchi Balabhadrapatruni on 6/27/13.
//  Copyright 2013 __MyCompanyName__. All rights reserved.
//
#import "EGOTextFieldAlertView.h"
#import "CODIalog.h"
#import <UIKit/UIKit.h>

#define	LOGIN_BUTTON 0
#define UPLOAD_BUTTON 1
#define CHANGE_NAME_BUTTON 2
#define RECORD_SETTINGS_BUTTON 3
#define RECORD_LENGTH_BUTTON 4


@interface CarRampPhysicsViewController : UIViewController <UIAccelerometerDelegate, UIActionSheetDelegate, UIAlertViewDelegate, UITextFieldDelegate> {

	IBOutlet UIButton *start;
	IBOutlet UILabel *log;
	UIAccelerometer *accelerometer;
	IBOutlet UIButton *menuItem;
	UIActionSheet *sheet;
	CODialog *dialog;
	NSString *first, *last ;
	
		
}

- (void)longPress:(UILongPressGestureRecognizer*)gesture;
- (void)accelerometer:(UIAccelerometer *)acelerometer didAccelerate:(UIAcceleration*)acceleration;
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex;
- (IBAction) menu;
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string;
- (void) showNameDialog;
- (void) hideNameDialog;
- (void) showLoginDialog;
- (void) okLoginDialog;
- (void) cancelLoginDialog;



@end

