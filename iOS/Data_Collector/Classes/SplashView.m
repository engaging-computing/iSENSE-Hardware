//
//  SplashView.m
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "SplashView.h"

#define DEGREES_TO_RADIANS(angle) (angle / 180.0 * M_PI)

@implementation SplashView

@synthesize dataCollectorLogo, orb, automatic, manual;

- (void)viewDidLoad {
	[super viewDidLoad];
	
	[self rotateImage:orb duration:1.5
				curve:UIViewAnimationCurveLinear degrees:180];
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [super dealloc];
}

- (IBAction) loadAutomatic:(id)sender {
	iSENSE_Data_CollectorViewController_iPad *autoView = [[iSENSE_Data_CollectorViewController_iPad alloc] init];
	autoView.title = @"Automatic";
	[self.navigationController pushViewController:autoView animated:YES];
	[autoView release];
}

- (IBAction) loadManual:(id)sender {
	ManualView *manualView = [[ManualView alloc] init];
	manualView.title = @"Manual";
	[self.navigationController pushViewController:manualView animated:YES];
	[manualView release];
}

- (void)rotateImage:(UIImageView *)image duration:(NSTimeInterval)duration 
			  curve:(int)curve degrees:(CGFloat)degrees {
	
	// Setup the animation
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:duration];
	[UIView setAnimationCurve:curve];
	[UIView setAnimationBeginsFromCurrentState:YES];
	[UIView setAnimationRepeatCount:1e100f];
	
	// The transform matrix
	CGAffineTransform transform = 
	CGAffineTransformMakeRotation(DEGREES_TO_RADIANS(degrees));
	image.transform = transform;
	
	// Commit the changes
	[UIView commitAnimations];
}

@end
