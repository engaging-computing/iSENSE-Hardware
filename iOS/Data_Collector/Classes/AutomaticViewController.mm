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

@synthesize isRecording, motionManager, dataToBeJSONed, expNum, timer, recordDataTimer, elapsedTime, locationManager, dfm, widController, qrResults, sessionTitle, sessionTitleLabel, recommendedSampleInterval, geoCoder, city, address, country, activeField, lastField, keyboardDismissProper;

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
                [self.view makeToast:@"No experiment chosen." duration:TOAST_LENGTH_SHORT position:TOAST_BOTTOM image:TOAST_RED_X];
                return;
            }
            
            // Check for login
            if (![isenseAPI isLoggedIn]) {
                [self.view makeToast:@"Not Logged In" duration:TOAST_LENGTH_SHORT position:TOAST_BOTTOM image:TOAST_RED_X];
                return;
            }
            
            // Check for a session title
            if ([[sessionTitle text] length] == 0) {
                [self.view makeToast:@"Enter a session title first" duration:TOAST_LENGTH_SHORT position:TOAST_BOTTOM image:TOAST_RED_X];
                return;
            }
            
            // Switch to green mode
            startStopButton.image = [UIImage imageNamed:@"green_button.png"];
            mainLogo.image = [UIImage imageNamed:@"logo_green.png"];
            startStopLabel.text = [StringGrabber grabString:@"stop_button_text"];
            [containerForMainButton updateImage:startStopButton];
            
            // Get Field Order
            [dfm getFieldOrderOfExperiment:expNum];
            NSLog(@"%@", [dfm order]);
            
            // Record Data
            [self setIsRecording:TRUE];
            [self recordData];
            
            // Update elapsed time
            elapsedTime = 0;
            [self updateElapsedTime];
            NSLog(@"updated Time");
            timer = [[NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateElapsedTime) userInfo:nil repeats:YES] retain];
            NSLog(@"timer was launched");
            
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
            
            [motionManager stopAccelerometerUpdates]; // TODO - is this all we need to stop updates for?
            [motionManager stopMagnetometerUpdates];
            if (motionManager.gyroAvailable) [motionManager stopGyroUpdates];
            
            [self setIsRecording:FALSE];
            
            // Open up description dialog
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:[StringGrabber grabString:@"description_or_delete"]
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Delete"
                                                    otherButtonTitles:@"Upload", nil];
            
            message.tag = DESCRIPTION_AUTOMATIC;
            message.delegate = self;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeDefault;
            [message show];
            [message release];
            
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

- (bool) uploadData:(NSMutableArray *)results withDescription:(NSString *)description {
    
    if (![isenseAPI isLoggedIn]) {
        [self.view makeToast:@"Not Logged In" duration:TOAST_LENGTH_SHORT position:TOAST_BOTTOM image:TOAST_RED_X];
        return false;
    }
    
    // Create a session on iSENSE/dev.
    NSString *name = @"Session From Mobile";
    if (sessionTitle.text.length != 0) name = sessionTitle.text;
    NSNumber *exp_num = [[NSNumber alloc] initWithInt:expNum];
    NSLog(@"%@", address);
    NSLog(@"%@", city);
    NSLog(@"%@", country);

    NSNumber *session_num = [isenseAPI createSession:name withDescription:description Street:address City:city Country:country toExperiment:exp_num];
    
    // Upload to iSENSE (pass me JSON data)
    NSError *error = nil;
    NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:results options:0 error:&error];
    bool success = [isenseAPI putSessionData:dataJSON forSession:session_num inExperiment:exp_num];

    [exp_num release];
    
    return success;
}

- (BOOL) containsAcceptedNumbers:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:
      [StringGrabber grabString:@"accepted_numbers"]] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    
	if (textField == sampleInterval) {
		if (![self containsAcceptedNumbers:string])
            return NO;
	}
    return YES;
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Loading..."];
    [message show];
    
    UIView *mainView;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT)];
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
        expNumLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 200, 768, 40)];
        expNumLabel.textColor = [UIColor whiteColor];
        expNumLabel.textAlignment = NSTextAlignmentCenter;
        expNumLabel.numberOfLines = 1;
        expNumLabel.backgroundColor = [UIColor clearColor];
        expNumLabel.font = [UIFont fontWithName:@"Arial" size:24];
        
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
        containerForMainButton = [[UILongClickButton alloc] initWithFrame:CGRectMake(180, 350, 400, 400) imageView:startStopButton target:self action:@selector(onStartStopLongClick:)];
        [containerForMainButton addSubview:startStopButton];
        [containerForMainButton addSubview:startStopLabel];
        
        // Add the elapsedTime counter at the bottom
        elapsedTimeView = [[UILabel alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height - 150, self.view.frame.size.width, 50)];
        elapsedTimeView.textAlignment = NSTextAlignmentCenter;
        elapsedTimeView.font = [elapsedTimeView.font fontWithSize:18];
        elapsedTimeView.textColor = [UIColor whiteColor];
        elapsedTimeView.backgroundColor = [UIColor clearColor];
        
        // Session Title TextField
        sessionTitle = [[UITextField alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2 - 25, 262, 200, 35)];
        sessionTitle.background = [UIImage imageNamed:@"underline.png"];
        sessionTitle.textAlignment = NSTextAlignmentCenter;
        sessionTitle.font = [sessionTitle.font fontWithSize:24];
        sessionTitle.textColor = [UIColor whiteColor];
        sessionTitle.backgroundColor = [UIColor clearColor];
        sessionTitle.delegate = self;
        sessionTitle.tag = TAG_AUTOMATIC_SESSION_TITLE;
        
        // Session Title Label
        sessionTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2 - 200, 260, 175, 35)];
        sessionTitleLabel.textAlignment = NSTextAlignmentLeft;
        sessionTitleLabel.font = [sessionTitle.font fontWithSize:24];
        sessionTitleLabel.textColor = [UIColor whiteColor];
        sessionTitleLabel.backgroundColor = [UIColor clearColor];
        sessionTitleLabel.text = @"Session Title:";
        
        // Recommended Sample Rate TextField
        sampleInterval = [[UITextField alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2, 762, 150, 35)];
        sampleInterval.background = [UIImage imageNamed:@"underline.png"];
        sampleInterval.textAlignment = NSTextAlignmentCenter;
        sampleInterval.font = [sampleInterval.font fontWithSize:24];
        sampleInterval.textColor = [UIColor whiteColor];
        sampleInterval.backgroundColor = [UIColor clearColor];
        sampleInterval.delegate = self;
        sampleInterval.tag = TAG_AUTOMATIC_SAMPLE_INTERVAL;
        
        // Session Title Label
        sampleIntervalLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2 - 200, 760, 225, 35)];
        sampleIntervalLabel.textAlignment = NSTextAlignmentLeft;
        sampleIntervalLabel.font = [sampleIntervalLabel.font fontWithSize:24];
        sampleIntervalLabel.textColor = [UIColor whiteColor];
        sampleIntervalLabel.backgroundColor = [UIColor clearColor];
        sampleIntervalLabel.text = @"Sample Interval:";
        
        // Add all the subviews to main view
        [self.view addSubview:expNumLabel];
        [self.view addSubview:loginStatus];
        [self.view addSubview:mainLogo];
        [self.view addSubview:sessionTitle];
        [self.view addSubview:sessionTitleLabel];
        [self.view addSubview:sampleInterval];
        [self.view addSubview:sampleIntervalLabel];
        [self.view addSubview:containerForMainButton];
        [self.view addSubview:elapsedTimeView];
        
        // Add a menu button
        menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];
        self.navigationItem.rightBarButtonItem = menuButton;
        
        // Attempt Login
        isenseAPI = [iSENSE getInstance];
        [isenseAPI toggleUseDev:YES];
        [self updateLoginStatus];
        [self updateExpNumLabel];
        
    } else {
        
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 480 - NAVIGATION_CONTROLLER_HEIGHT)];
        self.view = mainView;
        [mainView release];
        
        // Initialize isRecording to false
        [self setIsRecording:FALSE];
        
        // Add iSENSE LOGO background image at the top
        mainLogo = [[UIImageView alloc] initWithFrame:CGRectMake(10, 5, 300, 70)];
        mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
        
        // Create a label for login status
        loginStatus = [[UILabel alloc] initWithFrame:CGRectMake(0, 80, 320, 20)];
        loginStatus.textAlignment = NSTextAlignmentCenter;
        loginStatus.font = [UIFont fontWithName:@"Arial" size:12];
        loginStatus.numberOfLines = 1;
        loginStatus.backgroundColor = [UIColor clearColor];
        
        // Allocate space and initialize the main button
        startStopButton = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 225, 225)];
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
        containerForMainButton = [[UILongClickButton alloc] initWithFrame:CGRectMake(50, 145, 220, 220) imageView:startStopButton target:self action:@selector(onStartStopLongClick:)];
        [containerForMainButton addSubview:startStopButton];
        [containerForMainButton addSubview:startStopLabel];
        
        // Create a label for experiment number
        expNumLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 95, self.view.frame.size.width, 25)];
        expNumLabel.textColor = [UIColor whiteColor];
        expNumLabel.textAlignment = NSTextAlignmentCenter;
        expNumLabel.numberOfLines = 1;
        expNumLabel.backgroundColor = [UIColor clearColor];
        expNumLabel.font = [UIFont fontWithName:@"Arial" size:12];
        
        // Add the elapsedTime counter at the bottom
        elapsedTimeView = [[UILabel alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height - 50, self.view.frame.size.width, 25)];
        elapsedTimeView.textAlignment = NSTextAlignmentCenter;
        elapsedTimeView.font = [elapsedTimeView.font fontWithSize:12];
        elapsedTimeView.textColor = [UIColor whiteColor];
        elapsedTimeView.backgroundColor = [UIColor clearColor];
        
        // Session Title EditText
        sessionTitle = [[UITextField alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2, 122, 80, 20)];
        sessionTitle.background = [UIImage imageNamed:@"underline.png"];
        sessionTitle.textAlignment = NSTextAlignmentCenter;
        sessionTitle.font = [sessionTitle.font fontWithSize:12];
        sessionTitle.textColor = [UIColor whiteColor];
        sessionTitle.backgroundColor = [UIColor clearColor];
        sessionTitle.delegate = self;
        sessionTitle.tag = TAG_AUTOMATIC_SESSION_TITLE;
        
        // Session Title Label
        sessionTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2 - 80, 120, 75, 20)];
        sessionTitleLabel.textAlignment = NSTextAlignmentLeft;
        sessionTitleLabel.font = [sessionTitle.font fontWithSize:12];
        sessionTitleLabel.textColor = [UIColor whiteColor];
        sessionTitleLabel.backgroundColor = [UIColor clearColor];
        sessionTitleLabel.text = @"Session Title:";
        
        // Recommended Sample Rate TextField
        sampleInterval = [[UITextField alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2 + 15, self.view.frame.size.height - 32, 50, 20)];
        sampleInterval.background = [UIImage imageNamed:@"underline.png"];
        sampleInterval.textAlignment = NSTextAlignmentCenter;
        sampleInterval.font = [sampleInterval.font fontWithSize:12];
        sampleInterval.textColor = [UIColor whiteColor];
        sampleInterval.backgroundColor = [UIColor clearColor];
        sampleInterval.delegate = self;
        sampleInterval.tag = TAG_AUTOMATIC_SAMPLE_INTERVAL;
        
        // Session Title Label
        sampleIntervalLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.frame.size.width / 2 - 80, self.view.frame.size.height - 35, 150, 20)];
        sampleIntervalLabel.textAlignment = NSTextAlignmentLeft;
        sampleIntervalLabel.font = [sampleIntervalLabel.font fontWithSize:12];
        sampleIntervalLabel.textColor = [UIColor whiteColor];
        sampleIntervalLabel.backgroundColor = [UIColor clearColor];
        sampleIntervalLabel.text = @"Sample Interval:";
        
        // Add all the subviews to main view
        [self.view addSubview:loginStatus];
        [self.view addSubview:expNumLabel];
        [self.view addSubview:mainLogo];
        [self.view addSubview:sessionTitle];
        [self.view addSubview:sessionTitleLabel];
        [self.view addSubview:containerForMainButton];
        [self.view addSubview:elapsedTimeView];
        [self.view addSubview:sampleInterval];
        [self.view addSubview:sampleIntervalLabel];
        
        // Add a menu button
        menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];
        self.navigationItem.rightBarButtonItem = menuButton;
        
        // Prepare isenseAPI and set login status
        isenseAPI = [iSENSE getInstance];
        [isenseAPI toggleUseDev:YES];
        [self updateLoginStatus];
        
    }
    
    [self initLocations];
    dfm = [DataFieldManager alloc];
    [self resetAddressFields];
    recommendedSampleInterval = DEFAULT_SAMPLE_INTERVAL;
    [self registerForKeyboardNotifications];
    
    [message dismissWithClickedButtonIndex:nil animated:YES];
}

// Is called every time AutomaticView appears
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // Update ExperimentNumber status
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    [self updateExpNumLabel];
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

// Set your expNumLabel to show you the last experiment chosen.
- (void) updateExpNumLabel {
    if (expNum && expNumLabel) {
        NSString *update = [[NSString alloc] initWithFormat:@"Experiment Number: %d", expNum];
        expNumLabel.text = update;
        [update release];
    }
    if (recommendedSampleInterval) {
        if (recommendedSampleInterval == -1) sampleInterval.text = @"";
        else {
                sampleInterval.text = [NSString stringWithFormat:@"%d", (int)recommendedSampleInterval];
        }
        Experiment *experiment = [isenseAPI getExperiment:[NSNumber numberWithInt:expNum]];
        NSLog(@"srate = %@", experiment.srate);
        if ((NSNull *)experiment.srate != [NSNull null]) {
            sampleInterval.text = [NSString stringWithFormat:@"%d", experiment.srate.intValue];

        }
    }
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}

// Resizes all views during rotation
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        
        if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
            self.view.frame = CGRectMake(0, 0, 1024, 768 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(5, 5, 502, 125 );
            containerForMainButton.frame = CGRectMake(517, 184, 400, 400);
            loginStatus.frame = CGRectMake(5, 135, 502, 40);
            expNumLabel.frame = CGRectMake(5, 175, 502, 40);
            elapsedTimeView.frame = CGRectMake(5, 550, 502, 40);
            sessionTitle.frame = CGRectMake(225, 262, 200, 35);
            sessionTitleLabel.frame = CGRectMake(50, 260, 175, 35);
            sampleInterval.frame = CGRectMake(235, 312, 150, 35);
            sampleIntervalLabel.frame = CGRectMake(50, 310, 225, 35);
            
        } else {
            self.view.frame = CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(20, 5, 728, 150);
            containerForMainButton.frame = CGRectMake(180, 350, 400, 400);
            loginStatus.frame = CGRectMake(0, 160, 768, 40);
            expNumLabel.frame = CGRectMake(0, 200, 768, 40);
            elapsedTimeView.frame = CGRectMake(0, self.view.frame.size.height - 150, self.view.frame.size.width, 50);
            sessionTitle.frame = CGRectMake(self.view.frame.size.width/2 - 25, 262, 200, 35);
            sessionTitleLabel.frame = CGRectMake(self.view.frame.size.width/2 - 200, 260, 175, 35);
            sampleInterval.frame = CGRectMake(self.view.frame.size.width / 2, 762, 150, 35);
            sampleIntervalLabel.frame = CGRectMake(self.view.frame.size.width / 2 - 200, 760, 225, 35);
            
        }
    } else {
        
        if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
            self.view.frame = CGRectMake(0, 0, 480, 320 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(15, 5, 180, 40);
            containerForMainButton.frame = CGRectMake(240, 10, 220, 220);
            loginStatus.frame = CGRectMake(5, 50, 200, 20);
            expNumLabel.frame = CGRectMake(5, 65, 200, 20);
            elapsedTimeView.frame = CGRectMake(5, 220, 200, 20);
            sessionTitle.frame = CGRectMake(100, 122, 80, 20);
            sessionTitleLabel.frame = CGRectMake(25, 120, 75, 20);
            sampleInterval.frame = CGRectMake(115, 142, 50, 20);
            sampleIntervalLabel.frame = CGRectMake(25, 140, 150, 20);
        } else {
            self.view.frame = CGRectMake(0, 0, 320, 480 - NAVIGATION_CONTROLLER_HEIGHT);
            mainLogo.frame = CGRectMake(10, 5, 300, 70);
            containerForMainButton.frame = CGRectMake(50, 145, 220, 220);
            loginStatus.frame = CGRectMake(0, 80, 320, 20);
            expNumLabel.frame = CGRectMake(0, 95, self.view.frame.size.width, 20);
            elapsedTimeView.frame = CGRectMake(0, self.view.frame.size.height - 50, self.view.frame.size.width, 20);
            sessionTitle.frame = CGRectMake(self.view.frame.size.width / 2, 122, 80, 20);
            sessionTitleLabel.frame = CGRectMake(self.view.frame.size.width / 2 - 80, 120, 75, 20);
            sampleInterval.frame = CGRectMake(self.view.frame.size.width / 2 + 15, self.view.frame.size.height - 32, 50, 20);
            sampleIntervalLabel.frame = CGRectMake(self.view.frame.size.width / 2 - 80, self.view.frame.size.height - 35, 150, 20);

        }
    }
}

// Dismisses keyboard for sessionTitle
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return NO;
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 enable rotation
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
    [qrResults release];
    [widController release];
    [locationManager release];
    locationManager = nil;
    [super dealloc];
    
}

- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Logging in..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("manual_login_from_login_function", NULL);
    dispatch_async(queue, ^{
        BOOL success = [isenseAPI login:usernameInput with:passwordInput];
        dispatch_async(dispatch_get_main_queue(), ^{
            if (success) {
                [self.view makeToast:@"Login Successful!"
                            duration:TOAST_LENGTH_SHORT
                            position:TOAST_BOTTOM
                               image:TOAST_CHECKMARK];
                [self updateLoginStatus];
            } else {
                [self.view makeToast:@"Login Failed!"
                            duration:TOAST_LENGTH_SHORT
                            position:TOAST_BOTTOM
                               image:TOAST_RED_X];
            }
            [message dismissWithClickedButtonIndex:nil animated:YES];
        });
    });
    
}

// Record the data and return the NSMutable array to be JSONed
- (void) recordData {
    recommendedSampleInterval = [[NSString stringWithString:[sampleInterval text]] floatValue];
    motionManager = [[CMMotionManager alloc] init];
    
    // Make a new float
    float rate = .5;
    if (recommendedSampleInterval > 0) rate = recommendedSampleInterval / 1000;
    NSLog(@"Rate: %f", rate);
    
    // Set the accelerometer update interval to reccomended sample interval, and start updates
    motionManager.accelerometerUpdateInterval = rate;
    motionManager.magnetometerUpdateInterval = rate;
    motionManager.gyroUpdateInterval = rate;
    [motionManager startAccelerometerUpdates];
    [motionManager startMagnetometerUpdates];
    if (motionManager.gyroAvailable) [motionManager startGyroUpdates];
    
    // New JSON array to hold data
    dataToBeJSONed = [[NSMutableArray alloc] init];
    
    // Start the new timer
    recordDataTimer = [[NSTimer scheduledTimerWithTimeInterval:rate target:self selector:@selector(buildRowOfData) userInfo:nil repeats:YES] retain];
    
    NSLog(@"End Record Data");

}

// Fill dataToBeJSONed with a row of data
- (void) buildRowOfData {
    Fields *fieldsRow = [[Fields alloc] autorelease];
    
    // Fill a new row of data starting with time
    double time = [[NSDate date] timeIntervalSince1970];
    fieldsRow.time_millis = [[[NSNumber alloc] initWithDouble:time * 1000] autorelease];
    
    // acceleration in meters per second squared
    fieldsRow.accel_x = [[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].x * 9.80665] autorelease];
    fieldsRow.accel_y = [[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].y * 9.80665] autorelease];
    fieldsRow.accel_z = [[[NSNumber alloc] initWithDouble:[motionManager.accelerometerData acceleration].z * 9.80665] autorelease];
    fieldsRow.accel_total = [[[NSNumber alloc] initWithDouble:
                              sqrt(pow(fieldsRow.accel_x.doubleValue, 2)
                                   + pow(fieldsRow.accel_y.doubleValue, 2)
                                   + pow(fieldsRow.accel_z.doubleValue, 2))] autorelease];
    
    // latitude and longitude coordinates
    CLLocationCoordinate2D lc2d = [[locationManager location] coordinate];
    double latitude  = lc2d.latitude;
    double longitude = lc2d.longitude;
    fieldsRow.latitude = [[[NSNumber alloc] initWithDouble:latitude] autorelease];
    fieldsRow.longitude = [[[NSNumber alloc] initWithDouble:longitude] autorelease];
    
    // magnetic field in microTesla
    fieldsRow.mag_x = [[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].x] autorelease];
    fieldsRow.mag_y = [[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].y] autorelease];
    fieldsRow.mag_z = [[[NSNumber alloc] initWithDouble:[motionManager.magnetometerData magneticField].z] autorelease];
    fieldsRow.mag_total = [[[NSNumber alloc] initWithDouble:
                              sqrt(pow(fieldsRow.mag_x.doubleValue, 2)
                                   + pow(fieldsRow.mag_y.doubleValue, 2)
                                   + pow(fieldsRow.mag_z.doubleValue, 2))] autorelease];
    
    // rotation rate in radians per second
    if (motionManager.gyroAvailable) {
        fieldsRow.gyro_x = [[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].x] autorelease];
        fieldsRow.gyro_y = [[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].y] autorelease];
        fieldsRow.gyro_z = [[[NSNumber alloc] initWithDouble:[motionManager.gyroData rotationRate].z] autorelease];
    }
    
    // Update parent JSON object
    [dfm orderDataFromFields:fieldsRow];
    
    if (dfm.data != nil || dataToBeJSONed != nil)
        [dataToBeJSONed addObject:dfm.data];
    else {
        NSLog(@"something is wrong");
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
-(NSMutableArray *) stopRecording:(CMMotionManager *)finalMotionManager {
    [finalMotionManager stopAccelerometerUpdates];
    return dataToBeJSONed;
}

// TODO - be rid of these 2 useless functions...
- (void) experiment {
    [self.view makeToast:@"Experiment!"
                duration:TOAST_LENGTH_SHORT
                position:TOAST_BOTTOM];
}
- (void) upload {
    [self.view makeToast:@"Upload!"
                duration:TOAST_LENGTH_SHORT
                position:TOAST_BOTTOM];
    
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
        }
        
    } else if (actionSheet.tag == DESCRIPTION_AUTOMATIC) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            UIAlertView *message = [self getDispatchDialogWithMessage:@"Uploading data set..."];
            [message show];
            
            dispatch_queue_t queue = dispatch_queue_create("automatic_upload_data", NULL);
            dispatch_async(queue, ^{
                
                NSString *description = [[actionSheet textFieldAtIndex:0] text];
                if ([description length] == 0) {
                    description = @"Session data gathered and uploaded from mobile phone using iSENSE DataCollector application.";
                }
                
                bool success = [self uploadData:dataToBeJSONed withDescription:description];
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (success) {
                        [self.view makeToast:@"Upload success"
                                    duration:TOAST_LENGTH_SHORT
                                    position:TOAST_BOTTOM
                                       image:TOAST_CHECKMARK];
                    } else {
                        [self.view makeToast:@"Upload failed"
                                    duration:TOAST_LENGTH_SHORT
                                    position:TOAST_BOTTOM
                                       image:TOAST_RED_X];
                    }
                
                    [message dismissWithClickedButtonIndex:nil animated:YES];
                });
            });
            
        } else {
            
            [self.view makeToast:@"Data set deleted." duration:TOAST_LENGTH_SHORT position:TOAST_BOTTOM image:TOAST_CHECKMARK];
       
        }
    }
}

- (void)updateElapsedTime {
    if (elapsedTime == 1) elapsedTimeView.text = [NSString stringWithFormat:@"Elapsed Time: %d second", elapsedTime];
    else elapsedTimeView.text = [NSString stringWithFormat:@"Elapsed Time: %d seconds", elapsedTime];
    elapsedTime++;
}

- (void) zxingController:(ZXingWidgetController*)controller didScanResult:(NSString *)result {
    [widController.view removeFromSuperview];
    
    qrResults = [result retain];
    NSArray *split = [qrResults componentsSeparatedByString:@"="];
    if ([split count] != 2) {
        [self.view makeToast:@"Invalid QR code scanned"
                    duration:TOAST_LENGTH_LONG
                    position:TOAST_BOTTOM
                       image:TOAST_RED_X];
    } else {
        expNum = [[split objectAtIndex:1] intValue];
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

- (void)resetAddressFields {
    city = [[NSString alloc] initWithString:@"N/a"];
    country = [[NSString alloc] initWithString:@"N/a"];
    address = [[NSString alloc] initWithString:@"N/a"];
}

- (void) zxingControllerDidCancel:(ZXingWidgetController*)controller {
    [widController.view removeFromSuperview];
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
    
    if (activeField.tag == TAG_AUTOMATIC_SESSION_TITLE) {
        
        // adjust UI depending on field being editted
        UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
        if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
            if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                
            } else {
               
            }
        } else {
            if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                
            } else {
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y - KEY_OFFSET_SESSION_LAND_IPHONE,
                                             self.view.frame.size.width, self.view.frame.size.height);
            }
        }
        
    } else if (activeField.tag == TAG_AUTOMATIC_SAMPLE_INTERVAL) {
        // adjust UI depending on field being editted
        UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
        if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
            if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y - KEY_OFFSET_SAMPLE_PORT_IPAD,
                                             self.view.frame.size.width, self.view.frame.size.height);
            } else {
                
            }
        } else {
            if(orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown) {
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y - KEY_OFFSET_SAMPLE_PORT_IPHONE,
                                             self.view.frame.size.width, self.view.frame.size.height);
            } else {
                self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y - KEY_OFFSET_SAMPLE_LAND_IPHONE,
                                             self.view.frame.size.width, self.view.frame.size.height);
            }
        }
    }
    
    keyboardDismissProper = false;
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification {
    
    @try {
        if (activeField != nil && (activeField.tag == TAG_AUTOMATIC_SESSION_TITLE || activeField.tag == TAG_AUTOMATIC_SAMPLE_INTERVAL)) {
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
}

- (IBAction) textFieldFinished:(id)sender {}

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
