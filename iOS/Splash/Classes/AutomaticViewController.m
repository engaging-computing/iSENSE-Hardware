//
//  AutomaticViewController.m
//  Splash
//
//  Created by Mike S. on 12/4/12.
//  Advisor - Fred Martin
//  Copyright 2012 ECG. All rights reserved.
//

#import "AutomaticViewController.h"


@implementation AutomaticViewController

@synthesize window, secondView;

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	NSLog(@"second view viewDidLoad");
	
	[self.window makeKeyAndVisible];
	[self.window addSubview:[secondView view]];
	
    [super viewDidLoad];
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
	[window	release];
	[secondView release];
    [super dealloc];
}


@end
