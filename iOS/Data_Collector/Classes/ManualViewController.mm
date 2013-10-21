//
//  ManualView.mm
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ManualViewController.h"
#import "Constants.h"
#import "Data_CollectorAppDelegate.h"

@implementation ManualViewController

@synthesize logo, loggedInAsLabel, expNumLabel, upload, clear, sessionNameInput, media, scrollView, activeField, lastField, keyboardDismissProper;
@synthesize expNum, locationManager, browsing, initialExpDialogOpen, city, address, country, geoCoder, dataSaver, managedObjectContext, imageList;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewHasLoaded];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView~ipad"
                                          owner:self
                                        options:nil];
            [self viewHasLoaded];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewHasLoaded];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ManualView~iphone"
                                          owner:self
                                        options:nil];
            [self viewHasLoaded];
        }
    }
}

// substitute for viewDidLoad - allocates memory and sets up main UI
- (void) viewHasLoaded {
    
    NSLog(@"view has loaded");
    
    // allocations
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu"
                                                                   style:UIBarButtonItemStylePlain
                                                                  target:self
                                                                  action:@selector(displayMenu:)];
    self.navigationItem.rightBarButtonItem = menuButton;
    
    UITapGestureRecognizer *tapGestureM = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                                  action:@selector(hideKeyboard)];
    tapGestureM.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tapGestureM];
    
    [self initLocations];
    
    // iSENSE API
    iapi = [iSENSE getInstance];
    [iapi toggleUseDev:YES];
    if ([iapi isLoggedIn])
        loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
    else {
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        NSString *username = [prefs stringForKey:[StringGrabber grabString:@"key_username"]];
        NSString *password = [prefs stringForKey:[StringGrabber grabString:@"key_password"]];
        if ([username length] != 0) {
            bool success = [iapi login:username with:password];
            if (success) {
                loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
            } else {
                loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"_"]; 
            }
        } else {
            loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"_"];
        }
        
    }
    
    // prepare an empty image list
    imageList = [[NSMutableArray alloc] init];
        
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
    sessionNameInput.tag = TAG_DEFAULT;
    
    if (rds == nil) {
        rds = [[RotationDataSaver alloc] init];
    } else {
        if ([rds doesHaveName])
            sessionNameInput.text = [rds sesName];
    }
    
    // experiment number
    if (expNum && expNum > 0) {
        expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:[NSString stringWithFormat:@"%d", expNum]];
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setValue:[NSString stringWithFormat:@"%d", expNum] forKey:[StringGrabber grabString:@"key_exp_manual"]];
        
        if (browsing == YES) {
            browsing = NO;
            [self cleanRDSData];
            [self fillDataFieldEntryList:expNum withData:nil];
        } else {
            if (rds != nil && [rds doesHaveData])
                [self fillDataFieldEntryList:expNum withData:[rds data]];
            else
                [self fillDataFieldEntryList:expNum withData:nil];
        }
    } else {
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        int exp = [[prefs stringForKey:[StringGrabber grabString:@"key_exp_manual"]] intValue];
        if (exp > 0) {
            expNum = exp;
            expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num"
                                                                    with:[NSString stringWithFormat:@"%d", expNum]];
            if (rds != nil) rds.doesHaveData = true;
            [self fillDataFieldEntryList:expNum withData:nil];
        } else {
            if (!initialExpDialogOpen) {
                initialExpDialogOpen = true;
                UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Choose an experiment:"
                                                                  message:nil
                                                                 delegate:self
                                                        cancelButtonTitle:@"Cancel"
                                                        otherButtonTitles:@"Enter Experiment #", @"Browse", @"Scan QR Code", nil];
                message.tag = MANUAL_MENU_EXPERIMENT;
                [message show];
                
                expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:@"_"];
            }
        }
    }

    [self registerForKeyboardNotifications];
    
}

- (void) viewDidLoad {
    [super viewDidLoad];
    
    // Managed Object Context for Data_CollectorAppDelegate
    if (managedObjectContext == nil) {
        managedObjectContext = [(Data_CollectorAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext];
    }
    
    // DataSaver from Data_CollectorAppDelegate
    if (dataSaver == nil) {
        dataSaver = [(Data_CollectorAppDelegate *) [[UIApplication sharedApplication] delegate] dataSaver];
        NSLog(@"Current count = %d", dataSaver.dataQueue.count);
    }
    
    [self initLocations];
    [self resetAddressFields];

}

- (void) cleanRDSData {
    rds.doesHaveData = false;
    [rds.data removeAllObjects];
    for (NSInteger i = 0; i < 100; ++i)
        [rds.data addObject:[NSNull null]];
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
    
    if (activeField.tag >= TAG_TEXT) {
        
        NSDictionary* info = [aNotification userInfo];
    
        CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    
        CGRect aRect = self.view.frame;
        aRect.size.height -= kbSize.height;
        CGPoint origin = activeField.frame.origin;
        if (!CGRectContainsPoint(aRect, origin) ) {
            if ([UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationLandscapeLeft || [UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationLandscapeRight) {
                self.view.frame = CGRectMake(0.0, (aRect.size.height + RECT_HEIGHT_OFFSET), self.view.frame.size.width, self.view.frame.size.height);
            }
        }
        if ([UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationPortrait || [UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationPortraitUpsideDown)
            self.view.frame = CGRectMake(0.0, -(self.view.frame.size.height - aRect.size.height), self.view.frame.size.width, self.view.frame.size.height);
        
        // adjust for scrollview and frame drawing oddities across devices and orientations
        UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
        if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
            if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
            } else {
                if (keyboardDismissProper)
                    [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height + KEY_OFFSET_SCROLL_LAND_IPAD)];
            }
        } else {
            if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                if (keyboardDismissProper)
                    [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height + KEY_OFFSET_SCROLL_PORT_IPHONE)];
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y + KEY_OFFSET_FRAME_PORT_IPHONE,
                                              self.view.frame.size.width, self.view.frame.size.height);
            } else {
                if (keyboardDismissProper)
                    [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height + KEY_OFFSET_SCROLL_LAND_IPHONE)];
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y + KEY_OFFSET_FRAME_LAND_IPHONE,
                                             self.view.frame.size.width, self.view.frame.size.height);
            }
        }
        
    }
    
    [scrollView setScrollEnabled:YES];
    keyboardDismissProper = false;
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification {

    @try {
        if (activeField != nil && activeField.tag >= TAG_TEXT) {
            self.view.frame = CGRectMake(0.0, 0.0, self.view.frame.size.width, self.view.frame.size.height);
    
            // adjust for scrollview and frame drawing oddities across devices and orientations
            UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
            if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
                if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                } else {
                    if(!keyboardDismissProper)
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - KEY_OFFSET_SCROLL_LAND_IPAD)];
                }
            } else {
                if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                    if(!keyboardDismissProper)
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - KEY_OFFSET_SCROLL_PORT_IPHONE)];
                } else {
                    if(!keyboardDismissProper)
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - KEY_OFFSET_SCROLL_LAND_IPHONE)];
                }
            }
        } else {
            self.view.frame = CGRectMake(0.0, 0.0, self.view.frame.size.width, self.view.frame.size.height);
        }
    } @catch (NSException *e) {
        // couldn't check activeField - so ignore it
    }
    
    keyboardDismissProper = true;
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
    lastField   = textField;
    activeField = textField;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {

    activeField = nil;

    if (textField.tag >= TAG_TEXT) {
        if (textField.text.length > 0) {
            int TAG_VAL = (lastField.tag >= TAG_NUMERIC) ? TAG_NUMERIC : TAG_TEXT;
            NSString *text = lastField.text;
            rds.doesHaveData = true;
            [rds.data replaceObjectAtIndex:(lastField.tag - TAG_VAL) withObject:text];
        } else {
            int TAG_VAL = (lastField.tag >= TAG_NUMERIC) ? TAG_NUMERIC : TAG_TEXT;
            [rds.data replaceObjectAtIndex:(lastField.tag - TAG_VAL) withObject:[NSNull null]];
        }
    } else {
        if (sessionNameInput.text.length > 0) {
            rds.doesHaveName = true;
            rds.sesName = [sessionNameInput text];
        } else {
            rds.doesHaveName = false;
        }
    }
    
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
    
    NSLog(@"view did appear");
    //[self updateExpNumLabel];
}

- (void) dealloc {
    
    
    [self unregisterKeyboardNotifications];
    
    //[rds release];
    
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

// Reset address fields for next session
- (void)resetAddressFields {
    city = @"N/A";
    country = @"N/A";
    address = @"N/A";
}

// method not called on real device - don't assign a location to a global variable here
- (void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    //CLLocation *location = [locations lastObject];
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

// overridden to keep soft keyboard off screen when not editting a text field
- (BOOL) textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (IBAction) saveOnClick:(id)sender {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Saving data set..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("manual_upload_from_upload_function", NULL);
    dispatch_async(queue, ^{
        BOOL exp = TRUE, hasSessionName = TRUE, uploadSuccess = FALSE;
        
        if (expNum == 0 || expNum == -1)
            exp = FALSE;
        
        else
            if ([[sessionNameInput text] isEqualToString:@""])
                hasSessionName = FALSE;
        
            else {
                NSMutableArray *dataJSON = [self getDataFromFields];
                [self saveDataSet:dataJSON withDescription:@"Data set from iOS Data Collector - Manual Entry."];
                uploadSuccess = TRUE;
            }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if (!exp)
                [self.view makeWaffle:@"Please enter an experiment first"
                             duration:WAFFLE_LENGTH_LONG
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_WARNING];
            if (!hasSessionName)
                [self.view makeWaffle:@"Please enter a session name first"
                             duration:WAFFLE_LENGTH_LONG
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_WARNING];
            if (uploadSuccess)
                [self.view makeWaffle:@"Data set saved" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_CHECKMARK];
            
            
            [message dismissWithClickedButtonIndex:nil animated:YES];
        });
    });
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
}

- (IBAction) mediaOnClick:(id)sender {
    
    if (![self startCameraControllerFromViewController:self usingDelegate:self])
        [self.view makeWaffle:@"No camera found." duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];

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
}

- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
	UIAlertView *message;
    QueueUploaderView *queueUploader;

	switch (buttonIndex) {
        case MANUAL_MENU_UPLOAD:
            
            if (dataSaver.dataQueue.count > 0) {
                queueUploader = [[QueueUploaderView alloc] initWithParentName:PARENT_MANUAL];
                queueUploader.title = @"Manage and Upload Sessions";
                [self.navigationController pushViewController:queueUploader animated:YES];
            } else {
                [self.view makeWaffle:@"No data to upload" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_WARNING];
            }
            
            break;
            
		case MANUAL_MENU_EXPERIMENT:
            message = [[UIAlertView alloc] initWithTitle:nil
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Enter Experiment #", @"Browse", @"Scan QR Code", nil];
            message.tag = MANUAL_MENU_EXPERIMENT;
            [message show];
            
			break;
            
		case MANUAL_MENU_LOGIN:
            message = [[UIAlertView alloc] initWithTitle:@"Login"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            message.tag = MANUAL_MENU_LOGIN;
			[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
            [message textFieldAtIndex:0].tag = TAG_MANUAL_LOGIN;
            [message textFieldAtIndex:1].tag = TAG_MANUAL_LOGIN;
            [message textFieldAtIndex:0].delegate = self;
            [message textFieldAtIndex:1].delegate = self;
            [message show];
            
            break;
            
		default:
			break;
	}
	
}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == MANUAL_MENU_LOGIN) {
        
        if (buttonIndex != OPTION_CANCELED) {
            NSString *usernameInput = [[actionSheet textFieldAtIndex:0] text];
            NSString *passwordInput = [[actionSheet textFieldAtIndex:1] text];
            [self login:usernameInput withPassword:passwordInput];
        }
        
    } else if (actionSheet.tag == MANUAL_MENU_EXPERIMENT){
        
        if (buttonIndex == OPTION_ENTER_EXPERIMENT_NUMBER) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Experiment #:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = EXPERIMENT_MANUAL_ENTRY;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message textFieldAtIndex:0].tag = TAG_MANUAL_EXP;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            
        } else if (buttonIndex == OPTION_BROWSE_EXPERIMENTS) {
            
            initialExpDialogOpen = false;
            
            ExperimentBrowseViewController *browseView = [[ExperimentBrowseViewController alloc] init];
            browseView.title = @"Browse for Experiments";
            browseView.chosenExperiment = &expNum;
            browsing = YES;
            [self.navigationController pushViewController:browseView animated:YES];
            
        } else if (buttonIndex == OPTION_SCAN_QR_CODE) {
            
           if([[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo] supportsAVCaptureSessionPreset:AVCaptureSessionPresetMedium]){
        
               if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"pic2shop:"]]) {
                   NSURL *urlp2s = [NSURL URLWithString:@"pic2shop://scan?callback=DataCollector%3A//EAN"];
                   Data_CollectorAppDelegate *dcad = (Data_CollectorAppDelegate*)[[UIApplication sharedApplication] delegate];
                   [dcad setLastController:self];
                   [dcad setReturnToClass:DELEGATE_KEY_MANUAL];
                   [[UIApplication sharedApplication] openURL:urlp2s];
               } else {
                   NSURL *urlapp = [NSURL URLWithString:@"http://itunes.com/app/pic2shop"];
                   [[UIApplication sharedApplication] openURL:urlapp];
               }
           
           } else {
            
                UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Your device does not have a camera that supports QR Code scanning."
                                                                  message:nil
                                                                 delegate:self
                                                        cancelButtonTitle:@"Cancel"
                                                        otherButtonTitles:nil];
                
                [message setAlertViewStyle:UIAlertViewStyleDefault];
                [message show];
                
           }
            
        }
        
    } else if (actionSheet.tag == EXPERIMENT_MANUAL_ENTRY) {
        
        initialExpDialogOpen = false;
        
        if (buttonIndex != OPTION_CANCELED) {

            [self cleanRDSData];
            
            NSString *expNumString = [[actionSheet textFieldAtIndex:0] text];
            expNum = [expNumString intValue];
            expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num"
                                                                    with:[NSString stringWithFormat:@"%d", expNum]];
            
            NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
            [prefs setValue:expNumString forKey:[StringGrabber grabString:@"key_exp_manual"]];
            
            [self fillDataFieldEntryList:expNum withData:nil];
        }
        
    } else if (actionSheet.tag == CLEAR_FIELDS_DIALOG) {
        
        if (buttonIndex != OPTION_CANCELED) {
            sessionNameInput.text = @"";
            
            rds.doesHaveName = false;
            [self cleanRDSData];
            
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

- (BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    
    if (textField.tag == TAG_MANUAL_LOGIN) {
        NSUInteger newLength = [textField.text length] + [string length] - range.length;
        
        if (![self containsAcceptedCharacters:string])
            return NO;
        
        return (newLength > 100) ? NO : YES;
    }
    
    if (textField.tag == TAG_MANUAL_EXP) {
        NSUInteger newLength = [textField.text length] + [string length] - range.length;
        
        if (![self containsAcceptedDigits:string])
            return NO;
        
        return (newLength > 6) ? NO : YES;
    }
    
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

- (BOOL) containsAcceptedDigits:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:
      [StringGrabber grabString:@"accepted_digits"]] invertedSet];
    
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
                [self.view makeWaffle:@"Login Successful!"
                            duration:WAFFLE_LENGTH_SHORT
                            position:WAFFLE_BOTTOM
                               image:WAFFLE_CHECKMARK];
                
                // save the username and password in prefs
                NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                [prefs setObject:usernameInput forKey:[StringGrabber grabString:@"key_username"]];
                [prefs setObject:passwordInput forKey:[StringGrabber grabString:@"key_password"]];
                [prefs synchronize];
                
                loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[iapi getLoggedInUsername]];
            } else {
                [self.view makeWaffle:@"Login Failed!"
                            duration:WAFFLE_LENGTH_SHORT
                            position:WAFFLE_BOTTOM
                               image:WAFFLE_RED_X];
            }
            [message dismissWithClickedButtonIndex:nil animated:YES];
        });
    });
    
}

- (void) fillDataFieldEntryList:(int)eid withData:(NSMutableArray *)data {
    
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Retrieving experiment fields..."];
    [message show];
        
    dispatch_queue_t queue = dispatch_queue_create("manual_upload_from_upload_function", NULL);
    dispatch_async(queue, ^{
        
        NSMutableArray *fieldOrder = [iapi getExperimentFields:[NSNumber numberWithInt:eid]];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            int objNumber = 0;
            int scrollHeight = 0;
            
            for (ExperimentField *expField in fieldOrder) {
                
                if (expField.type_id.intValue == GEOSPACIAL || expField.type_id.intValue == TIME) {
                    if (expField.unit_id.intValue == UNIT_LATITUDE) {
                        scrollHeight = [self addDataField:expField withType:TYPE_LATITUDE andObjNumber:objNumber andData:nil];
                    } else if (expField.unit_id.intValue == UNIT_LONGITUDE) {
                        scrollHeight = [self addDataField:expField withType:TYPE_LONGITUDE andObjNumber:objNumber andData:nil];
                    } else /* Time */ {
                        // if (data == nil)
                        scrollHeight = [self addDataField:expField withType:TYPE_TIME andObjNumber:objNumber andData:nil];
                        // else
                        //    scrollHeight = [self addDataField:expField withType:TYPE_TIME andObjNumber:objNumber andData:[data objectAtIndex:objNumber]];
                    }
                } else {
                    if (data == nil)
                        scrollHeight = [self addDataField:expField withType:TYPE_DEFAULT andObjNumber:objNumber andData:nil];
                    else {
                        scrollHeight = [self addDataField:expField withType:TYPE_DEFAULT andObjNumber:objNumber andData:[data objectAtIndex:objNumber]];
                    }
                }
                
                ++objNumber;
                
            }
            
            
            scrollHeight += SCROLLVIEW_TEXT_HEIGHT;
            CGFloat scrollWidth = scrollView.frame.size.width;
            [scrollView setContentSize:CGSizeMake(scrollWidth, scrollHeight + KEY_HEIGHT_OFFSET)];
            
            if (scrollView.subviews.count == 0) {
                
                UILabel *noFields = [[UILabel alloc] initWithFrame:CGRectMake(0, SCROLLVIEW_Y_OFFSET, IPAD_WIDTH_PORTRAIT, SCROLLVIEW_LABEL_HEIGHT)];
                if ([iapi isConnectedToInternet])
                    noFields.text = @"Invalid experiment.";
                else
                    noFields.text = @"Cannot find experiment fields while not connected to the internet.";
                noFields.backgroundColor = [UIColor clearColor];
                noFields.textColor = [HexColor colorWithHexString:@"000000"];
                [scrollView addSubview: noFields];
            } else {
                // adjust scrollview's bottom bit
                UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
                if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                    [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - PORTRAIT_BOTTOM_CUT)];
                } else {
                    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad)
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - LANDSCAPE_BOTTOM_CUT_IPAD)];
                    else
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - LANDSCAPE_BOTTOM_CUT_IPHONE)];
                }
                
                // adjust scrollview's top overlap
                if(!([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad))
                    if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                        [scrollView setFrame:CGRectMake(scrollView.frame.origin.x, scrollView.frame.origin.y + TOP_ELEMENT_ADJUSTMENT,
                                                        scrollView.frame.size.width, scrollView.frame.size.height - TOP_ELEMENT_ADJUSTMENT)];
                    }
            }
            
            [message dismissWithClickedButtonIndex:nil animated:YES];
            
        });
        
    });
}

- (int) addDataField:(ExperimentField *)expField withType:(int)type andObjNumber:(int)objNum andData:(NSString *)data {
    
    CGFloat Y_FIELDNAME = SCROLLVIEW_Y_OFFSET + (objNum * SCROLLVIEW_Y_OFFSET);
    CGFloat Y_FIELDCONTENTS = Y_FIELDNAME + SCROLLVIEW_OBJ_INCR;

    // initial scrollview element starting point adjustments
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;        
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
            Y_FIELDNAME -= START_Y_PORTRAIT_IPAD;
            Y_FIELDCONTENTS -= START_Y_PORTRAIT_IPAD;
        } else {
            Y_FIELDNAME -= START_Y_LANDSCAPE_IPAD;
            Y_FIELDCONTENTS -= START_Y_LANDSCAPE_IPAD;
        }
    } else {
        if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
            Y_FIELDNAME -= START_Y_PORTRAIT_IPHONE;
            Y_FIELDCONTENTS -= START_Y_PORTRAIT_IPHONE;
        } else {
            Y_FIELDNAME -= START_Y_LANDSCAPE_IPHONE;
            Y_FIELDCONTENTS -= START_Y_LANDSCAPE_IPHONE;
        }
    }
    
    UILabel *fieldName;
    if (objNum == 0) {
        fieldName = [[UILabel alloc] initWithFrame:[self setScrollViewItem:UI_FIELDNAME toSizeWithY:Y_FIELDNAME]];
    } else {
        Y_FIELDNAME += (SCROLLVIEW_OBJ_INCR * objNum);
        Y_FIELDCONTENTS += (SCROLLVIEW_OBJ_INCR * objNum);
        fieldName = [[UILabel alloc] initWithFrame:[self setScrollViewItem:UI_FIELDNAME toSizeWithY:Y_FIELDNAME]];
    }
    fieldName.backgroundColor = [UIColor clearColor];
    fieldName.textColor = [UIColor blackColor];
    fieldName.text = [StringGrabber concatenate:expField.field_name withHardcodedString:@"colon"];
    
    UITextField *fieldContents = [[UITextField alloc] initWithFrame:[self setScrollViewItem:UI_FIELDCONTENTS toSizeWithY:Y_FIELDCONTENTS]];
    fieldContents.delegate = self;
    fieldContents.backgroundColor = [UIColor whiteColor];
    fieldContents.font = [UIFont systemFontOfSize:24];
    fieldContents.borderStyle = UITextBorderStyleRoundedRect;
    if (data != nil && !([data isKindOfClass:[NSNull class]])) {
        NSString *tmp = [NSString stringWithString:data];
        fieldContents.text = tmp;
    }
    
    if (type != TYPE_DEFAULT) {
        fieldContents.enabled = NO;
        if (type == TYPE_LATITUDE) {
            fieldContents.text = [StringGrabber grabString:@"auto_lat"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"AAAAAA"];
        } else if (type == TYPE_LONGITUDE) {
            fieldContents.text = [StringGrabber grabString:@"auto_long"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"AAAAAA"];
        } else {
            fieldContents.text = [StringGrabber grabString:@"auto_time"];
            fieldContents.backgroundColor = [HexColor colorWithHexString:@"AAAAAA"];
        }
    }
    
    NSLog(@"type id = %d", expField.type_id.intValue);
    if (expField.type_id.intValue == TEXT) {
        fieldContents.keyboardType = UIKeyboardTypeNamePhonePad;
        fieldContents.tag = TAG_TEXT + objNum;
    } else {
        fieldContents.keyboardType = UIKeyboardTypeNumbersAndPunctuation;
        fieldContents.tag = TAG_NUMERIC + objNum;
    }
    [fieldContents setReturnKeyType:UIReturnKeyDone];
    
    [scrollView addSubview:fieldName];
    [scrollView addSubview:fieldContents];
    
    
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

- (NSMutableArray *) getDataFromFields {
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
    
    return dataEncapsulator;

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
    return message;
}

- (BOOL) handleNewQRCode:(NSURL *)url {
    
    NSArray *arr = [[url absoluteString] componentsSeparatedByString:@"="];
    NSString *exp = arr[2];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    [prefs setValue:exp forKeyPath:[StringGrabber grabString:@"key_exp_manual"]];
    
    expNum = [exp intValue];
    expNumLabel.text = [StringGrabber concatenateHardcodedString:@"exp_num" with:[NSString stringWithFormat:@"%d", expNum]];
    
    if (browsing == YES) {
        browsing = NO;
        [self cleanRDSData];
        [self fillDataFieldEntryList:expNum withData:nil];
    } else {
        if (rds != nil && rds.doesHaveData)
            [self fillDataFieldEntryList:expNum withData:[rds data]];
        else
            [self fillDataFieldEntryList:expNum withData:nil];
    }
    
    return YES;
}

// Save a data set so you don't have to upload it immediately
- (void)saveDataSet:(NSMutableArray *)dataJSON withDescription:(NSString *)description {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    expNum = [[prefs stringForKey:[StringGrabber grabString:@"key_exp_manual"]] intValue];
    
    bool uploadable = false;
    if (expNum > 1) uploadable = true;
    
    QDataSet *ds = [[QDataSet alloc] initWithEntity:[NSEntityDescription entityForName:@"QDataSet" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    [ds setName:sessionNameInput.text];
    [ds setParentName:PARENT_MANUAL];
    [ds setDataDescription:description];
    [ds setProjID:[NSNumber numberWithInt:expNum]];
    [ds setData:dataJSON];
    [ds setPicturePaths:[imageList copy]];
    [ds setUploadable:[NSNumber numberWithBool:uploadable]];
    [ds setHasInitialProj:[NSNumber numberWithBool:(expNum != -1)]];
    // Add the new data set to the queue
    [dataSaver addDataSet:ds];
    NSLog(@"There are %d images in imageList.", imageList.count);
    NSLog(@"There are %d dataSets in the dataSaver.", dataSaver.dataQueue.count);
    
    [imageList removeAllObjects];
    
}

// Finds the associated address from a GPS location.
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    if (geoCoder) {
        [geoCoder reverseGeocodeLocation:newLocation completionHandler:^(NSArray *placemarks, NSError *error) {
            if ([placemarks count] > 0) {
                city = [[placemarks objectAtIndex:0] locality];
                country = [[placemarks objectAtIndex:0] country];
                NSString *subThoroughFare = [[placemarks objectAtIndex:0] subThoroughfare];
                NSString *thoroughFare = [[placemarks objectAtIndex:0] thoroughfare];
                address = [NSString stringWithFormat:@"%@ %@", subThoroughFare, thoroughFare];
                
                if (!address || !city || !country)
                    [self resetAddressFields];
                
                if ((NSNull *)address == [NSNull null] || (NSNull *)city == [NSNull null] || (NSNull *)country == [NSNull null])
                    [self resetAddressFields];
            } else {
                [self resetAddressFields];
            }
        }];
    }
}

// Called to start the camera app
- (BOOL)startCameraControllerFromViewController:(UIViewController*)controller usingDelegate:(id <UIImagePickerControllerDelegate, UINavigationControllerDelegate>) delegate {
    
    if (([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera] == NO) || (delegate == nil) || (controller == nil))
        return NO;
    
    UIImagePickerController *cameraUI = [[UIImagePickerController alloc] init];
    cameraUI.sourceType = UIImagePickerControllerSourceTypeCamera;
    
    // Displays a control that allows the user to choose picture or
    // movie capture, if both are available:
    BOOL hasCamera = false;
    NSArray *tmp = [UIImagePickerController availableMediaTypesForSourceType:UIImagePickerControllerSourceTypeCamera];
    for (int i = 0; i <tmp.count; i++) {
        NSString *mediaType = [tmp objectAtIndex:i];
        if (mediaType == (NSString *)kUTTypeImage) hasCamera = true;
    }
    
    if (!hasCamera) return NO;
    
    cameraUI.mediaTypes = [NSArray arrayWithObjects:(NSString *)kUTTypeImage, nil];
    NSLog(@"Media Types: %@", cameraUI.mediaTypes.description);
    
    // Hides the controls for moving & scaling pictures, or for
    // trimming movies. To instead show the controls, use YES.
    cameraUI.allowsEditing = NO;
    cameraUI.delegate = delegate;
    
    [controller presentModalViewController:cameraUI animated:YES];
    
    return YES;
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error andContextInfo:(void *)contextInfo {
    NSLog(@"Got zee image!");
    if  (error) {
        NSLog(@"%@", error);
    } else {
        NSLog(@"Added image");
        [imageList addObject:image];
    }
}

// For responding to the user tapping Cancel when taking a picture.
- (void) imagePickerControllerDidCancel:(UIImagePickerController *)picker  {
    
    [self dismissModalViewControllerAnimated: YES];
    
}

// For responding to the user accepting a newly-captured picture or movie
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *) info {
    NSLog(@"Image picker controller method");
    
    NSString *mediaType = [info objectForKey: UIImagePickerControllerMediaType];
    UIImage *originalImage, *editedImage, *imageToSave;
    
    // Handle a still image capture
    if (CFStringCompare ((CFStringRef) mediaType, kUTTypeImage, 0) == kCFCompareEqualTo) {
        
        editedImage = (UIImage *) [info objectForKey:UIImagePickerControllerEditedImage];
        originalImage = (UIImage *) [info objectForKey:UIImagePickerControllerOriginalImage];
        
        if (editedImage) {
            imageToSave = editedImage;
        } else {
            imageToSave = originalImage;
        }
        
        NSLog(@"About to save picture!");
        
        // Save the new image (original or edited) to the Camera Roll (then send the image to the caller's image:didFinishSavingWithError: method);
        UIImageWriteToSavedPhotosAlbum (imageToSave, self, @selector(image:didFinishSavingWithError:andContextInfo:), nil);
        
        NSLog(@"Image write finished.");

    }
    
    [self dismissModalViewControllerAnimated:YES];
}

@end
