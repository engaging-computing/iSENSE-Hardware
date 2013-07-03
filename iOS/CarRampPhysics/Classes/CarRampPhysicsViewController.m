//
//  CarRampPhysicsViewController.m
//  iOS Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 6/27/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "CarRampPhysicsViewController.h"

@implementation CarRampPhysicsViewController

@synthesize isRecording, motionManager, dataToBeJSONed, expNum, timer, recordDataTimer, elapsedTime, locationManager, dfm, testLength, sessionName,
sampleInterval, geoCoder, city, address, country, dataSaver, managedObjectContext, isenseAPI, longClickRecognizer, backFromSetup;

/*
// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPress:)];
	[start addGestureRecognizer:longPress];
	[longPress release];

	accelerometer = [UIAccelerometer sharedAccelerometer];
	accelerometer.updateInterval = 1.0f/60.0f;
	accelerometer.delegate = self;
	
	// Attempt Login
    isenseAPI = [iSENSE getInstance];
    [isenseAPI toggleUseDev:YES];
    
    // Initializes an Assortment of Variables
    motionManager = [[CMMotionManager alloc] init];
    dfm = [[DataFieldManager alloc] init];
    dataSaver = [[DataSaver alloc] init];
    sampleInterval = DEFAULT_SAMPLE_INTERVAL;
	
	[super viewDidLoad];
	
}



/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [super dealloc];
}

- (void)longPress:(UILongPressGestureRecognizer*)gesture {
    if ( gesture.state == UIGestureRecognizerStateEnded ) {
		//get data
    }
}

- (void)accelerometer:(UIAccelerometer *)accelerometer didAccelerate:(UIAcceleration*)acceleration 
{
	log.text = [NSString stringWithFormat:@"%@%f", @"X: ", acceleration.x];
}

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
	NSLog(@"Button %d", buttonIndex);
	
	switch (buttonIndex) {
		case LOGIN_BUTTON: {
			[self showLoginDialog];
			break;
		}
		case UPLOAD_BUTTON:
			//Upload code goes here
			break;
		case CHANGE_NAME_BUTTON: {
			[self showNameDialog];
			break;
		}
		case RECORD_SETTINGS_BUTTON:
			//Record settings dialog goes here
			break;
		case RECORD_LENGTH_BUTTON: {
			UIAlertView *view = [[UIAlertView alloc] initWithTitle:@"Enter Recording Length" message:@"" delegate:nil cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
			[[view textFieldAtIndex:0] setDelegate:self];
			[[view textFieldAtIndex:0] setKeyboardType:UIKeyboardTypeNumberPad];
			[[view textFieldAtIndex:0] becomeFirstResponder]; 
			[view show];
			break;
		}
		default:
			break;
	}
}

-(IBAction) menu {
	
	sheet = [[UIActionSheet alloc] initWithTitle:@"Select Menu Option"
										delegate:self
							   cancelButtonTitle:@"Cancel"
						  destructiveButtonTitle:nil
							   otherButtonTitles:@"Login", @"Upload", @"Change Name", @"Record Settings", @"Record Length", nil];
	
	// Show the sheet
	[sheet showInView:self.view];
	[sheet release];
	
}
			 
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
	NSUInteger newLength = [textField.text length] + [string length] - range.length;
	return (newLength > 1) ? NO : YES;
}


- (void) hideNameDialog {
	first = [dialog textForTextFieldAtIndex:0];
	last = [dialog textForTextFieldAtIndex:1];
	[dialog hideAnimated:YES];
}

- (void) showNameDialog {
	dialog = [[CODialog alloc] initWithWindow:self.view.window];
	[dialog setTitle:@"Enter First Name and Last Initial"];
	[dialog addTextFieldWithPlaceholder:@"First Name" secure:NO];
	[dialog addTextFieldWithPlaceholder:@"Last Initial" secure:NO];
	UITextField *second = [dialog textFieldAtIndex:1];
	[second setDelegate:self];
	[dialog addButtonWithTitle:@"OK" target:self selector:@selector(hideNameDialog)];
	[dialog showOrUpdateAnimated:YES];
}

- (void) showLoginDialog {
	dialog = [[CODialog alloc] initWithWindow:self.view.window];
	[dialog setTitle:@"Login to iSENSE"];
	[dialog addTextFieldWithPlaceholder:@"Username" secure:NO];
	[dialog addTextFieldWithPlaceholder:@"Password" secure:YES];
	[dialog addButtonWithTitle:@"OK" target:self selector:@selector(okLoginDialog)];
	[dialog addButtonWithTitle:@"Cancel" target:self selector:@selector(cancelLoginDialog)];
	[dialog showOrUpdateAnimated:YES];
}

- (void) okLoginDialog {
	
	//Login code here
	[self cancelLoginDialog];
}

- (void) cancelLoginDialog {
	
	[dialog hideAnimated:YES];
	
}

- (IBAction) onRecordLongClick:(UILongPressGestureRecognizer*)sender {
    if (sender.state == UIGestureRecognizerStateBegan) {
        if (!isRecording) {
            // Get Field Order
            [dfm getFieldOrderOfExperiment:expNum];
            NSLog(@"%@", [dfm order]);
            
            // Record Data
            isRecording = TRUE;
            [self recordData];
        } else {
            // Stop Recording
            isRecording = FALSE;
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
    if (sampleInterval > 0) rate = sampleInterval / 1000;
    
    // Set the accelerometer update interval to reccomended sample interval, and start updates
    motionManager.accelerometerUpdateInterval = rate;
    motionManager.magnetometerUpdateInterval = rate;
    motionManager.gyroUpdateInterval = rate;
    if (motionManager.accelerometerAvailable) [motionManager startAccelerometerUpdates];
    if (motionManager.magnetometerAvailable) [motionManager startMagnetometerUpdates];
    if (motionManager.gyroAvailable) [motionManager startGyroUpdates];
    
    // New JSON array to hold data
    dataToBeJSONed = [[NSMutableArray alloc] init];
    
    // Start the new timer
    recordDataTimer = [[NSTimer scheduledTimerWithTimeInterval:rate target:self selector:@selector(buildRowOfData) userInfo:nil repeats:YES] retain];
}

// Stops the recording and returns the actual data recorded :)
-(void) stopRecording:(CMMotionManager *)finalMotionManager {
    // Stop Timers
    [timer invalidate];
    [timer release];
    [recordDataTimer invalidate];
    [recordDataTimer release];
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
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



@end
