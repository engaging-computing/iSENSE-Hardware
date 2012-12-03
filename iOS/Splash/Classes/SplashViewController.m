//
//  SplashViewController.m
//  Splash
//
//  Created by CS Admin on 11/21/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "SplashViewController.h"

@implementation SplashViewController

// synthesis
@synthesize automatic;
@synthesize manual;
@synthesize isenseLogo;
@synthesize orb;


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
	
	//tabBar = [[UITabBar alloc] initWithNibName:@"tabBar" bundle:nil];
	/*
	UINavigationController *navigationController = self.navigationController;
	[navigationController popToRootViewControllerAnimated:NO];
	[navigationController.tabBarController setSelectedIndex:0];
    */
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
	[manual release];
	[automatic release];
	[isenseLogo release];
	[orb release];
    [super dealloc];
}

@end
