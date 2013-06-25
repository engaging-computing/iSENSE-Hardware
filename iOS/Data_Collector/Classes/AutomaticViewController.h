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
#import "ExperimentBrowseViewController.h"
#import "Constants.h"
#import "DataFieldManager.h"
#import "Data_CollectorAppDelegate.h"
#import "StepONeSetup.h"

@interface AutomaticViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate, CLLocationManagerDelegate>  {
	IBOutlet UIImageView *mainLogo;
    IBOutlet UILabel *mainLogoBackground;
    IBOutlet UIButton *step1;
    IBOutlet UIButton *step2;
    IBOutlet UIButton *step3;
    UIBarButtonItem *menuButton;
}

- (void)displayMenu;
- (IBAction) setup:(UIButton *)sender;
- (IBAction) uploadData:(UIButton *)sender;
- (IBAction) onRecordLongClick:(UILongPressGestureRecognizer *)sender;

- (void) login:(NSString *)usernameInput withPassword:(NSString *)password;
- (void) getExperiments;
- (void) recordData;

@property (nonatomic, assign) IBOutlet UILongPressGestureRecognizer *longClickRecognizer;

@property (nonatomic, retain) iSENSE *isenseAPI;
@property (nonatomic) BOOL isRecording;
@property (nonatomic) int elapsedTime;
@property (nonatomic) float recommendedSampleInterval;
@property (nonatomic, assign) NSTimer *timer;
@property (nonatomic, assign) NSTimer *recordDataTimer;
@property (nonatomic, assign) CMMotionManager *motionManager;
@property (nonatomic, assign) NSMutableArray *dataToBeJSONed;
@property (nonatomic, assign) int expNum;
@property (nonatomic, assign) CLLocationManager *locationManager;
@property (nonatomic, assign) DataFieldManager *dfm;
@property (nonatomic, assign) NSString *qrResults;
@property (nonatomic, assign) CLGeocoder *geoCoder;
@property (nonatomic, copy) NSString *city;
@property (nonatomic, copy) NSString *address;
@property (nonatomic, copy) NSString *country;

@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain) DataSaver *dataSaver;

@end
