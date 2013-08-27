//
//  DWMasterViewController.h
//  Data_Walk
//
//  Created by Michael Stowell on 8/22/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreData/CoreData.h>
#import <AudioToolbox/AudioToolbox.h>
#import <CoreMotion/CoreMotion.h>
#import <CoreLocation/CoreLocation.h>
#import <iSENSE_API/Waffle.h>
#import <iSENSE_API/API.h>

@interface MainViewController : UIViewController <UINavigationControllerDelegate, UIAlertViewDelegate, CLLocationManagerDelegate, UITextFieldDelegate, UIPickerViewDelegate, UIActionSheetDelegate> {
    
    API *api;
    
    BOOL isShowingPickerView;
    UIPickerView *intervalPickerView;
    
    BOOL isRecording;
    int recordingInterval;
}

// UI functions
- (void) onResetClick:(id)sender;
- (void) onAboutClick:(id)sender;

- (IBAction) onRecordDataClick:(id)sender;
- (IBAction) onRecordingIntervalClick:(id)sender;
- (IBAction) onLoggedInClick:(id)sender;
- (IBAction) onUploadClick:(id)sender;
- (IBAction) onSelectProjectClick:(id)sender;

// UI properties
@property (nonatomic, strong) UIBarButtonItem *reset;
@property (nonatomic, strong) UIBarButtonItem *about;

@property (nonatomic, strong) IBOutlet UILabel *latitudeLabel;
@property (nonatomic, strong) IBOutlet UILabel *longitudeLabel;
@property (nonatomic, strong) IBOutlet UIButton *recordData;
@property (nonatomic, strong) IBOutlet UIButton *recordingIntervalButton;
@property (nonatomic, strong) IBOutlet UITextField *nameTextField;
@property (nonatomic, strong) IBOutlet UIButton *loggedInAs;
@property (nonatomic, strong) IBOutlet UIButton *upload;
@property (nonatomic, strong) IBOutlet UIButton *selectProject;
@property (nonatomic, strong) IBOutlet UIImageView *gpsLock;

// Other properties
@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, strong) UITextField *activeField;

@end
