//
//  ViewController.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import "ViewController.h"

@interface ViewController ()

- (IBAction)showMenu:(id)sender;

@end

@implementation ViewController

@synthesize start, menuButton, vector_status, login_status, items, recordLength, countdown, change_name, iapi, running, timeOver, setupDone, dfm, motionmanager, locationManager, recordDataTimer, timer, testLength, expNum, sampleInterval, sessionName,geoCoder,city,country,address,dataToBeJSONed,elapsedTime,recordingRate, experiment,firstName,lastInitial,userName,useDev,passWord,session_num,managedObjectContext,dataSaver,x,y,z,mag,image,exp_num, loginalert, picker,lengths, lengthField, saveModeEnabled, saveMode, dataToBeOrdered ;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    saver->first = firstName;
    saver->last = lastInitial;
    saver->user = userName;
    saver->pass = passWord;
    
    if (running) {
        return;
    }
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController~landscape_iPad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController_iPad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController~landscape_iPhone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController_iPhone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
        
    }
    
    
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return (running) ? NO : YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return (running) ? NO : YES;
}

// iOS6 enable rotation
- (NSUInteger)supportedInterfaceOrientations {
    if (running) {
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

// returns the number of 'columns' to display.
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
    
}

// returns the # of rows in each component..
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent: (NSInteger)component
{
    return 6;
    
}

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return [self.lengths objectAtIndex:row];
}
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    lengthField.text = [self.lengths objectAtIndex:row];
}


- (void)viewDidLoad {
    
    [super viewDidLoad];
    
	UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPress:)];
    [start addGestureRecognizer:longPress];
    
    
    
    useDev = TRUE;
    
    
    iapi = [iSENSE getInstance];
    [iapi toggleUseDev: useDev];
    
    if (saver == nil) {
        saver = new RotationDataSaver;
        saver->hasName = false;
        saver->hasLogin = false;
        saver->first =  [[NSString alloc] init];
        saver->last = [[NSString alloc] init];
        saver->user = [[NSString alloc] init];
        saver->pass = [[NSString alloc] init];
        saver->saveMode = NO;
    }
    
    saveModeEnabled = saver->saveMode;
    
    
    running = NO;
    timeOver = NO;
    setupDone = NO;
    
    dfm = [[DataFieldManager alloc] init];
    //[dfm setEnabledField:YES atIndex:fACCEL_Y];
    motionmanager = [[CMMotionManager alloc] init];
    
    
    if (saver->hasName){
        firstName = saver->first;
        lastInitial = saver->last;
    } else {
        firstName = @"No Name";
        lastInitial = @"Provided";
    }
    
    if (saver->hasLogin){
        userName = saver->user;
        passWord = saver->pass;
    } else {
        userName = @"sor";
        passWord = @"sor";
        
        
    }
    
    
    if (saveModeEnabled) {
        expNum = -1;
    } else {
        if (useDev) {
            expNum = DEV_DEFAULT_EXP;
        } else {
            expNum = PROD_DEFAULT_EXP;
        }
    }
    
    
    
    
    [self login:userName withPassword:passWord];
    
    // Managed Object Context for Data_CollectorAppDelegate
    if (managedObjectContext == nil) {
        managedObjectContext = [(AppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext];
    }
    
    // DataSaver from Data_CollectorAppDelegate
    if (dataSaver == nil) {
        dataSaver = [(AppDelegate *) [[UIApplication sharedApplication] delegate] dataSaver];
        NSLog(@"Datasaver Details: %@", dataSaver.description);
        NSLog(@"Current count = %d", dataSaver.count);
    }
    
    self.navigationItem.rightBarButtonItem = menuButton;
    
    [image setAutoresizesSubviews:YES];
    
    lengths = [[NSMutableArray alloc] initWithObjects:@"1 sec", @"2 sec", @"5 sec", @"10 sec", @"30 sec", @"60 sec", nil];
    
    picker = [[UIPickerView alloc]init];
    [picker setDataSource:self];
    [picker setDelegate:self];
    
    [picker setShowsSelectionIndicator:YES];
    
    [self setPickerDefault];
    
    
    
    
    
    
}

- (void) setPickerDefault {
    switch (countdown) {
        case 1:
            [picker selectRow:0 inComponent:0 animated:YES];
            lengthField.text = [picker.delegate pickerView:picker titleForRow:0 forComponent:0];
            break;
        case 2:
            [picker selectRow:1 inComponent:0 animated:YES];
            lengthField.text = [picker.delegate pickerView:picker titleForRow:1 forComponent:0];
            break;
        case 5:
            [picker selectRow:2 inComponent:0 animated:YES];
            lengthField.text = [picker.delegate pickerView:picker titleForRow:2 forComponent:0];
            break;
        case 10:
            [picker selectRow:3 inComponent:0 animated:YES];
            lengthField.text = [picker.delegate pickerView:picker titleForRow:3 forComponent:0];
            break;
        case 30:
            [picker selectRow:4 inComponent:0 animated:YES];
            lengthField.text = [picker.delegate pickerView:picker titleForRow:4 forComponent:0];
            break;
        case 60:
            [picker selectRow:5 inComponent:0 animated:YES];
            lengthField.text = [picker.delegate pickerView:picker titleForRow:5 forComponent:0];
            break;
        default:
            break;
    }
    
    [picker reloadComponent:0];
}

- (void) viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    BOOL x1 = [prefs boolForKey:@"X"];
    BOOL y1 = [prefs boolForKey:@"Y"];
    BOOL z1 = [prefs boolForKey:@"Z"];
    BOOL mag1 = [prefs boolForKey:@"Magnitude"];
    
    
    x = x1;
    y = y1;
    z = z1;
    mag = mag1;
    
    if (self.isMovingToParentViewController == YES) {
        
        change_name = [[UIAlertView alloc] initWithTitle:@"Enter Name" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles:@"Done", nil];
        
        [change_name setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
        UITextField *last = [change_name textFieldAtIndex:1];
        [last setSecureTextEntry:NO];
        [change_name textFieldAtIndex:0].placeholder = @"First Name";
        UITextField *first = [change_name textFieldAtIndex:0];
        first.delegate = self;
        first.tag = FIRST_NAME_FIELD;
        last.placeholder = @"Last Initial";
        last.delegate = self;
        change_name.tag = FIRST_TIME_NAME;
        [change_name show];
        
        
        if (!iapi.isConnectedToInternet){
            [dfm setEnabledField:x atIndex:fACCEL_X];
            [dfm setEnabledField:y atIndex:fACCEL_Y];
            [dfm setEnabledField:z atIndex:fACCEL_Z];
            [dfm setEnabledField:mag atIndex:fACCEL_TOTAL];
        }
        
        recordLength = 10;
        countdown = 10;
        
        [self saveModeDialog];
        
    }
    
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    
}

- (void) saveModeDialog {
    if (![iapi isConnectedToInternet]) {
        saveMode = [[UIAlertView alloc] initWithTitle:@"No Connectivity" message:@"Could not connect to the Internet through either Wi-Fi or mobile service. You will not be able to upload data to iSENSE until either is enabled.\n* Turning on Save Mode will allow data to be saved until Internet is enabled." delegate:self cancelButtonTitle:@"Try Again" otherButtonTitles:@"Save Mode", nil];
        
        [saveMode show];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)longPress:(UILongPressGestureRecognizer*)gesture {
    if ( gesture.state == UIGestureRecognizerStateEnded ) {
        NSLog(@"Long Press");
        if (!running) {
            // Get Field Order
            if (!iapi.isConnectedToInternet){
                [dfm setEnabledField:x atIndex:fACCEL_X];
                [dfm setEnabledField:y atIndex:fACCEL_Y];
                [dfm setEnabledField:z atIndex:fACCEL_Z];
                [dfm setEnabledField:mag atIndex:fACCEL_TOTAL];
            } else {
                [self getEnabledFields];
            }
            // Record Data
            running = YES;
            [start setEnabled:NO];
            [self recordData];
        }
        
        NSString *path = [NSString stringWithFormat:@"%@%@", [[NSBundle mainBundle] resourcePath], @"/button-37.wav"];
        SystemSoundID soundID;
        NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
        CFURLRef url = (__bridge CFURLRef)filePath;
        AudioServicesCreateSystemSoundID(url, &soundID);
        AudioServicesPlaySystemSound(soundID);
        
    }
}

// Record the data and return the NSMutable array to be JSONed
- (void) recordData {
    
    // Get the recording rate
    float rate = .125;
    /*NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
     NSString *sampleIntervalString = [prefs valueForKey:[StringGrabber grabString:@"key_sample_interval"]];
     sampleInterval = [sampleIntervalString floatValue];*/
    sampleInterval = 125;
    if (sampleInterval > 0) rate = sampleInterval / 1000;
    
    elapsedTime = 0;
    recordingRate = rate * 1000;
    
    NSLog(@"rate: %d", recordingRate);
    
    // Set the accelerometer update interval to reccomended sample interval, and start updates
    motionmanager.accelerometerUpdateInterval = rate;
    motionmanager.magnetometerUpdateInterval = rate;
    motionmanager.gyroUpdateInterval = rate;
    if (motionmanager.accelerometerAvailable) [motionmanager startAccelerometerUpdates];
    if (motionmanager.magnetometerAvailable) [motionmanager startMagnetometerUpdates];
    if (motionmanager.gyroAvailable) [motionmanager startGyroUpdates];
    
    // New JSON array to hold data
    dataToBeJSONed = [[NSMutableArray alloc] init];
    dataToBeOrdered = [[NSMutableArray alloc] init];
    
    // Start the new timers TODO - put them on dispatch?
    recordDataTimer = [NSTimer scheduledTimerWithTimeInterval:rate target:self selector:@selector(buildRowOfData) userInfo:nil repeats:YES];
    timer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateElapsedTime) userInfo:nil repeats:YES];
}


- (void) updateElapsedTime {
    
    if (!running) {
        
    }
    
    
    
    dispatch_queue_t queue = dispatch_queue_create("automatic_update_elapsed_time", NULL);
    dispatch_async(queue, ^{
        elapsedTime += 1;
        
        int seconds = elapsedTime % 60;
        
        NSString *secondsStr;
        if (seconds < 10)
            secondsStr = [NSString stringWithFormat:@"0%d", seconds];
        else
            secondsStr = [NSString stringWithFormat:@"%d", seconds];
        
        int dataPoints = (1000 / recordingRate) * elapsedTime;
        
        NSLog(@"points: %d", dataPoints);
        
        if (countdown >= 0) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [start setTitle:[NSString stringWithFormat:@"%d", countdown] forState:UIControlStateNormal];
                countdown--;
            });
        }
        
        if (countdown < 0) {
            
            [self stopRecording:motionmanager];
        }
        
        
        
        
    });
    
}


// Fill dataToBeJSONed with a row of data
- (void) buildRowOfData {
    Fields *fieldsRow = [[Fields alloc] init];
    
    // Fill a new row of data starting with time
    double time = [[NSDate date] timeIntervalSince1970];
    fieldsRow.time_millis = [[NSNumber alloc] initWithDouble:time * 1000];
    NSLog(@"Current time is: %@.", fieldsRow.time_millis);
    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
    [formatter setMaximumFractionDigits:1];
    [formatter setMinimumFractionDigits:0];
    
    dispatch_queue_t queue = dispatch_queue_create("record_data", NULL);
    dispatch_async(queue, ^{
        // acceleration in meters per second squared
        
        fieldsRow.accel_x = [[NSNumber alloc] initWithDouble:[motionmanager.accelerometerData acceleration].x * 9.80665];
        NSLog(@"Current accel x is: %@.", fieldsRow.accel_x);
        if ([dfm enabledFieldAtIndex:fACCEL_X]){
            dispatch_async(dispatch_get_main_queue(), ^{
                
                vector_status.text = [@"X: " stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_x]];
            });
        }
        
        fieldsRow.accel_y = [[NSNumber alloc] initWithDouble:[motionmanager.accelerometerData acceleration].y * 9.80665];
        if ([dfm enabledFieldAtIndex:fACCEL_Y]) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if ([dfm enabledFieldAtIndex:fACCEL_X])
                    vector_status.text = [vector_status.text stringByAppendingString:[@", Y: " stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_y]]];
                else
                    vector_status.text = [@"Y: " stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_y]];
            });
        }
        NSLog(@"Current accel y is: %@.", fieldsRow.accel_y);
        fieldsRow.accel_z = [[NSNumber alloc] initWithDouble:[motionmanager.accelerometerData acceleration].z * 9.80665];
        NSLog(@"Current accel z is: %@.", fieldsRow.accel_z);
        if ([dfm enabledFieldAtIndex:fACCEL_Z]){
            dispatch_async(dispatch_get_main_queue(), ^{
                if ([dfm enabledFieldAtIndex:fACCEL_X] || [dfm enabledFieldAtIndex:fACCEL_Y]) {
                    vector_status.text = [vector_status.text stringByAppendingString:[@", Z: " stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_z]]];
                } else
                    vector_status.text = [@"Z: " stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_z]];
            });
        }
        fieldsRow.accel_total = [[NSNumber alloc] initWithDouble:
                                 sqrt(pow(fieldsRow.accel_x.doubleValue, 2)
                                      + pow(fieldsRow.accel_y.doubleValue, 2)
                                      + pow(fieldsRow.accel_z.doubleValue, 2))];
        NSLog(@"Current accel total is: %@.", fieldsRow.accel_total);
        if ([dfm enabledFieldAtIndex:fACCEL_TOTAL]){
            dispatch_async(dispatch_get_main_queue(), ^{
                if ([dfm enabledFieldAtIndex:fACCEL_X] || [dfm enabledFieldAtIndex:fACCEL_Y] || [dfm enabledFieldAtIndex:fACCEL_Z])
                    vector_status.text = [vector_status.text stringByAppendingString:[@", Magnitude: " stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_total]]];
                else
                    vector_status.text = [@"Magnitude: " stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_total]];
            });
        }
    });
    // Update parent JSON object
    if (dataToBeOrdered != nil)
        [dataToBeOrdered addObject:fieldsRow];
    
    
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
    [recordDataTimer invalidate];
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Stop Recording
    running = NO;
    [vector_status setText:@"Y: "];
    countdown = recordLength;
    dispatch_async(dispatch_get_main_queue(), ^{
        [start setTitle:@"Hold to Start" forState:UIControlStateNormal];
        [start setEnabled:YES];
        
    });
    
    NSString *name = firstName;
    name = [name stringByAppendingString:@" "];
    name = [name stringByAppendingString:lastInitial];
    
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"HH:mm:ss"];
    
    NSDate *now = [[NSDate alloc] init];
    
    NSString* timeString = [dateFormat stringFromDate:now];
    
    sessionName = [name stringByAppendingString:[@" " stringByAppendingString:timeString]];;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        
        UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Publish to iSENSE?"
                                                          message:nil
                                                         delegate:self
                                                cancelButtonTitle:@"Discard"
                                                otherButtonTitles:@"Publish", nil];
        
        message.delegate = self;
        [message show];
    });
    
}

-(void) stopRecordingWithoutPublishing:(CMMotionManager *)finalMotionManager {
    
    // Stop Timers
    [timer invalidate];
    [recordDataTimer invalidate];
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Stop Recording
    running = NO;
    [vector_status setText:@"Y: "];
    countdown = recordLength;
    dispatch_async(dispatch_get_main_queue(), ^{
        [start setTitle:@"Hold to Start" forState:UIControlStateNormal];
        [start setEnabled:YES];
        
    });
    
    NSString *name = firstName;
    name = [name stringByAppendingString:@" "];
    name = [name stringByAppendingString:lastInitial];
    
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"HH:mm:ss"];
    
    NSDate *now = [[NSDate alloc] init];
    
    NSString* timeString = [dateFormat stringFromDate:now];
    
    sessionName = [name stringByAppendingString:[@" " stringByAppendingString:timeString]];;
}


// Enabled fields check
- (void) getEnabledFields {
    
    // if exp# = -1 then enable all, else enable some
    if (expNum == -1) {
        
        for (int i = 0; i < [[dfm order] count]; i++) {
            [dfm setEnabledField:YES atIndex:i];
        }
        
    } else {
        
        int i = 0;
        
        for (NSString *s in [dfm order]) {
            if ([s isEqualToString:[StringGrabber grabField:@"accel_x"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_X];
                
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_y"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_Y];
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_z"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_Z];
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_total"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_TOTAL];
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"time"]]) {
                [dfm setEnabledField:YES atIndex:fTIME_MILLIS];
            }
            
            ++i;
        }
    }
}

- (void) browseExp {
    [experiment dismissWithClickedButtonIndex:1 animated:YES];
    ExperimentBrowseViewController *browse;
    browse = [[ExperimentBrowseViewController alloc] init];
    browse.title = @"Browse for Experiments";
    browse.chosenExperiment = &expNum;
    [self.navigationController pushViewController:browse animated:YES];
    
}

- (void) QRCode {
    [experiment dismissWithClickedButtonIndex:2 animated:YES];
    if ([[UIApplication sharedApplication]
         canOpenURL:[NSURL URLWithString:@"pic2shop:"]]) {
        NSURL *urlp2s = [NSURL URLWithString:@"pic2shop://scan?callback=carPhysics%3A//EAN"];
        [[UIApplication sharedApplication] openURL:urlp2s];
    } else {
        NSURL *urlapp = [NSURL URLWithString:
                         @"http://itunes.com/app/pic2shop"];
        [[UIApplication sharedApplication] openURL:urlapp];
    }
}

- (void) expCode {
    [experiment dismissWithClickedButtonIndex:0 animated:YES];
    exp_num = [[UIAlertView alloc] initWithTitle:@"Enter Experiment #" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
    [exp_num setAlertViewStyle:UIAlertViewStylePlainTextInput];
    if (useDev) {
        [exp_num textFieldAtIndex:0].text = [NSString stringWithFormat:@"%d",DEV_DEFAULT_EXP];
    } else {
        [exp_num textFieldAtIndex:0].text = [NSString stringWithFormat:@"%d",PROD_DEFAULT_EXP];
    }
    
    [exp_num show];
}

// Save a data set so you don't have to upload it immediately
- (void) saveDataSetWithDescription:(NSString *)description {
    
    bool uploadable = false;
    
    if (iapi.isConnectedToInternet)
        expNum = -1;
    
    if (expNum > 1) uploadable = true;
    [dfm getFieldOrderOfExperiment:expNum];
    
    // Organize the data from dataToBeOrdered
    for (int i = 0; i < [dataToBeOrdered count]; i++) {
        Fields *f = [dataToBeOrdered objectAtIndex:i];
        [dfm orderDataFromFields:f];
        [dataToBeJSONed addObject:dfm.data];
    }
    
    NSLog(@"Bla");
    DataSet *ds = [[DataSet alloc] initWithEntity:[NSEntityDescription entityForName:@"DataSet" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    [ds setName:sessionName];
    [ds setDataDescription:description];
    [ds setEid:[NSNumber numberWithInt:expNum]];
    [ds setData:dataToBeJSONed];
    [ds setPicturePaths:nil];
    [ds setSid:[NSNumber numberWithInt:-1]];
    [ds setCity:city];
    [ds setCountry:country];
    [ds setAddress:address];
    [ds setUploadable:[NSNumber numberWithBool:uploadable]];
    [ds setParentName:@"CarRampPhysics"];
    NSLog(@"Bla2");
    // Add the new data set to the queue
    [dataSaver addDataSet:ds];
    NSLog(@"There are %d dataSets in the dataSaver.", dataSaver.count);
    
    
}

- (bool) uploadData:(NSString *) description {
    
    if ([iapi isConnectedToInternet]) {
        
        if (![iapi isLoggedIn]) {
            [self.view makeWaffle:@"Not logged in" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
            return false;
            
        }
        
        session_num = [iapi createSession:sessionName withDescription:description Street:address City:city Country:country toExperiment:[NSNumber numberWithInt: expNum]];
        
        
        
        [dfm getFieldOrderOfExperiment:expNum];
        
        // Organize the data from dataToBeOrdered
        for (int i = 0; i < [dataToBeOrdered count]; i++) {
            Fields *f = [dataToBeOrdered objectAtIndex:i];
            [dfm orderDataFromFields:f];
            [dataToBeJSONed addObject:dfm.data];
        }
        
        NSArray *array = [NSArray arrayWithArray:dataToBeJSONed];
        NSError *error = nil;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:array options:0 error:&error];
        if (error != nil) {
            NSLog(@"Error:%@", error);
            return false;
        }
        
        bool success = [iapi putSessionData:jsonData forSession:session_num inExperiment:[NSNumber numberWithInt: expNum]];
        if (!success) {
            [self.view makeWaffle:@"Unable to upload" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
        } else {
            [self.view makeWaffle:@"Upload successful" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_CHECKMARK];
            
        }
        
        
        
    } else {
        NSLog(@"Derp");
        [self saveDataSetWithDescription:sessionName];
    }
    return true;
    
}

- (IBAction)showMenu:(id)sender {
    
    
    RNGridMenu *menu;
    
    UIImage *upload = [UIImage imageNamed:@"upload2"];
    UIImage *settings = [UIImage imageNamed:@"settings"];
    UIImage *code = [UIImage imageNamed:@"barcode"];
    UIImage *login = [UIImage imageNamed:@"users"];
    UIImage *about = [UIImage imageNamed:@"info"];
    UIImage *reset = [UIImage imageNamed:@"reset"];
    
    void (^uploadBlock)() = ^() {
        NSLog(@"Upload button pressed");
        if ([dataSaver count] > 0) {
            QueueUploaderView *queueUploader = [[QueueUploaderView alloc] init];
            queueUploader.title = @"Upload saved data";
            [self.navigationController pushViewController:queueUploader animated:YES];
        } else {
            [self.view makeWaffle:@"No data sets to upload!" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
        }
        
    };
    void (^settingsBlock)() = ^() {
        NSLog(@"Record Settings button pressed");
        UIActionSheet *settings_menu = [[UIActionSheet alloc] initWithTitle:@"Settings" delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Variables", @"Length", @"Name", nil];
        [settings_menu showInView:self.view];
        
    };
    void (^codeBlock)() = ^() {
        NSLog(@"Experiment button pressed");
        experiment = [[UIAlertView alloc] initWithTitle:@"Experiment Code" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles: nil];
        [experiment addButtonWithTitle:@"Enter Experiment #"];
        [experiment addButtonWithTitle:@"Browse"];
        [experiment addButtonWithTitle:@"QR Code"];
        [experiment addButtonWithTitle:@"Done"];
        [experiment show];
        
    };
    void (^loginBlock)() = ^() {
        NSLog(@"Login button pressed");
        
        loginalert = [[UIAlertView alloc] initWithTitle:@"Login to iSENSE" message:@"" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
        [loginalert setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
        [loginalert textFieldAtIndex:0].delegate = self;
        [loginalert textFieldAtIndex:1].delegate = self;
        [loginalert textFieldAtIndex:0].tag = LOGIN_USER;
        [loginalert textFieldAtIndex:1].tag = LOGIN_PASS;
        [loginalert show];
    };
    void (^aboutBlock)() = ^() {
        NSLog(@"About button pressed");
        
        AboutViewController *about;
        // Override point for customization after application launch.
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
            about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPhone" bundle:nil];
        } else {
            about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPad" bundle:nil];
        }
        
        [self.navigationController pushViewController:about animated:YES];
        
    };
    void (^resetBlock)() = ^() {
        NSLog(@"Reset button pressed");
        countdown = recordLength = 10;
        userName = passWord = @"sor";
        [self login:userName withPassword:passWord];
        login_status.text = [@"Logged in as: " stringByAppendingString: userName];
        login_status.text = [login_status.text stringByAppendingString:@" Name: "];
        login_status.text = [login_status.text stringByAppendingString:firstName];
        login_status.text = [login_status.text stringByAppendingString:@" "];
        login_status.text = [login_status.text stringByAppendingString:lastInitial];
    };
    
    RNGridMenuItem *uploadItem = [[RNGridMenuItem alloc] initWithImage:upload title:@"Upload" action:uploadBlock];
    RNGridMenuItem *recordSettingsItem = [[RNGridMenuItem alloc] initWithImage:settings title:@"Settings" action:settingsBlock];
    RNGridMenuItem *codeItem = [[RNGridMenuItem alloc] initWithImage:code title:@"Experiment" action:codeBlock];
    RNGridMenuItem *loginItem = [[RNGridMenuItem alloc] initWithImage:login title:@"Login" action:loginBlock];
    RNGridMenuItem *aboutItem = [[RNGridMenuItem alloc] initWithImage:about title:@"About" action:aboutBlock];
    RNGridMenuItem *resetItem = [[RNGridMenuItem alloc] initWithImage:reset title:@"Reset" action:resetBlock];
    
    items = [[NSArray alloc] initWithObjects:uploadItem, recordSettingsItem, codeItem, loginItem, aboutItem, resetItem, nil];
    
    menu = [[RNGridMenu alloc] initWithItems:items];
    
    menu.delegate = self;
    
    [menu showInViewController:self center:CGPointMake(self.view.bounds.size.width/2.f, self.view.bounds.size.height/2.f)];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
    if (buttonIndex == 0){
        VariablesViewController *var;
        // Override point for customization after application launch.
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
            var = [[VariablesViewController alloc] initWithNibName:@"VariablesViewController_iPhone" bundle:nil];
        } else {
            var = [[VariablesViewController alloc] initWithNibName:@"VariablesViewController_iPad" bundle:nil];
        }
        
        [self.navigationController pushViewController:var animated:YES];
    } else if (buttonIndex == 1) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Enter recording length" message:@"Enter time in seconds." delegate:self cancelButtonTitle:nil otherButtonTitles:@"Done", nil];
        [alert setAlertViewStyle:UIAlertViewStylePlainTextInput];
        lengthField = [alert textFieldAtIndex:0];
        lengthField.inputView = picker;
        [self setPickerDefault];
        [alert show];
    } else if (buttonIndex == 2){
        change_name = [[UIAlertView alloc] initWithTitle:@"Enter Name" message:@"" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"Done", nil];
        
        [change_name setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
        UITextField *last = [change_name textFieldAtIndex:1];
        [last setSecureTextEntry:NO];
        [change_name textFieldAtIndex:0].placeholder = @"First Name";
        UITextField *first = [change_name textFieldAtIndex:0];
        first.tag = FIRST_NAME_FIELD;
        first.delegate = self;
        last.placeholder = @"Last Initial";
        last.delegate = self;
        change_name.tag = ENTER_NAME;
        [change_name show];
    }
    
    
    
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    NSLog(@"Hello");
    NSCharacterSet *cs = [[NSCharacterSet characterSetWithCharactersInString:ACCEPTABLE_CHARACTERS] invertedSet];
    NSUInteger newLength = [textField.text length] + [string length] - range.length;
    if ([string rangeOfCharacterFromSet:cs].location == NSNotFound) {
        if (textField.tag == FIRST_NAME_FIELD || textField.tag == LOGIN_USER || textField.tag == LOGIN_PASS) {
            return (newLength > 20) ? NO : YES;
        } else {
            return (newLength > 1) ? NO : YES;
        }
    } else {
        return NO;
    }
    
    
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    NSString *title = [alertView buttonTitleAtIndex:buttonIndex];
    
    if ([alertView.title isEqualToString:@"Enter recording length"]) {
        
        if([title isEqualToString:@"Done"])
        {
            UITextField *length = [alertView textFieldAtIndex:0];
            
            NSArray *lolcats = [length.text componentsSeparatedByString:@" "];
            recordLength = countdown = [lolcats[0] intValue];
            NSLog(@"Length is %d", recordLength);
            
        }
    } else if ([alertView.title isEqualToString:@"Login to iSENSE"]) {
        [self login:[alertView textFieldAtIndex:0].text withPassword:[alertView textFieldAtIndex:1].text];
        [login_status setText:[@"Logged in as: " stringByAppendingString: [alertView textFieldAtIndex:0].text]];
    } else if ([alertView.title isEqualToString:@"Publish to iSENSE?"]) {
        if ([title isEqualToString:@"Discard"]) {
            [self.view makeWaffle:@"Data discarded!" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
        } else {
            UIAlertView *dis = [self getDispatchDialogWithMessage:@"Uploading to iSENSE..."];
            [dis show];
            dispatch_queue_t queue = dispatch_queue_create("upload", NULL);
            dispatch_async(queue, ^{
                [self uploadData: @""];
            });
            [dis dismissWithClickedButtonIndex:nil animated:YES];
            if (!saveModeEnabled){
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"View data on iSENSE?" message:nil delegate:self cancelButtonTitle:@"Don't View" otherButtonTitles:@"View", nil];
                [alert show];
            }
        }
    } else if ([alertView.title isEqualToString:@"View data on iSENSE?"]) {
        
        if ([title isEqualToString:@"View"]) {
            NSString *url;
            NSLog(@"%@",session_num);
            NSLog(@"\n%@", [NSString stringWithFormat:@"%d", [session_num intValue]]);
            if (useDev) {
                url = [DEV_VIS_URL stringByAppendingString:[NSString stringWithFormat:@"%d", [session_num intValue]]];
            } else {
                url = [PROD_VIS_URL stringByAppendingString:[NSString stringWithFormat:@"%d", [session_num intValue]]];
            }
            NSLog(@"%@",url);
            UIApplication *mySafari = [UIApplication sharedApplication];
            NSURL *myURL = [[NSURL alloc]initWithString:[url stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
            [mySafari openURL:myURL];
        }
        
    } else if ([alertView.title isEqualToString:@"Enter Name"]) {
        
        if ([title isEqualToString:@"Cancel"]) {
            [change_name dismissWithClickedButtonIndex:0 animated:YES];
            login_status.text = [@"Logged in as: " stringByAppendingString: userName];
            login_status.text = [login_status.text stringByAppendingString:@" Name: "];
            login_status.text = [login_status.text stringByAppendingString:firstName];
            login_status.text = [login_status.text stringByAppendingString:@" "];
            login_status.text = [login_status.text stringByAppendingString:lastInitial];
            saver->hasName = true;
        } else {
            if ([[alertView textFieldAtIndex:0].text isEqualToString:@""] || [[alertView textFieldAtIndex:1].text isEqualToString:@""]) {
                if (alertView.tag == FIRST_TIME_NAME) {
                    change_name = [[UIAlertView alloc] initWithTitle:@"Enter Name" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles:@"Done", nil];
                    change_name.tag = FIRST_TIME_NAME;
                } else {
                    change_name = [[UIAlertView alloc] initWithTitle:@"Enter Name" message:@"" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"Done", nil];
                    change_name.tag = ENTER_NAME;
                }
                
                
                [change_name setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
                UITextField *last = [change_name textFieldAtIndex:1];
                [last setSecureTextEntry:NO];
                [change_name textFieldAtIndex:0].placeholder = @"First Name";
                UITextField *first = [change_name textFieldAtIndex:0];
                first.delegate = self;
                first.tag = FIRST_NAME_FIELD;
                last.placeholder = @"Last Initial";
                last.delegate = self;
                
                [change_name show];
                [self.view makeWaffle:@"Please Enter Your Name" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
            } else {
                [self changeName];
            }
        }
    } else if ([alertView.title isEqualToString:@"Experiment Code"]){
        if ([title isEqualToString:@"Enter Experiment #"]) {
            [self expCode];
        } else if ([title isEqualToString:@"Browse"]) {
            [self browseExp];
        } else if ([title isEqualToString:@"QR Code"]) {
            [self QRCode];
        } else {
            [experiment dismissWithClickedButtonIndex:3 animated:YES];
        }
    } else if ([alertView.title isEqualToString:@"Enter Experiment #"]) {
        expNum = [[alertView textFieldAtIndex:0].text intValue];
        if (expNum == 0) {
            if (saveModeEnabled) {
                expNum = -1;
            } else {
                if (useDev) {
                    expNum = DEV_DEFAULT_EXP;
                } else {
                    expNum = PROD_DEFAULT_EXP;
                }
            }
        }
    } else if ([alertView.title isEqualToString:@"No Connectivity"]) {
        if ([title isEqualToString:@"Try Again"]){
            [self saveModeDialog];
        } else {
            saveModeEnabled = YES;
            saver->saveMode = YES;
            expNum = -1;
            [self.view makeWaffle:@"Save Mode Enabled" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_CHECKMARK];
        }
    }
}

- (void) changeName {
    
    [change_name dismissWithClickedButtonIndex:0 animated:YES];
    firstName = [change_name textFieldAtIndex:0].text;
    lastInitial = [change_name textFieldAtIndex:1].text;
    login_status.text = [@"Logged in as: " stringByAppendingString: userName];
    login_status.text = [login_status.text stringByAppendingString:@" Name: "];
    login_status.text = [login_status.text stringByAppendingString:firstName];
    login_status.text = [login_status.text stringByAppendingString:@" "];
    login_status.text = [login_status.text stringByAppendingString:lastInitial];
    saver->hasName = true;
    
}

- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    UIAlertView *message;
    if (!saver->hasLogin){
        message = [self getDispatchDialogWithMessage:@"Logging in..."];
        [message show];
    }
    NSLog(@"Making waffle");
    dispatch_queue_t queue = dispatch_queue_create("automatic_login_from_login_function", NULL);
    dispatch_async(queue, ^{
        BOOL success = [iapi login:usernameInput with:passwordInput];
        dispatch_async(dispatch_get_main_queue(), ^{
            if (success) {
                if (!saver->hasLogin){
                    NSLog(@"%@", self.view);
                    [self.view makeWaffle:@"Login Successful!"
                                 duration:WAFFLE_LENGTH_SHORT
                                 position:WAFFLE_BOTTOM
                                    image:WAFFLE_CHECKMARK];
                }
                login_status.text = [@"Logged in as: " stringByAppendingString: usernameInput];
                login_status.text = [login_status.text stringByAppendingString:@" Name: "];
                login_status.text = [login_status.text stringByAppendingString:firstName];
                login_status.text = [login_status.text stringByAppendingString:@" "];
                login_status.text = [login_status.text stringByAppendingString:lastInitial];
                userName = usernameInput;
                passWord = passwordInput;
                
                saver->hasLogin = true;
            } else {
                [self.view makeWaffle:@"Login Failed!"
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_RED_X];
            }
            if (message != nil)
                [message dismissWithClickedButtonIndex:nil animated:YES];
        });
    });
    
}

// This is for the loading spinner when the app starts things
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





@end
