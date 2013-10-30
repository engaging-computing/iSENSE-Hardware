//
//  StepOneSetup.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 06/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import <iSENSE_API/SensorCompatibility.h>
#import <iSENSE_API/SensorEnums.h>
#import "SensorSelection.h"
#import <iSENSE_API/ProjectBrowseViewController.h>

#import "Data_CollectorAppDelegate.h"
#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVCaptureDevice.h>

@interface StepOneSetup : UIViewController <UITextFieldDelegate, ProjectBrowseViewControllerDelegate> {
    
    API *api;
    int projNumInteger;
    bool sensorsSelected;
    bool displaySensorSelectFromBrowse;
    
}

- (IBAction)rememberMeToggled:(UISwitch *)switcher;
- (IBAction)selectLaterToggled:(UISwitch *)switcher;
- (IBAction)projectOnClick:(UIButton *)projButton;
- (IBAction)okOnClick:(UIButton *)okButton;

- (BOOL) handleNewQRCode:(NSURL *)url;

@property (nonatomic, strong) IBOutlet UITextField *sessionName;
@property (nonatomic, strong) IBOutlet UITextField *sampleInterval;
@property (nonatomic, strong) IBOutlet UITextField *testLength;
@property (nonatomic, strong) IBOutlet UILabel     *projNumLabel;
@property (nonatomic, strong) IBOutlet UISwitch    *rememberMe;
@property (nonatomic, strong) IBOutlet UIButton    *selectProj;
@property (nonatomic, strong) IBOutlet UISwitch    *selectLater;
@property (nonatomic, strong) IBOutlet UIButton    *ok;

@end