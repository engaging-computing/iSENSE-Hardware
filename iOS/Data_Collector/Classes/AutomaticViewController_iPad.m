//
//  AutomaticViewController_iPad.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 11/1/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "AutomaticViewController_iPad.h"

#define MENU_UPLOAD 0
#define MENU_EXPERIMENT 1
#define MENU_LOGIN 2

@implementation AutomaticViewController_iPad

@synthesize isRecording;

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
            
            [self setIsRecording:TRUE];
            
        // Stop Recording
        } else {
            // Back to red mode
            startStopButton.image = [UIImage imageNamed:@"red_button.png"];
            mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
            startStopLabel.text = [StringGrabber getString:@"start_button_text"];
            [containerForMainButton updateImage:startStopButton];
            
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

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	
	// Bound, allocate, and customize the main view
	self.view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024)];
    self.view.backgroundColor = [UIColor blackColor];
	
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
	
	// Allocate space and initialize the main button
    startStopButton = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 400, 400)];
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
    containerForMainButton = [[UILongClickButton alloc] initWithFrame:CGRectMake(174, 300, 400, 400) imageView:startStopButton target:self action:@selector(onStartStopLongClick:)];
	[containerForMainButton addSubview:startStopButton];
	[containerForMainButton addSubview:startStopLabel];
	
	// Add all the subviews to main view
    [self.view addSubview:loginStatus];
	[self.view addSubview:mainLogo];
	[self.view addSubview:containerForMainButton];
    
    // Add a menu button
    menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];
    self.navigationItem.rightBarButtonItem = menuButton;
	
	// Attempt Login
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
        loginStatus.text = [StringGrabber concatenateWithHardcodedString:@"logged_in_as":[isenseAPI getLoggedInUsername]];
    	loginStatus.textColor = [UIColor greenColor];
    } else {
        loginStatus.text = [StringGrabber concatenateWithHardcodedString:@"logged_in_as" :@"NOT LOGGED IN"]; //[StringGrabber getString:@"login_status_not_logged_in"];
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
		self.view.frame = CGRectMake(0, 0, 1024, 768);
		mainLogo.frame = CGRectMake(5, 5, 502, 125 );
		containerForMainButton.frame = CGRectMake(517, 184, 400, 400);
        loginStatus.frame = CGRectMake(5, 135, 502, 40);
	} else {
		self.view.frame = CGRectMake(0, 0, 768, 1024);
		mainLogo.frame = CGRectMake(20, 5, 728, 150);
        loginStatus.frame = CGRectMake(0, 160, 768, 40);
		containerForMainButton.frame = CGRectMake(174, 300, 400, 400);
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
	[containerForMainButton release];
	[loginStatus release];
	[startStopLabel release];
    [startStopButton release];
    [menuButton release];
    [super dealloc];
	
}

- (void) login {
    //* present dialog with login credentials
    if ([isenseAPI login:@"sor" with:@"sor"]) {
        UIImage *check = [[UIImage alloc] init];
        check = [UIImage imageNamed:@"bluecheck"];
        [self.view makeToast:@"Login Successful!"
				duration:2.0
				position:@"bottom"
				   image:check];	
	} else {
        UIImage *red_x = [[UIImage alloc] init];
        red_x = [UIImage imageNamed:@"red_x"];
        [self.view makeToast:@"Login Failed!"
                    duration:2.0
                    position:@"bottom"
                       image:red_x];
    }
    
    [self updateLoginStatus];
	
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

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
	BOOL showMsg = YES;
	UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Menu item clicked:"
													  message:@"Nil_message"
													 delegate:nil
											cancelButtonTitle:@"Okay"
											otherButtonTitles:nil];
	switch (buttonIndex) {
		case MENU_UPLOAD:
			message.message = @"Upload"; showMsg = NO; [self upload];
			break;
		case MENU_EXPERIMENT:
			message.message = @"Experiment"; showMsg = NO; [self experiment];
			break;
		case MENU_LOGIN:
			message.message = @"Login"; showMsg = NO; [self login];
			//[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput]; <- implemented later
			break;
		default:
			showMsg = NO;
			break;
	}
	
	if (showMsg)
		[message show];
	
	[message release];
}


@end
