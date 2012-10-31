//
//  iSENSE_Data_CollectorViewController.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/3/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "iSENSE_Data_CollectorViewController.h"
#import "UIPicButton.h"

@implementation iSENSE_Data_CollectorViewController
@synthesize startStopButton;
@synthesize mainLogo;
@synthesize longClickTimer;
@synthesize container;

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
	CGRect frame = CGRectMake(0, 0, 320, 480);
	
	//allocate the view
	self.view = [[UIView alloc] initWithFrame:frame];

	// Override point for customization after application launch.
	isRecording = FALSE;
	 
	// Attempt to make background black
	self.view.backgroundColor = [UIColor blackColor];
	 
	// Attempt to add custom background image at the top
	frame = CGRectMake(0, 10, self.view.bounds.size.width, 100);
	mainLogo = [[UIImageView alloc] initWithFrame:frame];
	mainLogo.image = [UIImage imageNamed:@"logo_red.png"];
	 
	// Allocate space and initialize the main button
	UIImage *redButton = [UIImage imageNamed:@"red_button.png"];
	frame = CGRectMake(0, 120, self.view.bounds.size.width, 300);
	container = [[UIPicButton alloc] initWithFrame:frame];
	frame = CGRectMake(0, 0, self.view.bounds.size.width, 300);
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
	
	[container release];
	[longPressGesture release];
	[redButton release];
	
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


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}



- (void)dealloc {
	[mainLogo release];
	[startStopButton release];
    [super dealloc];
}

@end
