//
//  AutomaticViewController.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/10/13.
//  Modified by Mike Stowell on 7/22/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "AutomaticViewController.h"

@implementation AutomaticViewController

@synthesize isRecording, motionManager, dataToBeJSONed, projNum, timer, recordDataTimer, elapsedTime, locationManager, dfm, testLength, dataSetName,
sampleInterval, geoCoder, city, address, country, dataSaver, managedObjectContext, api, longClickRecognizer, backFromSetup, recordingRate,
dataToBeOrdered, backFromQueue, f;

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

// Implement viewDidLoad after the nib has been loaded
- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Immediately kill the timers with fire
    if (timer != nil) {
        [timer invalidate];
        timer = nil;
    }
    if (recordDataTimer != nil) {
        [recordDataTimer invalidate];
        recordDataTimer = nil;
    }
    
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
    
    // API setup
    api = [API getInstance];
    [api useDev:TRUE];
    
    // Initializes an Assortment of Variables
    motionManager = [[CMMotionManager alloc] init];
    dfm = [[DataFieldManager alloc] init];
    sampleInterval = DEFAULT_SAMPLE_INTERVAL;
    
    // Initialize buttons
    bool step2Enabled = [prefs boolForKey:[StringGrabber grabString:@"key_step_2_enabled"]];
    
    [self setEnabled:true forButton:step1];
    
    if (backFromSetup || step2Enabled) {
        [self setEnabled:true forButton:step2]; 
    } else {
        [self setEnabled:false forButton:step2];
    }

    // Check if any dataSets from this view controller are here
    BOOL enableStep3 = false;
    NSArray *keys = [dataSaver.dataQueue allKeys];
    for (int i = 0; i < keys.count; i++) {
        QDataSet *tmp = [dataSaver.dataQueue objectForKey:keys[i]];
        if ([tmp.parentName isEqualToString:PARENT_AUTOMATIC]) {
            enableStep3 = true;
        }
    }
    
    if (enableStep3) {
        [self setEnabled:true forButton:step3];
    } else {
        [self setEnabled:false forButton:step3];
    }
    
    // Initialize locations
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
    
    // If true, then we're coming back from another ViewController
    if (self.isMovingToParentViewController == NO) {
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        backFromSetup = [prefs boolForKey:[StringGrabber grabString:@"key_setup_complete"]];
        
        // We have a data set name, sample interval, and test length ready
        if (backFromSetup) {
            
            // retrieve the data from the setup dialog
            NSString *sampleIntervalString = [prefs valueForKey:[StringGrabber grabString:@"key_sample_interval"]];
            sampleInterval = [sampleIntervalString floatValue];
            
            NSString *testLengthString = [prefs valueForKey:[StringGrabber grabString:@"key_test_length"]];
            testLength = [testLengthString integerValue];
            
            dataSetName = [prefs valueForKey:[StringGrabber grabString:@"key_step1_data_set_name"]];
            
            projNum = [[prefs stringForKey:[StringGrabber grabString:@"key_proj_automatic"]] intValue];
            
            // Set setup_complete key to false again, initialize the keep_step_2_enabled key to on
            [prefs setBool:false forKey:[StringGrabber grabString:@"key_setup_complete"]];
            [prefs setBool:true forKey:[StringGrabber grabString:@"key_step_2_enabled"]];
            
        }
        
    }
    
    // Reinitialize setup and queue to false
    backFromSetup = false;
    
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
                                 otherButtonTitles:@"Login", @"Media", nil];
	popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	[popupQuery showInView:self.view];
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


// Log you into to iSENSE using the iSENSE API
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Logging in..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("automatic_login_from_login_function", NULL);
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
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        if (!isRecording) {
            // Get the project
            projNum = [[prefs stringForKey:[StringGrabber grabString:@"key_proj_automatic"]] intValue];

            // Get Field Order
            //[dfm getOrder];
            //[dfm getFieldOrderOfExperiment:projNum]; TODO
            //[self getEnabledFields];
            f = [[Fields alloc] init];
            dfm = [[DataFieldManager alloc] initWithProjID:projNum API:api andFields:f];
            [dfm getOrder];
            
//            if (projNum == -1) {
//                [dfm getOrder];
//            } else {
//                [dfm getOrder];
//            }
            
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
            [prefs setBool:false forKey:[StringGrabber grabString:@"key_step_2_enabled"]];
            [self stopRecording:motionManager];
        }
        
        // Make the beep sound
        NSString *path = [NSString stringWithFormat:@"%@%@", [[NSBundle mainBundle] resourcePath], @"/button-37.wav"];
        SystemSoundID soundID;
        NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
        AudioServicesCreateSystemSoundID((CFURLRef)CFBridgingRetain(filePath), &soundID);
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
    dataToBeOrdered = [[NSMutableArray alloc] init];
    
    // Start the new timers TODO - put them on dispatch?
    recordDataTimer = [NSTimer scheduledTimerWithTimeInterval:rate target:self selector:@selector(buildRowOfData) userInfo:nil repeats:YES];
    timer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateElapsedTime) userInfo:nil repeats:YES];
}

- (void) updateElapsedTime {
    
    if (!isRecording || timer == nil) {
        [timer invalidate];
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
        
        dispatch_async(dispatch_get_main_queue(), ^{
			[step3Label setText:[NSString stringWithFormat:@"Time Elapsed: %d:%@\nData Point Count: %d", minutes, secondsStr, dataPoints]];
        });
    });
    
}


// Fill dataToBeOrdered with a row of data
- (void) buildRowOfData {
    
    if (!isRecording || recordDataTimer == nil) {
        
        [recordDataTimer invalidate];
        recordDataTimer = nil;
        
    } else {
        
        dispatch_queue_t queue = dispatch_queue_create("automatic_record_data", NULL);
        dispatch_async(queue, ^{
            
            Fields *fieldsRow = [[Fields alloc] init];
            
            // Fill a new row of data starting with time
            double time = [[NSDate date] timeIntervalSince1970];
            if ([dfm enabledFieldAtIndex:fTIME_MILLIS])
                fieldsRow.time_millis = [NSNumber numberWithDouble:time * 1000];
            
            
            // acceleration in meters per second squared
            if ([dfm enabledFieldAtIndex:fACCEL_X])
                fieldsRow.accel_x = [NSNumber numberWithDouble:[motionManager.accelerometerData acceleration].x * 9.80665];
            if ([dfm enabledFieldAtIndex:fACCEL_Y])
                fieldsRow.accel_y = [NSNumber numberWithDouble:[motionManager.accelerometerData acceleration].y * 9.80665];
            if ([dfm enabledFieldAtIndex:fACCEL_Z])
                fieldsRow.accel_z = [NSNumber numberWithDouble:[motionManager.accelerometerData acceleration].z * 9.80665];
            if ([dfm enabledFieldAtIndex:fACCEL_TOTAL])
                fieldsRow.accel_total = [NSNumber numberWithDouble:
                                          sqrt(pow(fieldsRow.accel_x.doubleValue, 2)
                                               + pow(fieldsRow.accel_y.doubleValue, 2)
                                               + pow(fieldsRow.accel_z.doubleValue, 2))];
            
            // latitude and longitude coordinates
            CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
            double latitude  = lc2d.latitude;
            double longitude = lc2d.longitude;
            if ([dfm enabledFieldAtIndex:fLATITUDE])
                fieldsRow.latitude = [NSNumber numberWithDouble:latitude];
            if ([dfm enabledFieldAtIndex:fLONGITUDE])
                fieldsRow.longitude = [NSNumber numberWithDouble:longitude];
            
            // magnetic field in microTesla
            if ([dfm enabledFieldAtIndex:fMAG_X])
                fieldsRow.mag_x = [NSNumber numberWithDouble:[motionManager.magnetometerData magneticField].x];
            if ([dfm enabledFieldAtIndex:fMAG_Y])
                fieldsRow.mag_y = [NSNumber numberWithDouble:[motionManager.magnetometerData magneticField].y];
            if ([dfm enabledFieldAtIndex:fMAG_Z])
                fieldsRow.mag_z = [NSNumber numberWithDouble:[motionManager.magnetometerData magneticField].z];
            if ([dfm enabledFieldAtIndex:fMAG_TOTAL])
                fieldsRow.mag_total = [NSNumber numberWithDouble:
                                        sqrt(pow(fieldsRow.mag_x.doubleValue, 2)
                                             + pow(fieldsRow.mag_y.doubleValue, 2)
                                             + pow(fieldsRow.mag_z.doubleValue, 2))];
            
            // rotation rate in radians per second
            if (motionManager.gyroAvailable) {
                if ([dfm enabledFieldAtIndex:fGYRO_X])
                    fieldsRow.gyro_x = [NSNumber numberWithDouble:[motionManager.gyroData rotationRate].x];
                if ([dfm enabledFieldAtIndex:fGYRO_Y])
                    fieldsRow.gyro_y = [NSNumber numberWithDouble:[motionManager.gyroData rotationRate].y];
                if ([dfm enabledFieldAtIndex:fGYRO_Z])
                    fieldsRow.gyro_z = [NSNumber numberWithDouble:[motionManager.gyroData rotationRate].z];
            }
            
            // TODO there's more fields, right...?

            
            // update data object
            if (dataToBeOrdered == nil)
                dataToBeOrdered = [[NSMutableArray alloc] init];

            //[dataToBeOrdered addObject:fieldsRow];
      
            if (dataToBeOrdered != nil) {
                [dfm setFields:fieldsRow];

                if (projNum == -1) {
                    [dataToBeOrdered addObject:[dfm putDataForNoProjectID]];
                } else {
                    [dataToBeOrdered addObject:[dfm putData]];
                }

            }
            
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
    timer = nil;
    [recordDataTimer invalidate];
    recordDataTimer = nil;
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Disable step 2
    step2.enabled = false;
    step2.alpha = 0.5;
    
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
    
}

- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
	UIAlertView *message;
    
	switch (buttonIndex) {
            
        // Login
		case 0:
            message = [[UIAlertView alloc] initWithTitle:@"Login"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            message.tag = MENU_LOGIN;
			[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
            [message textFieldAtIndex:0].tag = TAG_AUTO_LOGIN;
            [message textFieldAtIndex:1].tag = TAG_AUTO_LOGIN;
            [message textFieldAtIndex:0].delegate = self;
            [message textFieldAtIndex:1].delegate = self;
            [message show];
            
            break;
            
        // Media
        case 1:
            [self.view makeWaffle:@"This feature is currently disabled."
                         duration:WAFFLE_LENGTH_SHORT
                         position:WAFFLE_BOTTOM
                            image:WAFFLE_RED_X];
            
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
                description = @"Data set recorded and uploaded from mobile device using iSENSE iOS Uploader application.";
            }
            
            NSLog(@"save");
            [self saveDataSetWithDescription:description];
            NSLog(@"safe!!");
            [self setEnabled:true forButton:step3];
            
                        
        } else {
            
            [self.view makeWaffle:@"Data set deleted." duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_CHECKMARK];
            
        }
        
    } else if (actionSheet.tag == MENU_MEDIA_AUTOMATIC) {
        // TODO - media code
    }
    
    NSLog(@"dont mattah what ya shoveling");
}

// Save a data set so you don't have to upload it immediately
- (void) saveDataSetWithDescription:(NSString *)description {
    
    
//    UIAlertView *message = [self getDispatchDialogWithMessage:@"Please wait while we organize your data..."];
//    [message show];
    
//    dispatch_queue_t queue = dispatch_queue_create("automatic_ordering_data_in_upload", NULL);
//    dispatch_async(queue, ^{
    
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        projNum = [[prefs stringForKey:[StringGrabber grabString:@"key_proj_automatic"]] intValue];
        
        bool uploadable = false;
        if (projNum > 1) uploadable = true;
        
        QDataSet *ds = [[QDataSet alloc] initWithEntity:[NSEntityDescription entityForName:@"QDataSet"
                                                                  inManagedObjectContext:managedObjectContext]
                       insertIntoManagedObjectContext:managedObjectContext];
        
//        dataToBeJSONed = [[NSMutableArray alloc] init];
        
        // Organize the data from dataToBeOrdered TODO - what is this crap? can we just do the line below?
//        for (int i = 0; i < [dataToBeOrdered count]; i++) {
//            Fields *f = [dataToBeOrdered objectAtIndex:i];
//            [dfm orderDataFromFields:f];
//            [dataToBeJSONed addObject:dfm.data];
//        }
        
        // @Mike: dfm.data is currently nil so of course it is going to crash here
//        [dataToBeJSONed addObject:dfm.data];

//        dispatch_async(dispatch_get_main_queue(), ^{
    
            [ds setName:dataSetName];
            [ds setParentName:PARENT_AUTOMATIC];
            [ds setDataDescription:description];
            [ds setProjID:[NSNumber numberWithInt:projNum]];
            [ds setData:dataToBeOrdered];
            [ds setPicturePaths:nil];
            [ds setUploadable:[NSNumber numberWithBool:uploadable]];
            [ds setHasInitialProj:[NSNumber numberWithBool:(projNum != -1)]];
            
            // Add the new data set to the queue
            [dataSaver addDataSet:ds];
            
            [self.view makeWaffle:@"Data set saved"
                         duration:WAFFLE_LENGTH_SHORT
                         position:WAFFLE_BOTTOM
                            image:WAFFLE_CHECKMARK];
            
//            [message dismissWithClickedButtonIndex:nil animated:YES];
//        });
//        
//    });
    
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

// Reset address fields for next data set
- (void)resetAddressFields {
    city = @"N/A";
    country = @"N/A";
    address = @"N/A";
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
    return message;
}

// Calls step one to get an project, sample interval, test length, etc.
- (IBAction) setup:(UIButton *)sender {
    
    StepOneSetup *stepView = [[StepOneSetup alloc] init];
    stepView.title = @"Step 1: Setup";
    [self.navigationController pushViewController:stepView animated:YES];
    
}

// Launches a view that allows the user to upload and manage his/her datasets
- (IBAction) uploadData:(UIButton *)sender {
    
    if ([dataSaver dataSetCountWithParentName:PARENT_AUTOMATIC] > 0) {
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        backFromQueue = true;
        [prefs setBool:backFromQueue forKey:[StringGrabber grabString:@"key_back_from_queue"]];
        [prefs setInteger:DATA_NONE_UPLOADED forKey:@"key_data_uploaded"];
        
        QueueUploaderView *queueUploader = [[QueueUploaderView alloc] initWithParentName:PARENT_AUTOMATIC];
        queueUploader.title = @"Step 3: Upload";
        [self.navigationController pushViewController:queueUploader animated:YES];
    } else {
        [self.view makeWaffle:@"No data to upload" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_WARNING];
    }
    
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

- (void) setupDFMWithAllFields {
    
    for (int i = 0; i < [[dfm order] count]; i++) {
        [dfm setEnabledField:true atIndex:i];
    }
}

// Enabled fields check
- (void) getEnabledFields {
    
    if (projNum == -1) {
        [self setupDFMWithAllFields];
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
    [step2 setTitleColor:UIColorFromHex(0x59B048) forState:UIControlStateNormal];

    [step1 setAlpha:0.0];
    [step3 setAlpha:0.0];
    [step1 setEnabled:NO];
    [step3 setEnabled:NO];
    
    [step1Label setAlpha:1.0];
    [step3Label setAlpha:1.0];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    NSString *sampleIntervalString = [prefs valueForKey:[StringGrabber grabString:@"key_sample_interval"]];
    NSString *testLengthString = [prefs valueForKey:[StringGrabber grabString:@"key_test_length"]];
    NSString *dsName = [prefs valueForKey:[StringGrabber grabString:@"key_step1_data_set_name"]];
    [step1Label setText:[NSString stringWithFormat:@"Recording data for \"%@\" at a sample interval of %@ ms for %@ sec",
                     dsName, sampleIntervalString, testLengthString]];
    
    [step3Label setText:@"Time Elapsed: 0:00\nData Point Count: 0"];
    
}

- (void) setNonRecordingLayout {

    [step2 setTitle:@"Step 2: Record a Data Set (Hold Down)" forState:UIControlStateNormal];
    [step2 setTitleColor:UIColorFromHex(0x4C6FD9) forState:UIControlStateNormal];
    
    [step1 setAlpha:1.0];
    [step3 setAlpha:1.0];
    [step1 setEnabled:YES];
    [step3 setEnabled:YES];
    
    [step1Label setAlpha:0.0];
    [step3Label setAlpha:0.0];
    
    step2.titleLabel.textColor = UIColorFromHex(0x000066);

}

- (BOOL) containsAcceptedCharacters:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:
      [StringGrabber grabString:@"accepted_chars"]] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    
    NSUInteger newLength = [textField.text length] + [string length] - range.length;
    
    switch (textField.tag) {
            
        case TAG_AUTO_LOGIN:
            if (![self containsAcceptedCharacters:string])
                return NO;
            
            return (newLength > 100) ? NO : YES;
            
        default:
            return YES;
    }
}

/* // To see if you have a camera
 * [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
 */
- (BOOL)startCameraControllerFromViewController:(UIViewController*)controller usingDelegate:(id <UIImagePickerControllerDelegate, UINavigationControllerDelegate>) delegate {
    
    if (([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera] == NO) || (delegate == nil) || (controller == nil))
        return NO;
    
    UIImagePickerController *cameraUI = [[UIImagePickerController alloc] init];
    cameraUI.sourceType = UIImagePickerControllerSourceTypeCamera;
    
    // Displays a control that allows the user to choose picture or
    // movie capture, if both are available:
    cameraUI.mediaTypes = [UIImagePickerController availableMediaTypesForSourceType:UIImagePickerControllerSourceTypeCamera];
    
    // Hides the controls for moving & scaling pictures, or for
    // trimming movies. To instead show the controls, use YES.
    cameraUI.allowsEditing = NO;
    cameraUI.delegate = delegate;
    
    [controller presentModalViewController:cameraUI animated:YES];
    
    return YES;
}

- (IBAction) showCameraUI {
    [self startCameraControllerFromViewController:self usingDelegate:self];
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error {
    
}

@end
