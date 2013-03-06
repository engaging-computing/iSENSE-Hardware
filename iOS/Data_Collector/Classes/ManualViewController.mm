//
//  ManualView.mm
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ManualViewController.h"
#import "ManualConstants.h"
#import "Data_CollectorAppDelegate.h"

@implementation ManualViewController

@synthesize logo, loggedInAsLabel, expNumLabel, upload, clear, sessionNameInput, media, scrollView, activeField;
@synthesize sessionName, expNum, qrResults, locationManager;


- (void) viewDidLoad {
    [super viewDidLoad];
    
    // allocations
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu"
                                                                   style:UIBarButtonItemStylePlain
                                                                  target:self
                                                                  action:@selector(displayMenu:)];
    self.navigationItem.rightBarButtonItem = menuButton;
    [menuButton release];
    
    UITapGestureRecognizer *tapGestureM = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                                  action:@selector(hideKeyboard)];
    tapGestureM.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tapGestureM];
    [tapGestureM release];
    
    [self initLocations];
    
    // iSENSE API
    iapi = [iSENSE getInstance];
    [iapi toggleUseDev:YES];
    if ([iapi isLoggedIn])
        loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
    else
        loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"_"];
    
    // scrollview
    [self.view sendSubviewToBack:scrollView];
    scrollView.indicatorStyle = UIScrollViewIndicatorStyleWhite;
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    // session name
    [self.sessionNameInput addTarget:self
                              action:@selector(textFieldFinished:)
                    forControlEvents:UIControlEventEditingDidEndOnExit];
    sessionNameInput.delegate = self;
    sessionNameInput.enablesReturnKeyAutomatically = NO;
    sessionNameInput.borderStyle = UITextBorderStyleRoundedRect;
    
    // experiment number
    if (expNum && expNum != 0) {
        expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:[NSString stringWithFormat:@"%d", expNum]];
        [self fillDataFieldEntryList:expNum];
    } else
        expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:@"_"];
    
    /////Try to upload an image//////
    
    /*UIImage *image = [UIImage imageNamed:@"logo_manual.png"];
    [iapi login:@"sor" with:@"sor"];
    bool success = [iapi upload:image toExperiment:[NSNumber numberWithInt:281] forSession:[NSNumber numberWithInt:6352] withName:@"Name" andDescription:@"Description"];
    NSLog(@"Image success = %d", success);*/
    
    /////////////////////////////////
    
    [self registerForKeyboardNotifications];
    
}

// Sets up listeners for keyboard
- (void) registerForKeyboardNotifications {
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWasShown:)
                                                 name:UIKeyboardDidShowNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillBeHidden:)
                                                 name:UIKeyboardWillHideNotification
                                               object:nil];
}

// Unregisters listeners for keyboard
- (void) unregisterKeyboardNotifications {
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIKeyboardDidShowNotification
                                                  object:nil];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                 name:UIKeyboardWillHideNotification
                                                  object:nil];
}

// Called when the UIKeyboardDidShowNotification is sent.
- (void)keyboardWasShown:(NSNotification*)aNotification {
    
    if (activeField.tag == TAG_TEXT || activeField.tag == TAG_NUMERIC) {
        NSDictionary* info = [aNotification userInfo];
    
        CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;

    
        CGRect aRect = self.view.frame;
        aRect.size.height -= kbSize.height;
        CGPoint origin = activeField.frame.origin;
        //origin.y -= scrollView.contentOffset.y;
        if (!CGRectContainsPoint(aRect, origin) ) {
            if ([UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationLandscapeLeft || [UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationLandscapeRight) {
                //[scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height+30)];
                self.view.frame = CGRectMake(0.0, (aRect.size.height+30), self.view.frame.size.width, self.view.frame.size.height);
            }
        }
        if ([UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationPortrait || [UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationPortraitUpsideDown)
            self.view.frame = CGRectMake(0.0, -(self.view.frame.size.height - aRect.size.height), self.view.frame.size.width, self.view.frame.size.height);
    }
    
    [scrollView setScrollEnabled:YES];
}
// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification {
    self.view.frame = CGRectMake(0.0, 0.0, self.view.frame.size.width, self.view.frame.size.height);
    //[scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height-30)];
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
    activeField = textField;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    activeField = nil;
}


- (IBAction) textFieldFinished:(id)sender {}


- (void) didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void) viewDidUnload {
    //[locationManager stopUpdatingLocation];
    [super viewDidUnload];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    //[self updateExpNumLabel];
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
    [qrResults release];
    [widController release];
    
    [locationManager release];
    locationManager = nil;
    
    [self unregisterKeyboardNotifications];
    
	[super dealloc];
}

- (void) initLocations {
    if (!locationManager) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        locationManager.distanceFilter = kCLDistanceFilterNone;
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        [locationManager startUpdatingLocation];
    }
}

// method not called on real device - don't assign a location to a global variable here
- (void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    CLLocation *location = [locations lastObject];
    NSLog(@"lat: %f - lon: %f", location.coordinate.latitude, location.coordinate.longitude);
}

// pre-iOS6 rotating options
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

// iOS6 rotating options
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 interface orientations
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

// displays the correct xib based on orientation and device type
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}

// overridden to keep soft keyboard off screen when not editting a text field
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
                                 otherButtonTitles:@"Experiment", @"Login", nil];
	popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	[popupQuery showInView:self.view];
	[popupQuery release];
}

- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
	UIAlertView *message;
    
	switch (buttonIndex) {
		case MENU_EXPERIMENT:
            message = [[UIAlertView alloc] initWithTitle:nil
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
            
            ExperimentBrowseViewController *browseView = [[ExperimentBrowseViewController alloc] init];
            browseView.title = @"Browse for Experiments";
            browseView.chosenExperiment = &expNum;
            [self.navigationController pushViewController:browseView animated:YES];
            [browseView release];
            
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
        
    } else if (actionSheet.tag == EXPERIMENT_MANUAL_ENTRY) {
        
        if (buttonIndex != OPTION_CANCELED) {

            expNum = [[[actionSheet textFieldAtIndex:0] text] intValue];
            expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num"
                                                                    with:[NSString stringWithFormat:@"%d", expNum]];
            
            [self fillDataFieldEntryList:expNum];
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
        expNum = [[split objectAtIndex:1] intValue];
        [self fillDataFieldEntryList:expNum];
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
        
        if (textField.tag == TAG_NUMERIC) {
            if (![self containsAcceptedNumbers:string])
                return NO;
        
            // ensure we have only 1 decimal and only 1 negative sign (correctly placed)
            if ([string isEqualToString:@"-"] || [string isEqualToString:@"."]) {
                NSString *currString = [textField text];
                NSString *resultStr = [textField.text stringByReplacingCharactersInRange:range withString:string];
                NSArray *components = [currString componentsSeparatedByString:@"-"];
                NSUInteger count = [components count] - 1 + ([string isEqualToString:@"-"] ? 1 : 0);
                if (count > 1) return NO;
                if (count == 1) {
                    if ([[textField text] length] == 0)
                        return YES;
                    else {
                        NSString *firstChar = [resultStr substringToIndex:1];
                        if (![firstChar isEqualToString:@"-"]) return NO;
                    }
                }
                components = [currString componentsSeparatedByString:@"."];
                count = [components count] - 1 + ([string isEqualToString:@"."] ? 1 : 0);
                if (count > 1) return NO;
            }
            
        }
            
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
        [[NSCharacterSet characterSetWithCharactersInString:
        [StringGrabber grabString:@"accepted_chars"]] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (BOOL) containsAcceptedNumbers:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:
      [StringGrabber grabString:@"accepted_numbers"]] invertedSet];
    
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

- (void) upload:(NSMutableArray *)results {
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Uploading data set..."];
    [message show];
      
    dispatch_queue_t queue = dispatch_queue_create("manual_upload_from_upload_function", NULL);
    dispatch_async(queue, ^{
        BOOL exp = TRUE, loggedIn = TRUE, hasSessionName = TRUE;
        short uploadSuccess = -1;
        
        if (expNum == 0)
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
                    NSNumber *exp_num = [[[NSNumber alloc] initWithInt:expNum] autorelease];
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
            [scrollView setContentSize:CGSizeMake(scrollWidth, scrollHeight+40/* +40 for troll in keyboard code*/)];
            
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
        fieldName = [[UILabel alloc] initWithFrame:[self setScrollViewItem:UI_FIELDNAME toSizeWithY:Y_FIELDNAME]];
    } else {
        Y_FIELDNAME += (SCROLLVIEW_OBJ_INCR * objNum);
        Y_FIELDCONTENTS += (SCROLLVIEW_OBJ_INCR * objNum);
        fieldName = [[UILabel alloc] initWithFrame:[self setScrollViewItem:UI_FIELDNAME toSizeWithY:Y_FIELDNAME]];
    }
    fieldName.backgroundColor = [UIColor blackColor];
    fieldName.textColor = [UIColor whiteColor];
    fieldName.text = [StringGrabber concatenate:expField.field_name withHardcodedString:@"colon"];
    
    UITextField *fieldContents = [[UITextField alloc] initWithFrame:[self setScrollViewItem:UI_FIELDCONTENTS toSizeWithY:Y_FIELDCONTENTS]];
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
        fieldContents.tag = TAG_TEXT;
        // TODO - restrict amount of chars to 60, restrict digits
    } else {
        fieldContents.keyboardType = UIKeyboardTypeNumbersAndPunctuation;
        fieldContents.tag = TAG_NUMERIC;
        // TODO - restrict # to 20 chars, restrict nums
    }
    [fieldContents setReturnKeyType:UIReturnKeyDone];
    
    [scrollView addSubview:fieldName];
    [scrollView addSubview:fieldContents];
    
    [fieldName release];
    [fieldContents release];
    
    return (int) Y_FIELDCONTENTS;
}

- (CGRect) setScrollViewItem:(int)type toSizeWithY:(CGFloat)y {
    
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    // if needed, check orientation == 0 for a failsafe (should be true if it's portrait)
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
            if (type == UI_FIELDNAME) {
               return CGRectMake(0, y, IPAD_WIDTH_PORTRAIT, SCROLLVIEW_LABEL_HEIGHT);
            } else {
               return CGRectMake(0, y, IPAD_WIDTH_PORTRAIT, SCROLLVIEW_TEXT_HEIGHT);
            }
        } else {
            if (type == UI_FIELDNAME) {
                return CGRectMake(0, y, IPAD_WIDTH_LANDSCAPE, SCROLLVIEW_LABEL_HEIGHT);
            } else {
                return CGRectMake(0, y, IPAD_WIDTH_LANDSCAPE, SCROLLVIEW_TEXT_HEIGHT);
            }
        }
    } else {
        if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
            if (type == UI_FIELDNAME) {
                return CGRectMake(0, y, IPHONE_WIDTH_PORTRAIT, SCROLLVIEW_LABEL_HEIGHT);
            } else {
                return CGRectMake(0, y, IPHONE_WIDTH_PORTRAIT, SCROLLVIEW_TEXT_HEIGHT);
            }
        } else {
            if (type == UI_FIELDNAME) {
                return CGRectMake(0, y-50, IPHONE_WIDTH_LANDSCAPE, SCROLLVIEW_LABEL_HEIGHT);
            } else {
                return CGRectMake(0, y-50, IPHONE_WIDTH_LANDSCAPE, SCROLLVIEW_TEXT_HEIGHT);
            }
        }
    }
}

- (void) getDataFromFields {
    NSMutableArray *data = [[NSMutableArray alloc] init];
    int count = 0;
    
    for (UIView *element in scrollView.subviews) {
        if ([element isKindOfClass:[UITextField class]]) {
            if ([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_lat"]]) {
                
                CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
                double lat = lc2d.latitude;
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
    spinner.center = CGPointMake(139.5, 75.5);
    [message addSubview:spinner];
    [spinner startAnimating];
    [spinner release];
    return [message autorelease];
}

@end
