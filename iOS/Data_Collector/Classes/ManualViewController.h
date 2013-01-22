//
//  ManualView.h
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>


@interface ManualViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate> {
	
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
	NSString *username;
	int experimentNum;
	
}

- (IBAction) saveOnClick:(id)sender;
- (IBAction) clearOnClick:(id)sender;
- (IBAction) mediaOnClick:(id)sender;
- (IBAction) displayMenu:(id)sender;

- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput;
- (void) experiment;
- (void) upload;

- (void) getDataFromExpNumber;

- (void) initLocations;

- (void) fillDataFieldEntryList:(int)eid;
- (void) addDataField:(NSString *)expField andType:(int)type;

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
@property (nonatomic, retain) NSString *sessionName;
@property (nonatomic, retain) NSString *username;

@end

