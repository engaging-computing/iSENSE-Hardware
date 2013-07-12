//
//  AutomaticViewController.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/10/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "AutomaticViewController.h"

@implementation AutomaticViewController

@synthesize isRecording, motionManager, dataToBeJSONed, expNum, timer, recordDataTimer, elapsedTime, locationManager, dfm, testLength, sessionName,
sampleInterval, geoCoder, city, address, country, dataSaver, managedObjectContext, isenseAPI, longClickRecognizer, backFromSetup, recordingRate;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"Automatic-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"Automatic~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"Automatic-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"Automatic~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }

}


// Long Click Responder
//- (IBAction)onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer {
//
//    // Handle long press.
//    if (longClickRecognizer.state == UIGestureRecognizerStateBegan) {
//
//        // Make button unclickable until it gets released
//        longClickRecognizer.enabled = NO;
//
//        // Start Recording
//        if (![self isRecording]) {
//
//            // Check for a chosen experiment
//            if (!expNum) {
//                [self.view makeWaffle:@"No experiment chosen" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
//                return;
//            }
//
//            // Check for login
//            if (![isenseAPI isLoggedIn]) {
//                [self.view makeWaffle:@"Not logged in" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
//                return;
//            }
//
//            // Check for a session title
//            if ([[sessionTitle text] length] == 0) {
//                [self.view makeWaffle:@"Enter a session title first" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
//                return;
//            }
//
//            // Switch to green mode
//            startStopButton.image = [UIImage imageNamed:@"green_button.png"];
//            mainLogo.image = [UIImage imageNamed:@"logo_green.png"];
//            startStopLabel.text = [StringGrabber grabString:@"stop_button_text"];
//            [containerForMainButton updateImage:startStopButton];
//
//            // Get Field Order
//            [dfm getFieldOrderOfExperiment:expNum];
//            NSLog(@"%@", [dfm order]);
//
//            // Record Data
//            [self setIsRecording:TRUE];
//            [self recordData];
//
//            // Update elapsed time
//            elapsedTime = 0;
//            [self updateElapsedTime];
//            NSLog(@"updated Time");
//            timer = [[NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateElapsedTime) userInfo:nil repeats:YES] retain];
//            NSLog(@"timer was launched");
//
//        // Stop Recording
//        } else {
//            // Stop Timers
//            [timer invalidate];
//            [timer release];
//            [recordDataTimer invalidate];
//            [recordDataTimer release];
//
//            // Back to red mode
//            startStopButton.image = [UIImage imageNamed:@"red_button.png"];
//            mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
//            startStopLabel.text = [StringGrabber grabString:@"start_button_text"];
//            [containerForMainButton updateImage:startStopButton];
//
//            [self stopRecording:motionManager];
//            [self setIsRecording:FALSE];
//
//            // Open up description dialog
//            UIAlertView *message = [[UIAlertView alloc] initWithTitle:[StringGrabber grabString:@"description_or_delete"]
//                                                              message:nil
//                                                             delegate:self
//                                                    cancelButtonTitle:@"Delete"
//                                                    otherButtonTitles:@"Upload", nil];
//
//            message.tag = DESCRIPTION_AUTOMATIC;
//            message.delegate = self;
//            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
//            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeDefault;
//            [message show];
//            [message release];
//
//        }
//
//        // Make the beep sound
//        NSString *path = [NSString stringWithFormat:@"%@%@",
//                          [[NSBundle mainBundle] resourcePath],
//                          @"/button-37.wav"];
//        SystemSoundID soundID;
//        NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
//        AudioServicesCreateSystemSoundID((CFURLRef)filePath, &soundID);
//        AudioServicesPlaySystemSound(soundID);
//
//    }
//
//}

//- (bool) uploadData:(NSMutableArray *)results withDescription:(NSString *)description {
//
//    // Check login status
//    if (![isenseAPI isLoggedIn]) {
//        [self.view makeWaffle:@"Not logged in" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
//        return false;
//    }
//
//    // Create a session on iSENSE/dev.
//    NSString *name = @"Session From Mobile";
//    if (sessionTitle.text.length != 0) name = sessionTitle.text;
//    NSNumber *exp_num = [[NSNumber alloc] initWithInt:expNum];
//
//    NSNumber *session_num = [isenseAPI createSession:name withDescription:description Street:address City:city Country:country toExperiment:exp_num];
//    if ([session_num intValue] == -1) {
//        DataSet *ds = (DataSet *) [NSEntityDescription insertNewObjectForEntityForName:@"DataSet" inManagedObjectContext:managedObjectContext];
//        [ds setName:name];
//        [ds setDataDescription:description];
//        [ds setEid:exp_num];
//        [ds setData:nil];
//        [ds setPicturePaths:nil];
//        [ds setSid:[NSNumber numberWithInt:-1]];
//        [ds setCity:city];
//        [ds setCountry:country];
//        [ds setAddress:address];
//        [ds setUploadable:[NSNumber numberWithBool:true]];
//        // Add the new data set to the queue
//        [dataSaver addDataSet:ds];
//        NSLog(@"There are %d dataSets in the dataSaver.", dataSaver.count);
//
//        // Commit the changes
//        NSError *error = nil;
//        if (![managedObjectContext save:&error]) {
//            // Handle the error.
//            NSLog(@"%@", error);
//        }
//
//        return false;
//    }
//
//    // Upload to iSENSE (pass me JSON data)
//    NSError *error = nil;
//    NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:results options:0 error:&error];
//    if (error != nil) {
//        NSLog(@"%@", error);
//        return false;
//    }
//
//    bool success = [isenseAPI putSessionData:dataJSON forSession:session_num inExperiment:exp_num];
//    if (!success) {
//        DataSet *ds = [NSEntityDescription insertNewObjectForEntityForName:@"DataSet" inManagedObjectContext:managedObjectContext];
//        [ds setName:name];
//        [ds setDataDescription:description];
//        [ds setEid:exp_num];
//        [ds setData:results];
//        [ds setPicturePaths:nil];
//        [ds setSid:session_num];
//        [ds setCity:city];
//        [ds setCountry:country];
//        [ds setAddress:address];
//        [ds setUploadable:[NSNumber numberWithBool:true]];
//
//        // Add the new data set to the queue
//        [dataSaver addDataSet:ds];
//        NSLog(@"There are %d dataSets in the dataSaver.", dataSaver.count);
//
//        // Commit the changes
//        NSError *error = nil;
//        if (![managedObjectContext save:&error]) {
//            // Handle the error.
//            NSLog(@"%@", error);
//        }
//    }
//    [exp_num release];
//    return success;
//}

// Implement viewDidLoad after the nib has been loaded
- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Immediately kill the timers with fire
    if (timer != nil) {
        [timer invalidate];
        [timer release];
        timer = nil;
    }
    if (recordDataTimer != nil) {
        [recordDataTimer invalidate];
        [recordDataTimer release];
        recordDataTimer = nil;
    }
    
    // Managed Object Context for Data_CollectorAppDelegate
    if (managedObjectContext == nil) {
        managedObjectContext = [(Data_CollectorAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext];
    }
    
    // DataSaver from Data_CollectorAppDelegate
    if (dataSaver == nil) {
        dataSaver = [(Data_CollectorAppDelegate *) [[UIApplication sharedApplication] delegate] dataSaver];
    }
    
    // Loading message appears while seting up main view
    UIAlertView *message = [self getDispatchDialogWithMessage:[StringGrabber grabString:@"loading"]];
    [message show];
    
    // Add a menu button
    menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu)];
    self.navigationItem.rightBarButtonItem = menuButton;
    
    // Attempt Login
    isenseAPI = [iSENSE getInstance];
    [isenseAPI toggleUseDev:YES];
    
    // Initializes an Assortment of Variables
    motionManager = [[CMMotionManager alloc] init];
    dfm = [[DataFieldManager alloc] init];
    sampleInterval = DEFAULT_SAMPLE_INTERVAL;
    
    // Initialize buttons
    [self setEnabled:true forButton:step1];
    [self setEnabled:false forButton:step2];
    [self setEnabled:false forButton:step3];
    
    // Enabled step 2
    if (backFromSetup) [self setEnabled:true forButton:step2];
    
    // Enable upload depending on DataQueue
    if (dataSaver.count > 0) {
        [self setEnabled:true forButton:step3];
    } else {
        [self setEnabled:false forButton:step3];
    }
    
    [self initLocations];
    [self resetAddressFields];
    
    // Make labels dissapear
    [step1Label setAlpha:0.0];
    [step3Label setAlpha:0.0];
    
    [message dismissWithClickedButtonIndex:nil animated:YES];
        
    isRecording = FALSE;
}


// Is called every time AutomaticView is about to appear
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    // Reinitialize setup to false
    backFromSetup = false;
    
    // If true, then we're coming back from another ViewController
    if (self.isMovingToParentViewController == NO) {
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        backFromSetup = [prefs boolForKey:[StringGrabber grabString:@"key_setup_complete"]];
        
        // We have a session name, sample interval, and test length ready
        if (backFromSetup) {
            
            // retrieve the data from the setup dialog
            NSString *sampleIntervalString = [prefs valueForKey:[StringGrabber grabString:@"key_sample_interval"]];
            sampleInterval = [sampleIntervalString floatValue];
            
            NSString *testLengthString = [prefs valueForKey:[StringGrabber grabString:@"key_test_length"]];
            testLength = [testLengthString integerValue];
            
            sessionName = [prefs valueForKey:[StringGrabber grabString:@"key_step1_session_name"]];
            
            expNum = [[prefs stringForKey:[StringGrabber grabString:@"key_exp_automatic"]] intValue];
            
            // Set setup_complete key to false again
            [prefs setBool:false forKey:[StringGrabber grabString:@"key_setup_complete"]];
            
        }
        
    }
    
}

// Is called every time AutomaticView appears
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // Autorotate
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}


- (void) displayMenu {
	UIActionSheet *popupQuery = [[UIActionSheet alloc]
                                 initWithTitle:nil
                                 delegate:self
                                 cancelButtonTitle:@"Cancel"
                                 destructiveButtonTitle:nil
                                 otherButtonTitles:@"Login", nil];
	popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	[popupQuery showInView:self.view];
	[popupQuery release];
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

// Release all the extras
- (void)dealloc {
    [mainLogo release];
    [mainLogoBackground release];
    [step1 release];
    [step2 release];
    [step3 release];
    [menuButton release];
    [step1Label release];
    [step3Label release];
    
    [locationManager release];
    locationManager = nil;
    
    [super dealloc];
    
}

// Log you into to iSENSE using the iSENSE API
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Logging in..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("automatic_login_from_login_function", NULL);
    dispatch_async(queue, ^{
        BOOL success = [isenseAPI login:usernameInput with:passwordInput];
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

// Catches long click, starts and stops recording and beeps
- (IBAction) onRecordLongClick:(UILongPressGestureRecognizer*)sender {
    if (sender.state == UIGestureRecognizerStateBegan) {
        if (!isRecording) {
            // Get the experiment
            NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
            expNum = [[prefs stringForKey:[StringGrabber grabString:@"key_exp_automatic"]] intValue];
            NSLog(@"my exp is: %d", expNum);
            
            // Get Field Order
            [dfm getFieldOrderOfExperiment:expNum];
            [self getEnabledFields];
            
            // Change the UI
            [self setRecordingLayout];
            
            // Record Data
            isRecording = TRUE;
            [self recordData];
        } else {
            // Change the UI
            [self setNonRecordingLayout];
            
            // Stop Recording
            backFromSetup = false;
            [self stopRecording:motionManager];
        }
        
        // Make the beep sound
        NSString *path = [NSString stringWithFormat:@"%@%@", [[NSBundle mainBundle] resourcePath], @"/button-37.wav"];
        SystemSoundID soundID;
        NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
        AudioServicesCreateSystemSoundID((CFURLRef)filePath, &soundID);
        AudioServicesPlaySystemSound(soundID);
    }
}

// Record the data and return the NSMutable array to be JSONed
- (void) recordData {
    
    // Get the recording rate
    float rate = .125;
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    NSString *sampleIntervalString = [prefs valueForKey:[StringGrabber grabString:@"key_sample_interval"]];
    sampleInterval = [sampleIntervalString floatValue];
    if (sampleInterval > 0) rate = sampleInterval / 1000;
    
    elapsedTime = 0;
    recordingRate = rate * 1000;
    
    // Set the accelerometer update interval to reccomended sample interval, and start updates
    motionManager.accelerometerUpdateInterval = rate;
    motionManager.magnetometerUpdateInterval = rate;
    motionManager.gyroUpdateInterval = rate;
    if (motionManager.accelerometerAvailable) [motionManager startAccelerometerUpdates];
    if (motionManager.magnetometerAvailable) [motionManager startMagnetometerUpdates];
    if (motionManager.gyroAvailable) [motionManager startGyroUpdates];
    
    // New JSON array to hold data
    dataToBeJSONed = [[NSMutableArray alloc] init];
    
    // Start the new timers TODO - put them on dispatch?
    recordDataTimer = [[NSTimer scheduledTimerWithTimeInterval:rate target:self selector:@selector(buildRowOfData) userInfo:nil repeats:YES] retain];
    timer = [[NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateElapsedTime) userInfo:nil repeats:YES] retain];
}

- (void) updateElapsedTime {
    
    if (!isRecording || timer == nil) {
        [timer invalidate];
        [timer release];
        timer = nil;
    }
    
    dispatch_queue_t queue = dispatch_queue_create("automatic_update_elapsed_time", NULL);
    dispatch_async(queue, ^{
        elapsedTime += 1;
        
        int minutes = elapsedTime / 60;
        int seconds = elapsedTime % 60;
        
        NSString *secondsStr;
        if (seconds < 10)
            secondsStr = [NSString stringWithFormat:@"0%d", seconds];
        else
            secondsStr = [NSString stringWithFormat:@"%d", seconds];
        
        int dataPoints = (int) (1000 / ((float)recordingRate) * elapsedTime);
        
        NSLog(@"points: %d", dataPoints);

        dispatch_async(dispatch_get_main_queue(), ^{
			[step3Label setText:[NSString stringWithFormat:@"Time Elapsed: %d:%@\nData Point Count: %d", minutes, secondsStr, dataPoints]];
        });
    });
    
}


// Fill dataToBeJSONed with a row of data
- (void) buildRowOfData {
    
    if (!isRecording || recordDataTimer == nil) {
        
        [recordDataTimer invalidate];
        [recordDataTimer release];
        recordDataTimer = nil;
        
    } else {
        
        dispatch_queue_t queue = dispatch_queue_create("automatic_record_data", NULL);
        dispatch_async(queue, ^{
            
            Fields *fieldsRow = [[Fields alloc] autorelease];
            
            // Fill a new row of data starting with time
            double time = [[NSDate date] timeIntervalSince1970];
            if ([dfm enabledFieldAtIndex:fTIME_MILLIS])
                fieldsRow.time_millis = [[[NSNumber alloc] initWithDouble:time * 1000] autorelease];
            
            
            // acceleration in meters per second squared
            if ([dfm enabledFieldAtIndex:fACCEL_X])
                fieldsRow.accel_x = [[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].x * 9.80665] autorelease];
            NSLog(@"Current accel x is: %@.", fieldsRow.accel_x);
            if ([dfm enabledFieldAtIndex:fACCEL_Y])
                fieldsRow.accel_y = [[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].y * 9.80665] autorelease];
            if ([dfm enabledFieldAtIndex:fACCEL_Z])
                fieldsRow.accel_z = [[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].z * 9.80665] autorelease];
            if ([dfm enabledFieldAtIndex:fACCEL_TOTAL])
                fieldsRow.accel_total = [[[NSNumber alloc] initWithDouble:
                                          sqrt(pow(fieldsRow.accel_x.doubleValue, 2)
                                               + pow(fieldsRow.accel_y.doubleValue, 2)
                                               + pow(fieldsRow.accel_z.doubleValue, 2))] autorelease];
            
            // latitude and longitude coordinates
            CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
            double latitude  = lc2d.latitude;
            double longitude = lc2d.longitude;
            if ([dfm enabledFieldAtIndex:fLATITUDE])
                fieldsRow.latitude = [[[NSNumber alloc] initWithDouble:latitude] autorelease];
            if ([dfm enabledFieldAtIndex:fLONGITUDE])
                fieldsRow.longitude = [[[NSNumber alloc] initWithDouble:longitude] autorelease];
            
            // magnetic field in microTesla
            if ([dfm enabledFieldAtIndex:fMAG_X])
                fieldsRow.mag_x = [[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].x] autorelease];
            if ([dfm enabledFieldAtIndex:fMAG_Y])
                fieldsRow.mag_y = [[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].y] autorelease];
            if ([dfm enabledFieldAtIndex:fMAG_Z])
                fieldsRow.mag_z = [[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].z] autorelease];
            if ([dfm enabledFieldAtIndex:fMAG_TOTAL])
                fieldsRow.mag_total = [[[NSNumber alloc] initWithDouble:
                                        sqrt(pow(fieldsRow.mag_x.doubleValue, 2)
                                             + pow(fieldsRow.mag_y.doubleValue, 2)
                                             + pow(fieldsRow.mag_z.doubleValue, 2))] autorelease];
            
            // rotation rate in radians per second
            if (motionManager.gyroAvailable) {
                if ([dfm enabledFieldAtIndex:fGYRO_X])
                    fieldsRow.gyro_x = [[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].x] autorelease];
                if ([dfm enabledFieldAtIndex:fGYRO_Y])
                    fieldsRow.gyro_y = [[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].y] autorelease];
                if ([dfm enabledFieldAtIndex:fGYRO_Z])
                    fieldsRow.gyro_z = [[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].z] autorelease];
            }
            
            // update parent JSON object
            [dfm orderDataFromFields:fieldsRow];
            
            if (dfm.data != nil && dataToBeJSONed != nil)
                [dataToBeJSONed addObject:dfm.data];
            // else NOTHING IS WRONG!!!
            
        });
    }
    
}

// This inits locations
- (void) initLocations {
    if (!locationManager) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        locationManager.distanceFilter = kCLDistanceFilterNone;
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        [locationManager startUpdatingLocation];
        geoCoder = [[CLGeocoder alloc] init];
    }
}

// Stops the recording and returns the actual data recorded :)
-(void) stopRecording:(CMMotionManager *)finalMotionManager {
    // Stop Timers
    [timer invalidate];
    [timer release];
    timer = nil;
    [recordDataTimer invalidate];
    [recordDataTimer release];
    recordDataTimer = nil;
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Disable step 2
    step2.enabled = false;
    step2.alpha = 0.5;
    
    // Back to recording mode
    //    startStopButton.image = [UIImage imageNamed:@"red_button.png"];
    //    mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
    //    startStopLabel.text = [StringGrabber grabString:@"start_button_text"];
    //    containerForMainButton updateImage:startStopButton];
    
    // Open up description dialog
    UIAlertView *message = [[UIAlertView alloc] initWithTitle:[StringGrabber grabString:@"description_or_delete"]
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:@"Delete"
                                            otherButtonTitles:@"Save Data", nil];
    
    message.tag = DESCRIPTION_AUTOMATIC;
    message.delegate = self;
    [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
    [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeDefault;
    [message show];
    [message release];
    
}

// Fetch the experiments from iSENSE
- (void) getExperiments {
    NSMutableArray *results = [isenseAPI getExperiments:[NSNumber numberWithUnsignedInt:1] withLimit:[NSNumber numberWithUnsignedInt:10] withQuery:@"" andSort:@"recent"];
    if ([results count] == 0) NSLog(@"No experiments found.");
    
    NSMutableArray *resultsFields = [isenseAPI getExperimentFields:[NSNumber numberWithUnsignedInt:514]];
    if ([resultsFields count] == 0) NSLog(@"No experiment fields found.");
    
}

- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
	UIAlertView *message;
    
	switch (buttonIndex) {
            
		case 0:
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
        
    } else if (actionSheet.tag == DESCRIPTION_AUTOMATIC) {
        
        isRecording = FALSE;
        
        if (buttonIndex != OPTION_CANCELED) {
            
            NSString *description = [[actionSheet textFieldAtIndex:0] text];
            if ([description length] == 0) {
                description = @"Session data gathered and uploaded from mobile phone using iSENSE DataCollector application.";
            }
            
            [self saveDataSetWithDescription:description];
            [self setEnabled:true forButton:step3];
            
            [self.view makeWaffle:@"Data Saved!"
                        duration:WAFFLE_LENGTH_SHORT
                        position:WAFFLE_BOTTOM
                           image:WAFFLE_CHECKMARK];
                        
        } else {
            
            [self.view makeWaffle:@"Data set deleted." duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_CHECKMARK];
            
        }
    }
}

// Save a data set so you don't have to upload it immediately
- (void) saveDataSetWithDescription:(NSString *)description {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    expNum = [[prefs stringForKey:[StringGrabber grabString:@"key_exp_automatic"]] intValue];
    
    bool uploadable = false;
    if (expNum > 1) uploadable = true;
    
    DataSet *ds = [NSEntityDescription insertNewObjectForEntityForName:@"DataSet" inManagedObjectContext:managedObjectContext];
    [ds setName:sessionName];
    [ds setDataDescription:description];
    [ds setEid:[NSNumber numberWithInt:expNum]];
    [ds setData:dataToBeJSONed];
    [ds setPicturePaths:[NSNull null]];
    [ds setSid:[NSNumber numberWithInt:-1]];
    [ds setCity:city];
    [ds setCountry:country];
    [ds setAddress:address];
    [ds setUploadable:[NSNumber numberWithBool:uploadable]];
    
    // Add the new data set to the queue
    [dataSaver addDataSet:ds];
    NSLog(@"There are %d dataSets in the dataSaver.", dataSaver.count);
    
    // Commit the changes
    NSError *error = nil;
    if (![managedObjectContext save:&error]) {
        // Handle the error.
        NSLog(@"%@", error);
    }
    
}

// Finds the associated address from a GPS location.
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    if (geoCoder) {
        [geoCoder reverseGeocodeLocation:newLocation completionHandler:^(NSArray *placemarks, NSError *error) {
            if ([placemarks count] > 0) {
                city = [[[placemarks objectAtIndex:0] locality] retain];
                country = [[[placemarks objectAtIndex:0] country] retain];
                NSString *subThoroughFare = [[placemarks objectAtIndex:0] subThoroughfare];
                NSString *thoroughFare = [[placemarks objectAtIndex:0] thoroughfare];
                address = [[NSString stringWithFormat:@"%@ %@", subThoroughFare, thoroughFare] retain];
                
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

// Reset address fields for next session
- (void)resetAddressFields {
    city = [[NSString alloc] initWithString:@"N/a"];
    country = [[NSString alloc] initWithString:@"N/a"];
    address = [[NSString alloc] initWithString:@"N/a"];
}

// This is for the loading spinner when the app starts automatic mode
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

// Calls step one to get an experiment, sample interval, test length, etc.
- (IBAction) setup:(UIButton *)sender {
    
    StepOneSetup *stepView = [[StepOneSetup alloc] init];
    stepView.title = @"Step 1: Setup";
    [self.navigationController pushViewController:stepView animated:YES];
    [stepView release];
    
}

// Launches a view that allows the user to upload and manage his/her datasets
- (IBAction) uploadData:(UIButton *)sender {
    
    QueueUploaderView *queueUploader = [[QueueUploaderView alloc] init];
    queueUploader.title = @"Step 3: Manage and Upload Sessions";
    [self.navigationController pushViewController:queueUploader animated:YES];
    [queueUploader release];
    
}

// Button enable switch
- (void) setEnabled:(BOOL)enabled forButton:(UIButton *)button  {
    button.enabled = enabled;
    if (button.enabled) {
        button.alpha = 1;
    } else {
        button.alpha = .5;
    }
}

// Enabled fields check
- (void) getEnabledFields {
    
    // if exp# = -1 then enable all, else enable some
    if (expNum == -1) {
        
        for (int i = 0; i < [[dfm order] count]; i++) {
            [dfm setEnabledField:true atIndex:i];
            NSLog(@"setting: %d", i); // TODO - dfm array is empty?
        }
        
    } else {
        
        int i = 0;
        
        // get the sensorCompatability array first
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        NSMutableArray *selectedCells = [prefs objectForKey:@"selected_cells"];

        for (NSString *s in [dfm order]) {
            if ([s isEqualToString:[StringGrabber grabField:@"accel_x"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fACCEL_X];
                    
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_y"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fACCEL_Y];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_z"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fACCEL_Z];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_total"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fACCEL_TOTAL];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"time"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fTIME_MILLIS];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"latitude"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fLATITUDE];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"longitude"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fLONGITUDE];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"magnetic_x"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fMAG_X];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"magnetic_y"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fMAG_Y];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"magnetic_z"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fMAG_Z];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"magnetic_total"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fMAG_TOTAL];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"heading_deg"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fANGLE_DEG];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"heading_rad"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fANGLE_RAD];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"temperature_c"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fTEMPERATURE_C];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"temperature_f"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fTEMPERATURE_F];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"temperature_k"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fTEMPERATURE_K];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"pressure"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fPRESSURE];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"altitude"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fALTITUDE];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"luminous_flux"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fLUX];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_x"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fGYRO_X];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_y"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fGYRO_Y];
                }
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_z"]]) {
                if ([selectedCells[i] integerValue] == 1) {
                    [dfm setEnabledField:true atIndex:fGYRO_Z];
                }
            }
            
            ++i;
        }
    }
}

- (void) setRecordingLayout {
    
    [step2 setTitle:@"STOP\n(Press and Hold)" forState:UIControlStateNormal];
    [step2 setTitleColor:[HexColor colorWithHexString:@"59B048"] forState:UIControlStateNormal];

    [step1 setAlpha:0.0];
    [step3 setAlpha:0.0];
    [step1 setEnabled:NO];
    [step3 setEnabled:NO];
    
    [step1Label setAlpha:1.0];
    [step3Label setAlpha:1.0];
    
    [mainLogoBackground setBackgroundColor:[HexColor colorWithHexString:@"004400"]];
    [mainLogo setImage:[UIImage imageNamed:@"rsense_logo_recording"]];
    
    [step3Label setText:@"Time Elapsed: 0:00\nData Point Count: 0"];
    
}

- (void) setNonRecordingLayout {

    [step2 setTitle:@"Step 2: Record a Data Set (Hold Down)" forState:UIControlStateNormal];
    [step2 setTitleColor:[HexColor colorWithHexString:@"4C6FD9"] forState:UIControlStateNormal];
    
    [step1 setAlpha:1.0];
    [step3 setAlpha:1.0];
    [step1 setEnabled:YES];
    [step3 setEnabled:YES];
    
    [step1Label setAlpha:0.0];
    [step3Label setAlpha:0.0];
    
    step2.titleLabel.textColor = [HexColor colorWithHexString:@"000066"];
    
    [mainLogoBackground setBackgroundColor:[HexColor colorWithHexString:@"000066"]];
    [mainLogo setImage:[UIImage imageNamed:@"rsense_logo"]];

}

@end
