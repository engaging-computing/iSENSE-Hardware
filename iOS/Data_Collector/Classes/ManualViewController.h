//
//  ManualView.h
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>


@interface ManualViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate, UITextFieldDelegate> {
	
	// UI Elements
	UIImageView *logo;
	UILabel *loggedInAsLabel;
	UILabel *expNumLabel;
	UIButton *save;
	UIButton *clear;
	UITextField *sessionNameInput;
	UIButton *media;
	UIScrollView *scrollView;
	
	// Non-UI Elements
	iSENSE *iapi;
	NSString *sessionName;
	NSNumber *expNum;
    
}

- (IBAction) saveOnClick:(id)sender;
- (IBAction) clearOnClick:(id)sender;
- (IBAction) mediaOnClick:(id)sender;
- (IBAction) displayMenu:(id)sender;

- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput;
- (void) experiment;
- (void) upload;

- (void) getDataFromFields;

- (void) initLocations;

- (void) fillDataFieldEntryList:(int)eid;
- (int) addDataField:(ExperimentField *)expField withType:(int)type andObjNumber:(int)objNum;
- (void) hideKeyboard;

// UI Properties
@property (nonatomic, retain) IBOutlet UIImageView *logo;
@property (nonatomic, retain) IBOutlet UILabel *loggedInAsLabel;
@property (nonatomic, retain) IBOutlet UILabel *expNumLabel;
@property (nonatomic, retain) IBOutlet UIButton *save;
@property (nonatomic, retain) IBOutlet UIButton *clear;
@property (nonatomic, retain) IBOutlet UITextField *sessionNameInput;
@property (nonatomic, retain) IBOutlet UIButton *media;
@property (nonatomic, retain) IBOutlet UIScrollView *scrollView;

// Non-UI Properties
@property (nonatomic, copy) NSString *sessionName;
@property (nonatomic, strong) NSNumber *expNum;

@end

