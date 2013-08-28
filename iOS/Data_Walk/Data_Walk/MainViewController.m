//
//  DWMasterViewController.m
//  Data_Walk
//
//  Created by Michael Stowell on 8/22/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import "MainViewController.h"
#import "Constants.h"
#import "AboutViewController.h"

@implementation MainViewController

// Menu properties
@synthesize reset, about;
// UI properties
@synthesize latitudeLabel, longitudeLabel, recordData, recordingIntervalButton, nameTextField, loggedInAs, upload, selectProject, gpsLock;
// Other properties
@synthesize locationManager, activeField;

// Displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
    
}

- (void)viewDidLoad {
    // Initial super call
    [super viewDidLoad];
    
    // Set up iSENSE settings and API
    api = [API getInstance];
    [api createSessionWithUsername:kDEFAULT_USER andPassword:kDEFAULT_PASS];
    
    // Set up the menu bar
	reset = [[UIBarButtonItem alloc] initWithTitle:@"Reset" style:UIBarButtonItemStyleBordered target:self action:@selector(onResetClick:)];
    about = [[UIBarButtonItem alloc] initWithTitle:@"About" style:UIBarButtonItemStyleBordered target:self action:@selector(onAboutClick:)];
    self.navigationItem.rightBarButtonItems = [NSArray arrayWithObjects:reset, about, nil];
    self.navigationItem.title = @"iSENSE Data Walk";
    
    // Tag the UI objects
    latitudeLabel.tag           = kTAG_LABEL_LATITUDE;
    longitudeLabel.tag          = kTAG_LABEL_LONGITUDE;
    recordData.tag              = kTAG_BUTTON_RECORD;
    recordingIntervalButton.tag = kTAG_BUTTON_INTERVAL;
    nameTextField.tag           = kTAG_TEXTFIELD_NAME;
    loggedInAs.tag              = kTAG_BUTTON_LOGGED_IN;
    upload.tag                  = kTAG_BUTTON_UPLOAD;
    selectProject.tag           = kTAG_BUTTON_PROJECT;
    
    // Set up text field delegate to catch editing actions and return key type to end editing
    nameTextField.delegate = self;
    [nameTextField setReturnKeyType:UIReturnKeyDone];
    
    // Initialize other variables
    isRecording = NO;
    isShowingPickerView = NO;
    
    // Set up properties dependent on NSUserDefaults
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    int recInt = [prefs integerForKey:[StringGrabber grabString:@"recording_interval"]];
    if (recInt == 0) {
        recInt = kDEFAULT_REC_INTERVAL;
        [prefs setInteger:recInt forKey:[StringGrabber grabString:@"recording_interval"]];
    }
    recordingInterval = recInt;
    switch (recordingInterval) {
        case 1:
            [recordingIntervalButton setTitle:@"1 second" forState:UIControlStateNormal];
            break;
        case 2:
            [recordingIntervalButton setTitle:@"2 seconds" forState:UIControlStateNormal];
            break;
        case 5:
            [recordingIntervalButton setTitle:@"5 seconds" forState:UIControlStateNormal];
            break;
        case 10:
            [recordingIntervalButton setTitle:@"10 seconds" forState:UIControlStateNormal];
            break;
        case 30:
            [recordingIntervalButton setTitle:@"30 seconds" forState:UIControlStateNormal];
            break;
        case 60:
            [recordingIntervalButton setTitle:@"60 seconds" forState:UIControlStateNormal];
            break;
    }
    
    name = [prefs stringForKey:[StringGrabber grabString:@"first_name"]];
    [nameTextField setText:name];
    
    int proj = [prefs integerForKey:[StringGrabber grabString:@"project_id"]];
    if (proj == 0)
        projectID = kDEFAULT_PROJECT;
    else
        projectID = proj;
    [selectProject setTitle:[NSString stringWithFormat:@"to project %d", projectID] forState:UIControlStateNormal];
    
    // Set up location stuff
    [self resetGeospatialLabels];
    [self initLocations];
}

// Is called every time MainViewController is about to appear
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
}

// Is called every time MainViewController appears
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    
    // Register for keyboard notifications
    [self registerForKeyboardNotifications];
}

- (void) onResetClick:(id)sender {
    [self.view makeWaffle:@"Reset clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (void) onAboutClick:(id)sender {
    AboutViewController *avc = [[AboutViewController alloc] init];
    avc.title = @"About and Help";
    [self.navigationController pushViewController:avc animated:YES];
}

- (IBAction) onRecordDataClick:(id)sender {
    [self.view makeWaffle:@"Record clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (IBAction) onRecordingIntervalClick:(id)sender {
    
    if (isShowingPickerView && intervalPickerView != nil) {
        [intervalPickerView removeFromSuperview];
        isShowingPickerView = NO;
        
        switch (recordingInterval) {
            case 1:
                [recordingIntervalButton setTitle:@"1 second" forState:UIControlStateNormal];
                break;
            case 2:
                [recordingIntervalButton setTitle:@"2 seconds" forState:UIControlStateNormal];
                break;
            case 5:
                [recordingIntervalButton setTitle:@"5 seconds" forState:UIControlStateNormal];
                break;
            case 10:
                [recordingIntervalButton setTitle:@"10 seconds" forState:UIControlStateNormal];
                break;
            case 30:
                [recordingIntervalButton setTitle:@"30 seconds" forState:UIControlStateNormal];
                break;
            case 60:
                [recordingIntervalButton setTitle:@"60 seconds" forState:UIControlStateNormal];
                break;
        }
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setInteger:recordingInterval forKey:[StringGrabber grabString:@"recording_interval"]];
        
    } else {
        
        int x = recordingIntervalButton.frame.origin.x;
        int y = recordingIntervalButton.frame.origin.y + recordingIntervalButton.frame.size.height;
        if([UIDevice currentDevice].userInterfaceIdiom != UIUserInterfaceIdiomPad)
            if (UIInterfaceOrientationIsPortrait([UIApplication sharedApplication].statusBarOrientation))
                x = 0;
        
        [recordingIntervalButton setTitle:@"Done" forState:UIControlStateNormal];
        
        intervalPickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(x, y, 320, 200)];
        intervalPickerView.delegate = self;
        intervalPickerView.showsSelectionIndicator = YES;
        
        [self.view addSubview:intervalPickerView];
        isShowingPickerView = YES;
        
    }
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow: (NSInteger)row inComponent:(NSInteger)component {
    // Handle the selection
    if (row != 0) {
        switch (row) {
            case 1:
                recordingInterval = 1;
                break;
            case 2:
                recordingInterval = 2;
                break;
            case 3:
                recordingInterval = 5;
                break;
            case 4:
                recordingInterval = 10;
                break;
            case 5:
                recordingInterval = 30;
                break;
            case 6:
                recordingInterval = 60;
                break;
        }
    
    } else {
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        recordingInterval = [prefs integerForKey:[StringGrabber grabString:@"recording_interval"]];
    }
   
}

// tell the picker how many rows are available for a given component
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return 7;
}

// tell the picker how many components it will have
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

// tell the picker the title for a given component
- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    NSString *title;
    switch (row) {
        case 0:
            title = @"Return to previous";
            return title;
        case 1:
            title = @"1 second";
            return title;
        case 2:
            title = @"2 seconds";
            return title;
        case 3:
            title = @"5 seconds";
            return title;
        case 4:
            title = @"10 seconds";
            return title;
        case 5:
            title = @"30 seconds";
            return title;
        case 6:
            title = @"60 seconds";
            return title;
    }
    return title;
}

// tell the picker the width of each row for a given component
- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component {
    int sectionWidth = 300;
    return sectionWidth;
}

- (IBAction) onLoggedInClick:(id)sender {
    [self.view makeWaffle:@"Logged in clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (IBAction) onUploadClick:(id)sender {
    [self.view makeWaffle:@"Upload clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (IBAction) onSelectProjectClick:(id)sender {
    UIAlertView *message = [[UIAlertView alloc] initWithTitle:nil
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:@"Cancel"
                                            otherButtonTitles:@"Enter Project #", @"Browse Projects", @"Scan QR Code", nil];
    message.tag = kTAG_PROJECT_SELECTION;
    [message show];
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return (isRecording) ? NO : YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return (isRecording) ? NO : YES;
}

// iOS6 enable rotation
- (NSUInteger)supportedInterfaceOrientations {
    if (isRecording) {
        if (self.interfaceOrientation == UIInterfaceOrientationPortrait) {
            return UIInterfaceOrientationMaskPortrait;
        } else if (self.interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown) {
            return UIInterfaceOrientationMaskPortraitUpsideDown;
        } else if (self.interfaceOrientation == UIInterfaceOrientationLandscapeLeft) {
            return UIInterfaceOrientationMaskLandscapeLeft;
        } else {
            return UIInterfaceOrientationMaskLandscapeRight;
        }
    } else
        return UIInterfaceOrientationMaskAll;
}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == kTAG_PROJECT_SELECTION){
        
        if (buttonIndex == kOPTION_ENTER_PROJECT) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Project #:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = kOPTION_ENTER_PROJECT;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message textFieldAtIndex:0].tag = kENTER_PROJ_TEXTFIELD;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            
        } else if (buttonIndex == kOPTION_BROWSE_PROJECTS) {
            [self.view makeWaffle:@"Browse projects not currently implemented" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
        } else if (buttonIndex == kOPTION_SCAN_PROJECT_QR) {
            [self.view makeWaffle:@"Scan QR Code not currently implemented" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
        }
        
    } else if (actionSheet.tag == kOPTION_ENTER_PROJECT) {
        
        if (buttonIndex != kOPTION_CANCELED) {
            
            NSString *projNum = [[actionSheet textFieldAtIndex:0] text];
            projectID = [projNum intValue];
            
            NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
            [prefs setInteger:projectID forKey:[StringGrabber grabString:@"project_id"]];
            
            [selectProject setTitle:[NSString stringWithFormat:@"to project %d", projectID] forState:UIControlStateNormal];
        }
        
    }
}

- (void) initLocations {
    if (locationManager) locationManager = nil;
    
    locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    locationManager.distanceFilter = kCLDistanceFilterNone;
    locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    [locationManager startUpdatingLocation];
}

// Finds the associated address from a GPS location.
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    NSLog(@"New location = %@", newLocation);
    CLLocationCoordinate2D lc2d = [newLocation coordinate];

    double latitude  = lc2d.latitude;
    double longitude = lc2d.longitude;
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        [latitudeLabel setText:[NSString stringWithFormat:@"Latitude: %lf", latitude]];
        [longitudeLabel setText:[NSString stringWithFormat:@"Longitude: %lf", longitude]];
    } else {
        [latitudeLabel setText:[NSString stringWithFormat:@"Lat: %lf", latitude]];
        [longitudeLabel setText:[NSString stringWithFormat:@"Lon: %lf", longitude]];
    }
    
    [gpsLock setImage:[UIImage imageNamed:@"gps_icon.png"]];
}

- (void) resetGeospatialLabels {
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        [latitudeLabel setText:@"Latitude: ..."];
        [longitudeLabel setText:@"Longitude: ..."];
    } else {
        [latitudeLabel setText:@"Lat: ..."];
        [longitudeLabel setText:@"Lon: ..."];
    }
}

- (BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    if (textField.tag == kENTER_PROJ_TEXTFIELD) {
        NSUInteger newLength = [textField.text length] + [string length] - range.length;
        
        if (![self containsAcceptedDigits:string])
            return NO;
        
        return (newLength > 10) ? NO : YES;
    }
    
    return YES;
}

- (BOOL) containsAcceptedCharacters:(NSString *)mString {
    return YES;
}

- (BOOL) containsAcceptedDigits:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:
      [StringGrabber grabString:@"accepted_digits"]] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    [self unregisterKeyboardNotifications];
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
    
    if (activeField.tag == kTAG_TEXTFIELD_NAME) {
        
        NSDictionary* info = [aNotification userInfo];
        
        CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
        
        CGRect aRect = self.view.frame;
        aRect.size.height -= kbSize.height;
        CGPoint origin = activeField.frame.origin;
        
        if (!CGRectContainsPoint(aRect, origin) ) {
            UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
            if (orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown)
                self.view.frame = CGRectMake(0.0, -(kbSize.height), self.view.frame.size.width, self.view.frame.size.height);
        }
    }
    
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification {
    
    if (activeField != nil && activeField.tag == kTAG_TEXTFIELD_NAME)
        self.view.frame = CGRectMake(0.0, 0.0, self.view.frame.size.width, self.view.frame.size.height);
    
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
    activeField = textField;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    activeField = nil;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    if (textField.tag == kTAG_TEXTFIELD_NAME) {
        name = textField.text;
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setObject:name forKey:[StringGrabber grabString:@"first_name"]];
    }
    return YES;
}

- (IBAction)textFieldFinished:(id)sender {
}


@end
