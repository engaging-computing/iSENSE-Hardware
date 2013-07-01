//
//  CarRampPhysicsViewController.m
//  CarRampPhysics
//
//  Created by Virinchi Balabhadrapatruni on 6/27/13.
//  Copyright 2013 __MyCompanyName__. All rights reserved.
//

#import "CarRampPhysicsViewController.h"

@implementation CarRampPhysicsViewController



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
		case RECORD_LENGTH_BUTTON:
			//Record length dialog goes here
			break;
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
	

@end
