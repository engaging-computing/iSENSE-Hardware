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
#import <iSENSE_API/QueueUploaderView.h>
#import <MobileCoreServices/UTCoreTypes.h>
#import <iSENSE_API/ProjectBrowseViewController.h>
#import <iSENSE_API/ISKeys.h>

#import "RotationDataSaver.h"

@interface ManualViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate, UITextFieldDelegate, CLLocationManagerDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate, ProjectBrowseViewControllerDelegate> {
	
	// Non-UI Elements
	API   *api;
	NSString *dataSetName;
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
- (int)    addDataField:(RProjectField *)projField withType:(int)type andObjNumber:(int)objNum andData:(NSString *)data;
- (void)   hideKeyboard;
- (CGRect) setScrollViewItem:(int)type toSizeWithY:(CGFloat)y;
- (void)   cleanRDSData;

- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString;
- (BOOL)   handleNewQRCode:(NSURL *)url;

- (void)saveDataSet:(NSMutableArray *)dataJSON withDescription:(NSString *)description;
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation;

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error andContextInfo:(void *)contextInfo;


// UI Properties
@property (nonatomic, strong) IBOutlet UILabel      *loggedInAsLabel;
@property (nonatomic, strong) IBOutlet UILabel      *projNumLabel;
@property (nonatomic, strong) IBOutlet UIButton     *upload;
@property (nonatomic, strong) IBOutlet UIButton     *clear;
@property (nonatomic, strong) IBOutlet UITextField  *dataSetNameInput;
@property (nonatomic, strong) IBOutlet UIButton     *media;
@property (nonatomic, strong) IBOutlet UIScrollView *scrollView;

@property (nonatomic, strong) UITextField           *activeField;
@property (nonatomic, strong) UITextField           *lastField;

// Non-UI Properties
@property (nonatomic, copy)   NSString               *qrResults;
@property (nonatomic, strong) CLLocationManager      *locationManager;
@property (nonatomic, assign) int                     projNum;
@property (nonatomic, assign) bool                    keyboardDismissProper;
@property (nonatomic, assign) BOOL                    browsing;
@property (nonatomic, assign) BOOL                    initialProjDialogOpen;
@property (nonatomic, strong) CLGeocoder             *geoCoder;
@property (nonatomic, copy)   NSString               *city;
@property (nonatomic, copy)   NSString               *address;
@property (nonatomic, copy)   NSString               *country;
@property (nonatomic, strong) DataSaver              *dataSaver;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) NSMutableArray         *imageList;


@end

