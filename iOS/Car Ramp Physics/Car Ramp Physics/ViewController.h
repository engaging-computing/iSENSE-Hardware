//
//  ViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import "RNGridMenu.h"
#import "AboutViewController.h"
#import "iSENSE.h"
#import "StringGrabber.h"
#import "FieldGrabber.h"
#import "ProjectBrowseViewController.h"
#import "NewDFM.h"
#import "Constants.h"
#import "HexColor.h"
#import "VariablesViewController.h"
#import "DataSaver.h"
#import <iSENSE_API/headers/QDataSet.h>
#import "AppDelegate.h"
#import "QueueUploaderView.h"
#import <CoreMotion/CMMotionManager.h>
#import <CoreLocation/CLLocationManager.h>
#import <CoreLocation/CLGeocoder.h>
#import <CoreLocation/CLLocationManagerDelegate.h>
#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>
#import <iSENSE_API/API.h>

typedef struct _RotationDataSaver{
    __unsafe_unretained NSString *first;
    __unsafe_unretained NSString *last;
    __unsafe_unretained NSString *user;
    __unsafe_unretained NSString *pass;
    bool hasName;
    bool hasLogin;
    bool saveMode;
    
} RotationDataSaver;

@interface ViewController : UIViewController <RNGridMenuDelegate, UIActionSheetDelegate, UIAlertViewDelegate, CLLocationManagerDelegate, UITextFieldDelegate, UIPickerViewDataSource, UIPickerViewDelegate>
{
    
    RotationDataSaver *saver;
    
}

@property(nonatomic) IBOutlet UILabel *vector_status;
@property(nonatomic) IBOutlet UILabel *login_status;
@property(nonatomic) IBOutlet UIButton *start;
@property(nonatomic) IBOutlet UIBarButtonItem *menuButton;
@property(nonatomic) IBOutlet UIImageView *image;

@property(nonatomic) int recordLength;
@property(nonatomic) int countdown;
@property(nonatomic, retain) UIAlertView *change_name;
@property(nonatomic, retain) UIAlertView *experiment;
@property(nonatomic, retain) UIAlertView *exp_num;
@property(nonatomic, retain) UIAlertView *loginalert;
@property(nonatomic, retain) UIAlertView *saveMode;
@property(nonatomic, retain) API *api;
@property NewDFM *dfm;
@property(nonatomic, retain) CMMotionManager *motionmanager;
@property (nonatomic, strong) NSMutableArray *dataToBeJSONed;
@property (nonatomic, strong) NSMutableArray *dataToBeOrdered;
@property (nonatomic, assign) int expNum;
@property (nonatomic, retain) CLLocationManager *locationManager;
@property (nonatomic) float sampleInterval;
@property (nonatomic, copy) NSString *sessionName;
@property (nonatomic) int testLength;

@property (nonatomic, assign) NSTimer *timer;
@property (nonatomic, assign) NSTimer *recordDataTimer;

@property (nonatomic, strong) CLGeocoder *geoCoder;
@property (nonatomic, copy) NSString *city;
@property (nonatomic, copy) NSString *address;
@property (nonatomic, copy) NSString *country;
@property (nonatomic) int  elapsedTime;
@property (nonatomic) int  recordingRate;

@property (nonatomic) NSString *firstName;
@property (nonatomic) NSString *lastInitial;
@property (nonatomic) NSNumber *session_num;

@property (nonatomic) NSString *userName;
@property (nonatomic) NSString *passWord;

@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) DataSaver *dataSaver;

@property (nonatomic) UIPickerView *picker;
@property (nonatomic) NSMutableArray *lengths;
@property (nonatomic) UITextField *lengthField;

@property(nonatomic, retain) NSArray *items;

//Boolean variables
@property (nonatomic) BOOL running;
@property (nonatomic) BOOL timeOver;
@property (nonatomic) BOOL useDev;
@property (nonatomic) BOOL x;
@property (nonatomic) BOOL y;
@property (nonatomic) BOOL z;
@property (nonatomic) BOOL mag;
@property (nonatomic) BOOL  setupDone;

@property (nonatomic) BOOL saveModeEnabled;

- (void)longPress:(UILongPressGestureRecognizer*)gesture;
- (IBAction)showMenu:(id)sender;
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void)changeName;
- (void)login:(NSString *)usernameInput withPassword:(NSString *)passwordInput;
- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString;
- (void) updateElapsedTime;
-(void) stopRecordingWithoutPublishing:(CMMotionManager *)finalMotionManager;

@end


