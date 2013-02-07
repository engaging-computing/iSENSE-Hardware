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

#define TYPE_DEFAULT 0
#define TYPE_LATITUDE 1
#define TYPE_LONGITUDE 2
#define TYPE_TIME 3

#define SCROLLVIEW_Y_OFFSET 50
#define SCROLLVIEW_OBJ_INCR 30
#define SCROLLVIEW_LABEL_HEIGHT 20
#define SCROLLVIEW_TEXT_HEIGHT 35

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
     sessionNameInput.delegate = self;
	 sessionNameInput.enablesReturnKeyAutomatically = NO;
	 
	 UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];          
	 self.navigationItem.rightBarButtonItem = menuButton;
	 [menuButton release];
	 
	 iapi = [iSENSE getInstance];
     [iapi toggleUseDev:YES];
	 
	 [self initLocations]; //* make initLocations do something
	 
     if ([iapi isLoggedIn]) {
         loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
     } else {
         loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"_"]; 
     }
     
     scrollView.indicatorStyle = UIScrollViewIndicatorStyleWhite;
     [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
     
	 //* if exp. # is null, launch the dialog for choosing exp. num
     expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:@"_"];
     
     UITapGestureRecognizer *tapGestureM = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideKeyboard)];
     tapGestureM.cancelsTouchesInView = NO;
     [self.view addGestureRecognizer:tapGestureM];
     [tapGestureM release];
     
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

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (IBAction) saveOnClick:(id)sender {
	//* if exp is null, toast
	//* else if sessionName's length = 0, .setError
	//* else SavaDataTask
    [self getDataFromFields];
}

- (IBAction) clearOnClick:(id)sender {
	sessionNameInput.text = @"";
    
    for (UIView *element in scrollView.subviews) {
        if ([element isKindOfClass:[UITextField class]]) {
            if (!([((UITextField *) element).text isEqualToString:[StringGrabber getString:@"auto_lat" ]] ||
                  [((UITextField *) element).text isEqualToString:[StringGrabber getString:@"auto_long"]] ||
                  [((UITextField *) element).text isEqualToString:[StringGrabber getString:@"auto_time"]] ))
                
                ((UITextField *) element).text = @"";
        
        }
    }
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
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message show];
            [message release];
            
        } else if (buttonIndex == OPTION_BROWSE_EXPERIMENTS) {
            
            
        } else if (buttonIndex == OPTION_SCAN_QR_CODE) {
            
            ZXingWidgetController *widController = [[ZXingWidgetController alloc] initWithDelegate:(id <ZXingDelegate>)self
                                                                                        showCancel:YES
                                                                                          OneDMode:NO];
            QRCodeReader* qRCodeReader = [[QRCodeReader alloc] init];
            
            NSSet *readers = [[NSSet alloc] initWithObjects:qRCodeReader,nil];
            widController.readers = readers;
            
            [self presentModalViewController:widController animated:YES];
            
        }
        
    } else if (actionSheet.tag == MENU_UPLOAD) {
        
    } else if (actionSheet.tag == EXPERIMENT_MANUAL_ENTRY) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            [iapi setCurrentExp:[[[actionSheet textFieldAtIndex:0] text] intValue]];
            expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num"
                                                                    with:[NSString stringWithFormat:@"%d", [iapi getCurrentExp]]];
            
            [self fillDataFieldEntryList:[iapi getCurrentExp]];            
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
	//if (textField == sessionNameInput) {
		NSUInteger newLength = [textField.text length] + [string length] - range.length;
		return (newLength > 25) ? NO : YES;
	//}
	//return YES;
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

- (void) upload:(NSMutableArray *)results {
    if ([iapi getCurrentExp] == 0) {
        [self.view makeToast:@"Please Enter an Experiment # First"
                    duration:3.5
                    position:@"bottom"
                       image:@"red_x"];
        return;
    }
    if (!([iapi isLoggedIn])) {
        [self.view makeToast:@"Please Login First"
                    duration:3.5
                    position:@"bottom"
                       image:@"red_x"];
        return;
    }
    if ([[sessionNameInput text] isEqualToString:@""]) {
        [self.view makeToast:@"Please Enter a Session Name First"
                    duration:3.5
                    position:@"bottom"
                       image:@"red_x"];
        return;
    }
    
    NSString *name = [[[NSString alloc] initWithString:[sessionNameInput text]] autorelease];
    NSString *description = [[[NSString alloc] initWithString:@"Manual data entry from the iOS Data Collector application."] autorelease];
    NSString *street = [[[NSString alloc] initWithString:@"1 University Ave"] autorelease];
    NSString *city = [[[NSString alloc] initWithString:@"Lowell, MA"] autorelease];
    NSString *country = [[[NSString alloc] initWithString:@"United States"] autorelease];
    NSNumber *exp_num = [[[NSNumber alloc] initWithInt:[iapi getCurrentExp]] autorelease];
    NSNumber *session_num = [iapi createSession:name withDescription:description Street:street City:city Country:country toExperiment:exp_num];
    NSError *error = nil;
    NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:results options:0 error:&error];

    
    if ([iapi putSessionData:dataJSON forSession:session_num inExperiment:exp_num]) {
        [self.view makeToast:@"Upload Success!"
                    duration:2.0
                    position:@"bottom"
                       image:@"check"];
    } else {
        [self.view makeToast:@"Upload Failed!"
                    duration:2.0
                    position:@"bottom"
                       image:@"red_x"];
    }
}

// TODO allows for GPS to be recorded
- (void) initLocations {
	
}

- (void) fillDataFieldEntryList:(int)eid {
    
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    NSLog(@"eid = %d", eid);
    NSMutableArray *fieldOrder = [iapi getExperimentFields:[NSNumber numberWithInt:eid]];
    int objNumber = 0;
    int scrollHeight = 0;
    
    for (ExperimentField *expField in fieldOrder) {
        
        if (expField.type_id.intValue == GEOSPACIAL || expField.type_id.intValue == TIME) {
            if (expField.unit_id.intValue == UNIT_LATITUDE) {
                scrollHeight = [self addDataField:expField withType:TYPE_LATITUDE andObjNumber:objNumber];
            } else if (expField.unit_id.intValue == UNIT_LONGITUDE) {
                scrollHeight = [self addDataField:expField withType:TYPE_LONGITUDE andObjNumber:objNumber];
            } else /* Time */ {
                scrollHeight = [self addDataField:expField withType:TYPE_TIME andObjNumber:objNumber];
            }
        } else {
            scrollHeight = [self addDataField:expField withType:TYPE_DEFAULT andObjNumber:objNumber];
        }
        
        ++objNumber;
    }
    
    scrollHeight += SCROLLVIEW_TEXT_HEIGHT;
    CGFloat scrollWidth = scrollView.frame.size.width;
    [scrollView setContentSize:CGSizeMake(scrollWidth, scrollHeight)];
   
    if (scrollView.subviews.count == 0) {
        UILabel *noFields = [[UILabel alloc] initWithFrame:CGRectMake(0, SCROLLVIEW_Y_OFFSET, 730, SCROLLVIEW_LABEL_HEIGHT)];
        noFields.text = @"Invalid experiment.";
        noFields.backgroundColor = [HexColor colorWithHexString:@"000000"];
        noFields.textColor = [HexColor colorWithHexString:@"FFFFFF"];
        [scrollView addSubview: noFields];
    }
    
}

- (int) addDataField:(ExperimentField *)expField withType:(int)type andObjNumber:(int)objNum {
    
    CGFloat Y_FIELDNAME = SCROLLVIEW_Y_OFFSET + (objNum * SCROLLVIEW_Y_OFFSET);
    CGFloat Y_FIELDCONTENTS = Y_FIELDNAME + SCROLLVIEW_OBJ_INCR;
    
    UILabel *fieldName;
    if (objNum == 0) {
        fieldName = [[UILabel alloc] initWithFrame:CGRectMake(0, Y_FIELDNAME, 730, SCROLLVIEW_LABEL_HEIGHT)];
    } else {
        Y_FIELDNAME += (SCROLLVIEW_OBJ_INCR * objNum);
        Y_FIELDCONTENTS += (SCROLLVIEW_OBJ_INCR * objNum);
        fieldName = [[UILabel alloc] initWithFrame:CGRectMake(0, Y_FIELDNAME, 730, SCROLLVIEW_LABEL_HEIGHT)];
    }
    fieldName.backgroundColor = [UIColor blackColor];
    fieldName.textColor = [UIColor whiteColor];
    fieldName.text = [StringGrabber concatenate:expField.field_name withHardcodedString:@"colon"];
    
    UITextField *fieldContents = [[UITextField alloc] initWithFrame:CGRectMake(0, Y_FIELDCONTENTS, 730, SCROLLVIEW_TEXT_HEIGHT)];
    fieldContents.delegate = self;
    fieldContents.backgroundColor = [UIColor whiteColor];
    fieldContents.font = [UIFont systemFontOfSize:24];
    
    if (type != TYPE_DEFAULT) {
        fieldContents.enabled = NO;
        if (type == TYPE_LATITUDE) {
            fieldContents.text = [StringGrabber getString:@"auto_lat"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"666666"];
        } else if (type == TYPE_LONGITUDE) {
            fieldContents.text = [StringGrabber getString:@"auto_long"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"666666"];
        } else {
            fieldContents.text = [StringGrabber getString:@"auto_time"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"666666"];
        }
    }
    
    if (expField.type_id.intValue == TEXT) {
        fieldContents.keyboardType = UIKeyboardTypeNamePhonePad;
        // TOOO - restrict amount of chars to 60
        // TODO - restrict digits
    } else {
        fieldContents.keyboardType = UIKeyboardTypeNumbersAndPunctuation;
        // TODO - restrict # to 20 chars
        // TODO - restrict nums
    }
    [fieldContents setReturnKeyType:UIReturnKeyDone];
    
    [scrollView addSubview:fieldName];
    [scrollView addSubview:fieldContents];
    
    return (int) Y_FIELDCONTENTS;
}

- (void) getDataFromFields { // TODO - this stuff
    NSMutableArray *data = [[NSMutableArray alloc] init];
    int count = 0;
    
    for (UIView *element in scrollView.subviews) {
        if ([element isKindOfClass:[UITextField class]]) {
            if ([((UITextField *) element).text isEqualToString:[StringGrabber getString:@"auto_lat"]]) {
                
                // get geospacial latitude co-ordinate, store it in data
                [data addObject:@"ADD LATITUDE"];

            } else if ([((UITextField *) element).text isEqualToString:[StringGrabber getString:@"auto_long"]]) {
                
                // get geospacial longitude co-ordinate, store it in data
                [data addObject:@"ADD LONGITUDE"];
                
            } else if ([((UITextField *) element).text isEqualToString:[StringGrabber getString:@"auto_time"]]) {
                
                long timeStamp = [[NSDate date] timeIntervalSince1970];
                NSString *currentTime = [[NSString stringWithFormat:@"%ld", timeStamp] stringByAppendingString:@"000"];
                [data addObject:currentTime];
                
            } else {
                
                if ([((UITextField *) element).text length] != 0)
                    [data addObject:((UITextField *) element).text];
                else
                    [data addObject:@""];
                
            }
        }
        count++;
    }
    
    NSMutableArray *dataEncapsulator = [[NSMutableArray alloc] init];
    [dataEncapsulator addObject:data];
    [self upload:data];
    
    
    /*NSLog(@"Count = %d", count);
    int i = 0;
    for (id object in data) {
        NSLog(@"Obj %d = %@", ++i, object);
    }
    
    if (count)
        [self.view makeToast:@"Successfully saved data!" duration:2.0 position:@"bottom" image:@"check"];
    else
        [self.view makeToast:@"No data to save!" duration:2.0 position:@"bottom" image:@"red_x"];*/
    
    [data release];
    [dataEncapsulator release];
}

- (void)hideKeyboard {
    [sessionNameInput resignFirstResponder];
    for (UIView *element in scrollView.subviews) {
        if ([element isKindOfClass:[UITextField class]]) {
            [element resignFirstResponder];
        }
    }
}

@end
