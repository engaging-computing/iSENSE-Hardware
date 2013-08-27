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
@synthesize latitudeLabel, longitudeLabel, recordData, recordingInterval, nameTextField, loggedInAs, upload, selectProject;
// Other properties
@synthesize locationManager, isRecording, activeField;

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
    
    // Set up the menu bar
	reset = [[UIBarButtonItem alloc] initWithTitle:@"Reset" style:UIBarButtonItemStyleBordered target:self action:@selector(onResetClick:)];
    about = [[UIBarButtonItem alloc] initWithTitle:@"About" style:UIBarButtonItemStyleBordered target:self action:@selector(onAboutClick:)];
    self.navigationItem.rightBarButtonItems = [NSArray arrayWithObjects:reset, about, nil];
    self.navigationItem.title = @"iSENSE Data Walk";
    
    // Tag the UI objects
    latitudeLabel.tag   = kTAG_LABEL_LATITUDE;
    longitudeLabel.tag  = kTAG_LABEL_LONGITUDE;
    recordData.tag      = kTAG_BUTTON_RECORD;
    nameTextField.tag   = kTAG_TEXTFIELD_NAME;
    loggedInAs.tag      = kTAG_BUTTON_LOGGED_IN;
    upload.tag          = kTAG_BUTTON_UPLOAD;
    selectProject.tag   = kTAG_BUTTON_PROJECT;
    
    // Set up text field delegate to catch editing actions and return key type to end editing
    nameTextField.delegate = self;
    [nameTextField setReturnKeyType:UIReturnKeyDone];
    
    // Initialize other variables
    isRecording = NO;
    
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
    avc.title = @"About";
    [self.navigationController pushViewController:avc animated:YES];
}

- (IBAction) onRecordDataClick:(id)sender {
    [self.view makeWaffle:@"Record clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (IBAction) onRecordingIntervalClick:(id)sender {
    [self.view makeWaffle:@"Interval clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (IBAction) onLoggedInClick:(id)sender {
    [self.view makeWaffle:@"Logged in clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (IBAction) onUploadClick:(id)sender {
    [self.view makeWaffle:@"Upload clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
}

- (IBAction) onSelectProjectClick:(id)sender {
    [self.view makeWaffle:@"Project clicked" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM];
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

// Finds the associated address from a GPS location.
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    NSLog(@"New location = %@", newLocation);
    CLLocationCoordinate2D lc2d = [newLocation coordinate];
//    if (newLocation != nil && lc2d.latitude != 0.0) {
//        [self resetGeospatialLabels];
//        return;
//    }
    double latitude  = lc2d.latitude;
    double longitude = lc2d.longitude;
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        [latitudeLabel setText:[NSString stringWithFormat:@"Latitude: %lf", latitude]];
        [longitudeLabel setText:[NSString stringWithFormat:@"Longitude: %lf", longitude]];
    } else {
        [latitudeLabel setText:[NSString stringWithFormat:@"Lat: %lf", latitude]];
        [longitudeLabel setText:[NSString stringWithFormat:@"Lon: %lf", longitude]];
    }
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
    return YES;
}

- (BOOL) containsAcceptedCharacters:(NSString *)mString {
    return YES;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
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
    return YES;
}

- (IBAction)textFieldFinished:(id)sender {
}


@end
