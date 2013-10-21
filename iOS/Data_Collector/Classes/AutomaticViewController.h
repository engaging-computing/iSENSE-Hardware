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
#import <iSENSE_API/DataFieldManager.h>
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

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error;

@property (nonatomic, strong) iSENSE *isenseAPI;
@property (nonatomic) BOOL isRecording;
@property (nonatomic) BOOL backFromSetup;
@property (nonatomic) BOOL backFromQueue;
@property (nonatomic) int  elapsedTime;
@property (nonatomic) int  recordingRate;

@property (nonatomic, assign) float sampleInterval;
@property (nonatomic, copy) NSString *sessionName;
@property (nonatomic) int testLength;

@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic, strong) NSTimer *recordDataTimer;
@property (nonatomic, strong) CMMotionManager *motionManager;
@property (nonatomic, strong) NSMutableArray *dataToBeJSONed;
@property (nonatomic, strong) NSMutableArray *dataToBeOrdered;
@property (nonatomic, assign) int expNum;
@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, strong) DataFieldManager *dfm;

@property (nonatomic, strong) CLGeocoder *geoCoder;
@property (nonatomic, copy) NSString *city;
@property (nonatomic, copy) NSString *address;
@property (nonatomic, copy) NSString *country;

@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) IBOutlet UILongPressGestureRecognizer *longClickRecognizer;
@property (nonatomic, strong) DataSaver *dataSaver;

@end
