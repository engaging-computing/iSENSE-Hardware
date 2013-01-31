//
//  ManualView.m
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ManualViewController.h"
#import "Data_CollectorAppDelegate.h"

#import <ZXingWidgetController.h>
#import <QRCodeReader.h>

#define MENU_UPLOAD 0
#define MENU_EXPERIMENT 1
#define MENU_LOGIN 2
#define EXPERIMENT_MANUAL_ENTRY 3
#define EXPERIMENT_BROWSE_EXPERIMENTS 4
#define EXPERIMENT_SCAN_QR_CODE 5

#define OPTION_CANCELED 0
#define OPTION_ENTER_EXPERIMENT_NUMBER 1
#define OPTION_BROWSE_EXPERIMENTS 2
#define OPTION_SCAN_QR_CODE 3

#define TYPE_DEFAULT 1000
#define TYPE_LATITUDE 1001
#define TYPE_LONGITUDE 1002
#define TYPE_TIME 1003

#define SCROLLVIEW_IPAD_TROLL 50
#define SCROLLVIEW_Y_OFFSET 50
#define SCROLLVIEW_OBJ_INCR 30

@implementation ManualViewController

@synthesize logo, loggedInAsLabel, expNumLabel, save, clear, sessionNameInput, media, scrollView;
@synthesize sessionName, expNum;


 // Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
 - (void)viewDidLoad {
	 [super viewDidLoad];
	 
	 [self.view sendSubviewToBack:scrollView];
	 
	 [self.sessionNameInput addTarget:self
						  action:@selector(textFieldFinished:)
				forControlEvents:UIControlEventEditingDidEndOnExit];
	 sessionNameInput.enablesReturnKeyAutomatically = NO;
	 
	 UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];          
	 self.navigationItem.rightBarButtonItem = menuButton;
	 [menuButton release];
	 
	 iapi = [iSENSE getInstance];
     [iapi toggleUseDev:YES];
	 
	 [self initLocations]; //* make initLocations.. ya know.. do something
	 
     if ([iapi isLoggedIn]) {
         loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
     } else {
         loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"_"]; 
     }
     
     CGFloat scrollHeight = scrollView.frame.size.height;
     CGFloat scrollWidth = scrollView.frame.size.width;
     [scrollView setContentSize:CGSizeMake(scrollHeight, scrollWidth)];
     scrollView.indicatorStyle = UIScrollViewIndicatorStyleWhite;
     
     //* get exp. # from prefs
	 expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:@"_"];
	 
	 //* if exp. # is null, launch the dialog for choosing exp. num
 
 
 
     /*** DELETEY STUFFZ!!!!!!!! **////////////
     
     NSLog(@"ExperimentNum = %@", expNum);
     NSLog(@"Temperature is: %d", TEMPERATURE);
     NSLog(@"Height = %f, Width = %f", scrollHeight, scrollWidth);
     
     /****************************************/

 }

- (IBAction)textFieldFinished:(id)sender {}
 

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[logo release];
	[loggedInAsLabel release];
	[expNumLabel release];
	[save release];
	[clear release];
	[sessionNameInput release];
	[media release];
	[scrollView release];
	
	[sessionName release];
    
	[super dealloc];
}

- (IBAction) saveOnClick:(id)sender {
	//* if exp is null, toast
	//* else if sessionName's length = 0, .setError
	//* else SavaDataTask
}

- (IBAction) clearOnClick:(id)sender {
	sessionNameInput.text = @"";
	//* loop through UITextFields and clear them out
}

- (IBAction) mediaOnClick:(id)sender {
	//* useCamera iff sessionNameInput.length != 0
	[CameraUsage useCamera];
}

- (IBAction) displayMenu:(id)sender {
	UIActionSheet *popupQuery = [[UIActionSheet alloc] 
				  initWithTitle:nil 
			           delegate:self
	          cancelButtonTitle:@"Cancel" 
	     destructiveButtonTitle:nil 
			  otherButtonTitles:@"Upload", @"Experiment", @"Login", nil];
	popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	[popupQuery showInView:self.view];
	[popupQuery release];
}

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
	BOOL showMsg = YES;
	UIAlertView *message = [UIAlertView alloc];
    
	switch (buttonIndex) {
		case MENU_UPLOAD:
			message = [[UIAlertView alloc] initWithTitle:@"Upload"
                                                 message:@"Would you like to upload your data to iSENSE?"
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            
            message.tag = MENU_UPLOAD;
            
			break;
            
		case MENU_EXPERIMENT:
            message = [[UIAlertView alloc] initWithTitle:@"Experiment Selection"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Enter Experiment #", @"Browse", @"Scan QR Code", nil];
            
            message.tag = MENU_EXPERIMENT;
            
			break;
            
		case MENU_LOGIN:
            message = [[UIAlertView alloc] initWithTitle:@"Login"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            
            message.tag = MENU_LOGIN;
			[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
            
            break;
            
		default:
			showMsg = NO;
			break;
	}
	
	if (showMsg)
		[message show];
	
    [message release];
}

- (void)alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == MENU_LOGIN) {
        
        if (buttonIndex != OPTION_CANCELED) {
            NSString *usernameInput = [[actionSheet textFieldAtIndex:0] text];
            NSString *passwordInput = [[actionSheet textFieldAtIndex:1] text];
            [self login:usernameInput withPassword:passwordInput];
        }
        
    } else if (actionSheet.tag == MENU_EXPERIMENT){
        
        if (buttonIndex == OPTION_ENTER_EXPERIMENT_NUMBER) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Experiment #:"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            
            message.tag = EXPERIMENT_MANUAL_ENTRY;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message show];
            [message release];
            
        } else if (buttonIndex == OPTION_BROWSE_EXPERIMENTS) {
            
            
        } else if (buttonIndex == OPTION_SCAN_QR_CODE) {
            
            ZXingWidgetController *widController = [[ZXingWidgetController alloc] initWithDelegate:(id <ZXingDelegate>)self showCancel:YES OneDMode:NO];
            QRCodeReader* qRCodeReader = [[QRCodeReader alloc] init];
            
            NSSet *readers = [[NSSet alloc] initWithObjects:qRCodeReader,nil];
            widController.readers = readers;
            
            [self presentModalViewController:widController animated:YES];
            
        }
        
    } else if (actionSheet.tag == MENU_UPLOAD) {
        
    } else if (actionSheet.tag == EXPERIMENT_MANUAL_ENTRY) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            expNum = [NSNumber numberWithInt: [[[actionSheet textFieldAtIndex:0] text] intValue]];
            NSLog(@"ExperimentNum = %@", expNum);
            
            // check if the expNum changed since last time, then do ----v
            [self fillDataFieldEntryList:expNum.intValue];
        }
        
    } else if (actionSheet.tag == EXPERIMENT_BROWSE_EXPERIMENTS) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            // fill view with stuffz!
        }
        
    } else if (actionSheet.tag == EXPERIMENT_SCAN_QR_CODE) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            // fill view with stuffz!
        }
        
    }
}

// TODO - make this actually restrict character limits
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
	if (textField == sessionNameInput) {
		NSUInteger newLength = [textField.text length] + [string length] - range.length;
		return (newLength > 25) ? NO : YES;
	}
	return YES;
}

- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    if ([iapi login:usernameInput with:passwordInput]) {
        [self.view makeToast:@"Login Successful!"
                    duration:2.0
                    position:@"bottom"
                       image:@"check"];
        loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
	} else {
        [self.view makeToast:@"Login Failed!"
                    duration:2.0
                    position:@"bottom"
                       image:@"red_x"];
    }
}

- (void) experiment {
	[self.view makeToast:@"Experiment!"
				duration:2.0
				position:@"bottom"
                   image:@"red_x"];
}

- (void) upload {
	[self.view makeToast:@"Upload!"
				duration:2.0
				position:@"bottom"
                   image:@"check"];
	
}

// TODO gets data from the exp. #
- (void) getDataFromExpNumber {
	
}

// TODO allows for GPS to be recorded
- (void) initLocations {
	
}

- (void) fillDataFieldEntryList:(int)eid {
    
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    NSMutableArray *fieldOrder = [iapi getExperimentFields:[NSNumber numberWithInt:eid]];
    int objNumber = 0;
    
    for (ExperimentField *expField in fieldOrder) {
        
        if (expField.type_id == [NSNumber numberWithInt:GEOSPACIAL]) {
            if (expField.unit_id == [NSNumber numberWithInt:UNIT_LATITUDE]) {
                [self addDataField:expField withType:TYPE_LATITUDE andObjNumber:objNumber];
            } else {
                [self addDataField:expField withType:TYPE_LONGITUDE andObjNumber:objNumber];
            }
        } else if (expField.type_id == [NSNumber numberWithInt:TIME]) {
            [self addDataField:expField withType:TYPE_TIME andObjNumber:objNumber];
        } else {
            [self addDataField:expField withType:TYPE_DEFAULT andObjNumber:objNumber];
        }
        ++objNumber;
    }
}

- (void) addDataField:(ExperimentField *)expField withType:(int)type andObjNumber:(int)objNum {
    
    CGFloat Y_FIELDNAME = SCROLLVIEW_IPAD_TROLL + (objNum * SCROLLVIEW_Y_OFFSET);
    CGFloat Y_FIELDCONTENTS = Y_FIELDNAME + SCROLLVIEW_OBJ_INCR;
    
    UILabel *fieldName;
    if (objNum == 0) {
        fieldName = [[UILabel alloc] initWithFrame:CGRectMake(0, Y_FIELDNAME, 730, 20)];
    } else {
        Y_FIELDNAME += (SCROLLVIEW_OBJ_INCR * objNum);
        Y_FIELDCONTENTS += (SCROLLVIEW_OBJ_INCR * objNum);
        fieldName = [[UILabel alloc] initWithFrame:CGRectMake(0, Y_FIELDNAME, 730, 20)];
    }
    fieldName.backgroundColor = [UIColor blackColor];
    fieldName.textColor = [UIColor whiteColor];
    fieldName.text = [StringGrabber concatenate:expField.field_name withHardcodedString:@"colon"];
    
    UITextField *fieldContents = [[UITextField alloc] initWithFrame:CGRectMake(0, Y_FIELDCONTENTS, 730, 35)];
    fieldContents.backgroundColor = [UIColor whiteColor];
    fieldContents.font = [UIFont systemFontOfSize:24];
    
    if (type != TYPE_DEFAULT) {
        fieldContents.text = @"Auto";
        fieldContents.enabled = NO;
        //fieldContents.editable = NO;
        fieldContents.backgroundColor = [UIColor grayColor];
    }
    
    if (expField.type_id == [NSNumber numberWithInt:TEXT]) {
        fieldContents.keyboardType = UIKeyboardTypeNamePhonePad;
        // TOOO - restrict amount of chars to 60
        // TODO - restrict digits
    } else {
        fieldContents.keyboardType = UIKeyboardTypeNumberPad;
        // TODO - restrict # to 20 chars
        // TODO - restrict nums
    }
    
    [scrollView addSubview:fieldName];
    [scrollView addSubview:fieldContents];        
}

@end
