//
//  ManualView.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>
#import "StepOneSetup.h"

typedef struct _RotationDataSaver {
    NSString *sesName;
    NSMutableArray *data;
    bool doesHaveName;
    bool doesHaveData;
} RotationDataSaver;

@interface ManualViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate, UITextFieldDelegate, CLLocationManagerDelegate> {
	
	// UI Elements
	UIImageView  *logo;
	UILabel      *loggedInAsLabel;
	UILabel      *expNumLabel;
	UIButton     *upload;
	UIButton     *clear;
	UITextField  *sessionNameInput;
	UIButton     *media;
	UIScrollView *scrollView;
	
	// Non-UI Elements
	iSENSE   *iapi;
	NSString *sessionName;
    RotationDataSaver *rds;
    
    CLLocationManager *locationManager;
    
}

// Storyboard functions
- (IBAction) uploadOnClick:(id)sender;
- (IBAction) clearOnClick:(id)sender;
- (IBAction) mediaOnClick:(id)sender;
- (IBAction) displayMenu:(id)sender;

// Programmatic functions
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput;
- (void) upload:(NSMutableArray *)results;

- (void) getDataFromFields;
- (void) initLocations;
- (BOOL) containsAcceptedCharacters:(NSString *)mString;
- (BOOL) containsAcceptedNumbers:(NSString *)mString;

- (void)   fillDataFieldEntryList:(int)eid withData:(NSMutableArray *) data;
- (int)    addDataField:(ExperimentField *)expField withType:(int)type andObjNumber:(int)objNum andData:(NSString *)data;
- (void)   hideKeyboard;
- (CGRect) setScrollViewItem:(int)type toSizeWithY:(CGFloat)y;
- (void)   cleanRDSData;

- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString;

// UI Properties
@property (nonatomic, retain) IBOutlet UIImageView  *logo;
@property (nonatomic, retain) IBOutlet UILabel      *loggedInAsLabel;
@property (nonatomic, retain) IBOutlet UILabel      *expNumLabel;
@property (nonatomic, retain) IBOutlet UIButton     *upload;
@property (nonatomic, retain) IBOutlet UIButton     *clear;
@property (nonatomic, retain) IBOutlet UITextField  *sessionNameInput;
@property (nonatomic, retain) IBOutlet UIButton     *media;
@property (nonatomic, retain) IBOutlet UIScrollView *scrollView;

@property (nonatomic, retain) UITextField           *activeField;
@property (nonatomic, retain) UITextField           *lastField;

// Non-UI Properties
@property (nonatomic, copy)   NSString              *sessionName;
@property (nonatomic, copy)   NSString              *qrResults;
@property (nonatomic, retain) CLLocationManager     *locationManager;
@property (nonatomic, assign) int                    expNum;
@property (nonatomic, assign) bool                   keyboardDismissProper;
@property (nonatomic, assign) BOOL                   browsing;

@end

