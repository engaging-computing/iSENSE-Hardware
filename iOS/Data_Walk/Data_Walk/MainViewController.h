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


@interface MainViewController : UIViewController <UINavigationControllerDelegate, UIActionSheetDelegate, UIAlertViewDelegate,
CLLocationManagerDelegate, UITextFieldDelegate> {
    
}

// UI functions
- (void) onMenuClick;

// UI properties
@property (nonatomic, strong) UIBarButtonItem *menu;
@property (nonatomic, strong) IBOutlet UILabel *name;
@property (nonatomic, strong) IBOutlet UILabel *timeElapsed;
@property (nonatomic, strong) IBOutlet UILabel *pointsRecorded;
@property (nonatomic, strong) IBOutlet UILabel *loggedInAs;
@property (nonatomic, strong) IBOutlet UILabel *projNumber;
@property (nonatomic, strong) IBOutlet UILabel *dataRecordedEvery;
@property (nonatomic, strong) IBOutlet UILabel *latitudeLabel;
@property (nonatomic, strong) IBOutlet UILabel *longitudeLabel;

// Other properties
@property (nonatomic, assign) CLLocationManager *locationManager;
@property (nonatomic) BOOL isRecording;

@end
