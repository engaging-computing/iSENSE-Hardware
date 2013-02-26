//
//  ManualView.mm
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ManualViewController.h"
#import "Data_CollectorAppDelegate.h"

#define MENU_UPLOAD                   0
#define MENU_EXPERIMENT               1
#define MENU_LOGIN                    2
#define EXPERIMENT_MANUAL_ENTRY       3
#define EXPERIMENT_BROWSE_EXPERIMENTS 4
#define CLEAR_FIELDS_DIALOG           5

#define OPTION_CANCELED                0
#define OPTION_ENTER_EXPERIMENT_NUMBER 1
#define OPTION_BROWSE_EXPERIMENTS      2
#define OPTION_SCAN_QR_CODE            3

#define TYPE_DEFAULT   0
#define TYPE_LATITUDE  1
#define TYPE_LONGITUDE 2
#define TYPE_TIME      3

#define SCROLLVIEW_Y_OFFSET     50
#define SCROLLVIEW_OBJ_INCR     30
#define SCROLLVIEW_LABEL_HEIGHT 20
#define SCROLLVIEW_TEXT_HEIGHT  35

@implementation ManualViewController

@synthesize logo, loggedInAsLabel, expNumLabel, upload, clear, sessionNameInput, media, scrollView;
@synthesize sessionName, expNum, qrResults, locationManager;


- (void) viewDidLoad {
    [super viewDidLoad];
    
    [self.view sendSubviewToBack:scrollView];
    
    [self.sessionNameInput addTarget:self
                              action:@selector(textFieldFinished:)
                    forControlEvents:UIControlEventEditingDidEndOnExit];
    sessionNameInput.delegate = self;
    sessionNameInput.enablesReturnKeyAutomatically = NO;
    sessionNameInput.borderStyle = UITextBorderStyleRoundedRect;
    
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];
    self.navigationItem.rightBarButtonItem = menuButton;
    [menuButton release];
    
    iapi = [iSENSE getInstance];
    [iapi toggleUseDev:YES];
    
    [self initLocations];
    
    if ([iapi isLoggedIn]) {
        loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
    } else {
        loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"_"];
    }
    
    scrollView.indicatorStyle = UIScrollViewIndicatorStyleWhite;
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:@"_"];
    
    UITapGestureRecognizer *tapGestureM = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideKeyboard)];
    tapGestureM.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tapGestureM];
    [tapGestureM release];
    
    /////Try to upload an image//////
    
    /*UIImage *image = [UIImage imageNamed:@"logo_manual.png"];
    [iapi login:@"sor" with:@"sor"];
    bool success = [iapi upload:image toExperiment:[NSNumber numberWithInt:281] forSession:[NSNumber numberWithInt:6352] withName:@"Name" andDescription:@"Description"];
    NSLog(@"Image success = %d", success);*/
    
    /////////////////////////////////
}

- (IBAction) textFieldFinished:(id)sender {}


- (void) didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void) viewDidUnload {
    //[locationManager stopUpdatingLocation];
    [super viewDidUnload];
}


- (void) dealloc {
	[logo release];
	[loggedInAsLabel release];
	[expNumLabel release];
	[upload release];
	[clear release];
	[sessionNameInput release];
	[media release];
	[scrollView release];
	
	[sessionName release];
    [expNum release];
    [qrResults release];
    [widController release];
    
    [locationManager release];
    
	[super dealloc];
}

- (void) initLocations {
	locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    locationManager.distanceFilter = kCLDistanceFilterNone;
    locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    [locationManager startUpdatingLocation];
}

// method not called on real device - don't assign a location to a global variable here
- (void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    CLLocation *location = [locations lastObject];
    NSLog(@"lat: %f - lon: %f", location.coordinate.latitude, location.coordinate.longitude);
}

/*- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

// iOS6
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:[NSString stringWithFormat:@"%@-landscape-ipad", NSStringFromClass([self class])]
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:[NSString stringWithFormat:@"%@~ipad", NSStringFromClass([self class])]
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        
    }
}*/

- (BOOL) textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (IBAction) uploadOnClick:(id)sender {
    [self getDataFromFields];
}

- (IBAction) clearOnClick:(id)sender {
	UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Are you sure you want to clear your session name and all field data?"
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:@"Cancel"
                                            otherButtonTitles:@"Okay", nil];
    [message setTag:CLEAR_FIELDS_DIALOG];
    [message setAlertViewStyle:UIAlertViewStyleDefault];
    [message show];
    [message release];
}

- (IBAction) mediaOnClick:(id)sender {
    if (sessionNameInput.text.length != 0)
        [CameraUsage useCamera];
    else
        [self.view makeToast:@"Please Enter a Session Name First"
                    duration:3.0
                    position:@"bottom"
                       image:@"red_x"];
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

- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
	UIAlertView *message;
    
	switch (buttonIndex) {
		case MENU_UPLOAD:
			message = [[UIAlertView alloc] initWithTitle:@"Upload"
                                                 message:@"Would you like to upload your data to iSENSE?"
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            message.tag = MENU_UPLOAD;
            [message show];
            [message release];
            
			break;
            
		case MENU_EXPERIMENT:
            message = [[UIAlertView alloc] initWithTitle:@"Experiment Selection"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Enter Experiment #", @"Browse", @"Scan QR Code", nil];
            message.tag = MENU_EXPERIMENT;
            [message show];
            [message release];
            
			break;
            
		case MENU_LOGIN:
            message = [[UIAlertView alloc] initWithTitle:@"Login"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            message.tag = MENU_LOGIN;
			[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
            [message show];
            [message release];
            
            break;
            
		default:
			break;
	}
	
}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
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
            
            if([[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo] supportsAVCaptureSessionPreset:AVCaptureSessionPresetMedium]){
                
                widController = [[ZXingWidgetController alloc] initWithDelegate:self
                                                                     showCancel:YES
                                                                       OneDMode:NO];
                QRCodeReader* qRCodeReader = [[QRCodeReader alloc] init];
                
                NSSet *readers = [[NSSet alloc] initWithObjects:qRCodeReader,nil];
                widController.readers = readers;
                
                [self presentModalViewController:widController animated:YES];
                [qRCodeReader release];
                [readers release];
                
            } else {
                
                UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"You device does not have a camera that supports QR Code scanning."
                                                                  message:nil
                                                                 delegate:self
                                                        cancelButtonTitle:@"Cancel"
                                                        otherButtonTitles:nil];
                
                [message setAlertViewStyle:UIAlertViewStyleDefault];
                [message show];
                [message release];
                
            }
            
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
            
            // TODO - get Jeremy's experiment browsing code when he's done
        }
        
    } else if (actionSheet.tag == CLEAR_FIELDS_DIALOG) {
        
        if (buttonIndex != OPTION_CANCELED) {
            sessionNameInput.text = @"";
            
            for (UIView *element in scrollView.subviews) {
                if ([element isKindOfClass:[UITextField class]]) {
                    if (!([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_lat" ]] ||
                          [((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_long"]] ||
                          [((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_time"]] ))
                        
                        ((UITextField *) element).text = @"";
                    
                }
            }
        }
    }
}

- (void) zxingController:(ZXingWidgetController*)controller didScanResult:(NSString *)result {
    [widController.view removeFromSuperview];
    
    qrResults = [result retain];
    NSArray *split = [qrResults componentsSeparatedByString:@"="];
    if ([split count] != 2) {
        [self.view makeToast:@"Invalid QR code scanned"
                    duration:3.0
                    position:@"bottom"
                       image:@"red_x"];
    } else {
        expNum = [NSNumber numberWithInt:[[split objectAtIndex:1] intValue]];
        [iapi setCurrentExp:[expNum intValue]];
        [self fillDataFieldEntryList:[expNum intValue]];
    }
}

- (void) zxingControllerDidCancel:(ZXingWidgetController*)controller {
    [widController.view removeFromSuperview];
}

- (BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    
    if (![self containsAcceptedCharacters:string])
        return NO;
    
	if (textField == sessionNameInput) {
        
		NSUInteger newLength = [textField.text length] + [string length] - range.length;
		return (newLength > 52) ? NO : YES;
        
	} else {
        
        NSUInteger newLength = [textField.text length] + [string length] - range.length;
        if (newLength > 25)
            return NO;
        else
            return YES;
        
    }
    return YES;
}

- (BOOL) containsAcceptedCharacters:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:[StringGrabber grabString:@"accepted_chars"]] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Logging in..."];
    [message show];
        
    dispatch_queue_t queue = dispatch_queue_create("manual_login_from_login_function", NULL);
    dispatch_async(queue, ^{
        BOOL success = [iapi login:usernameInput with:passwordInput];
        dispatch_async(dispatch_get_main_queue(), ^{
            if (success) {
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
            [message dismissWithClickedButtonIndex:nil animated:YES];
        });
    });
    
}

- (void) experiment {
	[self.view makeToast:@"Experiment!"
				duration:2.0
				position:@"bottom"
                   image:@"red_x"];
}

- (void) upload:(NSMutableArray *)results {
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Uploading data set..."];
    [message show];
      
    dispatch_queue_t queue = dispatch_queue_create("manual_upload_from_upload_function", NULL);
    dispatch_async(queue, ^{
        BOOL exp = TRUE, loggedIn = TRUE, hasSessionName = TRUE;
        short uploadSuccess = -1;
        
        if ([iapi getCurrentExp] == 0)
            exp = FALSE;
        
        else
            if (!([iapi isLoggedIn]))
                loggedIn = FALSE;
        
            else
                if ([[sessionNameInput text] isEqualToString:@""])
                    hasSessionName = FALSE;
        
                else {
                    
                    NSString *name = [[[NSString alloc] initWithString:[sessionNameInput text]] autorelease];
                    NSString *description = [[[NSString alloc] initWithString:@"Manual data entry from the iOS Data Collector application."] autorelease];
                    NSString *street = [[[NSString alloc] initWithString:@"1 University Ave"] autorelease];
                    NSString *city = [[[NSString alloc] initWithString:@"Lowell, MA"] autorelease];
                    NSString *country = [[[NSString alloc] initWithString:@"United States"] autorelease];
                    NSNumber *exp_num = [[[NSNumber alloc] initWithInt:[iapi getCurrentExp]] autorelease];
                    NSNumber *session_num = [iapi createSession:name withDescription:description Street:street City:city Country:country toExperiment:exp_num];
                    NSError  *error = nil;
                    NSData   *dataJSON = [NSJSONSerialization dataWithJSONObject:results options:0 error:&error];
                    
                    
                    if (([iapi putSessionData:dataJSON forSession:session_num inExperiment:exp_num]))
                        uploadSuccess = TRUE;
                    else
                        uploadSuccess = FALSE;
                    
                }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if (!exp)
                [self.view makeToast:@"Please Enter an Experiment # First"
                            duration:3.5
                            position:@"bottom"
                               image:@"red_x"];
            if (!loggedIn)
                [self.view makeToast:@"Please Login First"
                            duration:3.5
                            position:@"bottom"
                               image:@"red_x"];
            if (!hasSessionName)
                [self.view makeToast:@"Please Enter a Session Name First"
                            duration:3.5
                            position:@"bottom"
                               image:@"red_x"];
            if (uploadSuccess != -1) {
                if (uploadSuccess)
                    [self.view makeToast:@"Upload Success!"
                                duration:2.0
                                position:@"bottom"
                                   image:@"check"];
                else
                    [self.view makeToast:@"Upload Failed!"
                                duration:2.0
                                position:@"bottom"
                                   image:@"check"];
            }
            
            [message dismissWithClickedButtonIndex:nil animated:YES];
        });
    });
}

- (void) fillDataFieldEntryList:(int)eid {
    
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Retrieving experiment fields..."];
    [message show];
        
    dispatch_queue_t queue = dispatch_queue_create("manual_upload_from_upload_function", NULL);
    dispatch_async(queue, ^{
        
        NSMutableArray *fieldOrder = [[iapi getExperimentFields:[NSNumber numberWithInt:eid]] retain];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
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
            
            [fieldOrder release];
            
            scrollHeight += SCROLLVIEW_TEXT_HEIGHT;
            CGFloat scrollWidth = scrollView.frame.size.width;
            [scrollView setContentSize:CGSizeMake(scrollWidth, scrollHeight)];
            
            if (scrollView.subviews.count == 0) {
                
                UILabel *noFields = [[UILabel alloc] initWithFrame:CGRectMake(0, SCROLLVIEW_Y_OFFSET, 730, SCROLLVIEW_LABEL_HEIGHT)];
                noFields.text = @"Invalid experiment.";
                noFields.backgroundColor = [HexColor colorWithHexString:@"000000"];
                noFields.textColor = [HexColor colorWithHexString:@"FFFFFF"];
                [scrollView addSubview: noFields];
                [noFields release];
            }
            
            [message dismissWithClickedButtonIndex:nil animated:YES];
            
        });
        
    });
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
    fieldContents.borderStyle = UITextBorderStyleRoundedRect;
    
    if (type != TYPE_DEFAULT) {
        fieldContents.enabled = NO;
        if (type == TYPE_LATITUDE) {
            fieldContents.text = [StringGrabber grabString:@"auto_lat"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"666666"];
        } else if (type == TYPE_LONGITUDE) {
            fieldContents.text = [StringGrabber grabString:@"auto_long"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"666666"];
        } else {
            fieldContents.text = [StringGrabber grabString:@"auto_time"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"666666"];
        }
    }
    
    if (expField.type_id.intValue == TEXT) {
        fieldContents.keyboardType = UIKeyboardTypeNamePhonePad;
        // TODO - restrict amount of chars to 60, restrict digits
    } else {
        fieldContents.keyboardType = UIKeyboardTypeNumbersAndPunctuation;
        // TODO - restrict # to 20 chars, restrict nums
    }
    [fieldContents setReturnKeyType:UIReturnKeyDone];
    
    [scrollView addSubview:fieldName];
    [scrollView addSubview:fieldContents];
    
    [fieldName release];
    [fieldContents release];
    
    return (int) Y_FIELDCONTENTS;
}

- (void) getDataFromFields {
    NSMutableArray *data = [[NSMutableArray alloc] init];
    int count = 0;
    
    for (UIView *element in scrollView.subviews) {
        if ([element isKindOfClass:[UITextField class]]) {
            if ([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_lat"]]) {
                
                CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
                double lat  = lc2d.latitude;
                NSString *latitude = [NSString stringWithFormat:@"%lf", lat];
                [data addObject:latitude];
                
            } else if ([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_long"]]) {
                
                CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
                double lon = lc2d.longitude;
                NSString *longitude = [NSString stringWithFormat:@"%lf", lon];
                [data addObject:longitude];
                
            } else if ([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_time"]]) {
                
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
    [self upload:dataEncapsulator];
    
    [data release];
    [dataEncapsulator release];
}

- (void) hideKeyboard {
    [sessionNameInput resignFirstResponder];
    for (UIView *element in scrollView.subviews) {
        if ([element isKindOfClass:[UITextField class]]) {
            [element resignFirstResponder];
        }
    }
}

- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString {
    UIAlertView *message = [[UIAlertView alloc] initWithTitle:dString
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:nil
                                            otherButtonTitles:nil];
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    spinner.center = CGPointMake(139.5, 75.5); // .5 so it doesn't blur
    [message addSubview:spinner];
    [spinner startAnimating];
    [spinner release];
    return [message autorelease];
}

@end
