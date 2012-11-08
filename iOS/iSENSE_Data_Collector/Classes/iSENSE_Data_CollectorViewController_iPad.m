    //
//  iSENSE_Data_CollectorViewController_iPad.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/1/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "iSENSE_Data_CollectorViewController_iPad.h"
#import "SystemVersion.h"


@implementation iSENSE_Data_CollectorViewController_iPad

@synthesize startStopButton;
@synthesize mainLogo;
@synthesize longClickTimer;
@synthesize container;
@synthesize startStopLabel;

- (IBAction)onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer {
	
	if (container.getClickEnabled) {
		
		if (!isRecording) {
			// Switch to green mode
			startStopButton.image = [UIImage imageNamed:@"green_button.png"];
			mainLogo.image = [UIImage imageNamed:@"logo_green.png"];
			startStopLabel.text = @"STOP\n(Press and Hold)";
			
			isRecording = TRUE;
		} else {
			// Back to red mode
			startStopButton.image = [UIImage imageNamed:@"red_button.png"];
			mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
			startStopLabel.text = @"START\n(Press and Hold)";
			
			isRecording = FALSE;
		}	
		
		// Make the beep sound
		// Get the filename of the sound file:
		NSString *path = [NSString stringWithFormat:@"%@%@",
						  [[NSBundle mainBundle] resourcePath],
						  @"/button-37.wav"];
		
		// Declare a system sound id
		SystemSoundID soundID;
		
		// Get a URL for the sound file
		NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
		
		// Use audio sevices to create the sound
		AudioServicesCreateSystemSoundID((CFURLRef)filePath, &soundID);
		
		// Use audio services to play the sound
		AudioServicesPlaySystemSound(soundID);
		
		// Set clickEnabled false until release
		[container setClickEnabled:FALSE];
		NSLog(@"ClickEnabled = %s", container.getClickEnabled ? "true" : "false");
		
		
	}
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	
	//create a frame that sets the bounds of the view
	CGRect frame = CGRectMake(0, 0, 748,1024);
	
	//allocate the view
	self.view = [[UIView alloc] initWithFrame:frame];
	
	// Override point for customization after application launch.
	isRecording = FALSE;
	
	// Attempt to make background black
	self.view.backgroundColor = [UIColor blackColor];
	
	// Attempt to add custom background image at the top
	frame = CGRectMake(0, 10, 300, 100);
	mainLogo = [[UIImageView alloc] initWithFrame:frame];
	mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
	
	// Allocate space and initialize the main button
	UIImage *redButton = [UIImage imageNamed:@"red_button.png"];
	frame = CGRectMake(0, 120, 300, 300);
	container = [[UIPicButton alloc] initWithFrame:frame];
	frame = CGRectMake(0, 0, 300, 300);
	startStopButton = [[UIImageView alloc] initWithFrame:frame];
	
	// Make the main button label
	frame = CGRectMake(0, 0, container.bounds.size.width, container.bounds.size.height);
	startStopLabel = [[UILabel alloc] initWithFrame:frame];
	startStopLabel.text = @"START\n(Press and Hold)";
	startStopLabel.textAlignment = UITextAlignmentCenter;
	startStopLabel.textColor = [UIColor whiteColor];
	startStopLabel.font = [startStopLabel.font fontWithSize:25];
	startStopLabel.numberOfLines = 2;
	[startStopLabel setBackgroundColor:[UIColor clearColor]];
	startStopButton.image = redButton;
	
	// Add subviews to the UIView called container
	[container addSubview:startStopButton];
	[container addSubview:startStopLabel];
	
	
	// Long Press Listener
	UILongPressGestureRecognizer *longPressGesture = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(onStartStopLongClick:)];
	[longPressGesture setMinimumPressDuration:1];
	longPressGesture.allowableMovement = 5;
	[container addGestureRecognizer:longPressGesture];
	
	// Adding Subviews
	[self.view addSubview:mainLogo];
	[self.view addSubview:container];
	
	
	[longPressGesture release];
	[redButton release];
	
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
	CGRect frame;
	[container removeFromSuperview];
	NSLog(@"Rotate Initiated!");
	if (toInterfaceOrientation == UIDeviceOrientationLandscapeLeft || toInterfaceOrientation == UIDeviceOrientationLandscapeRight) {
		// Remake the parent view
		 frame = CGRectMake(0, 0, 1024, 748);
		//allocate the view
		self.view = [[UIView alloc] initWithFrame:frame];
		
		// Move the button the the right
		frame = CGRectMake(724, 200, 300, 300);
		container = [[UIPicButton alloc] initWithFrame:frame];
	} else {
		// Remake the parent view
		CGRect frame = CGRectMake(0, 0, 748, 1024);
		//allocate the view
		self.view = [[UIView alloc] initWithFrame:frame];
		
		frame = CGRectMake(0, 120, 300, 300);
		container = [[UIPicButton alloc] initWithFrame:frame];
	}	
			

	// Attempt to make background black
	self.view.backgroundColor = [UIColor blackColor];
	
	// Attempt to add custom background image at the top
	frame = CGRectMake(0, 10, 300, 100);
	mainLogo = [[UIImageView alloc] initWithFrame:frame];
	mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
	
	// Allocate space and initialize the main button
	UIImage *redButton = [UIImage imageNamed:@"red_button.png"];
	frame = CGRectMake(0, 120, 300, 300);
	container = [[UIPicButton alloc] initWithFrame:frame];
	frame = CGRectMake(0, 0, 300, 300);
	startStopButton = [[UIImageView alloc] initWithFrame:frame];
	
	// Make the main button label
	frame = CGRectMake(0, 0, container.bounds.size.width, container.bounds.size.height);
	startStopLabel = [[UILabel alloc] initWithFrame:frame];
	startStopLabel.text = @"START\n(Press and Hold)";
	startStopLabel.textAlignment = UITextAlignmentCenter;
	startStopLabel.textColor = [UIColor whiteColor];
	startStopLabel.font = [startStopLabel.font fontWithSize:25];
	startStopLabel.numberOfLines = 2;
	[startStopLabel setBackgroundColor:[UIColor clearColor]];
	startStopButton.image = redButton;
	
	// Add subviews to the UIView called container
	[container addSubview:startStopButton];
	[container addSubview:startStopLabel];
	
	
	// Long Press Listener
	UILongPressGestureRecognizer *longPressGesture = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(onStartStopLongClick:)];
	[longPressGesture setMinimumPressDuration:1];
	longPressGesture.allowableMovement = 5;
	[container addGestureRecognizer:longPressGesture];
	
	// Adding Subviews
	[self.view addSubview:mainLogo];	
	[self.view addSubview:container];
	[self didRotateFromInterfaceOrientation:toInterfaceOrientation];
	
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
	NSLog(@"Rotation? Returns yes");
	[self willRotateToInterfaceOrientation:interfaceOrientation duration:2];
    return YES;
}


-(void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation {
	NSLog(@"Rotate powahhh");

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
    [super dealloc];
	[mainLogo release];
	[container release];
	[startStopLabel release];
}


@end
