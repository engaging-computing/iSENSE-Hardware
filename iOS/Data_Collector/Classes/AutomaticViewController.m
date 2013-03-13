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

@synthesize isRecording, motionManager, dataToBeJSONed, expNum, timer, recordDataTimer, elapsedTime, locationManager;

// Long Click Responder
- (IBAction)onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer {
    
    // Handle long press.
    if (longClickRecognizer.state == UIGestureRecognizerStateBegan) {
        
        // Make button unclickable until it gets released
        longClickRecognizer.enabled = NO;
        
        // Start Recording
        if (![self isRecording]) {
            
            // Check for a chosen experiment
            if (!expNum) {
                [self.view makeToast:@"No experiment chosen." duration:1 position:@"bottom" image:@"red_x"];
                return;
            }
            
            // Check for login
            if (![isenseAPI isLoggedIn]) {
                [self.view makeToast:@"Not Logged In" duration:1 position:@"bottom" image:@"red_x"];
                return;
            }
            
            // Switch to green mode
            startStopButton.image = [UIImage imageNamed:@"green_button.png"];
            mainLogo.image = [UIImage imageNamed:@"logo_green.png"];
            startStopLabel.text = [StringGrabber grabString:@"stop_button_text"];
            [containerForMainButton updateImage:startStopButton];
            
            // Record Data
            [self setIsRecording:TRUE];
            [self recordData];
            
            // Update elapsed time
            elapsedTime = 0;
            [self updateElapsedTime];
            timer = [[NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateElapsedTime) userInfo:nil repeats:YES] retain];
            
        // Stop Recording
        } else {
            // Stop Timers
            [timer invalidate];
            [timer release];
            [recordDataTimer invalidate];
            [recordDataTimer release];
            
            // Back to red mode
            startStopButton.image = [UIImage imageNamed:@"red_button.png"];
            mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
            startStopLabel.text = [StringGrabber grabString:@"start_button_text"];
            [containerForMainButton updateImage:startStopButton];
            
            NSMutableArray *results = [self stopRecording:motionManager];
            NSLog(@"Received %d results.", [results count]);
            [self uploadData:results];
            
            [self setIsRecording:FALSE];
            
        }
        
        // Make the beep sound
        NSString *path = [NSString stringWithFormat:@"%@%@",
                          [[NSBundle mainBundle] resourcePath],
                          @"/button-37.wav"];
        SystemSoundID soundID;
        NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
        AudioServicesCreateSystemSoundID((CFURLRef)filePath, &soundID);
        AudioServicesPlaySystemSound(soundID);
        
    }
    
}

- (void) uploadData:(NSMutableArray *)results {
    
    if (![isenseAPI isLoggedIn]) {
        [self.view makeToast:@"Not Logged In" duration:1 position:@"bottom" image:@"red_x"];
        return;
    }
    
    // Create a session on iSENSE/dev.
    NSString *name = [[[NSString alloc] initWithString:@"Automatic Test"] autorelease];
    NSString *description = [[[NSString alloc] initWithString:@"Automated Session Test from API"] autorelease];
    NSString *street = [[[NSString alloc] initWithString:@"1 University Ave"] autorelease];
    NSString *city = [[[NSString alloc] initWithString:@"Lowell, MA"] autorelease];
    NSString *country = [[[NSString alloc] initWithString:@"United States"] autorelease];
    NSNumber *exp_num = [[[NSNumber alloc] initWithInt:expNum] autorelease];
    
    NSNumber *session_num = [isenseAPI createSession:name withDescription:description Street:street City:city Country:country toExperiment:exp_num];
    
    // Upload to iSENSE (pass me JSON data)
    NSError *error = nil;
    NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:results options:0 error:&error];
    [isenseAPI putSessionData:dataJSON forSession:session_num inExperiment:exp_num];
    
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    UIView *mainView;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT)];
        mainView.backgroundColor = [UIColor blackColor];
        self.view = mainView;
        [mainView release];
        
        // Initialize isRecording to false
        [self setIsRecording:FALSE];
        
        // Add iSENSE LOGO background image at the top
        mainLogo = [[UIImageView alloc] initWithFrame:CGRectMake(20, 5, 728, 150)];
        mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
        
        // Create a label for login status
        loginStatus = [[UILabel alloc] initWithFrame:CGRectMake(0, 160, 768, 40)];
        loginStatus.textAlignment = NSTextAlignmentCenter;
        loginStatus.font = [UIFont fontWithName:@"Arial" size:32];
        loginStatus.numberOfLines = 1;
        loginStatus.backgroundColor = [UIColor clearColor];
        
        // Create a label for experiment number
        expNumStatus = [[UILabel alloc] initWithFrame:CGRectMake(0, 200, 768, 40)];
        expNumStatus.textColor = [UIColor whiteColor];
        expNumStatus.textAlignment = NSTextAlignmentCenter;
        expNumStatus.numberOfLines = 1;
        expNumStatus.backgroundColor = [UIColor clearColor];
        expNumStatus.font = [UIFont fontWithName:@"Arial" size:24];
        
        // Allocate space and initialize the main button
        startStopButton = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 400, 400)];
        startStopButton.image = [UIImage imageNamed:@"red_button.png"];
        
        // Allocate space and add the label to the main button
        startStopLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, startStopButton.bounds.size.width, startStopButton.bounds.size.height)];
        startStopLabel.text = [StringGrabber grabString:@"start_button_text"];
        startStopLabel.textAlignment = NSTextAlignmentCenter;
        startStopLabel.textColor = [UIColor whiteColor];
        startStopLabel.font = [startStopLabel.font fontWithSize:25];
        startStopLabel.numberOfLines = 2;
        startStopLabel.backgroundColor = [UIColor clearColor];
        
        // Add main button subviews to the UIPicButton called containerForMainButton (so the whole thing is clickable)
        containerForMainButton = [[UILongClickButton alloc] initWithFrame:CGRectMake(174, 300, 400, 400) imageView:startStopButton target:self action:@selector(onStartStopLongClick:)];
        [containerForMainButton addSubview:startStopButton];
        [containerForMainButton addSubview:startStopLabel];
        
        // Add the elapsedTime counter at the bottom
        elapsedTimeView = [[UILabel alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height - 150, self.view.frame.size.width, 50)];
        elapsedTimeView.textAlignment = NSTextAlignmentCenter;
        elapsedTimeView.font = [elapsedTimeView.font fontWithSize:18];
        elapsedTimeView.textColor = [UIColor whiteColor];
        elapsedTimeView.backgroundColor = [UIColor clearColor];
        
        // Add all the subviews to main view
        [self.view addSubview:expNumStatus];
        [self.view addSubview:loginStatus];
        [self.view addSubview:mainLogo];
        [self.view addSubview:containerForMainButton];
        [self.view addSubview:elapsedTimeView];
        
        // Add a menu button
        menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];
        self.navigationItem.rightBarButtonItem = menuButton;
        
        // Attempt Login
        isenseAPI = [iSENSE getInstance];
        [isenseAPI toggleUseDev:YES];
        [self updateLoginStatus];
        [self updateExpNumStatus];
        
    } else {
        
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 480 - NAVIGATION_CONTROLLER_HEIGHT)];
        mainView.backgroundColor = [UIColor blackColor];
        self.view = mainView;
        [mainView release];
        
        // Initialize isRecording to false
        [self setIsRecording:FALSE];
        
        // Add iSENSE LOGO background image at the top
        mainLogo = [[UIImageView alloc] initWithFrame:CGRectMake(10, 5, 300, 70)];
        mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
        
        // Create a label for login status
        loginStatus = [[UILabel alloc] initWithFrame:CGRectMake(0, 85, 320, 20)];
        loginStatus.textAlignment = NSTextAlignmentCenter;
        loginStatus.font = [UIFont fontWithName:@"Arial" size:12];
        loginStatus.numberOfLines = 1;
        loginStatus.backgroundColor = [UIColor clearColor];
        
        // Allocate space and initialize the main button
        startStopButton = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 250, 250)];
        startStopButton.image = [UIImage imageNamed:@"red_button.png"];
        
        // Allocate space and add the label to the main button
        startStopLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, startStopButton.bounds.size.width, startStopButton.bounds.size.height)];
        startStopLabel.text = [StringGrabber grabString:@"start_button_text"];
        startStopLabel.textAlignment = NSTextAlignmentCenter;
        startStopLabel.textColor = [UIColor whiteColor];
        startStopLabel.font = [startStopLabel.font fontWithSize:25];
        startStopLabel.numberOfLines = 2;
        startStopLabel.backgroundColor =[UIColor clearColor];
        
        // Add main button subviews to the UIPicButton called containerForMainButton (so the whole thing is clickable)
        containerForMainButton = [[UILongClickButton alloc] initWithFrame:CGRectMake(35, 130, 250, 250) imageView:startStopButton target:self action:@selector(onStartStopLongClick:)];
        [containerForMainButton addSubview:startStopButton];
        [containerForMainButton addSubview:startStopLabel];
        
        // Create a label for experiment number
        expNumStatus = [[UILabel alloc] initWithFrame:CGRectMake(0, 100, self.view.frame.size.width, 25)];
        expNumStatus.textColor = [UIColor whiteColor];
        expNumStatus.textAlignment = NSTextAlignmentCenter;
        expNumStatus.numberOfLines = 1;
        expNumStatus.backgroundColor = [UIColor clearColor];
        expNumStatus.font = [UIFont fontWithName:@"Arial" size:12];
        
        // Add the elapsedTime counter at the bottom
        elapsedTimeView = [[UILabel alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height - 50, self.view.frame.size.width, 25)];
        elapsedTimeView.textAlignment = NSTextAlignmentCenter;
        elapsedTimeView.font = [elapsedTimeView.font fontWithSize:12];
        elapsedTimeView.textColor = [UIColor whiteColor];
        elapsedTimeView.backgroundColor = [UIColor clearColor];
        
        // Add all the subviews to main view
        [self.view addSubview:loginStatus];
        [self.view addSubview:expNumStatus];
        [self.view addSubview:mainLogo];
        [self.view addSubview:containerForMainButton];
        [self.view addSubview:elapsedTimeView];
        
        // Add a menu button
        menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];
        self.navigationItem.rightBarButtonItem = menuButton;
        
        // Prepare isenseAPI and set login status
        isenseAPI = [iSENSE getInstance];
        [isenseAPI toggleUseDev:YES];
        [self updateLoginStatus];
        
    }
    
    [self initLocations];
}

// Is called every time AutomaticView appears
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // UpdateExperimentNumber status
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    [self updateExpNumStatus];
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

// Set your login status to your username to not logged in as necessary
- (void) updateLoginStatus {
    if ([isenseAPI isLoggedIn]) {
        loginStatus.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:[isenseAPI getLoggedInUsername]];
        loginStatus.textColor = [UIColor greenColor];
    } else {
        loginStatus.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"NOT LOGGED IN"];
        loginStatus.textColor = [UIColor yellowColor];
    }
}

// Set your expNumStatus to show you the last experiment chosen.
- (void) updateExpNumStatus {
    if (expNum && expNumStatus) {
        NSString *update = [[NSString alloc] initWithFormat:@"Experiment Number: %d", expNum];
        expNumStatus.text = update;
        [update release];
    }
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        
        if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
            self.view.frame = CGRectMake(0, 0, 1024, 768 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(5, 5, 502, 125 );
            containerForMainButton.frame = CGRectMake(517, 184, 400, 400);
            loginStatus.frame = CGRectMake(5, 135, 502, 40);
            expNumStatus.frame = CGRectMake(5, 175, 502, 40);
            elapsedTimeView.frame = CGRectMake(5, 550, 502, 40);
        } else {
            self.view.frame = CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(20, 5, 728, 150);
            containerForMainButton.frame = CGRectMake(174, 300, 400, 400);
            loginStatus.frame = CGRectMake(0, 160, 768, 40);
            expNumStatus.frame = CGRectMake(0, 200, 768, 40);
            elapsedTimeView.frame = CGRectMake(0, self.view.frame.size.height - 150, self.view.frame.size.width, 50);
        }
    } else {
        
        if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
            self.view.frame = CGRectMake(0, 0, 480, 320 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(15, 5, 180, 40);
            containerForMainButton.frame = CGRectMake(220, 5, 250, 250);
            loginStatus.frame = CGRectMake(5, 50, 200, 20);
            expNumStatus.frame = CGRectMake(5, 65, 200, 20);
            elapsedTimeView.frame = CGRectMake(5, 220, 200, 20);
        } else {
            self.view.frame = CGRectMake(0, 0, 320, 480 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(10, 5, 300, 70);
            containerForMainButton.frame = CGRectMake(35, 130, 250, 250);
            loginStatus.frame = CGRectMake(0, 85, 320, 20);
            expNumStatus.frame = CGRectMake(0, 100, self.view.frame.size.width, 20);
            elapsedTimeView.frame = CGRectMake(0, self.view.frame.size.height - 50, self.view.frame.size.width, 25);
        }
    }
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
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


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    //[self addChildViewController:(UIViewController*) self.yourChildController];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [mainLogo release];
    [menuButton release];
    [containerForMainButton release];
    [loginStatus release];
    [startStopLabel release];
    [startStopButton release];
    [super dealloc];
    
}

- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    if ([isenseAPI login:usernameInput with:passwordInput]) {
        [self.view makeToast:@"Login Successful!"
                    duration:2.0
                    position:@"bottom"
                       image:@"check"];
        [self updateLoginStatus];
    } else {
        [self.view makeToast:@"Login Failed!"
                    duration:2.0
                    position:@"bottom"
                       image:@"red_x"];
    }
}

// Record the data and return the NSMutable array to be JSONed
- (void) recordData {
    motionManager = [[CMMotionManager alloc] init];
    
    // Set the accelerometer update interval to reccomended sample interval, and start updates
    motionManager.accelerometerUpdateInterval = .5;
    motionManager.magnetometerUpdateInterval = .5;
    motionManager.gyroUpdateInterval = .5;
    [motionManager startAccelerometerUpdates];
    [motionManager startMagnetometerUpdates];
    if (motionManager.gyroAvailable) [motionManager startGyroUpdates];
    
    // New JSON array to hold data
    dataToBeJSONed = [[NSMutableArray alloc] init];
    
    // Start the new timer
    recordDataTimer = [[NSTimer scheduledTimerWithTimeInterval:.5 target:self selector:@selector(buildRowOfData) userInfo:nil repeats:YES] retain];

}

// Fill dataToBeJSONed with a row of data
- (void) buildRowOfData {
    NSMutableArray *temp = [[[NSMutableArray alloc] init] autorelease];
    
    // Fill a new row of data
    double time = [[NSDate date] timeIntervalSince1970];
    
    // acceleration in meters per second squared
    [temp addObject:[[[NSNumber alloc] initWithDouble:time * 1000] autorelease]];
    [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].x * 9.80665] autorelease]];
    [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].y * 9.80665] autorelease]];
    [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].z * 9.80665] autorelease]];
    
    // latitude and longitude coordinates
    CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
    double latitude  = lc2d.latitude;
    double longitude = lc2d.longitude;
    [temp addObject:[[[NSNumber alloc] initWithDouble:latitude] autorelease]];
    [temp addObject:[[[NSNumber alloc] initWithDouble:longitude] autorelease]];
    
    // magnetic field in microTesla
    [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].x] autorelease]];
    [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].y] autorelease]];
    [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].z] autorelease]];
    
    // rotation rate in radians per second
    if (motionManager.gyroAvailable) {
        [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].x] autorelease]];
        [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].y] autorelease]];
        [temp addObject:[[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].z] autorelease]];
    }
    
    // Update parent JSON object
    [dataToBeJSONed addObject:temp];

}

// This inits locations
- (void) initLocations {
    if (!locationManager) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        locationManager.distanceFilter = kCLDistanceFilterNone;
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        [locationManager startUpdatingLocation];
    }
}

// Stops the recording and returns the actual data recorded :)
-(NSMutableArray *) stopRecording:(CMMotionManager *)finalMotionManager {
    [finalMotionManager stopAccelerometerUpdates];
    return dataToBeJSONed;
}


- (void) experiment {
    [self.view makeToast:@"Experiment!"
                duration:2.0
                position:@"bottom"];
}

- (void) upload {
    [self.view makeToast:@"Upload!"
                duration:2.0
                position:@"bottom"];
    
}

// Fetch the experiments from iSENSE
- (void) getExperiments {
    NSMutableArray *results = [isenseAPI getExperiments:[NSNumber numberWithUnsignedInt:1] withLimit:[NSNumber numberWithUnsignedInt:10] withQuery:@"" andSort:@"recent"];
    if ([results count] == 0) NSLog(@"No experiments found.");
    
    NSMutableArray *resultsFields = [isenseAPI getExperimentFields:[NSNumber numberWithUnsignedInt:514]];
    if ([resultsFields count] == 0) NSLog(@"No experiment fields found.");
    
}

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
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

- (void)alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
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
            [message show];
            [message release];
            
        } else if (buttonIndex == OPTION_BROWSE_EXPERIMENTS) {
            ExperimentBrowseViewController *browseView = [[ExperimentBrowseViewController alloc] init];
            browseView.title = @"Browse for Experiments";
            browseView.chosenExperiment = &expNum;
            [self.navigationController pushViewController:browseView animated:YES];
            [browseView release];
            
        } else if (buttonIndex == OPTION_SCAN_QR_CODE) {
            
        }
        
    } else if (actionSheet.tag == MENU_UPLOAD) {
        
    } else if (actionSheet.tag == EXPERIMENT_MANUAL_ENTRY) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            expNum = [[[actionSheet textFieldAtIndex:0] text] intValue];
            [self updateExpNumStatus];
        }
        
    } else if (actionSheet.tag == EXPERIMENT_BROWSE_EXPERIMENTS) {
        
    } else if (actionSheet.tag == EXPERIMENT_SCAN_QR_CODE) {
        
    }
}

- (void)updateElapsedTime {
    if (elapsedTime == 1) elapsedTimeView.text = [NSString stringWithFormat:@"Elapsed Time: %d second", elapsedTime];
    else elapsedTimeView.text = [NSString stringWithFormat:@"Elapsed Time: %d seconds", elapsedTime];
    elapsedTime++;
}

@end
