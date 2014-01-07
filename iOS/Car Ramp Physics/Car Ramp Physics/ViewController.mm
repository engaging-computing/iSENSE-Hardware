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

@synthesize start, menuButton, vector_status, login_status, items, recordLength, countdown, change_name, api, running, timeOver, setupDone, dfm, motionmanager, locationManager, recordDataTimer, timer, testLength, expNum, sampleInterval, sessionName,geoCoder,city,country,address,dataToBeJSONed,elapsedTime,recordingRate, experiment,firstName,lastInitial,userName,useDev,passWord,session_num,managedObjectContext,dataSaver,x,y,z,mag,image,exp_num, loginalert, picker,lengths, lengthField, saveModeEnabled, saveMode, dataToBeOrdered, formatter;

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
    
    formatter = [[NSNumberFormatter alloc] init];
    
    [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
    
    [formatter setMaximumFractionDigits:3];
    
    [formatter setRoundingMode: NSNumberFormatterRoundUp];
    
    useDev = TRUE;
    
    api = [API getInstance];
    [api useDev: useDev];
    
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
    
    motionmanager = [[CMMotionManager alloc] init];
    
    if (saver->hasLogin){
        userName = saver->user;
        passWord = saver->pass;
    } else {
        userName = @"mobile";
        passWord = @"mobile";
        
        
    }
    
    if (saver->hasName){
        firstName = saver->first;
        lastInitial = saver->last;
    } else {
        firstName = @"Mobile";
        lastInitial = @"U.";
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
        NSLog(@"Current count = %d", dataSaver.dataQueue.count);
    }
    
    self.navigationItem.rightBarButtonItem = menuButton;
    
    [image setAutoresizesSubviews:YES];
    
    lengths = [[NSMutableArray alloc] initWithObjects:@"1 sec", @"2 sec", @"5 sec", @"10 sec", @"30 sec", @"60 sec", nil];
    
    picker = [[UIPickerView alloc]init];
    [picker setDataSource:self];
    [picker setDelegate:self];
    
    [picker setShowsSelectionIndicator:YES];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    recordLength = countdown = [defaults integerForKey:@"recordLength"];    
    
    [self setPickerDefault];
    
    dfm = [[DataFieldManager alloc] initWithProjID:expNum API:api andFields:nil];    
    
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
        [last setPlaceholder:@"Last Initial"];
        UITextField *first = [change_name textFieldAtIndex:0];
        first.tag = FIRST_NAME_FIELD;
        first.delegate = self;
        last.delegate = self;
        [first setPlaceholder:@"First Name"];
        change_name.tag = FIRST_TIME_NAME;
        [change_name show];
        
        
        if (![API hasConnectivity]){
            [dfm setEnabledField:x atIndex:fACCEL_X];
            [dfm setEnabledField:y atIndex:fACCEL_Y];
            [dfm setEnabledField:z atIndex:fACCEL_Z];
            [dfm setEnabledField:mag atIndex:fACCEL_TOTAL];
        }
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        recordLength = countdown = [defaults integerForKey:@"recordLength"];
        
        [self saveModeDialog];
        
    }
    
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    
}

- (void) saveModeDialog {
    if (![API hasConnectivity]) {
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
            if (![API hasConnectivity]){
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
    
    [start setTitle:[NSString stringWithFormat:@"%d", countdown] forState:UIControlStateNormal];
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
    [dfm enableAllFields];
    
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
        
        if (countdown >=  1) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [start setTitle:[NSString stringWithFormat:@"%d", --countdown] forState:UIControlStateNormal];
            });
        }
        
        if (countdown < 1) {
            
            [self stopRecording:motionmanager];
        }
        
        
        
        
    });
    
}


// Fill dataToBeOrdered with a row of data
- (void) buildRowOfData {
    
    if (!running || recordDataTimer == nil) {
        
        [recordDataTimer invalidate];
        recordDataTimer = nil;
        
    } else {
        
        dispatch_queue_t queue = dispatch_queue_create("automatic_record_data", NULL);
        dispatch_async(queue, ^{
            
            Fields *fieldsRow = [[Fields alloc] init];
            
            NSString *vector = @"";
            
            // Fill a new row of data starting with time
            double time = [[NSDate date] timeIntervalSince1970];
            if ([dfm enabledFieldAtIndex:fTIME_MILLIS])
                fieldsRow.time_millis = [NSNumber numberWithDouble:time * 1000];
            
            
            // acceleration in meters per second squared
            if ([dfm enabledFieldAtIndex:fACCEL_X]) {
                fieldsRow.accel_x = [NSNumber numberWithDouble:[motionmanager.accelerometerData acceleration].x * 9.80665];
                vector = [vector stringByAppendingString:@"X: "];
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_x]];
            } if ([dfm enabledFieldAtIndex:fACCEL_Y]) {
                fieldsRow.accel_y = [NSNumber numberWithDouble:[motionmanager.accelerometerData acceleration].y * 9.80665];
                if ([vector length] == 0) {
                    vector = [vector stringByAppendingString:@"Y: "];
                } else {
                    vector = [vector stringByAppendingString:@", Y: "];
                }
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_y]];
            } if ([dfm enabledFieldAtIndex:fACCEL_Z]) {
                fieldsRow.accel_z = [NSNumber numberWithDouble:[motionmanager.accelerometerData acceleration].z * 9.80665];
                if ([vector length] == 0) {
                    vector = [vector stringByAppendingString:@"Z: "];
                } else {
                    vector = [vector stringByAppendingString:@", Z: "];
                }
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_z]];
            } if ([dfm enabledFieldAtIndex:fACCEL_TOTAL]) {
                fieldsRow.accel_total = [NSNumber numberWithDouble:
                                         sqrt(pow(fieldsRow.accel_x.doubleValue, 2)
                                              + pow(fieldsRow.accel_y.doubleValue, 2)
                                              + pow(fieldsRow.accel_z.doubleValue, 2))];
            
                if ([vector length] == 0) {
                    vector = [vector stringByAppendingString:@"Total: "];
                } else {
                    vector = [vector stringByAppendingString:@", Total: "];
                }
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_total]];
            
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [vector_status setText:vector];
            });
            
            
            // update data object
            if (dataToBeOrdered == nil)
                dataToBeOrdered = [[NSMutableArray alloc] init];
            
            //[dataToBeOrdered addObject:fieldsRow];
            
            if (dataToBeOrdered != nil) {
                [dfm setFields:fieldsRow];
                
            [dataToBeOrdered addObject:[dfm putData]];
                
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
    [recordDataTimer invalidate];
    [menuButton setEnabled:YES];
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Stop Recording
    running = NO;
    [vector_status setText:@""];
    countdown = recordLength;
    dispatch_async(dispatch_get_main_queue(), ^{
        [start setTitle:@"Hold to Start" forState:UIControlStateNormal];
        [start setEnabled:YES];
        
    });
    
    NSString *name = firstName;
    name = [name stringByAppendingString:@" "];
    name = [name stringByAppendingString:lastInitial];
    name = [name stringByAppendingString:@". "];
    
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
    [menuButton setEnabled:YES];
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Stop Recording
    running = NO;
    [vector_status setText:@""];
    countdown = recordLength;
    dispatch_async(dispatch_get_main_queue(), ^{
        [start setTitle:@"Hold to Start" forState:UIControlStateNormal];
        [start setEnabled:YES];
        
    });
    
    NSString *name = firstName;
    name = [name stringByAppendingString:@" "];
    name = [name stringByAppendingString:lastInitial];
    name = [name stringByAppendingString:@". "];
    
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
    ProjectBrowseViewController *browse;
    browse = [[ProjectBrowseViewController alloc] init];
    browse.title = @"Browse for Projects";
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
    exp_num = [[UIAlertView alloc] initWithTitle:@"Enter Project ID" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
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
    [menuButton setEnabled:YES];
    
    if (![API hasConnectivity])
        expNum = -1;
    
    if (expNum > 1) uploadable = true;
    
    NSLog(@"Bla");
    QDataSet *ds = [[QDataSet alloc] initWithEntity:[NSEntityDescription entityForName:@"QDataSet" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    [ds setName:sessionName];
    [ds setDataDescription:description];
    [ds setProjID:[NSNumber numberWithInt:expNum]];
    [ds setData:dataToBeJSONed];
    [ds setPicturePaths:nil];
    [ds setUploadable:[NSNumber numberWithBool:uploadable]];
    [ds setParentName:@"CarRampPhysics"];
    NSLog(@"Bla2");
    // Add the new data set to the queue
    [dataSaver addDataSet:ds];
    NSLog(@"There are %d dataSets in the dataSaver.", dataSaver.dataQueue.count);
    
    
}

- (bool) uploadData:(NSString *) description {
    
    if ([API hasConnectivity]) {
        
        
        if ([[api getCurrentUser].name isEqualToString:@""]) {
            [self.view makeWaffle:@"Not logged in" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
            return false;
            
        }
        
        dataToBeJSONed = [DataFieldManager reOrderData:dataToBeOrdered forProjectID:expNum withFieldOrder:[dfm getOrderList] andFieldIDs:[dfm getFieldIDs]];
        NSLog(@"REORDER SUCCESSFUL: %@", dataToBeJSONed);
        NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
        [data setObject:dataToBeJSONed forKey:@"data"];
        data = [[api rowsToCols:data] mutableCopy];
        
        bool success = [api jsonDataUploadWithId:expNum withData:data andName:sessionName];
        if (!success) {
            [self.view makeWaffle:@"Unable to upload" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
        } else {
            [self.view makeWaffle:@"Upload successful" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_CHECKMARK];
         
        }
        
        
        
    } else {
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
        if (dataSaver.dataQueue.count > 0) {
            QueueUploaderView *queueUploader = [[QueueUploaderView alloc] init];
            queueUploader.title = @"Upload saved data";
            [self.navigationController pushViewController:queueUploader animated:YES];
        } else {
            [self.view makeWaffle:@"No data sets to upload!" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
        }
        
    };
    void (^settingsBlock)() = ^() {
        NSLog(@"Record Settings button pressed");
        UIActionSheet *settings_menu = [[UIActionSheet alloc] initWithTitle:@"Settings" delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Length", @"Name", nil];
        [settings_menu showInView:self.view];
        
    };
    void (^codeBlock)() = ^() {
        NSLog(@"Experiment button pressed");
        experiment = [[UIAlertView alloc] initWithTitle:@"Project ID" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles: nil];
        [experiment addButtonWithTitle:@"Enter Project ID"];
        [experiment addButtonWithTitle:@"Browse"];
        [experiment addButtonWithTitle:@"QR Code"];
        [experiment addButtonWithTitle:@"Create New Project"];
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
        userName = passWord = @"mobile";
        [self login:userName withPassword:passWord];
        login_status.text = [@"Logged in as: " stringByAppendingString: userName];
        login_status.text = [login_status.text stringByAppendingString:@" Name: "];
        login_status.text = [login_status.text stringByAppendingString:firstName];
        login_status.text = [login_status.text stringByAppendingString:@" "];
        login_status.text = [login_status.text stringByAppendingString:lastInitial];
        login_status.text = [login_status.text stringByAppendingString:@". "];
    };
    
    RNGridMenuItem *uploadItem = [[RNGridMenuItem alloc] initWithImage:upload title:@"Upload" action:uploadBlock];
    RNGridMenuItem *recordSettingsItem = [[RNGridMenuItem alloc] initWithImage:settings title:@"Settings" action:settingsBlock];
    RNGridMenuItem *codeItem = [[RNGridMenuItem alloc] initWithImage:code title:@"Project ID" action:codeBlock];
    RNGridMenuItem *loginItem = [[RNGridMenuItem alloc] initWithImage:login title:@"Login" action:loginBlock];
    RNGridMenuItem *aboutItem = [[RNGridMenuItem alloc] initWithImage:about title:@"About" action:aboutBlock];
    RNGridMenuItem *resetItem = [[RNGridMenuItem alloc] initWithImage:reset title:@"Reset" action:resetBlock];
    
    items = [[NSArray alloc] initWithObjects:uploadItem, recordSettingsItem, codeItem, loginItem, aboutItem, resetItem, nil];
    
    menu = [[RNGridMenu alloc] initWithItems:items];
    
    menu.delegate = self;
    
    [menu showInViewController:self center:CGPointMake(self.view.bounds.size.width/2.f, self.view.bounds.size.height/2.f)];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
    
    if (buttonIndex == 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Enter recording length" message:@"Enter time in seconds." delegate:self cancelButtonTitle:nil otherButtonTitles:@"Done", nil];
        [alert setAlertViewStyle:UIAlertViewStylePlainTextInput];
        lengthField = [alert textFieldAtIndex:0];
        lengthField.inputView = picker;
        [self setPickerDefault];
        [alert show];
    } else if (buttonIndex == 1){
        change_name = [[UIAlertView alloc] initWithTitle:@"Enter Name" message:@"" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"Done", nil];
        
        [change_name setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
        UITextField *last = [change_name textFieldAtIndex:1];
        [last setSecureTextEntry:NO];
        [last setPlaceholder:@"Last Initial"];
        UITextField *first = [change_name textFieldAtIndex:0];
        first.tag = FIRST_NAME_FIELD;
        first.delegate = self;
        last.delegate = self;
        [first setPlaceholder:@"First Name"];
        change_name.tag = ENTER_NAME;
        [change_name show];
    }
    
    
    
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
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
            
            NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
            [defaults setInteger:recordLength forKey:@"recordLength"];
            
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
            [self uploadData: @""];
            [dis dismissWithClickedButtonIndex:nil animated:YES];
            
            if (!saveModeEnabled){
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"View data on iSENSE?" message:nil delegate:self cancelButtonTitle:@"Don't View" otherButtonTitles:@"View", nil];
                dispatch_async(dispatch_get_main_queue(), ^{ [alert show]; });
                
            }
             
        }
    } else if ([alertView.title isEqualToString:@"View data on iSENSE?"]) {
        
        if ([title isEqualToString:@"View"]) {
            NSString *url;
            NSLog(@"%@",session_num);
            NSLog(@"\n%@", [NSString stringWithFormat:@"%d", [session_num intValue]]);
            if (useDev) {
                url = [DEV_VIS_URL stringByAppendingString:[NSString stringWithFormat:@"%d", expNum]];
            } else {
                url = [PROD_VIS_URL stringByAppendingString:[NSString stringWithFormat:@"%d", expNum]];
            }
            
            [url stringByAppendingString:@"/data_sets/"];
            [url stringByAppendingString:[NSString stringWithFormat:@"%d", [session_num intValue]]];
                
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
            login_status.text = [login_status.text stringByAppendingString:@". "];
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
                [last setPlaceholder:@"Last Initial"];
                UITextField *first = [change_name textFieldAtIndex:0];
                first.tag = FIRST_NAME_FIELD;
                first.delegate = self;
                last.delegate = self;
                [first setPlaceholder:@"First Name"];
                [change_name show];
                [self.view makeWaffle:@"Please Enter Your Name" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
            } else {
                [self changeName];
            }
        }
    } else if ([alertView.title isEqualToString:@"Project ID"]){
        if ([title isEqualToString:@"Enter Project ID"]) {
            [self expCode];
        } else if ([title isEqualToString:@"Browse"]) {
            [self browseExp];
        } else if ([title isEqualToString:@"QR Code"]) {
            [self QRCode];
        } else if ([title isEqualToString:@"Create New Project"]) {
            [self createProject];
        } else {
            [experiment dismissWithClickedButtonIndex:3 animated:YES];
        }
    } else if ([alertView.title isEqualToString:@"Enter Project ID"]) {
        expNum = [[alertView textFieldAtIndex:0].text intValue];
            if (saveModeEnabled) {
                expNum = -1;
            } else {
                if (useDev) {
                    expNum = DEV_DEFAULT_EXP;
                } else {
                    expNum = PROD_DEFAULT_EXP;
                }
            }
            [self launchFieldMatchingViewControllerFromBrowse:FALSE];
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
    login_status.text = [login_status.text stringByAppendingString:@". "];
    saver->hasName = true;
    
}

- (void) createProject {
    
    
    
    
    
}

// Log into iSENSE
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    // __block BOOL success;
    // __block RPerson *curUser;
    
    UIAlertView *spinnerDialog = [self getDispatchDialogWithMessage:@"Logging in..."];
    [spinnerDialog show];

    dispatch_queue_t queue = dispatch_queue_create("dispatch_queue_t_dialog", NULL);
    dispatch_async(queue, ^{
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            BOOL success = [api createSessionWithUsername:usernameInput andPassword:passwordInput];
            if (success) {
                [self.view makeWaffle:[NSString stringWithFormat:@"Login as %@ successful", usernameInput]
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_CHECKMARK];
                
                // save the username and password in prefs
                NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                [prefs setObject:usernameInput forKey:[StringGrabber grabString:@"key_username"]];
                [prefs setObject:passwordInput forKey:[StringGrabber grabString:@"key_password"]];
                [prefs synchronize];
                
                RPerson *curUser = [api getCurrentUser];
                
                NSString *loginstat = [@"Logged in as: " stringByAppendingString:usernameInput];
                loginstat = [loginstat stringByAppendingString:@", Name: "];
                loginstat = [loginstat stringByAppendingString:firstName];
                loginstat = [loginstat stringByAppendingString:@" "];
                loginstat = [loginstat stringByAppendingString:lastInitial];
                loginstat = [loginstat stringByAppendingString:@". "];
                
                [login_status setText:loginstat];
                userName = usernameInput;
                passWord = passwordInput;
                saver->hasLogin = TRUE;
            } else {
                [self.view makeWaffle:@"Login failed"
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_RED_X];
            }
            [spinnerDialog dismissWithClickedButtonIndex:0 animated:YES];
            
        });
    });


                 
    
    
}

// Default dispatch_async dialog with custom spinner
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

-(void)projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)project {
    expNum = project.intValue;
    [self launchFieldMatchingViewControllerFromBrowse:TRUE];
}

- (void) launchFieldMatchingViewControllerFromBrowse:(bool)fromBrowse {
    // get the fields to field match
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Loading fields..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("loading_project_fields", NULL);
    dispatch_async(queue, ^{
        [dfm getOrder];
        dispatch_async(dispatch_get_main_queue(), ^{
            // set an observer for the field matched array caught from FieldMatching
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(retrieveFieldMatchedArray:) name:kFIELD_MATCHED_ARRAY object:nil];
            
            // launch the field matching dialog
            FieldMatchingViewController *fmvc = [[FieldMatchingViewController alloc] initWithMatchedFields:[dfm getOrderList] andProjectFields:[dfm getRealOrder]];
            fmvc.title = @"Field Matching";
            
            if (fromBrowse) {
                double delayInSeconds = 0.1;
                dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, delayInSeconds * NSEC_PER_SEC);
                dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                    [self.navigationController pushViewController:fmvc animated:YES];
                });
            } else
                [self.navigationController pushViewController:fmvc animated:YES];
            
            if (fromBrowse) [NSThread sleepForTimeInterval:1.0];
            [message dismissWithClickedButtonIndex:nil animated:YES];
            
        });
    });
}

- (void) retrieveFieldMatchedArray:(NSNotification *)obj {
    NSMutableArray *fieldMatch =  (NSMutableArray *)[obj object];
    if (fieldMatch != nil) {
        // user pressed okay button - set the cell's project and fields
        
    }
    // else user canceled
}



@end
