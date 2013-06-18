//
//  AutomaticViewController.h
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/10/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "UILongClickButton.h"
#import "UIImageTint.h"
#import <AudioToolbox/AudioToolbox.h>
#import <CoreMotion/CoreMotion.h>
#import <CoreLocation/CoreLocation.h>
#import "ZXingWidgetController.h"
#import "QRCodeReader.h"
#import "ExperimentBrowseViewController.h"
#import "Constants.h"
#import "DataFieldManager.h"
#import <iSENSE_API/headers/DataSaver.h>
#import <iSENSE_API/headers/DataSet.h>
#import <iSENSE_API/headers/Queue.h>
#import "Data_CollectorAppDelegate.h"

@interface AutomaticViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate, CLLocationManagerDelegate, ZXingDelegate, UITextFieldDelegate>  {
	// Fuctionality
    UILongClickButton *containerForMainButton;
    iSENSE *isenseAPI;
    // GUI
    UIImageView *startStopButton;
    UIImageView *mainLogo;
    UILabel *startStopLabel;
    UILabel *loginStatus;
    UILabel *expNumLabel;
    UIBarButtonItem *menuButton;
    UILabel *elapsedTimeView;
    UILabel *sampleIntervalLabel;
    UITextField *sampleInterval;
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;
-(IBAction) displayMenu:(id)sender;
-(void) updateLoginStatus;
-(void) recordData;
-(NSMutableArray *) stopRecording:(CMMotionManager *)finalMotionManager;

- (void) login:(NSString *)usernameInput withPassword:(NSString *)password;
- (void) experiment;
- (void) upload;
- (void) getExperiments;

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
@property (nonatomic, assign) ZXingWidgetController *widController;
@property (nonatomic, assign) UITextField *sessionTitle;
@property (nonatomic, assign) UILabel *sessionTitleLabel;
@property (nonatomic, assign) CLGeocoder *geoCoder;
@property (nonatomic, copy) NSString *city;
@property (nonatomic, copy) NSString *address;
@property (nonatomic, copy) NSString *country;
@property (nonatomic, retain) UITextField *activeField;
@property (nonatomic, retain) UITextField *lastField;
@property (nonatomic, assign) bool keyboardDismissProper;

@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain) DataSaver *dataSaver;

@end
