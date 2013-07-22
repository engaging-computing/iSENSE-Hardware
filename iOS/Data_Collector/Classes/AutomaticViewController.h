//
//  AutomaticViewController.h
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/10/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <AudioToolbox/AudioToolbox.h>
#import <CoreMotion/CoreMotion.h>
#import <CoreLocation/CoreLocation.h>
#import <iSENSE_API/headers/DataSaver.h>
#import "QueueUploaderView.h"
#import "Data_CollectorAppDelegate.h"
#import "ExperimentBrowseViewController.h"
#import "Constants.h"
#import "DataFieldManager.h"
#import "Data_CollectorAppDelegate.h"
#import "StepOneSetup.h"

@interface AutomaticViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate, CLLocationManagerDelegate, UITextFieldDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate>  {
	IBOutlet UIImageView *mainLogo;
    IBOutlet UILabel *mainLogoBackground;
    IBOutlet UIButton *step1;
    IBOutlet UIButton *step2;
    IBOutlet UIButton *step3;
    UIBarButtonItem *menuButton;
    IBOutlet UILabel *step1Label;
    IBOutlet UILabel *step3Label;
}

- (void)displayMenu;
- (IBAction) setup:(UIButton *)sender;
- (IBAction) uploadData:(UIButton *)sender;
- (IBAction) onRecordLongClick:(UILongPressGestureRecognizer *)sender;

- (void) login:(NSString *)usernameInput withPassword:(NSString *)password;
- (void) getExperiments;
- (void) recordData;

@property (nonatomic, retain) iSENSE *isenseAPI;
@property (nonatomic) BOOL isRecording;
@property (nonatomic) BOOL backFromSetup;
@property (nonatomic) int  elapsedTime;
@property (nonatomic) int  recordingRate;

@property (nonatomic) float sampleInterval;
@property (nonatomic, copy) NSString *sessionName;
@property (nonatomic) int testLength;

@property (nonatomic, assign) NSTimer *timer;
@property (nonatomic, assign) NSTimer *recordDataTimer;
@property (nonatomic, assign) CMMotionManager *motionManager;
@property (nonatomic, assign) NSMutableArray *dataToBeJSONed;
@property (nonatomic, assign) NSMutableArray *dataToBeOrdered;
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
