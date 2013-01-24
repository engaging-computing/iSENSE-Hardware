//
//  AutomaticViewController.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/10/13.
//
//

#import "AutomaticViewController.h"

#define MENU_UPLOAD 0
#define MENU_EXPERIMENT 1
#define MENU_LOGIN 2

@implementation AutomaticViewController

@synthesize isRecording;
@synthesize motionManager;
@synthesize dataToBeJSONed;


// Long Click Responder
- (IBAction)onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer {
    
    // Handle long press.
    if (longClickRecognizer.state == UIGestureRecognizerStateBegan) {
        
        // Make button unclickable until it gets released
        longClickRecognizer.enabled = NO;
        
        // Start Recording
        if (![self isRecording]) {
            
            // Switch to green mode
            startStopButton.image = [UIImage imageNamed:@"green_button.png"];
            mainLogo.image = [UIImage imageNamed:@"logo_green.png"];
            startStopLabel.text = [StringGrabber getString:@"stop_button_text"];
            [containerForMainButton updateImage:startStopButton];
            
            // Record Data
            [self setIsRecording:TRUE];
            motionManager = [[self recordData] retain];
            
            // Stop Recording
        } else {
            // Back to red mode
            startStopButton.image = [UIImage imageNamed:@"red_button.png"];
            mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
            startStopLabel.text = [StringGrabber getString:@"start_button_text"];
            [containerForMainButton updateImage:startStopButton];
            
            NSMutableArray *results = [self stopRecording:motionManager];
            NSLog(@"Received %d results.", [results count]);
            
            [self setIsRecording:FALSE];
            
            // Create a session on iSENSE/dev.
            /*
            NSString *name = [[[NSString alloc] initWithString:@"Automatic Test"] autorelease];
            NSString *description = [[[NSString alloc] initWithString:@"Automated Session Test from API"] autorelease];
            NSString *street = [[[NSString alloc] initWithString:@"1 University Ave"] autorelease];
            NSString *city = [[[NSString alloc] initWithString:@"Lowell, MA"] autorelease];
            NSString *country = [[[NSString alloc] initWithString:@"United States"] autorelease];
            NSNumber *exp_num = [[[NSNumber alloc] initWithInt:518] autorelease];
            
            NSNumber *session_num = [isenseAPI createSession:name withDescription:description Street:street City:city Country:country toExperiment:exp_num];
            
            // Upload to iSENSE (pass me JSON data)
            NSError *error = nil;
            NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:results options:0 error:&error];
            [isenseAPI putSessionData:dataJSON forSession:session_num inExperiment:exp_num];
            */
            
            [self getExperiments];
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

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	
	// Bound, allocate, and customize the main view
	self.view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 480)];
    self.view.backgroundColor = [UIColor blackColor];
	
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
	startStopLabel.text = [StringGrabber getString:@"start_button_text"];
	startStopLabel.textAlignment = NSTextAlignmentCenter;
	startStopLabel.textColor = [UIColor whiteColor];
	startStopLabel.font = [startStopLabel.font fontWithSize:25];
	startStopLabel.numberOfLines = 2;
   	startStopLabel.backgroundColor =[UIColor clearColor];
    
	// Add main button subviews to the UIPicButton called containerForMainButton (so the whole thing is clickable)
    containerForMainButton = [[UILongClickButton alloc] initWithFrame:CGRectMake(35, 120, 250, 250) imageView:startStopButton target:self action:@selector(onStartStopLongClick:)];
	[containerForMainButton addSubview:startStopButton];
	[containerForMainButton addSubview:startStopLabel];
	
	// Add all the subviews to main view
    [self.view addSubview:loginStatus];
	[self.view addSubview:mainLogo];
	[self.view addSubview:containerForMainButton];
    
    // Add a menu button
    menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];
    self.navigationItem.rightBarButtonItem = menuButton;
    
	
	// Prepare isenseAPI and set login status
	isenseAPI = [iSENSE getInstance];
	[isenseAPI toggleUseDev:YES];
    [self updateLoginStatus];
    
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
        loginStatus.text = [StringGrabber concatenateHardcodedString:@"logged_in_as" with:@"NOT LOGGED IN"]; //[StringGrabber getString:@"login_status_not_logged_in"];
       	loginStatus.textColor = [UIColor yellowColor];
    }
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
	NSLog(@"Rotate Initiated!");
	if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
		self.view.frame = CGRectMake(0, 0, 480, 320);
		mainLogo.frame = CGRectMake(15, 5, 180, 40);
		containerForMainButton.frame = CGRectMake(220, 5, 250, 250);
        loginStatus.frame = CGRectMake(5, 50, 200, 20);
	} else {
		self.view.frame = CGRectMake(0, 0, 320, 480);
		mainLogo.frame = CGRectMake(10, 5, 300, 70);
        loginStatus.frame = CGRectMake(0, 85, 320, 20);
		containerForMainButton.frame = CGRectMake(35, 120, 250, 250);
	}
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
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
- (CMMotionManager *) recordData {
    CMMotionManager *newMotionManager = [[CMMotionManager alloc] init];
    NSOperationQueue *queue = [[[NSOperationQueue alloc] init] autorelease];
    dataToBeJSONed = [[NSMutableArray alloc] init];
    
    CMAccelerometerHandler accelerationHandler = ^(CMAccelerometerData *data, NSError *error) {
        NSMutableArray *temp = [[[NSMutableArray alloc] init] autorelease];
        [temp addObject:[[[NSNumber alloc] initWithDouble:[data acceleration].x * 9.80665] autorelease]];
        [temp addObject:[[[NSNumber alloc] initWithDouble:[data acceleration].y * 9.80665] autorelease]];
        [temp addObject:[[[NSNumber alloc] initWithDouble:[data acceleration].z * 9.80665] autorelease]];
        
        [dataToBeJSONed addObject:temp];
    };
    
    [newMotionManager startAccelerometerUpdatesToQueue:queue withHandler:accelerationHandler];
    
    return [newMotionManager autorelease];
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

- (void) getExperiments {
    NSMutableArray *results = [isenseAPI getExperiments:[NSNumber numberWithUnsignedInt:1] withLimit:[NSNumber numberWithUnsignedInt:10] withQuery:@"" andSort:@"recent"];
    if ([results count] == 0) NSLog(@"No results found");
    for (int i = 0; i < [results count]; i++) {
        NSLog(@"Experiment %d: %@", i + 1, ((Experiment *)results[i]).name);
        NSLog(@"Session Count: %@", ((Experiment *)results[i]).session_count);
    }

    Experiment *myExperiment = [isenseAPI getExperiment:[NSNumber numberWithUnsignedInt:514]];
    NSLog(@"My experiment name is %@.", myExperiment.name);

    NSMutableArray *resultsFields = [isenseAPI getExperimentFields:[NSNumber numberWithUnsignedInt:514]];
    if ([resultsFields count] == 0) NSLog(@"No results found");
    for (int i = 0; i < [resultsFields count]; i++) {
        NSLog(@"Experiment Field %d: %@", i + 1, ((ExperimentField*)resultsFields[i]).field_name);
    }

}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
	BOOL showMsg = YES;
	UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Menu item clicked:"
													  message:@"Nil_message"
													 delegate:self
											cancelButtonTitle:@"Cancel"
											otherButtonTitles:@"Okay", nil];
	switch (buttonIndex) {
		case MENU_UPLOAD:
			message.message = @"Upload"; showMsg = NO; [self upload];
			break;
		case MENU_EXPERIMENT:
			message.message = @"Experiment"; showMsg = NO; [self experiment];
			break;
		case MENU_LOGIN:
			message.message = nil;
			[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
            message.title = @"Login";
            message.tag = MENU_LOGIN;
            break;
		default:
			showMsg = NO;
			break;
	}
	
	if (showMsg)
		[message show];
	
	[message release];
}

- (void)alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == MENU_LOGIN) {
        if (buttonIndex != 0) {
            NSString *usernameInput = [[actionSheet textFieldAtIndex:0] text];
            NSString *passwordInput = [[actionSheet textFieldAtIndex:1] text];
            [self login:usernameInput withPassword:passwordInput];
        }
    } else {
        
    }
}

@end
