//
//  CarRampPhysicsViewController.h
//  CarRampPhysics
//
//  Created by Virinchi Balabhadrapatruni on 6/27/13.
//  Copyright 2013 __MyCompanyName__. All rights reserved.
//
#import "DataFieldManager.h"
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
	IBOutlet UIBarButtonItem *menuItem;
	UIActionSheet *sheet;
	CODialog *dialog;
	NSString *first, *last ;
	
		
}

- (void)accelerometer:(UIAccelerometer *)acelerometer didAccelerate:(UIAcceleration*)acceleration;
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex;
- (IBAction) menu;
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string;
- (void) showNameDialog;
- (void) hideNameDialog;
- (void) showLoginDialog;
- (void) okLoginDialog;
- (void) cancelLoginDialog;
- (IBAction) onRecordLongClick:(UILongPressGestureRecognizer *)sender;
- (void) recordData;

@property (nonatomic, retain) iSENSE *isenseAPI;
@property (nonatomic) BOOL isRecording;
@property (nonatomic) BOOL backFromSetup;
@property (nonatomic) int elapsedTime;

@property (nonatomic) float sampleInterval;
@property (nonatomic, copy) NSString *sessionName;
@property (nonatomic) int testLength;

@property (nonatomic, assign) NSTimer *timer;
@property (nonatomic, assign) NSTimer *recordDataTimer;
@property (nonatomic, assign) CMMotionManager *motionManager;
@property (nonatomic, assign) NSMutableArray *dataToBeJSONed;
@property (nonatomic, assign) int expNum;
@property (nonatomic, assign) CLLocationManager *locationManager;
@property (nonatomic, assign) DataFieldManager *dfm;

@property (nonatomic, assign) CLGeocoder *geoCoder;
@property (nonatomic, copy) NSString *city;
@property (nonatomic, copy) NSString *address;
@property (nonatomic, copy) NSString *country;

@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain) IBOutlet UILongPressGestureRecognizer *longClickRecognizer;
@property (nonatomic, retain) DataSaver *dataSaver;





@end

