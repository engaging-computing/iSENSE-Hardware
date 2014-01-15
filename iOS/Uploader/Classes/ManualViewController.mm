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

@synthesize loggedInAsLabel, projNumLabel, upload, clear, dataSetNameInput, media, scrollView, activeField, lastField, keyboardDismissProper;
@synthesize projNum, locationManager, browsing, initialProjDialogOpen, city, address, country, geoCoder, dataSaver, managedObjectContext, imageList;

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
    
    // Check backFromQueue status to inform user of data set upload success or failure
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    backFromQueue = [prefs boolForKey:[StringGrabber grabString:@"key_back_from_queue"]];
    if (backFromQueue) {
        int uploaded = [prefs integerForKey:@"key_data_uploaded"];
        switch (uploaded) {
            case DATA_NONE_UPLOADED:
                [self.view makeWaffle:@"No data sets uploaded" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
                break;
                
            case DATA_UPLOAD_SUCCESS:
                [self.view makeWaffle:@"All selected data sets uploaded successfully" duration:WAFFLE_LENGTH_LONG position:WAFFLE_BOTTOM image:WAFFLE_CHECKMARK];
                break;
                
            case DATA_UPLOAD_FAILED:
                [self.view makeWaffle:@"At least one data set failed to upload" duration:WAFFLE_LENGTH_LONG position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
                break;
        }
        
        // Set back_from_queue key to false again
        [prefs setBool:false forKey:[StringGrabber grabString:@"key_back_from_queue"]];
    }
    
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
    api = [API getInstance];
    [api useDev:[prefs boolForKey:kUSE_DEV]];
    
    if ([api getCurrentUser] != nil)
        loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[[api getCurrentUser] username]];
    else {
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        NSString *username = [prefs stringForKey:[StringGrabber grabString:@"key_username"]];
        NSString *password = [prefs stringForKey:[StringGrabber grabString:@"key_password"]];
        if ([username length] != 0) {
            bool success = [api createSessionWithUsername:username andPassword:password];
            if (success) {
                loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[[api getCurrentUser] username]];
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
    
    // data set name
    [self.dataSetNameInput addTarget:self
                              action:@selector(textFieldFinished:)
                    forControlEvents:UIControlEventEditingDidEndOnExit];
    dataSetNameInput.delegate = self;
    dataSetNameInput.enablesReturnKeyAutomatically = NO;
    dataSetNameInput.borderStyle = UITextBorderStyleRoundedRect;
    dataSetNameInput.tag = TAG_DEFAULT;
    
    if (rds == nil) {
        rds = [[RotationDataSaver alloc] init];
    } else {
        if ([rds doesHaveName])
            dataSetNameInput.text = [rds dsName];
    }
    
    // project number
    if (projNum > 0) {
        projNumLabel.text = [StringGrabber concatenateHardcodedString:@"proj_num" with:[NSString stringWithFormat:@"%d", projNum]];
        [prefs setValue:[NSString stringWithFormat:@"%d", projNum] forKey:[StringGrabber grabString:@"key_proj_manual"]];
        
        if (browsing == YES) {
            browsing = NO;
            [self cleanRDSData];
            [self fillDataFieldEntryList:projNum withData:nil andResetGlobal:FALSE];
        } else {
            if (rds != nil && [rds doesHaveData])
                [self fillDataFieldEntryList:projNum withData:[rds data] andResetGlobal:FALSE];
            else
                [self fillDataFieldEntryList:projNum withData:nil andResetGlobal:FALSE];
        }
    } else {
        int proj = [prefs integerForKey:kPROJECT_ID_MANUAL];
        if (proj > 0) {
            // we have a global proj to use
            projNum = proj;
            projNumLabel.text = [StringGrabber concatenateHardcodedString:@"proj_num"
                                                                    with:[NSString stringWithFormat:@"%d", projNum]];
            if (rds != nil) rds.doesHaveData = true;
            [self fillDataFieldEntryList:projNum withData:nil andResetGlobal:FALSE];
        
        } else {
        
            proj = [[prefs stringForKey:[StringGrabber grabString:@"key_proj_manual"]] intValue];
            
            if (proj > 0) {
                // reset the global proj since we have a local one to use now
                projNum = proj;
                projNumLabel.text = [StringGrabber concatenateHardcodedString:@"proj_num"
                                                                         with:[NSString stringWithFormat:@"%d", projNum]];
                if (rds != nil) rds.doesHaveData = true;
                [self fillDataFieldEntryList:projNum withData:nil andResetGlobal:TRUE];
                
            } else {
                // no local or global proj found
                if (!initialProjDialogOpen) {
                    initialProjDialogOpen = true;
                    UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Choose a project:"
                                                                      message:nil
                                                                     delegate:self
                                                            cancelButtonTitle:@"Cancel"
                                                            otherButtonTitles:@"Enter Project #", @"Browse", nil];
                    message.tag = MANUAL_MENU_PROJECT;
                    [message show];
                    
                    projNumLabel.text = [StringGrabber concatenateHardcodedString:@"proj_num" with:@"_"];
                }
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
    if (dataSaver == nil)
        dataSaver = [(Data_CollectorAppDelegate *) [[UIApplication sharedApplication] delegate] dataSaver];
    
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
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y + KEY_OFFSET_FRAME_PORT_IPHONE+15,
                                              self.view.frame.size.width, self.view.frame.size.height);
            } else {
                if (keyboardDismissProper)
                    [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height + KEY_OFFSET_SCROLL_LAND_IPHONE-60)];
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y + KEY_OFFSET_FRAME_LAND_IPHONE+40,
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
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - KEY_OFFSET_SCROLL_LAND_IPHONE+60)];
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
        if (dataSetNameInput.text.length > 0) {
            rds.doesHaveName = true;
            rds.dsName = [dataSetNameInput text];
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
    [super viewDidUnload];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}

- (void) dealloc {
    
    [self unregisterKeyboardNotifications];
    
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

// Reset address fields for next data set
- (void)resetAddressFields {
    city    = @"N/A";
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
        BOOL proj = TRUE, hasDataSetName = TRUE, uploadSuccess = FALSE;
        
        if (projNum <= 0)
            proj = FALSE;
        
        else
            if ([[dataSetNameInput text] isEqualToString:@""])
                hasDataSetName = FALSE;
        
            else {
                NSMutableArray *dataJSON = [self getDataFromFields];
                [self saveDataSet:dataJSON withDescription:@"Data set from iOS Data Collector - Manual Entry."];
                uploadSuccess = TRUE;
            }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if (!proj)
                [self.view makeWaffle:@"Please enter a project first"
                             duration:WAFFLE_LENGTH_LONG
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_WARNING];
            if (!hasDataSetName)
                [self.view makeWaffle:@"Please enter a data set name first"
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
	UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Are you sure you want to clear your data set name and all field data?"
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:@"Cancel"
                                            otherButtonTitles:@"Okay", nil];
    [message setTag:CLEAR_FIELDS_DIALOG];
    [message setAlertViewStyle:UIAlertViewStyleDefault];
    [message show];
}

- (IBAction) mediaOnClick:(id)sender {
    [self.view makeWaffle:@"Feature to be implemented in future release" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_WARNING];
    /*
    if (![self startCameraControllerFromViewController:self usingDelegate:self])
        [self.view makeWaffle:@"No camera found." duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
     */
}

- (IBAction) displayMenu:(id)sender {
	UIActionSheet *popupQuery = [[UIActionSheet alloc]
                                 initWithTitle:nil
                                 delegate:self
                                 cancelButtonTitle:@"Cancel"
                                 destructiveButtonTitle:nil
                                 otherButtonTitles:@"Upload", @"Project", @"Login", nil];
	popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	//[popupQuery showInView:self.view];
    [popupQuery showInView:[UIApplication sharedApplication].keyWindow];
}

- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
	UIAlertView *message;

	switch (buttonIndex) {
        case MANUAL_MENU_UPLOAD:
            
            if ([dataSaver dataSetCountWithParentName:PARENT_MANUAL] > 0) {
                NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
                backFromQueue = true;
                [prefs setBool:backFromQueue forKey:[StringGrabber grabString:@"key_back_from_queue"]];
                [prefs setInteger:DATA_NONE_UPLOADED forKey:@"key_data_uploaded"];
                
                QueueUploaderView *queueUploader = [[QueueUploaderView alloc] initWithParentName:PARENT_MANUAL];
                queueUploader.title = @"Upload";
                [self.navigationController pushViewController:queueUploader animated:YES];
            } else {
                [self.view makeWaffle:@"No data to upload" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_WARNING];
            }
            
            break;
            
		case MANUAL_MENU_PROJECT:
            message = [[UIAlertView alloc] initWithTitle:nil
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Enter Project #", @"Browse", nil];
            message.tag = MANUAL_MENU_PROJECT;
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
        
    } else if (actionSheet.tag == MANUAL_MENU_PROJECT){
        
        if (buttonIndex == OPTION_ENTER_PROJECT_NUMBER) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Project #:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = PROJ_MANUAL;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message textFieldAtIndex:0].tag = TAG_MANUAL_PROJ;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            
        } else if (buttonIndex == OPTION_BROWSE_PROJECTS) {
            
            initialProjDialogOpen = false;
            
            ProjectBrowseViewController *browseView = [[ProjectBrowseViewController alloc] init];
            browseView.title = @"Browse Projects";
            browseView.delegate = self;
            browsing = YES;
            [self.navigationController pushViewController:browseView animated:YES];
            
        }
        
    } else if (actionSheet.tag == PROJ_MANUAL) {
        
        initialProjDialogOpen = false;
        
        if (buttonIndex != OPTION_CANCELED) {

            [self cleanRDSData];
            
            NSString *projNumString = [[actionSheet textFieldAtIndex:0] text];
            projNum = [projNumString intValue];
            projNumLabel.text = [StringGrabber concatenateHardcodedString:@"proj_num"
                                                                    with:[NSString stringWithFormat:@"%d", projNum]];
            
            NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
            [prefs setValue:projNumString forKey:[StringGrabber grabString:@"key_proj_manual"]];
            
            [self fillDataFieldEntryList:projNum withData:nil andResetGlobal:TRUE];
        }
        
    } else if (actionSheet.tag == CLEAR_FIELDS_DIALOG) {
        
        if (buttonIndex != OPTION_CANCELED) {
            dataSetNameInput.text = @"";
            
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
    
    if (textField.tag == TAG_MANUAL_PROJ) {
        NSUInteger newLength = [textField.text length] + [string length] - range.length;
        
        if (![self containsAcceptedDigits:string])
            return NO;
        
        return (newLength > 6) ? NO : YES;
    }
    
    if (![self containsAcceptedCharacters:string])
        return NO;
    
	if (textField == dataSetNameInput) {
        
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
        BOOL success = [api createSessionWithUsername:usernameInput andPassword:passwordInput];
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
                
                loggedInAsLabel.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[[api getCurrentUser] username]];
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

- (void) fillDataFieldEntryList:(int)projID withData:(NSMutableArray *)data andResetGlobal:(BOOL)reset {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    if (reset) [prefs setInteger:-1 forKey:kPROJECT_ID_MANUAL];
    
    // always save proj passed in to prefs
    [prefs setValue:[NSString stringWithFormat:@"%d", projID] forKey:[StringGrabber grabString:@"key_proj_manual"]];
    
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Retrieving project fields..."];
    [message show];
        
    dispatch_queue_t queue = dispatch_queue_create("manual_upload_from_upload_function", NULL);
    dispatch_async(queue, ^{
        
        NSArray *fieldOrder = [api getProjectFieldsWithId:projID];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            int objNumber = 0;
            int scrollHeight = 0;
            
            for (RProjectField *projField in fieldOrder) {
                
                long fieldID = projField.field_id.longValue;
                
                if (projField.type.intValue == TYPE_LAT) {
                    scrollHeight = [self addDataField:projField
                                             withType:TYPE_LATITUDE
                                         andObjNumber:objNumber
                                              andData:nil
                                               andTag:fieldID];
                } else if (projField.type.intValue == TYPE_LON) {
                     scrollHeight = [self addDataField:projField
                                              withType:TYPE_LONGITUDE
                                          andObjNumber:objNumber
                                               andData:nil
                                                andTag:fieldID];
                } else if (projField.type.intValue == TYPE_TIMESTAMP) {
                    scrollHeight = [self addDataField:projField
                                              withType:TYPE_TIME
                                         andObjNumber:objNumber
                                              andData:nil
                                               andTag:fieldID];
                } else {
                    if (data == nil)
                        scrollHeight = [self addDataField:projField
                                                 withType:TYPE_DEFAULT
                                             andObjNumber:objNumber
                                                  andData:nil
                                                   andTag:fieldID];
                    else {
                        scrollHeight = [self addDataField:projField
                                                 withType:TYPE_DEFAULT
                                             andObjNumber:objNumber
                                                  andData:[data objectAtIndex:objNumber]
                                                   andTag:fieldID];
                    }
                }
            
                ++objNumber;
            }
            
            
            scrollHeight += SCROLLVIEW_TEXT_HEIGHT;
            CGFloat scrollWidth = scrollView.frame.size.width;
            [scrollView setContentSize:CGSizeMake(scrollWidth, scrollHeight + KEY_HEIGHT_OFFSET)];
            
            if (scrollView.subviews.count == 0) {
                
                UILabel *noFields = [[UILabel alloc] initWithFrame:CGRectMake(0, SCROLLVIEW_Y_OFFSET, IPAD_WIDTH_PORTRAIT, SCROLLVIEW_LABEL_HEIGHT)];
                if ([API hasConnectivity])
                    noFields.text = @"Invalid project.";
                else
                    noFields.text = @"Cannot find project fields while not connected to the internet.";
                noFields.backgroundColor = [UIColor clearColor];
                noFields.textColor = UIColorFromHex(0x000000);
                [scrollView addSubview: noFields];
            } else {
                // adjust scrollview's bottom bit
                UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
                if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad)
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - PORTRAIT_BOTTOM_CUT_IPAD)];
                    else
                        [scrollView setContentSize:CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height - PORTRAIT_BOTTOM_CUT_IPHONE)];
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

- (int) addDataField:(RProjectField *)projField withType:(int)type andObjNumber:(int)objNum andData:(NSString *)data andTag:(long)tag {
    
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
    fieldName.text = [StringGrabber concatenate:projField.name withHardcodedString:@"colon"];
    fieldName.tag = tag; // TODO
    
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
            fieldContents.backgroundColor = UIColorFromHex(0xAAAAAA);
        } else if (type == TYPE_LONGITUDE) {
            fieldContents.text = [StringGrabber grabString:@"auto_long"];
            fieldContents.backgroundColor = UIColorFromHex(0xAAAAAA);
        } else {
            fieldContents.text = [StringGrabber grabString:@"auto_time"];
            fieldContents.backgroundColor = UIColorFromHex(0xAAAAAA);
        }
    }

    if (projField.type.intValue == TYPE_TEXT) {
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
                return CGRectMake(0, y-50, IPHONE_WIDTH_LANDSCAPE-100, SCROLLVIEW_LABEL_HEIGHT);
            } else {
                return CGRectMake(0, y-50, IPHONE_WIDTH_LANDSCAPE-100, SCROLLVIEW_TEXT_HEIGHT);
            }
        }
    }
}

- (NSMutableArray *) getDataFromFields {
    NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
    int count = 0;
    long key = -1;
    
    for (UIView *element in scrollView.subviews) {
        if ([element isKindOfClass:[UILabel class]]) {
            
            key = [element tag];
            
        } else if ([element isKindOfClass:[UITextField class]]) {
            
            if ([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_lat"]]) {
                
                CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
                double lat = lc2d.latitude;
                NSString *latitude = [NSString stringWithFormat:@"%lf", lat];
                [data setValue:latitude forKey:[NSString stringWithFormat:@"%ld", key]];
                
            } else if ([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_long"]]) {
                
                CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
                double lon = lc2d.longitude;
                NSString *longitude = [NSString stringWithFormat:@"%lf", lon];
                [data setValue:longitude forKey:[NSString stringWithFormat:@"%ld", key]];
                
            } else if ([((UITextField *) element).text isEqualToString:[StringGrabber grabString:@"auto_time"]]) {
                
                long timeStamp = [[NSDate date] timeIntervalSince1970];
                NSString *currentTime = [[NSString stringWithFormat:@"u %ld", timeStamp] stringByAppendingString:@"000"];
                [data setValue:currentTime forKey:[NSString stringWithFormat:@"%ld", key]];
                
            } else {
                
                if ([((UITextField *) element).text length] != 0)
                    [data setValue:((UITextField *) element).text forKey:[NSString stringWithFormat:@"%ld", key]];
                else
                    [data setValue:@"" forKey:[NSString stringWithFormat:@"%ld", key]];
            }
            count++;
        }
    }

    NSMutableArray *dataEncapsulator = [[NSMutableArray alloc] init];
    [dataEncapsulator addObject:data];
    
    return dataEncapsulator;

}

- (void) hideKeyboard {
    [dataSetNameInput resignFirstResponder];
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

// Save a data set so you don't have to upload it immediately
- (void)saveDataSet:(NSMutableArray *)dataJSON withDescription:(NSString *)description {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    projNum = [[prefs stringForKey:[StringGrabber grabString:@"key_proj_manual"]] intValue];
    
    bool uploadable = false;
    if (projNum > 0) uploadable = true;
    
    QDataSet *ds = [[QDataSet alloc] initWithEntity:[NSEntityDescription entityForName:@"QDataSet" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    [ds setName:dataSetNameInput.text];
    [ds setParentName:PARENT_MANUAL];
    [ds setDataDescription:description];
    [ds setProjID:[NSNumber numberWithInt:projNum]];
    [ds setData:dataJSON];
    [ds setPicturePaths:[imageList copy]];
    [ds setUploadable:[NSNumber numberWithBool:uploadable]];
    [ds setHasInitialProj:[NSNumber numberWithBool:(projNum > 0)]];
    
    // Add the new data set to the queue and remove all media
    [dataSaver addDataSet:ds];
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
    
    // Hides the controls for moving & scaling pictures, or for
    // trimming movies. To instead show the controls, use YES.
    cameraUI.allowsEditing = NO;
    cameraUI.delegate = delegate;
    
    [controller presentModalViewController:cameraUI animated:YES];
    
    return YES;
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error andContextInfo:(void *)contextInfo {
    NSLog(@"Got the image!");
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
        
        // Save the new image (original or edited) to the Camera Roll (then send the image to the caller's image:didFinishSavingWithError: method);
        UIImageWriteToSavedPhotosAlbum (imageToSave, self, @selector(image:didFinishSavingWithError:andContextInfo:), nil);
    }
    
    [self dismissModalViewControllerAnimated:YES];
}

-(void)projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)project {
    projNum = [project intValue];
    projNumLabel.text = [StringGrabber concatenateHardcodedString:@"proj_num"
                                                             with:[NSString stringWithFormat:@"%d", projNum]];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    [prefs setValue:[NSString stringWithFormat:@"%d", project.intValue] forKey:[StringGrabber grabString:@"key_proj_manual"]];
    
    [self fillDataFieldEntryList:projNum withData:nil andResetGlobal:TRUE];
}

@end
