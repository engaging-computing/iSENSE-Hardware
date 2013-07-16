//
//  ViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import "RNGridMenu.h"
#import "Waffle.h"
#import "AboutViewController.h"
#import "CODialog.h"
#import "iSENSE.h"
#import "StringGrabber.h"
#import "ExperimentBrowseViewController.h"
#import "DataFieldManager.h"
#import "Constants.h"
#import "Fields.h"
#import "HexColor.h"
#import "VariablesViewController.h"
#import "DataSaver.h"
#import "DataSet.h"
#import "Queue.h"
#import "AppDelegate.h"
#import "QueueUploaderView.h"
#import <CoreMotion/CMMotionManager.h>
#import <CoreLocation/CLLocationManager.h>
#import <CoreLocation/CLGeocoder.h>
#import <CoreLocation/CLLocationManagerDelegate.h>
#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <RNGridMenuDelegate, UIActionSheetDelegate, UIAlertViewDelegate, CLLocationManagerDelegate, UITextFieldDelegate>
{
    
    
    
}

@property(nonatomic) IBOutlet UILabel *vector_status;
@property(nonatomic) IBOutlet UILabel *login_status;
@property(nonatomic) IBOutlet UIButton *start;
@property(nonatomic) IBOutlet UIBarButtonItem *menuButton;
@property(nonatomic) IBOutlet UINavigationBar *navBar;

@property(nonatomic) int recordLength;
@property(nonatomic) int countdown;
@property(nonatomic, retain) CODialog *change_name;
@property(nonatomic, retain) CODialog *experiment;
@property(nonatomic, retain) UIAlertView *view;
@property(nonatomic, retain) iSENSE *iapi;
@property DataFieldManager *dfm;
@property(nonatomic, retain) CMMotionManager *motionmanager;
@property (nonatomic, strong) NSMutableArray *dataToBeJSONed;
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
@property (nonatomic) BOOL  setupDone;
@property (nonatomic) NSString *userName;
@property (nonatomic) NSString *passWord;

@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) DataSaver *dataSaver;


@property(nonatomic, retain) NSArray *items;

//Boolean variables
@property(nonatomic) BOOL running;
@property(nonatomic) BOOL timeOver;
@property(nonatomic) BOOL useDev;
@property(nonatomic) BOOL x;
@property(nonatomic) BOOL y;
@property(nonatomic) BOOL z;
@property(nonatomic) BOOL mag;

- (void)longPress:(UILongPressGestureRecognizer*)gesture;
- (IBAction)showMenu:(id)sender;
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void)changeName;
- (void)login:(NSString *)usernameInput withPassword:(NSString *)passwordInput;
- (CODialog *) getDispatchDialogWithMessage:(NSString *)dString;
- (void) updateElapsedTime;

@end


