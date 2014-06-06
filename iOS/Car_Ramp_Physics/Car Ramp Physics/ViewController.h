//
//  ViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import <RNGridMenu.h>
#import "AboutViewController.h"
#import "StringGrabber.h"
#import "FieldGrabber.h"
#import <ProjectBrowserViewController.h>
#import "Constants.h"
#import <DataSaver.h>
#import <iSENSE_API/headers/QDataSet.h>
#import "AppDelegate.h"
#import <QueueUploaderView.h>
#import <CoreMotion/CMMotionManager.h>
#import <CoreLocation/CLLocationManager.h>
#import <CoreLocation/CLGeocoder.h>
#import <CoreLocation/CLLocationManagerDelegate.h>
#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>
#import <iSENSE_API/API.h>
#import <FieldMatchingViewController.h>
#import <CredentialManager.h>
#import <DLAVAlertViewController.h>
#import <ISKeys.h>
#import <RadioButton.h>

typedef struct _RotationDataSaver{
    __unsafe_unretained NSString *user;
    __unsafe_unretained NSString *pass;
    bool hasLogin;
    bool saveMode;
    bool isLoggedIn;
    
} RotationDataSaver;

@interface ViewController : UIViewController <RNGridMenuDelegate, UIAlertViewDelegate, CLLocationManagerDelegate, UITextFieldDelegate, UIPickerViewDataSource, UIPickerViewDelegate, ProjectBrowserDelegate, ZBarReaderDelegate, CredentialManagerDelegate, UIActionSheetDelegate>
{
    
    RotationDataSaver *saver;
    
}

@property(nonatomic) IBOutlet UILabel *vector_status;
@property(nonatomic) UIView *start;
@property(nonatomic) IBOutlet UIBarButtonItem *menuButton;
@property(nonatomic) UILabel *buttonText;
@property(nonatomic) IBOutlet UILabel *countdownLbl;


@property(nonatomic) int recordLength;
@property(nonatomic) int countdown;
@property(nonatomic, retain) UIAlertView *project;
@property(nonatomic, retain) UIAlertView *proj_num;
@property(nonatomic, retain) UIAlertView *loginalert;
@property(nonatomic, retain) UIAlertView *saveMode;
@property(nonatomic, retain) DLAVAlertView *enterName;
@property(nonatomic, retain) NSString *name;
@property(nonatomic, retain) API *api;
@property(nonatomic, retain) NSNumberFormatter *formatter;
@property DataFieldManager *dfm;
@property(nonatomic, retain) CMMotionManager *motionmanager;
@property (nonatomic, strong) NSMutableArray *dataToBeJSONed;
@property (nonatomic, strong) NSMutableArray *dataToBeOrdered;
@property (nonatomic, assign) int projNum;
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

@property (nonatomic) NSNumber *session_num;

@property (nonatomic) NSString *userName;
@property (nonatomic) NSString *passWord;

@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) DataSaver *dataSaver;

@property (nonatomic) UIPickerView *pickerLength;
@property (nonatomic) UIPickerView *pickerRate;
@property (nonatomic) NSMutableArray *lengths;
@property (nonatomic) NSMutableArray *rates;
@property (nonatomic) UITextField *lengthField;
@property (nonatomic) UITextField *rateField;

@property(nonatomic, retain) NSArray *items;
@property(nonatomic, strong) CredentialManager *mngr;
@property (strong, nonatomic) DLAVAlertView *alert;
@property(nonatomic, strong) RNGridMenu *menu;

//Boolean variables
@property (nonatomic) BOOL running;
@property (nonatomic) BOOL timeOver;
@property (nonatomic) BOOL useDev;
@property (nonatomic) BOOL x;
@property (nonatomic) BOOL y;
@property (nonatomic) BOOL z;
@property (nonatomic) BOOL mag;
@property (nonatomic) BOOL  setupDone;

@property (nonatomic) BOOL  menuShown;

@property (nonatomic) BOOL saveModeEnabled;

- (void)longPress:(UILongPressGestureRecognizer*)gesture;
- (void)showMenu;
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void)login:(NSString *)usernameInput withPassword:(NSString *)passwordInput;
- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString;
- (void) updateElapsedTime;
-(void) stopRecordingWithoutPublishing:(CMMotionManager *)finalMotionManager;

@end


