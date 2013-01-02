//
//  iSENSE_Data_CollectorViewController_iPad.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/1/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "iSENSE_Data_CollectorViewController_iPad.h"

@implementation iSENSE_Data_CollectorViewController_iPad

@synthesize isRecording;

// Long Click Responder
- (IBAction)onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer {
	// Is the button ready to be clicked?
	if ([containerForMainButton clickEnabled]) {
		
        // Start Recording
		if (![self isRecording]) {
			// Switch to green mode
			startStopButton.image = [UIImage imageNamed:@"green_button.png"];
			mainLogo.image = [UIImage imageNamed:@"logo_green.png"];
			startStopLabel.text = @"STOP\n(Press and Hold)";
			
			[self setIsRecording:TRUE];
			
			// Stop Recording
		} else {
			// Back to red mode
			startStopButton.image = [UIImage imageNamed:@"red_button.png"];
			mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
			startStopLabel.text = @"START\n(Press and Hold)";
			
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
		
		// Make button unclickable until it gets released
		[containerForMainButton setClickEnabled:FALSE];
		
	}
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	
	// bounds, allocate, and customize the view
	self.view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024)];
    self.view.backgroundColor = [UIColor blackColor];
	
	// Initialize isRecording to false
	[self setIsRecording:FALSE];
	
	// Add iSENSE LOGO background image at the top
	mainLogo = [[UIImageView alloc] initWithFrame:CGRectMake(40, 0, 688, 150)];
	mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
	
	// Create a label for login status
	loginStatus = [[UILabel alloc] initWithFrame:CGRectMake(234, 160, 300, 20)];
	loginStatus.text = @"Login Status: NOT LOGGED IN";
	loginStatus.textAlignment = UITextAlignmentCenter;
	loginStatus.textColor = [UIColor whiteColor];
	loginStatus.font = [startStopLabel.font fontWithSize:18];
	loginStatus.numberOfLines = 1;
	[loginStatus setBackgroundColor:[UIColor clearColor]];
	
	// Allocate space and initialize the main button with its label
	containerForMainButton = [[UIPicButton alloc] initWithFrame:CGRectMake(174, 300, 400, 400)];
	startStopButton = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 400, 400)];
	startStopLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, containerForMainButton.bounds.size.width, containerForMainButton.bounds.size.height)];
	startStopLabel.text = @"START\n(Press and Hold)";
	startStopLabel.textAlignment = UITextAlignmentCenter;
	startStopLabel.textColor = [UIColor whiteColor];
	startStopLabel.font = [startStopLabel.font fontWithSize:25];
	startStopLabel.numberOfLines = 2;
    startStopButton.image = [UIImage imageNamed:@"red_button.png"];
	[startStopLabel setBackgroundColor:[UIColor clearColor]];
	
	// Add main button subviews to the UIPicButton called containerForMainButton (so the whole thing is clickable)
	[containerForMainButton addSubview:startStopButton];
	[containerForMainButton addSubview:startStopLabel];
	
	// Add long click listener to the containerForMainButton
	UILongPressGestureRecognizer *longPressGesture = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(onStartStopLongClick:)];
	[longPressGesture setMinimumPressDuration:1];
	longPressGesture.allowableMovement = 5;
	[containerForMainButton addGestureRecognizer:longPressGesture];
    [longPressGesture release];
	
	// Add all the subviews to main view
    [self.view addSubview:loginStatus];
	[self.view addSubview:mainLogo];
	[self.view addSubview:containerForMainButton];
	
	// Attempt Login
	isenseAPI = [iSENSE instance];
	[isenseAPI toggleUseDev:YES];
	[isenseAPI login:@"sor" with:@"sor"];	
	
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
	NSLog(@"Rotate Initiated!");
	if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
		self.view.frame = CGRectMake(0, 0, 1024, 768);
		mainLogo.frame = CGRectMake(10, 0, 502, 125 );	
		containerForMainButton.frame = CGRectMake(568, 150, 400, 400);
	} else {
		self.view.frame = CGRectMake(0, 0, 768, 1024);
		mainLogo.frame = CGRectMake(10, 0, 748, 150);
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
    //[rapi release];
    [super dealloc];
	
}


@end
