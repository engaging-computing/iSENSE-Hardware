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

#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVCaptureDevice.h>
#import "QueueUploaderView.h"
#import <MobileCoreServices/UTCoreTypes.h>

typedef struct _RotationDataSaver {
    NSString *sesName;
    NSMutableArray *data;
    bool doesHaveName;
    bool doesHaveData;
} RotationDataSaver;

@interface ManualViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate, UITextFieldDelegate, CLLocationManagerDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate> {
	
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

// XIB functions
- (IBAction) saveOnClick:(id)sender;
- (IBAction) clearOnClick:(id)sender;
- (IBAction) mediaOnClick:(id)sender;
- (IBAction) displayMenu:(id)sender;

// Behavioral functions
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput;

- (NSMutableArray *) getDataFromFields;
- (void) initLocations;
- (BOOL) containsAcceptedCharacters:(NSString *)mString;
- (BOOL) containsAcceptedNumbers:(NSString *)mString;

- (void)   fillDataFieldEntryList:(int)eid withData:(NSMutableArray *) data;
- (int)    addDataField:(ExperimentField *)expField withType:(int)type andObjNumber:(int)objNum andData:(NSString *)data;
- (void)   hideKeyboard;
- (CGRect) setScrollViewItem:(int)type toSizeWithY:(CGFloat)y;
- (void)   cleanRDSData;

- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString;
- (BOOL)   handleNewQRCode:(NSURL *)url;

- (void)saveDataSet:(NSMutableArray *)dataJSON withDescription:(NSString *)description;
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation;

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error;


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
@property (nonatomic, copy)   NSString               *qrResults;
@property (nonatomic, retain) CLLocationManager      *locationManager;
@property (nonatomic, assign) int                     expNum;
@property (nonatomic, assign) bool                    keyboardDismissProper;
@property (nonatomic, assign) BOOL                    browsing;
@property (nonatomic, assign) BOOL                    initialExpDialogOpen;
@property (nonatomic, assign) CLGeocoder             *geoCoder;
@property (nonatomic, copy)   NSString               *city;
@property (nonatomic, copy)   NSString               *address;
@property (nonatomic, copy)   NSString               *country;
@property (nonatomic, retain) DataSaver              *dataSaver;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain) NSMutableArray         *imageList;


@end

